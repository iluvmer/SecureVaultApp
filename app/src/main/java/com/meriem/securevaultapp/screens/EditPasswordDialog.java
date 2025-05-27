package com.meriem.securevaultapp.screens;

import android.app.Dialog;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;

import com.meriem.securevaultapp.R;
import com.meriem.securevaultapp.models.RealmPasswords;

public class EditPasswordDialog extends Dialog {

    public interface OnPasswordUpdatedListener {
        void onPasswordUpdated(RealmPasswords updatedEntry);
    }

    public EditPasswordDialog(Context context, RealmPasswords originalEntry, OnPasswordUpdatedListener listener) {
        super(context);
        setContentView(R.layout.dialog_edit_password);

        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        int width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.9);
        getWindow().setLayout(width, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);

        EditText editWebsite = findViewById(R.id.editWebsite);
        EditText editEmail = findViewById(R.id.editEmail);
        EditText editPassword = findViewById(R.id.editPassword);
        Button btnSave = findViewById(R.id.btnSave);

        // Fill existing values
        editWebsite.setText(originalEntry.getWebsite());
        editEmail.setText(originalEntry.getEmail());
        editPassword.setText(CryptoHelper.decrypt(originalEntry.getPassword()));


        editWebsite.setEnabled(false);
        editWebsite.setFocusable(false);
        editWebsite.setClickable(false);

        btnSave.setOnClickListener(v -> {
            String newEmail = editEmail.getText().toString();
            String newPassword = editPassword.getText().toString();
            String newEncrypted = CryptoHelper.encrypt(newPassword);


            originalEntry.setEmail(newEmail);
            originalEntry.setPassword(newEncrypted);

            listener.onPasswordUpdated(originalEntry);
            dismiss();
        });
    }
}
