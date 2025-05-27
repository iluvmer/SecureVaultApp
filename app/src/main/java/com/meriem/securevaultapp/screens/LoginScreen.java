package com.meriem.securevaultapp.screens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.meriem.securevaultapp.R;

import java.util.Objects;
import java.util.concurrent.Executor;

public class LoginScreen extends AppCompatActivity {
    EditText emailEditText, passwordEditText;
    TextView loginButton, goToRegisterButton, biometricLoginButton;
    FirebaseAuth mAuth;
    Executor executor;
    BiometricPrompt biometricPrompt;
    BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_afak);

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
                            Toast.makeText(LoginScreen.this, "Login successful!", Toast.LENGTH_SHORT).show();
                            // Navigate to home screen or next screen
                            String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                            Intent intent = new Intent(LoginScreen.this, Notes_Activity.class);
                            intent.putExtra("uid", uid); // Pass UID
                            startActivity(intent);
                            finish(); // optional: prevent back navigation to login
                        } else {
                            Toast.makeText(LoginScreen.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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
                // Navigate to the next screen after successful authentication
                // startActivity(new Intent(LoginScreen.this, HomeScreen.class)); // Example
                String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                Intent intent = new Intent(LoginScreen.this, Notes_Activity.class);
                intent.putExtra("uid", uid);
                startActivity(intent);
                finish();
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
}
