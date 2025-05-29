package com.meriem.securevaultapp.services;

import android.app.assist.AssistStructure;
import android.os.Build;
import android.os.CancellationSignal;
import android.service.autofill.AutofillService;
import android.service.autofill.Dataset;
import android.service.autofill.FillCallback;
import android.service.autofill.FillContext;
import android.service.autofill.FillRequest;
import android.service.autofill.FillResponse;
import android.service.autofill.SaveCallback;
import android.service.autofill.SaveInfo;
import android.service.autofill.SaveRequest;
import android.util.Log;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillValue;
import android.widget.RemoteViews;

import androidx.annotation.RequiresApi;

import com.meriem.securevaultapp.models.RealmPasswords;
import com.meriem.securevaultapp.screens.CryptoHelper;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import io.realm.Realm;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MyAutofillService extends AutofillService {

    private static final String TAG = "Autofill";

    @Override
    public void onFillRequest(FillRequest request, CancellationSignal cancellationSignal, FillCallback callback) {
        List<FillContext> contexts = request.getFillContexts();
        AssistStructure structure = contexts.get(contexts.size() - 1).getStructure();
        Log.d(TAG, "onFillRequest triggered!");

        Realm realm = Realm.getDefaultInstance();
        List<RealmPasswords> savedEntries = realm.where(RealmPasswords.class).findAll();
        Log.d(TAG, "Saved Entries: " + savedEntries.size());

        if (savedEntries.isEmpty()) {
            callback.onSuccess(null);
            realm.close();
            return;
        }

        AtomicReference<AutofillId> usernameId = new AtomicReference<>();
        AtomicReference<AutofillId> passwordId = new AtomicReference<>();

        traverseStructure(structure.getWindowNodeAt(0).getRootViewNode(), (node, id, value, hint) -> {
            String autofillHint = getPrimaryHint(node, hint);
            int inputType = node.getInputType();

            if (usernameId.get() == null && isUsernameField(autofillHint, inputType)) {
                Log.d(TAG, "Detected username/email field: " + autofillHint);
                usernameId.set(id);
            }

            if (passwordId.get() == null && isPasswordField(autofillHint, inputType)) {
                Log.d(TAG, "Detected password field: " + autofillHint);
                passwordId.set(id);
            }
        });

        if (usernameId.get() == null || passwordId.get() == null) {
            Log.w(TAG, "Username or Password field not found. Aborting autofill.");
            callback.onSuccess(null);
            realm.close();
            return;
        }

        RealmPasswords entry = savedEntries.get(0);
        String decryptedPassword = CryptoHelper.decrypt(entry.getPassword());

        RemoteViews presentation = new RemoteViews(getPackageName(), android.R.layout.simple_list_item_1);
        presentation.setTextViewText(android.R.id.text1, entry.getEmail());

        Dataset dataset = new Dataset.Builder()
                .setValue(usernameId.get(), AutofillValue.forText(entry.getEmail()), presentation)
                .setValue(passwordId.get(), AutofillValue.forText(decryptedPassword), presentation)
                .build();

        FillResponse response = new FillResponse.Builder()
                .addDataset(dataset)
                .setSaveInfo(new SaveInfo.Builder(
                        SaveInfo.SAVE_DATA_TYPE_USERNAME | SaveInfo.SAVE_DATA_TYPE_PASSWORD,
                        new AutofillId[]{usernameId.get(), passwordId.get()})
                        .build())
                .build();

        callback.onSuccess(response);
        realm.close();
    }

    @Override
    public void onSaveRequest(SaveRequest request, SaveCallback callback) {
        Realm realm = Realm.getDefaultInstance();
        Log.d(TAG, "onSaveRequest called");

        List<FillContext> contexts = request.getFillContexts();
        AssistStructure structure = contexts.get(contexts.size() - 1).getStructure();

        AtomicReference<String> emailValue = new AtomicReference<>();
        AtomicReference<String> passwordValue = new AtomicReference<>();

        traverseStructure(structure.getWindowNodeAt(0).getRootViewNode(), (node, id, value, hint) -> {
            String autofillHint = getPrimaryHint(node, hint);
            int inputType = node.getInputType();

            if (emailValue.get() == null && isUsernameField(autofillHint, inputType) && value != null) {
                emailValue.set(value);
            }
            if (passwordValue.get() == null && isPasswordField(autofillHint, inputType) && value != null) {
                passwordValue.set(value);
            }
        });

        if (emailValue.get() != null && passwordValue.get() != null) {
            String finalEmail = emailValue.get();
            String finalPasswordEncrypted = CryptoHelper.encrypt(passwordValue.get());
            Log.d(TAG, "Saving Email: " + finalEmail);

            realm.executeTransaction(r -> {
                RealmPasswords newEntry = r.createObject(RealmPasswords.class, UUID.randomUUID().toString());
                newEntry.setEmail(finalEmail);
                newEntry.setPassword(finalPasswordEncrypted);
                String packageName = structure.getActivityComponent().getPackageName();
                newEntry.setWebsite(packageName);
            });
        } else {
            Log.w(TAG, "Email or Password value missing; not saving.");
        }

        callback.onSuccess();
        realm.close();
    }

    private boolean isUsernameField(String hint, int inputType) {
        if (hint == null) return false;
        hint = hint.toLowerCase();
        return hint.contains("email") || hint.contains("username") ||
                (inputType & android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS) == android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
    }

    private boolean isPasswordField(String hint, int inputType) {
        if (hint == null) return false;
        hint = hint.toLowerCase();
        return hint.contains("password") ||
                (inputType & android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD) == android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                (inputType & android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD) == android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD;
    }

    private String getPrimaryHint(AssistStructure.ViewNode node, String fallbackHint) {
        CharSequence[] autofillHints = node.getAutofillHints();
        if (autofillHints != null && autofillHints.length > 0) {
            return autofillHints[0].toString();
        }
        return fallbackHint != null ? fallbackHint : "";
    }

    interface StructureVisitor {
        void onField(AssistStructure.ViewNode node, AutofillId id, String value, String hint);
    }

    private void traverseStructure(AssistStructure.ViewNode node, StructureVisitor visitor) {
        AutofillId id = node.getAutofillId();
        CharSequence hint = node.getHint();
        AutofillValue value = node.getAutofillValue();

        if (id != null) {
            String textValue = (value != null && value.isText()) ? value.getTextValue().toString() : null;
            String hintText = hint != null ? hint.toString() : null;
            visitor.onField(node, id, textValue, hintText);
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            traverseStructure(node.getChildAt(i), visitor);
        }
    }

    @Override
    public void onConnected() {
        super.onConnected();
        Log.d(TAG, "Autofill service connected!");
    }
}
