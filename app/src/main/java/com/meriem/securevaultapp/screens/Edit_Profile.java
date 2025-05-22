package com.meriem.securevaultapp.screens;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.meriem.securevaultapp.R;

public class Edit_Profile extends AppCompatActivity {

    TextInputEditText editName, editEmail, editPhone, editPassword;
    Button savebutton;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        editName = findViewById(R.id.edit_Name);
        editEmail=findViewById(R.id.edit_Email);
        editPhone=findViewById(R.id.edit_Phone);
        editPassword=findViewById(R.id.edit_Password);
        savebutton=findViewById(R.id.savebtn);

        savebutton.setOnClickListener(v -> {
            String name = editName.getText().toString().trim();
            String email = editEmail.getText().toString().trim();
            String phone = editPhone.getText().toString().trim();

            //saving_profile_infoss
        });
    }
}