package com.example.notesapp.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.notesapp.data.Note
import com.example.notesapp.data.NoteDatabase
import com.example.notesapp.data.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: NoteRepository
    private val _searchQuery = MutableLiveData<String?>(null)

    val displayedNotes: LiveData<List<Note>>

    init {
        val dao = NoteDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(dao)
        displayedNotes = _searchQuery.switchMap { query ->
            if (query.isNullOrBlank()) repository.allNotes
            else repository.searchNotes(query)
        }
    }

    fun insert(note: Note) = viewModelScope.launch(Dispatchers.IO) { repository.insert(note) }
    fun update(note: Note) = viewModelScope.launch(Dispatchers.IO) { repository.update(note) }
    fun delete(note: Note) = viewModelScope.launch(Dispatchers.IO) { repository.delete(note) }
    fun setSearchQuery(query: String?) { _searchQuery.value = query }
    suspend fun getNoteById(id: Int) = repository.getNoteById(id)
}
