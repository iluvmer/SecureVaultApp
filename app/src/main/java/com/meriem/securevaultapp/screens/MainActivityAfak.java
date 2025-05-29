package com.meriem.securevaultapp.screens;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.meriem.securevaultapp.R;
import com.meriem.securevaultapp.models.RealmUser;

import io.realm.Realm;


public class MainActivityAfak extends AppCompatActivity implements View.OnClickListener {
    private String uid;
    private TextView headerText;
    private Realm realm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_afak);

        // Initialize Realm
        realm = Realm.getDefaultInstance();

        // Initialize views
        headerText = findViewById(R.id.headerLayout);

        // Receive UID from LoginScreen
        uid = getIntent().getStringExtra("uid");
        if (uid == null || uid.isEmpty()) {
            Toast.makeText(this, "No user ID received", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        displayUserName();

        CardView notepadCard = findViewById(R.id.notepadCard);
        CardView cleeCard = findViewById(R.id.cleeCard);
        CardView coeurCard = findViewById(R.id.coeurCard);
        CardView settingsCard = findViewById(R.id.settingsCard);

        notepadCard.setOnClickListener(this);
        cleeCard.setOnClickListener(this);
        coeurCard.setOnClickListener(this);
        settingsCard.setOnClickListener(this);
    }
    private void displayUserName() {
        realm.executeTransactionAsync(realm -> {
            RealmUser user = realm.where(RealmUser.class)
                    .equalTo("uid", uid.trim())
                    .findFirst();

            if (user != null) {
                String fullName = user.getFirstName() + " " + user.getLastName();
                runOnUiThread(() -> {
                    headerText.setText("Hello, " + fullName);
                });
            }
        }, error -> {
            Log.e("MainActivity", "Error loading user: " + error.getMessage());
            runOnUiThread(() -> {
                headerText.setText("Hello, User");
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null) {
            realm.close();
        }
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
                intent = new Intent(this, PasswordGeneratorActivity.class); // hedi te3 password generator
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

