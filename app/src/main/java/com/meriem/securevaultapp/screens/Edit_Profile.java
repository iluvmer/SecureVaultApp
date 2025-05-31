package com.meriem.securevaultapp.screens;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.meriem.securevaultapp.R;
import com.meriem.securevaultapp.models.RealmUser;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.realm.Realm;

public class Edit_Profile extends AppCompatActivity {
    private TextInputEditText editName, editEmail, editPhone, editPassword;
    private Button saveButton;
    private Realm realm;
    private String uid;
    private ImageView profileImage;
    private ImageButton editImageButton;
    private Uri selectedImageUri;
    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_GALLERY = 2;
    private Uri photoURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize Realm
        realm = Realm.getDefaultInstance();

        // Get user ID from intent
        uid = getIntent().getStringExtra("uid");
        if (uid == null || uid.isEmpty()) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        editName = findViewById(R.id.edit_Name);
        editEmail = findViewById(R.id.edit_Email);
        editPhone = findViewById(R.id.edit_Phone);
        editPassword = findViewById(R.id.edit_Password);
        saveButton = findViewById(R.id.savebtn);
        ImageButton go_back = findViewById(R.id.back_btn);
        profileImage = findViewById(R.id.profile_image);
        editImageButton = findViewById(R.id.edit_img);

        // Load current user data
        loadCurrentUserData();

        go_back.setOnClickListener(v -> finish());

        editImageButton.setOnClickListener(v -> showImagePickerDialog());

