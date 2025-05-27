package com.meriem.securevaultapp;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Realm.init(this);  // 🔁 Initializes Realm once globally
        Realm.setDefaultConfiguration(new RealmConfiguration.Builder()
                .allowWritesOnUiThread(true)
                .deleteRealmIfMigrationNeeded()
                .build());
    }
}

