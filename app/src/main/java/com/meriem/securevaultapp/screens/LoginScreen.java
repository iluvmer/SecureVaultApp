package com.meriem.securevaultapp.screens;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.meriem.securevaultapp.R;
import com.meriem.securevaultapp.models.RealmUser;

import java.util.Objects;
import java.util.concurrent.Executor;

import io.realm.Realm;

public class LoginScreen extends AppCompatActivity {
    EditText emailEditText, passwordEditText;
    TextView loginButton, goToRegisterButton, biometricLoginButton;
    FirebaseAuth mAuth;
    Executor executor;
    BiometricPrompt biometricPrompt;
    BiometricPrompt.PromptInfo promptInfo;
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_afak);

        // Initialize Realm
        realm = Realm.getDefaultInstance();
        mAuth = FirebaseAuth.getInstance();

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        goToRegisterButton = findViewById(R.id.goToRegisterButton);
        biometricLoginButton = findViewById(R.id.fingerprintLoginButton);

        // Login with email and password
        loginButton.setOnClickListener(view -> {
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

                            // Add delay to ensure Realm sync
                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                checkRealmUser(uid);
                            }, 500);
                        } else {
                            Toast.makeText(LoginScreen.this,
                                    "Login failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Navigate to register screen
        goToRegisterButton.setOnClickListener(view -> {
            startActivity(new Intent(LoginScreen.this, RegisterScreen.class));
        });

        // Biometric setup
        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(LoginScreen.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(LoginScreen.this, "Fingerprint recognized!", Toast.LENGTH_SHORT).show();
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if (firebaseUser != null) {
                    String uid = firebaseUser.getUid();
                    // Check Realm user for biometric login too
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        checkRealmUser(uid);
                    }, 500);
                }
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(LoginScreen.this, "Fingerprint not recognized. Try again.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(LoginScreen.this, "Error: " + errString, Toast.LENGTH_SHORT).show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Login with Fingerprint")
                .setSubtitle("Use biometric authentication to login")
                .setNegativeButtonText("Use Password Instead")
                .build();

        biometricLoginButton.setOnClickListener(view -> {
            BiometricManager biometricManager = BiometricManager.from(this);
            if (biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS) {
                biometricPrompt.authenticate(promptInfo);
            } else {
                Toast.makeText(this, "Biometric authentication is not available on this device", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkRealmUser(String uid) {
        try (Realm localRealm = Realm.getDefaultInstance()) {
            RealmUser user = localRealm.where(RealmUser.class)
                    .equalTo("uid", uid.trim())
                    .findFirst();

            if (user != null) {
                Log.d("Login", "Realm user data - UID: " + user.getUid());
                Log.d("Login", "Email: " + user.getEmail());
                Log.d("Login", "Name: " + user.getFirstName() + " " + user.getLastName());

                Intent intent = new Intent(LoginScreen.this, MainActivityAfak.class);
                intent.putExtra("uid", uid);
                startActivity(intent);
                finish();
            } else {
                Log.e("Login", "User NOT FOUND in Realm");
                Toast.makeText(this, "User data missing. Please register again.", Toast.LENGTH_LONG).show();
                FirebaseAuth.getInstance().signOut();
            }
        } catch (Exception e) {
            Log.e("Login", "Realm error", e);
            Toast.makeText(this, "Error accessing user data", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
    }
}