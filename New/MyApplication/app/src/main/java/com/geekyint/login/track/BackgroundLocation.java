package com.geekyint.login.track;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.geekyint.login.utils.Constants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by geekyint on 1/7/16.
 */
public class BackgroundLocation extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    static String data;
    IBinder mBinder = new LocalBinder();

    private GoogleApiClient mGoogleApiClient;
    private PowerManager.WakeLock mWakeLock;
    private LocationRequest mlocationRequest;

    final static String MY_ACTION = "MY_ACTION";

    //Flag for boolean request
    private boolean mInProgress;

    private boolean serviceAvailabe = false;

    public class LocalBinder extends Binder {
        public BackgroundLocation getServerInstance() {
            return BackgroundLocation.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInProgress = false;

        //Intent intent = Intent.getIntentOld()

        //Create the lcoation request object
        mlocationRequest = LocationRequest.create();

        //Use the acurecy
        mlocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //The  INTERVAL
        mlocationRequest.setInterval(Constants.UPDATE_INTERVAL);

        //The FAST INTERVAL
        mlocationRequest.setFastestInterval(Constants.FAST_INTERVAL);

        serviceAvailabe = serviceConnected();


        setUpALocationClientIfNeeded();


        ComponentName receiver = new ComponentName(this, LocationReceiver.class);
        PackageManager pm = this.getPackageManager();
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
        /*ComponentName receiver1 = new ComponentName(this, FireBaseSender.class);
        PackageManager pm1 = this.getPackageManager();
        pm1.setComponentEnabledSetting(receiver1,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);*/
    }

    private void setUpALocationClientIfNeeded() {
        if (mGoogleApiClient == null) {
            buildGoogleApiClient();
        }
    }

    //Create the new Connection to the client
    private void buildGoogleApiClient() {
        this.mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
    }

    private boolean serviceConnected() {
        //Check the google Play service availibility
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        //IF AVAILABLE
        if (resultCode == ConnectionResult.SUCCESS) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //super.onStartCommand(intent, flags, startId);


        Log.e("Backgr_Loc", "OnstartCommand");
        Log.e("Backgr_Loc", String.valueOf(intent.getExtras().getString("data")));
        PowerManager mgr = (PowerManager) getSystemService(Context.POWER_SERVICE);

        if (this.mWakeLock == null) {
            this.mWakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
        }
        if (!this.mWakeLock.isHeld()) {
            this.mWakeLock.acquire();
        }

        if (!serviceAvailabe || mGoogleApiClient.isConnected() || mInProgress) {
            return START_STICKY;
        }

        setUpALocationClientIfNeeded();

        if (!mGoogleApiClient.isConnected() || !mGoogleApiClient.isConnecting() || !mInProgress) {
           //appendLog(DateFormat.getDateTimeInstance().format(new Date()) + ": Started", Constants.LOG_FILE);
            mInProgress = true;
            mGoogleApiClient.connect();
        }
        return START_STICKY;
    }

    @Override
    public void onLocationChanged(Location location) {
        String msg = Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        Log.d("debug", msg);
      //  appendLog(DateFormat.getDateTimeInstance().format(new Date()) + ":" + msg, Constants.LOCATION_FILE);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e ("BackGroun", String.valueOf(intent.getStringExtra("data")));
        return mBinder;
    }

    public String getTime() {
        SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return mDateFormat.format(new Date());
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

    @Override
    public void onDestroy() {
        // Turn off the request flag
        this.mInProgress = false;

        if (this.serviceAvailabe && this.mGoogleApiClient != null) {
            this.mGoogleApiClient.unregisterConnectionCallbacks(this);
            this.mGoogleApiClient.unregisterConnectionFailedListener(this);
            this.mGoogleApiClient.disconnect();
            // Destroy the current location client
            this.mGoogleApiClient = null;
        }
        // Display the connection status
        // Toast.makeText(this, DateFormat.getDateTimeInstance().format(new Date()) + ":
        // Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();

        if (this.mWakeLock != null) {
            this.mWakeLock.release();
            this.mWakeLock = null;
        }
       //appendLog(DateFormat.getDateTimeInstance().format(new Date()) + ": Stopped", Constants.LOG_FILE);


        ComponentName receiver = new ComponentName(this, LocationReceiver.class);
        PackageManager pm = this.getPackageManager();
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        /*
        ComponentName receiver1 = new ComponentName(this, FireBaseSender.class);
        PackageManager pm1 = this.getPackageManager();
        pm1.setComponentEnabledSetting(receiver1,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);*/
        super.onDestroy();
    }

    /*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle bundle) {

        // Request location updates using static settings
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Intent intent  = new Intent (this, LocationReceiver.class);
        PendingIntent pendingIntent = PendingIntent
                .getBroadcast(this, 54321, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        LocationServices.FusedLocationApi.requestLocationUpdates(this.mGoogleApiClient,
                mlocationRequest, pendingIntent);
    }

    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    @Override
    public void onConnectionSuspended(int i) {
        // Turn off the request flag
        mInProgress = false;
        // Destroy the current location client
        mGoogleApiClient = null;
        setUpALocationClientIfNeeded();

        // Toast.makeText(this, DateFormat.getDateTimeInstance().format(new Date()) + ": Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
       // appendLog(DateFormat.getDateTimeInstance().format(new Date()) + ": Disconnected", Constants.LOG_FILE);
    }

    /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        mInProgress = false;

        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        setUpALocationClientIfNeeded();
        if (connectionResult.hasResolution()) {

        } else {

        }
    }
}
