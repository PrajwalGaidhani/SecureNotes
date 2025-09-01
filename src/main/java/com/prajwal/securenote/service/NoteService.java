package com.prajwal.securenote.service;

import com.prajwal.securenote.models.Note;

import java.util.List;

public interface NoteService {
    Note createNoteForUser(String username, String content);
    Note updateNoteForUser(Long noteId,String content, String username);
    void deleteNoteForUser( Long id, String username);
    List<Note> getNoteForUser(String username);
}
