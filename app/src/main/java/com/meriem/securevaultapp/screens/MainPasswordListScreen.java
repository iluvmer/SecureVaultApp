package com.meriem.securevaultapp.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.meriem.securevaultapp.R;

import java.util.ArrayList;
import java.util.List;

public class MainPasswordListScreen extends AppCompatActivity {

    private static final int ADD_PASSWORD_REQUEST_CODE = 1;

    private RecyclerView recyclerView;
    private PasswordAdapter adapter;
    private List<PasswordEntry> passwordList;//we should use a bd and pu what in the list in the bd to store the passwords safely

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.password_list);
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // or use finish();
            }
        });


        recyclerView = findViewById(R.id.recyclerViewPasswords);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        passwordList = new ArrayList<>();
        adapter = new PasswordAdapter(passwordList);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(MainPasswordListScreen.this, AddPasswordActivity.class);
            startActivityForResult(intent, ADD_PASSWORD_REQUEST_CODE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_PASSWORD_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String website = data.getStringExtra("website");
            String email = data.getStringExtra("email");
            String password = data.getStringExtra("password");
            String encryptedPassword = CryptoHelper.encrypt(password);
            PasswordEntry entry = new PasswordEntry(website, email, encryptedPassword);//the password should be stored in the db cuz the adapter's job is to showcase the website name and the email and the icon but not the password so we shall have the password in the list for now :>
            passwordList.add(entry);
            adapter.notifyItemInserted(passwordList.size() - 1);
        }
    }
}