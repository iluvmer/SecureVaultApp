package com.meriem.securevaultapp.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.meriem.securevaultapp.R;

public class Edit_Profile extends AppCompatActivity {

    TextInputEditText editName, editEmail, editPhone, editPassword;
    Button saveButton;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        editName = findViewById(R.id.edit_Name);
        editEmail=findViewById(R.id.edit_Email);
        editPhone=findViewById(R.id.edit_Phone);
        editPassword=findViewById(R.id.edit_Password);
        saveButton=findViewById(R.id.savebtn);
        ImageButton go_back=findViewById(R.id.back_btn);

        go_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Edit_Profile.this, Profile.class);
                startActivity(intent);
            }
        });


        saveButton.setOnClickListener(v -> {
            String name = editName.getText().toString().trim();
            String email = editEmail.getText().toString().trim();
            String phone = editPhone.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}