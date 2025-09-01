package com.prajwal.securenote.repositories;

import com.prajwal.securenote.models.Note;
import jdk.jfr.Registered;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

@Registered
public interface NoteRepository extends JpaRepository<Note,Long> {
    List<Note> findByOwnerUsername(String ownerUsername);
}
