package com.meriem.securevaultapp.screens;

import com.meriem.securevaultapp.models.RealmPasswords;

// PasswordEntry.java
public class PasswordEntry extends RealmPasswords {
    private String website;
    private String email;
    private String password;

    public PasswordEntry(String website, String email, String password) {
        this.website = website;
        this.email = email;
        this.password = password;
    }

    public String getWebsite() { return website; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PasswordEntry)) return false;
        PasswordEntry that = (PasswordEntry) o;
        return website.equals(that.website);
    }

    @Override
    public int hashCode() {
        return website.hashCode();
    }
}