package com.meriem.securevaultapp.screens;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.meriem.securevaultapp.R;
import com.meriem.securevaultapp.models.RealmUser;

import java.util.Objects;

import io.realm.Realm;

public class RegisterScreen extends AppCompatActivity {
    EditText emailEditText, passwordEditText, firstNameEditText, lastNameEditText;
    Button registerButton, goToLoginButton;
    CheckBox enableBiometricCheckbox;
    FirebaseAuth mAuth;
    Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_afak);

        //Realm.init(this);
        realm = Realm.getDefaultInstance();
        mAuth = FirebaseAuth.getInstance();

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        enableBiometricCheckbox = findViewById(R.id.enableBiometricCheckbox);
        registerButton = findViewById(R.id.registerButton);
        goToLoginButton = findViewById(R.id.goToLoginButton);

        registerButton.setOnClickListener(view -> {
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            String firstName = firstNameEditText.getText().toString();
            String lastName = lastNameEditText.getText().toString();

            Log.d("Register", "Attempting register: " + email + ", Name: " + firstName + " " + lastName);

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                            Log.d("Register", "Firebase auth success! UID: " + uid);

                            saveUserToRealm(uid, email, firstName, lastName, false); // Save to Realm
                            Toast.makeText(this, "Registered!", Toast.LENGTH_SHORT).show();

                            // Proceed to login
                            startActivity(new Intent(this, LoginScreen.class));
                            finish();
                        } else {
                            Log.e("Register", "Registration failed", task.getException());
                            Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        goToLoginButton.setOnClickListener(view -> startActivity(new Intent(this, LoginScreen.class)));
    }

    private void saveUserToRealm(String uid, String email, String firstName, String lastName, boolean useFingerprintLogin) {
        realm.executeTransaction(realm -> {
            // Create new object using constructor
            RealmUser user = new RealmUser(uid, email, firstName, lastName);
            realm.copyToRealmOrUpdate(user);

            // Immediate verification
            RealmUser verifiedUser = realm.where(RealmUser.class)
                    .equalTo("uid", uid)
                    .findFirst();
            Log.d("Register", "Verified UID: " + verifiedUser.getUid());
            Log.d("Register", "Verified Email: " + verifiedUser.getEmail());
        });

        startActivity(new Intent(this, LoginScreen.class));
        finish();
    }


    private void saveBiometricEnabled(String email) {
        SharedPreferences prefs = getSharedPreferences("BiometricPrefs", MODE_PRIVATE);
        prefs.edit().putString("biometric_email", email).apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
