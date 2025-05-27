package com.meriem.securevaultapp.screens;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.meriem.securevaultapp.R;

public class PasswordDetailActivity extends AppCompatActivity {

    private TextView textViewWebsite;
    private TextView textViewEmail;
    private TextView textViewPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_detail);

        // toolbar
        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());

        // references to views
        textViewWebsite = findViewById(R.id.textViewWebsite);
        textViewEmail = findViewById(R.id.textViewEmail);
        textViewPassword = findViewById(R.id.textViewPassword);

        // get intent data from the adapter
        Intent intent = getIntent();
        if (intent != null) {
            String website = intent.getStringExtra("website");
            String email = intent.getStringExtra("email");
            String encryptedPassword = intent.getStringExtra("password");
            String decryptedPassword = CryptoHelper.decrypt(encryptedPassword);
            textViewWebsite.setText("Website: " + website);
            textViewEmail.setText("Email: " + email);
            textViewPassword.setText("Password: " + decryptedPassword);
        }
    }
}
