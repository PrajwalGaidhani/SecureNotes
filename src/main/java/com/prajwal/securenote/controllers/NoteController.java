package com.prajwal.securenote.controllers;

import com.prajwal.securenote.models.Note;
import com.prajwal.securenote.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/notes")
public class NoteController {


    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }
//    @Autowired
//    private UserDetailsService userDetailsService;

    @GetMapping
//    @PreAuthorize("#principal.name == authentication.name")
    public ResponseEntity<List<Note>> getNotes(@AuthenticationPrincipal UserDetails userDetails) {
        List<Note> notes = noteService.getNoteForUser(userDetails.getUsername());
        System.out.println(notes + " "+userDetails.getUsername());
        return ResponseEntity.ok(notes);
    }

    @PostMapping
    public ResponseEntity<Note> createNote(@RequestBody String content,@AuthenticationPrincipal UserDetails userDetails ) {
        Note note = noteService.createNoteForUser(userDetails.getUsername(), content);
        System.out.println(note);
        return ResponseEntity.status(HttpStatus.CREATED).body(note);
    }

    @PutMapping("/{noteId}")
//    @PreAuthorize("#principal.name == authentication.name")
    public ResponseEntity<Note> updateNote(@PathVariable Long noteId, @RequestBody String content, @AuthenticationPrincipal UserDetails userDetails) {
        Note note = noteService.updateNoteForUser(noteId, content, userDetails.getUsername());
        return ResponseEntity.ok(note);
    }

    @DeleteMapping("/{id}")
//    @PreAuthorize("#principal.name == authentication.name")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        noteService.deleteNoteForUser(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
