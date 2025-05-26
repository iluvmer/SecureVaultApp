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

public class Add_notes extends AppCompatActivity {

    private TextInputEditText noteContentEditText, noteTitleEditText;
    private Button saveButton, cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        // Get the TextInputLayouts
        TextInputLayout titleLayout = findViewById(R.id.NoteTitle);
        TextInputLayout contentLayout = findViewById(R.id.noteContentLayout);

        // Get the EditTexts from the layouts
        noteTitleEditText = (TextInputEditText) titleLayout.getEditText();
        noteContentEditText = findViewById(R.id.NoteContent); // Directly get the EditText with ID NoteContent

        saveButton = findViewById(R.id.buttonSave);
        cancelButton = findViewById(R.id.buttonCancel);
        ImageButton go_back=findViewById(R.id.back_btn);

        go_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Add_notes.this, Notes_Activity.class);
                startActivity(intent);
            }
        });

        saveButton.setOnClickListener(v -> {
            String title = noteTitleEditText.getText().toString().trim();
            String content = noteContentEditText.getText().toString().trim();

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Note saved!", Toast.LENGTH_SHORT).show();
                finish(); // Close activity after saving
            }
        });

        cancelButton.setOnClickListener(v -> {
            finish();
        });
    }
}