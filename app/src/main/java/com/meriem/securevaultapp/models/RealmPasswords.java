package com.meriem.securevaultapp.models;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
public class RealmPasswords extends RealmObject {
    @PrimaryKey
    private String uid;
    private String website;
    private String email;
    private String password;

    public String getUid() {
        return uid;
    }

    public String getPassword() {
        return password;
    }

    public String getWebsite() {
        return website;
    }

    public String getEmail() {
        return email;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public void setWebsite(String website) {
        this.website = website;
    }
    public void setEmail(String email) {
        this.email = email;
    }
}
