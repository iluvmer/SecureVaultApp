package com.meriem.securevaultapp.screens;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.meriem.securevaultapp.R;

import java.util.ArrayList;
import java.util.List;

public class Notes_Activity extends AppCompatActivity {
    private GridView gridView;
    private ImageButton addNoteBtn, profilebtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        addNoteBtn = findViewById(R.id.btnAddNote);
        profilebtn = findViewById(R.id.btnProfile);

        addNoteBtn.setOnClickListener(v -> {
            startActivity(new Intent(Notes_Activity.this, Add_notes.class));
        });

        profilebtn.setOnClickListener(v -> {
            startActivity(new Intent(Notes_Activity.this, Profile.class));
        });

        gridView = findViewById(R.id.notesGridView);

        ArrayList<String> noteTitles = new ArrayList<>();
        ArrayList<String> noteDescriptions = new ArrayList<>();

        noteTitles.add("Shopping list");
        noteDescriptions.add("Eggs, milk, bread...");

        noteTitles.add("To-Do");
        noteDescriptions.add("Finish project, call mom...");

        noteTitles.add("Ideas");
        noteDescriptions.add("New app for recipes...");

        noteTitles.add("Passwords");
        noteDescriptions.add("Email: pass1234...");

        noteTitles.add("Books");
        noteDescriptions.add("1984, Brave New World...");

        noteTitles.add("Reminders");
        noteDescriptions.add("Doctor appointment at 4pm...");

        NoteAdapter adapter = new NoteAdapter(this, noteTitles, noteDescriptions);
        gridView.setAdapter(adapter);
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

            String title = titles.get(position);
            String description = descriptions.get(position);

            noteTitle.setText(title);
            noteDesc.setText(description);

            // Click listener on the entire item
            view.setOnClickListener(v -> {
                Intent intent = new Intent(context, Note_content.class);
                intent.putExtra("title", title);
                intent.putExtra("description", description);
                context.startActivity(intent);
            });

            return view;
        }

    }
}