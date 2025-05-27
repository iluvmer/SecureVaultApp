package com.meriem.securevaultapp.screens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.meriem.securevaultapp.R;
import com.meriem.securevaultapp.models.RealmPasswords;

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

        realm = Realm.getDefaultInstance();
        passwordList = realm.where(RealmPasswords.class).findAll();
        Log.d("RealmDebug", "Password list size: " + passwordList.size());
        adapter = new PasswordAdapter(passwordList, new PasswordAdapter.OnPasswordClickListener() {
            @Override
            public void onDeleteClicked(PasswordEntry password) {
                new AlertDialog.Builder(MainPasswordListScreen.this)
                        .setTitle("Delete Password")
                        .setMessage("Are you sure you want to delete this password?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            int index = passwordList.indexOf(password);
                            if (index != -1) {
                                passwordList.remove(index);
                                adapter.notifyItemRemoved(index);
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }

            @Override
            public void onEditClicked(PasswordEntry password) {
                // Show popup dialog to edit
                int index = passwordList.indexOf(password);
                if (index != -1) {
                    showEditDialog(password, index);
                }
            }
            @Override
            public void onPasswordUpdated(PasswordEntry updatedEntry) {
                int index = passwordList.indexOf(updatedEntry);
                if (index != -1) {
                    adapter.notifyItemChanged(index);
                }
            }
        });
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
            new Thread(() -> {
                Realm realm = Realm.getDefaultInstance();
                realm.executeTransaction(r -> {
                    RealmPasswords entry = r.createObject(RealmPasswords.class, UUID.randomUUID().toString());
                    entry.setWebsite(website);
                    entry.setEmail(email);
                    Log.d("CryptoDebug", "Before saving: " + encryptedPassword);
                    entry.setPassword(encryptedPassword);
                    Log.d("RealmDebug", "Saved entry: " + entry.getWebsite());
                });
                realm.close();
            }).start();
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

    private void showEditDialog(PasswordEntry entry, int position) {
        EditPasswordDialog dialog = new EditPasswordDialog(this, entry, (updatedEntry) -> {

            passwordList.set(position, updatedEntry);
            adapter.notifyItemChanged(position);
        });
        dialog.show();
    }
}