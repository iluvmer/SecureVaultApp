package com.meriem.securevaultapp.screens;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.meriem.securevaultapp.R;
import com.meriem.securevaultapp.models.RealmUser;

import io.realm.Realm;


public class MainActivityAfak extends AppCompatActivity implements View.OnClickListener {
    private String uid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_afak);


        // Receive UID from LoginScreen
        uid = getIntent().getStringExtra("uid");
        if (uid == null || uid.isEmpty()) {
            Toast.makeText(this, "No user ID received", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        CardView notepadCard = findViewById(R.id.notepadCard);
        CardView cleeCard = findViewById(R.id.cleeCard);
        CardView coeurCard = findViewById(R.id.coeurCard);
        CardView settingsCard = findViewById(R.id.settingsCard);

        notepadCard.setOnClickListener(this);
        cleeCard.setOnClickListener(this);
        coeurCard.setOnClickListener(this);
        settingsCard.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        int id = view.getId();
            if (id == R.id.notepadCard) {
                // activity li n3aytolha de chaque box
                intent = new Intent(this, Notes_Activity.class);
                intent.putExtra("uid", uid); //here to pass the UID to NotesActivity
                startActivity(intent);
            } else if ( id == R.id.cleeCard) {
                intent = new Intent(this, PasswordEntry.class); // hedi te3 password generator
                startActivity(intent);
            } else if (id == R.id.coeurCard) {
                intent = new Intent(this, MainPasswordListScreen.class);
                startActivity(intent);
            } else if (id == R.id.settingsCard) {
                Realm debugRealm = Realm.getDefaultInstance();
                try {
                    RealmUser debugUser = debugRealm.where(RealmUser.class)
                            .equalTo("uid", uid.trim())
                            .findFirst();
                    Log.d("MainDebug", "User exists: " + (debugUser != null));
                    if (debugUser != null) {
                        Log.d("MainDebug", "UID: " + debugUser.getUid());
                        Log.d("MainDebug", "Email: " + debugUser.getEmail());
                    }
                } finally {
                    debugRealm.close();
                }
                intent = new Intent(this, Profile.class);
                intent.putExtra("uid", uid);
                startActivity(intent);
            }
    }
}

