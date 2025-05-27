package com.meriem.securevaultapp.screens;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.meriem.securevaultapp.R;
import com.meriem.securevaultapp.models.RealmUser;

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
            boolean enableBiometric = enableBiometricCheckbox.isChecked();

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                saveUserToRealm(firebaseUser.getUid(), email, firstName, lastName, enableBiometric);

                                if (enableBiometric) {
                                    saveBiometricEnabled(email);
                                }
                                Toast.makeText(this, "Registered successfully!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, LoginScreen.class));
                                finish();
                            }
                        } else {
                            Toast.makeText(this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        goToLoginButton.setOnClickListener(view -> startActivity(new Intent(this, LoginScreen.class)));
    }

    private void saveUserToRealm(String uid, String email, String firstName, String lastName, boolean useFingerprintLogin) {
        realm.executeTransaction(r -> {
            RealmUser user = r.createObject(RealmUser.class, uid);
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setUseFingerprintLogin(useFingerprintLogin);
        });
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
