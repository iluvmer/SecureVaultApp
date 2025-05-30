package com.meriem.securevaultapp.screens;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.meriem.securevaultapp.R;
import com.meriem.securevaultapp.helpers.EncryptionUtils;
import com.meriem.securevaultapp.models.RealmNote;

import io.realm.Realm;
import io.realm.RealmResults;

public class Note_content extends AppCompatActivity {
    private EditText noteTitleEdit, noteContentEdit;
    private ImageButton backBtn, saveBtn, deleteBtn;
    private String originalTitle, originalContent, userId;
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_content);

        // Initialize Realm
        realm = Realm.getDefaultInstance();

        // Get data from intent
        Intent intent = getIntent();
        originalTitle = intent.getStringExtra("title");
        originalContent = intent.getStringExtra("description");
        userId = intent.getStringExtra("uid");

        // Initialize views
        noteTitleEdit = findViewById(R.id.noteTitle);
        noteContentEdit = findViewById(R.id.noteDesc);
        backBtn = findViewById(R.id.back_btn);
        saveBtn = findViewById(R.id.save_btn);
        ImageButton deleteBtn = findViewById(R.id.deleteButton);

        // Change TextViews to EditTexts in your XML first!
        noteTitleEdit.setText(originalTitle);
        noteContentEdit.setText(originalContent);

        backBtn.setOnClickListener(v -> finish());

        saveBtn.setOnClickListener(v -> saveNoteChanges());

        deleteBtn.setOnClickListener(v -> deleteNote());
    }

    private void saveNoteChanges() {
        String newTitle = noteTitleEdit.getText().toString().trim();
        String newContent = noteContentEdit.getText().toString().trim();

        if (newTitle.isEmpty()) {
            Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get ALL notes for this user first
        RealmResults<RealmNote> allNotes = realm.where(RealmNote.class)
                .equalTo("userId", userId)
                .findAll();

        // Find matching note by comparing decrypted titles
        for (RealmNote note : allNotes) {
            try {
                String decryptedTitle = EncryptionUtils.decrypt(note.getEncryptedTitle());
                if (decryptedTitle.equals(originalTitle)) {
                    // Found our note - update it
                    realm.executeTransaction(r -> {
                        note.setEncryptedTitle(EncryptionUtils.encrypt(newTitle));
                        note.setEncryptedContent(EncryptionUtils.encrypt(newContent));
                    });
                    Toast.makeText(this, "Note updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
            } catch (Exception e) {
                Log.e("NoteUpdate", "Decryption error", e);
            }
        }

        Toast.makeText(this, "Note not found", Toast.LENGTH_SHORT).show();
    }

    private void deleteNote() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Note")
                .setMessage("Are you sure you want to delete this note?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Execute delete operation
                    realm.executeTransactionAsync(realm -> {
                        // Get all notes for this user
                        RealmResults<RealmNote> userNotes = realm.where(RealmNote.class)
                                .equalTo("userId", userId)
                                .findAll();

                        // Find and delete the matching note
                        for (RealmNote note : userNotes) {
                            try {
                                String decryptedTitle = EncryptionUtils.decrypt(note.getEncryptedTitle());
                                if (decryptedTitle.equals(originalTitle)) {
                                    note.deleteFromRealm();
                                    break;
                                }
                            } catch (Exception e) {
                                Log.e("NoteDelete", "Decryption error", e);
                            }
                        }
                    }, () -> {
                        // On success
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }, error -> {
                        // On error
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Failed to delete note", Toast.LENGTH_SHORT).show();
                            Log.e("NoteDelete", "Error deleting note", error);
                        });
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null) {
            realm.close();
        }
    }
}