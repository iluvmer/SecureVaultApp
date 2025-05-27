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
import com.meriem.securevaultapp.models.RealmPasswords;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmResults;

public class MainPasswordListScreen extends AppCompatActivity {

    private static final int ADD_PASSWORD_REQUEST_CODE = 1;

    private RecyclerView recyclerView;
    private PasswordAdapter adapter;
    private RealmResults<RealmPasswords> passwordList;
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Realm.init(this);
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

        Realm realm = Realm.getDefaultInstance();
        passwordList = realm.where(RealmPasswords.class).findAll();
        adapter = new PasswordAdapter(passwordList);
        recyclerView.setAdapter(adapter);

        passwordList.addChangeListener(results -> adapter.notifyDataSetChanged());

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
            // Save password to Realm
            Realm realm = Realm.getDefaultInstance();
            realm.executeTransaction(realm1 -> {
                RealmPasswords passwordEntry = realm1.createObject(RealmPasswords.class, UUID.randomUUID().toString());
                passwordEntry.setWebsite(website);
                passwordEntry.setEmail(email);
                passwordEntry.setPassword(encryptedPassword);
            });
            realm.close();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (passwordList != null && passwordList.isValid()) {
            passwordList.removeAllChangeListeners();
        }
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
    }
}