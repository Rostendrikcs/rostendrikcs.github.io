package com.example.notesapp.ui

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.notesapp.R
import com.example.notesapp.data.Note
import com.example.notesapp.databinding.FragmentNoteEditBinding
import com.example.notesapp.viewmodel.NoteViewModel
import kotlinx.coroutines.launch

class NoteEditFragment : Fragment() {
    private var _binding: FragmentNoteEditBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NoteViewModel by activityViewModels()
    private var currentNote: Note? = null
    private var noteId = -1
    private var selectedColor = -1

    private val colors = listOf(-1,
        Color.parseColor("#FFF9C4"), Color.parseColor("#C8E6C9"),
        Color.parseColor("#BBDEFB"), Color.parseColor("#F8BBD0"),
        Color.parseColor("#E1BEE7"))

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        FragmentNoteEditBinding.inflate(inflater, container, false).also { _binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        noteId = arguments?.getInt("noteId", -1) ?: -1
        if (noteId != -1) {
            lifecycleScope.launch {
                currentNote = viewModel.getNoteById(noteId)
                currentNote?.let {
                    binding.etTitle.setText(it.title)
                    binding.etContent.setText(it.content)
                    selectedColor = it.color
                    applyColor(selectedColor)
                    requireActivity().title = "Редактирование"
                }
            }
        } else requireActivity().title = "Новая заметка"

        // Цветные кнопки
        listOf(binding.colorDefault, binding.colorYellow, binding.colorGreen,
            binding.colorBlue, binding.colorPink, binding.colorPurple)
            .forEachIndexed { i, btn ->
                if (colors[i] != -1) btn.setCardBackgroundColor(colors[i])
                btn.setOnClickListener { selectedColor = colors[i]; applyColor(colors[i]) }
            }

        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) =
                menuInflater.inflate(R.menu.menu_note_edit, menu)
            override fun onMenuItemSelected(item: MenuItem): Boolean {
                if (item.itemId == R.id.action_save) { saveNote(); return true }
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun applyColor(color: Int) {
        binding.root.setBackgroundColor(if (color != -1) color else Color.WHITE)
    }

    private fun saveNote() {
        val title = binding.etTitle.text.toString().trim()
        val content = binding.etContent.text.toString().trim()
        if (title.isBlank() && content.isBlank()) {
            Toast.makeText(requireContext(), "Заметка пустая", Toast.LENGTH_SHORT).show(); return
        }
        val now = System.currentTimeMillis()
        if (currentNote == null) viewModel.insert(Note(title = title, content = content, timestamp = now, color = selectedColor))
        else viewModel.update(currentNote!!.copy(title = title, content = content, timestamp = now, color = selectedColor))
        Toast.makeText(requireContext(), "Сохранено ✓", Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
