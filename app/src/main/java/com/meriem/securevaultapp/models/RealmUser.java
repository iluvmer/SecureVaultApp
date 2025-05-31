package com.meriem.securevaultapp.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;


public class RealmUser extends RealmObject {
    @PrimaryKey
    @Required
    private String uid;
    @Required
    private String email;
    @Required
    private String firstName;
    @Required
    private String lastName;
    private boolean useFingerprintLogin;
    private String phone;
    private String profileImageUrl;
    public RealmUser() {}
    // Constructor with all fields
    public RealmUser(String uid, String email, String firstName, String lastName) {
        this.uid = uid;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }
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
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
    @Override
    public String toString() {
        return "RealmUser{" +
                "uid='" + uid + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }
}

