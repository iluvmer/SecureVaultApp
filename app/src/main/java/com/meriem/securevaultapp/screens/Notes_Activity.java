package com.meriem.securevaultapp.screens;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.meriem.securevaultapp.R;

import java.util.ArrayList;
import java.util.List;

public class Notes_Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        // GridView setup
        GridView gridView = findViewById(R.id.notesGridView);
        ArrayList<String> notesList = new ArrayList<>();
        notesList.add("Shopping list");
        notesList.add("To-Do");
        notesList.add("Ideas");
        notesList.add("Passwords");
        notesList.add("Books");
        notesList.add("Reminders");

        // NoteAdapter setup
        NoteAdapter adapter = new NoteAdapter(this, notesList);
        gridView.setAdapter(adapter);
    }

    // NoteAdapter defined inside the activity
    public class NoteAdapter extends BaseAdapter {

        private Context context;
        private List<String> notes;
        private LayoutInflater inflater;

        public NoteAdapter(Context context, List<String> notes) {
            this.context = context;
            this.notes = notes;
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return notes.size();
        }

        @Override
        public Object getItem(int position) {
            return notes.get(position);
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

            TextView noteText = view.findViewById(R.id.noteText);
            noteText.setText(notes.get(position));

            return view;
        }
    }
}
