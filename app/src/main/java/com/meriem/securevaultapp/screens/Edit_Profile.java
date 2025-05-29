package com.meriem.securevaultapp.screens;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.meriem.securevaultapp.R;
import com.meriem.securevaultapp.models.RealmUser;

import java.util.Objects;

import io.realm.Realm;

public class Edit_Profile extends AppCompatActivity {
    private TextInputEditText editName, editEmail, editPhone, editPassword;
    private Button saveButton;
    private Realm realm;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize Realm
        realm = Realm.getDefaultInstance();

        // Get user ID from intent
        uid = getIntent().getStringExtra("uid");
        if (uid == null || uid.isEmpty()) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        editName = findViewById(R.id.edit_Name);
        editEmail = findViewById(R.id.edit_Email);
        editPhone = findViewById(R.id.edit_Phone);
        editPassword = findViewById(R.id.edit_Password);
        saveButton = findViewById(R.id.savebtn);
        ImageButton go_back = findViewById(R.id.back_btn);

        // Load current user data
        loadCurrentUserData();

        go_back.setOnClickListener(v -> finish());

        saveButton.setOnClickListener(v -> {
            String name = editName.getText().toString().trim();
            String email = editEmail.getText().toString().trim();
            String phone = editPhone.getText().toString().trim();
            String newPassword = editPassword.getText().toString().trim();

            // Validate inputs
            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Name and Email are required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.isEmpty() && newPassword.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            updateUserProfile(name, phone, newPassword);
        });
        editEmail.setOnClickListener(v -> {
            Toast.makeText(this, R.string.email_locked_message, Toast.LENGTH_LONG).show();
        });
    }

    private void loadCurrentUserData() {
        RealmUser user = realm.where(RealmUser.class)
                .equalTo("uid", uid)
                .findFirst();

        if (user != null) {
            // Set name (combine first and last)
            String fullName = user.getFirstName() +
                    (user.getLastName() != null && !user.getLastName().isEmpty() ?
                            " " + user.getLastName() : "");
            editName.setText(fullName);

            // Set email
            editEmail.setText(user.getEmail());

            // Set phone if available
            if (user.getPhone() != null && !user.getPhone().isEmpty()) {
                editPhone.setText(user.getPhone());
            } else {
                editPhone.setText("");
            }

            // Password field is intentionally left empty
            editPassword.setText("");
        }
    }

    private void updateUserProfile(String name, String phone, String newPassword) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        // Only handle password changes if needed
        if (!newPassword.isEmpty()) {
            showReauthenticationDialog(newPassword);
        } else {
            updateRealmProfile(name, firebaseUser.getEmail(), phone);
        }
    }

    private void updateRealmProfile(String name, String email, String phone) {
        realm.executeTransactionAsync(r -> {
            RealmUser user = r.where(RealmUser.class)
                    .equalTo("uid", uid)
                    .findFirst();

            if (user != null) {
                String[] names = name.split(" ", 2);
                user.setFirstName(names[0]);
                user.setLastName(names.length > 1 ? names[1] : "");
                user.setEmail(email);
                user.setPhone(phone.trim().isEmpty() ? null : phone.trim());
            }
        }, () -> {
            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        }, error -> {
            Toast.makeText(this, "Failed to update profile: " + error.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void showReauthenticationDialog(String newPassword) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_reauthenticate, null);
        builder.setView(dialogView);

        TextInputEditText passwordInput = dialogView.findViewById(R.id.current_password_input);
        Button confirmButton = dialogView.findViewById(R.id.confirm_button);

        AlertDialog dialog = builder.create();
        dialog.show();

        confirmButton.setOnClickListener(v -> {
            String currentPassword = passwordInput.getText().toString().trim();
            if (currentPassword.isEmpty()) {
                passwordInput.setError("Please enter your current password");
                return;
            }

            reauthenticateAndUpdatePassword(currentPassword, newPassword, dialog);
        });
    }

    private void reauthenticateAndUpdatePassword(String currentPassword, String newPassword, AlertDialog dialog) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.getEmail() == null) return;

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);

        user.reauthenticate(credential)
                .addOnCompleteListener(reauthTask -> {
                    if (reauthTask.isSuccessful()) {
                        dialog.dismiss();
                        updateFirebasePassword(newPassword);
                    } else {
                        Toast.makeText(this, "Authentication failed: Wrong password", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateFirebasePassword(String newPassword) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        user.updatePassword(newPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(this, "Password update failed: " +
                                task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
    }
}