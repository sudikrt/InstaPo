package com.geekyint.login.utils;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;

/**
 * Created by geekyint on 8/7/16.
 */
public class FireBaseLog extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (!FirebaseApp.getApps(this).isEmpty()) {
            FirebaseDatabase.getInstance().setLogLevel(Logger.Level.DEBUG);
        }
    }
}