        saveButton.setOnClickListener(v -> {
            String name = editName.getText().toString().trim();
            String email = editEmail.getText().toString().trim();
            String phone = editPhone.getText().toString().trim();
            String newPassword = editPassword.getText().toString().trim();

            // Validate inputs
            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Name and Email are required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.isEmpty() && newPassword.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedImageUri != null) {
                uploadImageAndUpdateProfile(name, phone, newPassword);
            } else {
                updateUserProfile(name, phone, newPassword);
            }
        });

        editEmail.setOnClickListener(v -> {
            Toast.makeText(this, R.string.email_locked_message, Toast.LENGTH_LONG).show();
        });
        checkPermissions();

    }
    public void onClickTakePicture(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        getApplicationContext().getPackageName() + ".provider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_CAMERA);
            }
        }
    }
    private File currentPhotoFile; // Add this class variable

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        currentPhotoFile = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        return currentPhotoFile;
    }
    private void showImagePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Profile Picture");
        builder.setItems(new CharSequence[]{"Take Photo", "Choose from Gallery", "Cancel"},
                (dialog, which) -> {
                    switch (which) {
                        case 0: // Take Photo
                            onClickTakePicture(null);
                            break;
                        case 1: // Choose from Gallery
                            onClickPickGallery(null);
                            break;
                        case 2: // Cancel
                            dialog.dismiss();
                            break;
                    }
                });
        builder.show();
    }
    public void onClickPickGallery(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                if (currentPhotoFile != null && currentPhotoFile.exists()) {
                    selectedImageUri = Uri.fromFile(currentPhotoFile);
                    Picasso.get()
                            .load(selectedImageUri)
                            .into(profileImage);
                }
            } else if (requestCode == REQUEST_GALLERY && data != null) {
                selectedImageUri = data.getData();
                try {
                    // Make a local copy of the gallery image
                    currentPhotoFile = createImageFile();
                    copyUriToFile(selectedImageUri, currentPhotoFile);
                    selectedImageUri = Uri.fromFile(currentPhotoFile);
                    Picasso.get()
                            .load(selectedImageUri)
                            .into(profileImage);
                } catch (IOException e) {
                    Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    private void copyUriToFile(Uri uri, File file) throws IOException {
        try (InputStream in = getContentResolver().openInputStream(uri);
             OutputStream out = new FileOutputStream(file)) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        }
    }

    private void uploadImageAndUpdateProfile(String name, String phone, String newPassword) {
        if (selectedImageUri == null) {
            updateUserProfile(name, phone, newPassword);
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving profile...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        try {
            // Convert image to Base64
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] imageBytes = baos.toByteArray();
            String imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            // Save to Realm
            realm.executeTransactionAsync(r -> {
                RealmUser user = r.where(RealmUser.class)
                        .equalTo("uid", uid)
                        .findFirst();

                if (user != null) {
                    user.setProfileImageUrl(imageString);
                    // Update other fields
                    String[] names = name.split(" ", 2);
                    user.setFirstName(names[0]);
                    user.setLastName(names.length > 1 ? names[1] : "");
                    user.setPhone(phone.trim().isEmpty() ? null : phone.trim());
                }
            }, () -> {
                progressDialog.dismiss();
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }, error -> {
                progressDialog.dismiss();
                Toast.makeText(this, "Failed to save profile: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            });

        } catch (IOException e) {
            progressDialog.dismiss();
            Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadCurrentUserData() {
        RealmUser user = realm.where(RealmUser.class)
                .equalTo("uid", uid)
                .findFirst();

        if (user != null) {
            // Set name (combine first and last)
            String fullName = user.getFirstName() +
                    (user.getLastName() != null && !user.getLastName().isEmpty() ?
                            " " + user.getLastName() : "");
            editName.setText(fullName);

            // Set email
            editEmail.setText(user.getEmail());

            // Set phone if available
            if (user.getPhone() != null && !user.getPhone().isEmpty()) {
                editPhone.setText(user.getPhone());
            } else {
                editPhone.setText("");
            }

            // Load profile image if available
            if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                byte[] imageBytes = Base64.decode(user.getProfileImageUrl(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                profileImage.setImageBitmap(bitmap);
            } else {
                profileImage.setImageResource(R.drawable.pfp); // Default image
            }

            // Password field is intentionally left empty
            editPassword.setText("");
        }
    }

    private void updateUserProfile(String name, String phone, String newPassword) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        // Only handle password changes if needed
        if (!newPassword.isEmpty()) {
            showReauthenticationDialog(newPassword);
        } else {
            updateRealmProfile(name, firebaseUser.getEmail(), phone);
        }
    }

    private void updateRealmProfile(String name, String email, String phone) {
        realm.executeTransactionAsync(r -> {
            RealmUser user = r.where(RealmUser.class)
                    .equalTo("uid", uid)
                    .findFirst();

            if (user != null) {
                String[] names = name.split(" ", 2);
                user.setFirstName(names[0]);
                user.setLastName(names.length > 1 ? names[1] : "");
                user.setEmail(email);
                user.setPhone(phone.trim().isEmpty() ? null : phone.trim());
            }
        }, () -> {
            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        }, error -> {
            Toast.makeText(this, "Failed to update profile: " + error.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void showReauthenticationDialog(String newPassword) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_reauthenticate, null);
        builder.setView(dialogView);

        TextInputEditText passwordInput = dialogView.findViewById(R.id.current_password_input);
        Button confirmButton = dialogView.findViewById(R.id.confirm_button);

        AlertDialog dialog = builder.create();
        dialog.show();

        confirmButton.setOnClickListener(v -> {
            String currentPassword = passwordInput.getText().toString().trim();
            if (currentPassword.isEmpty()) {
                passwordInput.setError("Please enter your current password");
                return;
            }

            reauthenticateAndUpdatePassword(currentPassword, newPassword, dialog);
        });
    }

    private void reauthenticateAndUpdatePassword(String currentPassword, String newPassword, AlertDialog dialog) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.getEmail() == null) return;

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);

        user.reauthenticate(credential)
                .addOnCompleteListener(reauthTask -> {
                    if (reauthTask.isSuccessful()) {
                        dialog.dismiss();
                        updateFirebasePassword(newPassword);
                    } else {
                        Toast.makeText(this, "Authentication failed: Wrong password", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateFirebasePassword(String newPassword) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        user.updatePassword(newPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(this, "Password update failed: " +
                                task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
    // Add this to your Edit_Profile activity
    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showImagePickerDialog();
            } else {
                Toast.makeText(this, "Permissions are required to change profile picture", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    100);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
    }
}