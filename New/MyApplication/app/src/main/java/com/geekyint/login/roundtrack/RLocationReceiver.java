package com.geekyint.login.roundtrack;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.geekyint.login.MainActivity;
import com.google.android.gms.location.LocationResult;

/**
 * Created by geekyint on 21/7/16.
 */
public class RLocationReceiver extends WakefulBroadcastReceiver {

    private String TAG = this.getClass().getSimpleName();

    private LocationResult mLocationResult;

    @Override
    public void onReceive(Context context, Intent intent) {
        // Need to check and grab the Intent's extras like so
        if(LocationResult.hasResult(intent)) {

            String s = intent.getStringExtra("data");
            Log.e(TAG, String.valueOf(s));
            this.mLocationResult = LocationResult.extractResult(intent);

            new RFBSender(MainActivity.DATE).put(mLocationResult.getLastLocation());
            Log.i(TAG, "Location Received: " + this.mLocationResult.toString());
        }
    }
}