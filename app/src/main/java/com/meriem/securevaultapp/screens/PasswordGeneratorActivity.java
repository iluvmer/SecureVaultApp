package com.meriem.securevaultapp.screens;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.slider.Slider;
import com.meriem.securevaultapp.R;

import java.security.SecureRandom;

public class PasswordGeneratorActivity extends AppCompatActivity {

    private TextView generatedPasswordTextView;
    private CheckBox uppercaseCheckBox, lowercaseCheckBox, numbersCheckBox, symbolsCheckBox;
    private Slider lengthSlider;
    private TextView lengthValueTextView;
    private TextView passwordStrengthTextView;

    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String NUMBERS = "0123456789";
    private static final String SYMBOLS = "!@#$%^&*()-_=+[]{};:,.<>?/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_generator);

        // Initialize Views
        generatedPasswordTextView = findViewById(R.id.textViewGeneratedPassword);
        uppercaseCheckBox = findViewById(R.id.checkUppercase);
        lowercaseCheckBox = findViewById(R.id.checkLowercase);
        numbersCheckBox = findViewById(R.id.checkNumbers);
        symbolsCheckBox = findViewById(R.id.checkSymbols);
        lengthSlider = findViewById(R.id.sliderLength);
        lengthValueTextView = findViewById(R.id.textPasswordLength);
        passwordStrengthTextView = findViewById(R.id.textStrengthIndicator);

        // Update password length as slider moves
        lengthSlider.addOnChangeListener((slider, value, fromUser) -> {
            int length = Math.round(value);
            lengthValueTextView.setText(length + " characters");
        });

        // Generate button
        findViewById(R.id.buttonGenerate).setOnClickListener(v -> generatePassword());

        // Copy button
        findViewById(R.id.buttonCopyPassword).setOnClickListener(v -> {
            String password = generatedPasswordTextView.getText().toString();
            if (!password.isEmpty()) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Generated Password", password);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "Password copied!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void generatePassword() {
        StringBuilder charPool = new StringBuilder();

        if (uppercaseCheckBox.isChecked()) charPool.append(UPPERCASE);
        if (lowercaseCheckBox.isChecked()) charPool.append(LOWERCASE);
        if (numbersCheckBox.isChecked()) charPool.append(NUMBERS);
        if (symbolsCheckBox.isChecked()) charPool.append(SYMBOLS);

        if (charPool.length() == 0) {
            Toast.makeText(this, "Please select at least one option", Toast.LENGTH_SHORT).show();
            return;
        }

        int length = Math.round(lengthSlider.getValue());
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(charPool.length());
            password.append(charPool.charAt(index));
        }

        String generatedPassword = password.toString();

        generatedPasswordTextView.setText(password.toString());
        updateStrengthIndicator(password.toString());

        Intent resultIntent = new Intent();
        resultIntent.putExtra("generatedPassword", generatedPassword);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void updateStrengthIndicator(String password) {
        int score = 0;

        if (password.length() >= 12) {
            score++;
            Log.d("PasswordStrength", "Length is good → +1");//thee logs are here just for me to see what is counting and what is not
        }

        if (password.matches(".*[A-Z].*")) {
            score++;
            Log.d("PasswordStrength", "Contains uppercase → +1");
        }

        if (password.matches(".*[a-z].*")) {
            score++;
            Log.d("PasswordStrength", "Contains lowercase → +1");
        }

        if (password.matches(".*\\d.*")) {
            score++;
            Log.d("PasswordStrength", "Contains number → +1");
        }

        if (password.matches(".*[!@#$%^&*()\\-_=+\\[{\\]};:,.<>?/].*")) {
            score++;
            Log.d("PasswordStrength", "Contains symbol → +1");
        }
        Log.d("PasswordStrength", "Total Score = " + score);


        String strength;
        int color;

        switch (score) {
            case 5:
                strength = "Very Strong";
                color = Color.GREEN;
                break;
            case 4:
                strength = "Strong";
                color = Color.BLUE;
                break;
            case 3:
                strength = "Medium";
                color = Color.YELLOW;
                break;
            default:
                strength = "Weak";
                color = Color.RED;
                break;
        }

        passwordStrengthTextView.setText(strength);
        passwordStrengthTextView.setTextColor(color);
    }
}
