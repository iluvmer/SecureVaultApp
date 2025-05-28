package com.meriem.securevaultapp.screens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.meriem.securevaultapp.R;
import com.meriem.securevaultapp.models.RealmUser;

import io.realm.Realm;

public class Profile extends AppCompatActivity {
    private Realm realm;
    private String uid;
    private TextView tvName, tvEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        uid = getIntent().getStringExtra("uid");
        if (uid == null || uid.isEmpty()) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d("Profile", "Received UID: " + uid);

        tvName = findViewById(R.id.tv_name);
        tvEmail = findViewById(R.id.tv_email);
        Button edit = findViewById(R.id.edit_profile);
        ImageButton go_back = findViewById(R.id.back_btn);
        Button btnLogout = findViewById(R.id.btn_logout);
        Button btnFingerprint = findViewById(R.id.btn_fingerprint_settings);

        // Initialize Realm
        realm = Realm.getDefaultInstance();

        loadUserData();

        edit.setOnClickListener(v -> {
            Intent intent = new Intent(Profile.this, Edit_Profile.class);
            intent.putExtra("uid", uid);
            startActivityForResult(intent, 1);
        });

        go_back.setOnClickListener(v -> {
            finish();
        });

        btnLogout.setOnClickListener(v -> {

            FirebaseAuth.getInstance().signOut();

            // Close all activities and go back to LoginScreen
            Intent intent = new Intent(Profile.this, LoginScreen.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

            Toast.makeText(Profile.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        });

        btnFingerprint.setOnClickListener(v -> {
            // Handle fingerprint settings
            // Add your fingerprint implementation here
        });
    }

    private void loadUserData() {
        // Use try-with-resources to ensure Realm is closed
        try (Realm freshRealm = Realm.getDefaultInstance()) {
            freshRealm.refresh(); // Force sync with latest data

            Log.d("Profile", "Fresh Realm query for UID: " + uid.trim());

            RealmUser user = freshRealm.where(RealmUser.class)
                    .equalTo("uid", uid.trim())
                    .findFirst();

            if (user != null) {
                String firstName = user.getFirstName();
                String lastName = user.getLastName();
                String email = user.getEmail();

                Log.d("Profile", "Loaded data - " +
                        "UID: " + user.getUid() + ", " +
                        "Email: " + email + ", " +
                        "Name: " + firstName + " " + lastName);

                runOnUiThread(() -> {
                    tvName.setText(firstName + " " + lastName);
                    tvEmail.setText(email);
                });
            } else {
                throw new IllegalStateException("User with UID " + uid + " not found in Realm");
            }
        } catch (Exception e) {
            Log.e("Profile", "Error loading user", e);
            runOnUiThread(() -> {
                tvName.setText("Error loading profile");
                tvEmail.setText(e.getMessage());
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Force a refresh of the user data
            loadUserData();

            // Optional: Show a success message
            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null) {
            realm.close();
        }
    }
}