package com.meriem.securevaultapp.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RealmNote extends RealmObject {
    @PrimaryKey
    private String id;
    private String encryptedTitle;
    private String encryptedContent;
    private String userId; // To link notes to the user

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEncryptedTitle() { return encryptedTitle; }
    public void setEncryptedTitle(String encryptedTitle) { this.encryptedTitle = encryptedTitle; }

    public String getEncryptedContent() { return encryptedContent; }
    public void setEncryptedContent(String encryptedContent) { this.encryptedContent = encryptedContent; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
