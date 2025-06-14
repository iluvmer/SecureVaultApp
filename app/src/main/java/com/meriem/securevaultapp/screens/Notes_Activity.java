package com.meriem.securevaultapp.screens;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.meriem.securevaultapp.R;
import com.meriem.securevaultapp.models.RealmNote;
import com.meriem.securevaultapp.models.RealmUser;
import com.meriem.securevaultapp.helpers.EncryptionUtils;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class Notes_Activity extends AppCompatActivity {
    private GridView gridView;
    private ImageButton addNoteBtn, profilebtn,go_back;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        // Get user ID FIRST
        userId = getIntent().getStringExtra("uid");
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "No user ID received", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize UI components
        addNoteBtn = findViewById(R.id.btnAddNote);
       // profilebtn = findViewById(R.id.btnProfile);
        gridView = findViewById(R.id.notesGridView);

        go_back=findViewById(R.id.back_btn);

        go_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Notes_Activity.this, MainActivityAfak.class);
                intent.putExtra("uid", userId);
                startActivity(intent);
                finish();

            }
        });

        // Set up listeners
        addNoteBtn.setOnClickListener(v -> {
            Log.d("NotesActivity", "Add Note button clicked!"); // Debug log

            Intent intent = new Intent(Notes_Activity.this, Add_notes.class);
            intent.putExtra("uid", userId);

            // Verify intent before starting
            Log.d("NotesActivity", "Starting Add_notes with UID: " + userId);

            startActivity(intent);
        });

        // Load notes
        loadNotesFromRealm();

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning from Add_notes
        loadNotesFromRealm();
    }

    private void loadNotesFromRealm() {
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "User ID not available", Toast.LENGTH_SHORT).show();
            return;
        }

        Realm realm = Realm.getDefaultInstance();
        try {
            RealmResults<RealmNote> notes = realm.where(RealmNote.class)
                    .equalTo("userId", userId)
                    .findAll();

            ArrayList<String> noteTitles = new ArrayList<>();
            ArrayList<String> noteDescriptions = new ArrayList<>();

            for (RealmNote note : notes) {
                try {
                    String decryptedTitle = EncryptionUtils.decrypt(note.getEncryptedTitle());
                    String decryptedContent = EncryptionUtils.decrypt(note.getEncryptedContent());

                    noteTitles.add(decryptedTitle != null ? decryptedTitle : "[No Title]");
                    noteDescriptions.add(decryptedContent != null ? decryptedContent : "[No Content]");
                } catch (Exception e) {
                    e.printStackTrace();
                    noteTitles.add("[Decryption Error]");
                    noteDescriptions.add("[Decryption Error]");
                }
            }

            NoteAdapter adapter = new NoteAdapter(this, noteTitles, noteDescriptions);
            gridView.setAdapter(adapter);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading notes: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            realm.close();
        }
    }

    public class NoteAdapter extends BaseAdapter {
        private Context context;
        private List<String> titles;
        private List<String> descriptions;
        private LayoutInflater inflater;

        public NoteAdapter(Context context, List<String> titles, List<String> descriptions) {
            this.context = context;
            this.titles = titles;
            this.descriptions = descriptions;
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return titles.size();
        }

        @Override
        public Object getItem(int position) {
            return titles.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = inflater.inflate(R.layout.note_item, parent, false);
            }

            TextView noteTitle = view.findViewById(R.id.noteTitle);
            TextView noteDesc = view.findViewById(R.id.noteDesc);
            ImageButton deleteBtn = view.findViewById(R.id.deleteButton);


            String title = titles.get(position);
            String description = descriptions.get(position);

            noteTitle.setText(title);
            noteDesc.setText(description);

            view.setOnClickListener(v -> {
                Intent intent = new Intent(context, Note_content.class);
                intent.putExtra("title", title);
                intent.putExtra("description", description);
                intent.putExtra("uid", userId);
                context.startActivity(intent);
            });

            deleteBtn.setOnClickListener(v -> deleteNote(position, title));
            return view;
        }
        private void deleteNote(int position, String title) {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Note")
                    .setMessage("Are you sure you want to delete this note?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        Realm realm = Realm.getDefaultInstance();
                        try {
                            realm.executeTransaction(r -> {
                                // Find and delete the note
                                RealmResults<RealmNote> notes = r.where(RealmNote.class)
                                        .equalTo("userId", userId)
                                        .findAll();

                                for (RealmNote note : notes) {
                                    try {
                                        String decryptedTitle = EncryptionUtils.decrypt(note.getEncryptedTitle());
                                        if (decryptedTitle.equals(title)) {
                                            note.deleteFromRealm();
                                            break;
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                            // Remove from local lists and update UI
                            titles.remove(position);
                            descriptions.remove(position);
                            notifyDataSetChanged();

                            Toast.makeText(context, "Note deleted", Toast.LENGTH_SHORT).show();
                        } finally {
                            realm.close();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
    }
} }
