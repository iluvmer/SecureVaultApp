package com.meriem.securevaultapp.screens;

// PasswordEntry.java
public class PasswordEntry {
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
}