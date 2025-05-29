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
                getOnBackPressedDispatcher().onBackPressed();
            }
        });


        RecyclerView recyclerView = findViewById(R.id.recyclerViewPasswords);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        realm = Realm.getDefaultInstance();
        passwordList = realm.where(RealmPasswords.class).findAll();
        Log.d("RealmDebug", "Password list size: " + passwordList.size());
        adapter = new PasswordAdapter(passwordList, new PasswordAdapter.OnPasswordClickListener() {
            @Override
            public void onDeleteClicked(RealmPasswords password) {
                new AlertDialog.Builder(MainPasswordListScreen.this)
                        .setTitle("Delete Password")
                        .setMessage("Are you sure you want to delete this password?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            String uidToDelete = password.getUid();
                            new Thread(() -> {
                                try (Realm bgRealm = Realm.getDefaultInstance()) {
                                    bgRealm.executeTransaction(r -> {
                                        RealmPasswords itemToDelete = r.where(RealmPasswords.class)
                                                .equalTo("uid", uidToDelete)
                                                .findFirst();

                                        if (itemToDelete != null && itemToDelete.isValid()) {
                                            itemToDelete.deleteFromRealm();
                                            Log.d("Debug", "Successfully deleted: " + uidToDelete);
                                        } else {
                                            Log.d("Debug", "Item not found or already deleted: " + uidToDelete);
                                        }
                                    });
                                } catch (Exception e) {
                                    Log.e("Debug", "Exception during deletion", e);
                                }
                            }).start();

                        })
                        .setNegativeButton("Cancel", null)
                        .show();


            }
            @Override
            public void onEditClicked(RealmPasswords password) {
                String uidToDelete = password.getUid();
                if (uidToDelete != null) {
                    showEditDialog(password);
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

    private void showEditDialog(RealmPasswords entry) {
        // Make a copy from Realm-managed object to use inside the new background thread ;)
        RealmPasswords detachedEntry;
        try {
            detachedEntry = realm.copyFromRealm(entry); // <- This makes it safe
        } catch (Exception e) {
            Log.e("EditError", "Failed to copy Realm object", e);
            return;
        }
        String uidToEdit = detachedEntry.getUid(); // hna we use a copy of entry bcs it's thread confined to UI thread and we want to use detached copy inside the other thread :D
        EditPasswordDialog dialog = new EditPasswordDialog(this, detachedEntry, (updatedEntry) -> {
            String newEmail = updatedEntry.getEmail();
            String newPassword = updatedEntry.getPassword();

            Log.d("EditDebug", "New values: " + updatedEntry.getWebsite() + ", " + newEmail + ", " + newPassword);

            new Thread(() -> {
                try (Realm bgRealm = Realm.getDefaultInstance()) {
                    bgRealm.executeTransaction(r -> {
                        RealmPasswords itemToEdit = r.where(RealmPasswords.class)
                                .equalTo("uid", uidToEdit)
                                .findFirst();
                        if (itemToEdit != null && itemToEdit.isValid()) {
                            itemToEdit.setEmail(newEmail);
                            itemToEdit.setPassword(newPassword);
                            Log.d("EditDebug", "Successfully edited: " + uidToEdit);
                        } else {
                            Log.d("EditDebug", "Item not found or already deleted: " + uidToEdit);
                        }
                    });
                    runOnUiThread(() -> adapter.notifyDataSetChanged());
                } catch (Exception e) {
                    Log.e("EditError", "Failed to edit item", e);
                }
            }).start();
        });

        dialog.show();
    }

}