package com.meriem.securevaultapp.screens;

import android.app.Application;

import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("vault.realm")
                .schemaVersion(5)
                .deleteRealmIfMigrationNeeded() // Use only during development
                .migration(new RealmMigration() {
                    @Override
                    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                        // Add any migration logic if needed
                    }
                })
                .allowWritesOnUiThread(true)  // Crucial for UI thread operations
                .allowQueriesOnUiThread(true)
                .build();

        Realm.setDefaultConfiguration(config);
    }
}
