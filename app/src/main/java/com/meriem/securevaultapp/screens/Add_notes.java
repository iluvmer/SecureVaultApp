package com.meriem.securevaultapp.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.meriem.securevaultapp.R;
import com.meriem.securevaultapp.helpers.EncryptionUtils;
import com.meriem.securevaultapp.models.RealmNote;
import com.meriem.securevaultapp.models.RealmUser;

import io.realm.Realm;

public class Add_notes extends AppCompatActivity {

    private TextInputEditText noteContentEditText, noteTitleEditText;
    private Button saveButton, cancelButton;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        userId = getIntent().getStringExtra("uid");
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "No user ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        // CORRECT: Get the EditTexts directly from their IDs
        noteTitleEditText = findViewById(R.id.NoteTitle);  // This is the TextInputEditText
        noteContentEditText = findViewById(R.id.NoteContent);  // This is the TextInputEditText

        saveButton = findViewById(R.id.buttonSave);
        ImageButton go_back = findViewById(R.id.back_btn);

        go_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Add_notes.this, Notes_Activity.class);
                intent.putExtra("uid", userId);
                startActivity(intent);
                finish();

            }
        });

        saveButton.setOnClickListener(v -> {
            String title = noteTitleEditText.getText().toString().trim();
            String content = noteContentEditText.getText().toString().trim();

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
            } else {
                Realm realm = Realm.getDefaultInstance();
                try {
                    EncryptionUtils.generateAESKeyIfNeeded();
                    String encryptedTitle = EncryptionUtils.encrypt(title);
                    String encryptedContent = EncryptionUtils.encrypt(content);

                    realm.executeTransaction(r -> {
                        RealmNote note = r.createObject(RealmNote.class, java.util.UUID.randomUUID().toString());
                        note.setEncryptedTitle(encryptedTitle);
                        note.setEncryptedContent(encryptedContent);
                        note.setUserId(userId); // Directly set the userId from intent

                    });

                    Toast.makeText(this, "Note saved!", Toast.LENGTH_SHORT).show();
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Encryption failed", Toast.LENGTH_SHORT).show();
                } finally {
                    realm.close();
                }
            }
        });


    }
}