package com.prajwal.securenote.service.impl;

import com.prajwal.securenote.models.Note;
import com.prajwal.securenote.repositories.NoteRepository;
import com.prajwal.securenote.service.AuditLogService;
import com.prajwal.securenote.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoteServiceImpl implements NoteService {
    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private AuditLogService auditLogService;
    @Override
    public Note createNoteForUser(String username, String content){
        Note note = new Note();
        note.setContent(content);
        note.setOwnerUsername(username);
        Note savedNote = noteRepository.save(note);
        auditLogService.logNoteCreation(username, note);
        return savedNote;
    }

    @Override
    public Note updateNoteForUser(Long noteId,String content, String username){
        Note note = noteRepository.findById(noteId).orElseThrow(()
                -> new RuntimeException("Note not found"));
        note.setContent(content);
        Note updatedNote = noteRepository.save(note);
        auditLogService.logNoteUpdate(username, note);
        return updatedNote;

    }

    @Override
    public void deleteNoteForUser(Long id, String username){
        Note note = noteRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Note not found")
        );
        auditLogService.logNoteDeletion(username, id);
        noteRepository.delete(note);
    }

    @Override
    public List<Note> getNoteForUser(String username) {
        List<Note> personalNotes = noteRepository
                .findByOwnerUsername(username);
        return personalNotes;
    }
}
