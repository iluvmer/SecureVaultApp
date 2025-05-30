package com.meriem.securevaultapp.screens;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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
        Button enableAutofillButton = findViewById(R.id.enable_autofill_button);
        enableAutofillButton.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE);
            intent.setData(Uri.parse("package:com.meriem.securevaultapp"));
            startActivity(intent);
        });

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

        Button reportButton = findViewById(R.id.btn_report);
        reportButton.setOnClickListener(v -> {
            LayoutInflater inflater = LayoutInflater.from(this);
            View dialogView = inflater.inflate(R.layout.report_dialog, null);

            EditText editProblem = dialogView.findViewById(R.id.edit_problem);
            Button btnSave = dialogView.findViewById(R.id.btnSend);
            Button btnCancel = dialogView.findViewById(R.id.btnCancel);

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .create();

            btnSave.setOnClickListener(view -> {
                String problemText = editProblem.getText().toString().trim();

                if (problemText.isEmpty()) {
                    Toast.makeText(this, "Please describe your issue", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:"));

                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"support@gmail.com"});
                emailIntent.putExtra(Intent.EXTRA_TEXT, problemText);
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Reporting a problem:");

                try {
                    startActivity(Intent.createChooser(emailIntent, "Send email using:"));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(this, "No email clients installed.", Toast.LENGTH_SHORT).show();
                }

                dialog.dismiss();
            });

            btnCancel.setOnClickListener(view -> dialog.dismiss());

            dialog.show();
            Window window = dialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                WindowManager.LayoutParams params = window.getAttributes();
                DisplayMetrics dm = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(dm);
                params.width = (int)(dm.widthPixels * 0.9);

                window.setAttributes(params);
            }
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