package com.meriem.securevaultapp.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RealmUser extends RealmObject {
    @PrimaryKey
    private String uid;
    private String email;
    private String firstName;
    private String lastName;
    private boolean useFingerprintLogin;

    // Getters and setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public boolean isUseFingerprintLogin() { return useFingerprintLogin; }
    public void setUseFingerprintLogin(boolean useFingerprintLogin) {
        this.useFingerprintLogin = useFingerprintLogin;
    }
}
