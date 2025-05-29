package com.meriem.securevaultapp.screens;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.meriem.securevaultapp.R;
import com.meriem.securevaultapp.models.RealmPasswords;

import java.util.UUID;

import io.realm.Realm;

public class AddPasswordActivity extends AppCompatActivity {

    EditText editTextWebsite, editTextEmail, editTextPassword;
    Button buttonSave;
    boolean isPasswordVisible = false;
    private static final int PASSWORD_GENERATOR_REQUEST = 100;

    @SuppressLint("ClickableViewAccessibility")
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
            if (website.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            }
            else {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("website", website);
                resultIntent.putExtra("email", email);
                resultIntent.putExtra("password", password);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });

        ImageButton buttonOpenGenerator = findViewById(R.id.buttonGenerator);
        buttonOpenGenerator.setOnClickListener(v -> {
            Intent intent = new Intent(this, PasswordGeneratorActivity.class);
            startActivityForResult(intent, PASSWORD_GENERATOR_REQUEST);
        });

        editTextPassword.setOnTouchListener((v, event) -> {
            final int DRAWABLE_END = 2;

            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (editTextPassword.getRight() - editTextPassword.getCompoundDrawables()[DRAWABLE_END].getBounds().width())) {
                    isPasswordVisible = !isPasswordVisible;


                    if (isPasswordVisible) {
                        editTextPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        editTextPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.eye, 0);

                    } else {
                        editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        editTextPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.eyec, 0);

                    }


                    editTextPassword.setSelection(editTextPassword.length());
                    v.performClick();
                    return true;
                }
            }
            return false;
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PASSWORD_GENERATOR_REQUEST && resultCode == RESULT_OK) {
            String password = data.getStringExtra("generatedPassword");
            editTextPassword.setText(password);
        }
    }
}