package com.example.notesapp.ui

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.notesapp.R
import com.example.notesapp.data.Note
import com.example.notesapp.databinding.FragmentNotesListBinding
import com.example.notesapp.viewmodel.NoteViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class NotesListFragment : Fragment() {
    private var _binding: FragmentNotesListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NoteViewModel by activityViewModels()
    private lateinit var adapter: NoteAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        FragmentNotesListBinding.inflate(inflater, container, false).also { _binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = NoteAdapter(
            onNoteClick = { note ->
                findNavController().navigate(R.id.action_list_to_edit, Bundle().apply { putInt("noteId", note.id) })
            },
            onNoteLongClick = { note -> showDeleteDialog(note) }
        )
        binding.recyclerView.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            this.adapter = this@NotesListFragment.adapter
        }
        viewModel.displayedNotes.observe(viewLifecycleOwner) { notes ->
            adapter.submitList(notes)
            binding.layoutEmpty.visibility = if (notes.isEmpty()) View.VISIBLE else View.GONE
            binding.recyclerView.visibility = if (notes.isEmpty()) View.GONE else View.VISIBLE
        }
        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_list_to_edit, Bundle().apply { putInt("noteId", -1) })
        }
        setupMenu()
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_notes_list, menu)
                val searchView = menu.findItem(R.id.action_search).actionView as SearchView
                searchView.queryHint = "Поиск..."
                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?) = false
                    override fun onQueryTextChange(newText: String?): Boolean {
                        viewModel.setSearchQuery(newText); return true
                    }
                })
                menu.findItem(R.id.action_search).setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                    override fun onMenuItemActionExpand(item: MenuItem) = true
                    override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                        viewModel.setSearchQuery(null); return true
                    }
                })
            }
            override fun onMenuItemSelected(menuItem: MenuItem) = false
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun showDeleteDialog(note: Note) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Удалить заметку?")
            .setMessage("\"${note.title.ifBlank { "Без заголовка" }}\" будет удалена.")
            .setPositiveButton("Удалить") { _, _ ->
                viewModel.delete(note)
                Snackbar.make(binding.root, "Удалено", Snackbar.LENGTH_LONG)
                    .setAction("Отмена") { viewModel.insert(note) }.show()
            }
            .setNegativeButton("Отмена", null).show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
