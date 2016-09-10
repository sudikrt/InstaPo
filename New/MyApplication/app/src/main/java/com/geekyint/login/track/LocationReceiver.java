package com.geekyint.login.track;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.google.android.gms.location.LocationResult;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


/**
 * Created by geekyint on 1/7/16.
 */
public class LocationReceiver extends WakefulBroadcastReceiver {

    private String TAG = this.getClass().getSimpleName();

    private LocationResult mLocationResult;

    @Override
    public void onReceive(Context context, Intent intent) {
        // Need to check and grab the Intent's extras like so
        if(LocationResult.hasResult(intent)) {

            String s = intent.getStringExtra("data");
            Log.e(TAG, String.valueOf(s));
            this.mLocationResult = LocationResult.extractResult(intent);

            new FBSender().put(mLocationResult.getLastLocation());
            Log.i(TAG, "Location Received: " + this.mLocationResult.toString());
            String msg = String.valueOf(mLocationResult.getLastLocation().getLongitude()) + "  " +
                        String.valueOf(mLocationResult.getLastLocation().getLatitude());

           // appendLog(DateFormat.getDateTimeInstance().format(new Date()) + ":" + msg, Constants.LOCATION_FILE);

        }

    }

    public void appendLog(String text, String filename) {
        File logFile = new File(filename);
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}