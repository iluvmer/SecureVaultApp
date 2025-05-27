package com.meriem.securevaultapp.screens;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("vault.realm")
                .schemaVersion(1)
                .deleteRealmIfMigrationNeeded() // Use only during development
                .allowQueriesOnUiThread(true)
                .build();

        Realm.setDefaultConfiguration(config);
    }
}
