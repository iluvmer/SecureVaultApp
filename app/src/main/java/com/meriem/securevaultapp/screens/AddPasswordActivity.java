package com.meriem.securevaultapp.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.meriem.securevaultapp.R;

public class AddPasswordActivity extends AppCompatActivity {

    EditText editTextWebsite, editTextEmail, editTextPassword;
    Button buttonSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_password);
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // or use finish();
            }
        });

        editTextWebsite = findViewById(R.id.editTextWebsite);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonSave = findViewById(R.id.buttonSave);

        buttonSave.setOnClickListener(v -> {
            String website = editTextWebsite.getText().toString();
            String email = editTextEmail.getText().toString();
            String password = editTextPassword.getText().toString();

            Intent resultIntent = new Intent();
            resultIntent.putExtra("website", website);
            resultIntent.putExtra("email", email);
            resultIntent.putExtra("password", password);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }
}