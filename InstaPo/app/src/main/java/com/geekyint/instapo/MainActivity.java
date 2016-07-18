package com.geekyint.instapo;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private Button btnSignout, btnStart;
    private Button btnRoundTrip;
    private int i = 0;
    private FirebaseAuth auth;
    public boolean chkStatus() {
        final ConnectivityManager connMgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI ||
                    networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                return true;
            }
        }
        return false;
    }

    public void turnGPSOn() {
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                !lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            // Build the alert dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Location Services Not Active");
            builder.setMessage("Please enable Location Services and GPS");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    // Show location settings when the user acknowledges the alert dialog
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            Dialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        auth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        turnGPSOn();

        btnSignout = (Button) findViewById(R.id.btnstop);
        btnStart = (Button) findViewById(R.id.btnstart);
        btnRoundTrip = (Button) findViewById(R.id.btn_round_chase);
        btnSignout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();
                startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                finish();
            }
        });
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (i % 2 == 0) {
                    if (!chkStatus()) {
                        Toast.makeText(getApplicationContext(),
                                "You are not connected to network switch on internet",
                                Toast.LENGTH_LONG)
                                .show();
                        return;
                    }
                    if (isReadLocationAllowed () ) {
                       // Toast.makeText(getApplicationContext(), "You already have the permission",
                         //       Toast.LENGTH_LONG).show();
                        startService(new Intent(MainActivity.this, BackgroundLocation.class).putExtra("data_1", "Hel"));
                        Log.e("MainACtivity", "Service started");
                        btnStart.setText("Stop Chasing me");
                        i++;
                        return;
                    }
                    else {
                        requestLocationPermission ();
                        Toast.makeText(getApplicationContext(),
                                "Now you press start",
                                Toast.LENGTH_LONG)
                                .show();
                    }

                } else {
                    stopService(new Intent(MainActivity.this, BackgroundLocation.class));
                    Log.e("MainActivity", "Service Stopped");
                    btnStart.setText("Start Chasing me");
                    i++;
                }
            }
        });

        btnRoundTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity (new Intent(MainActivity.this, RoundTripChase.class));
            }
        });
    }



    private boolean isReadLocationAllowed () {
        int res = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION);
        int res1 = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        int res2 = ContextCompat.checkSelfPermission(this, android.Manifest.permission.INTERNET);

        if (res == PackageManager.PERMISSION_GRANTED && res1 == PackageManager.PERMISSION_GRANTED
                && res2 == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    private void requestLocationPermission () {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) &&
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION)) {

        }
        ActivityCompat.requestPermissions(this,
                new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.INTERNET},2);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull
    int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 2) {
            if (grantResults.length > 0 && grantResults [0] == PackageManager.PERMISSION_GRANTED
                    && grantResults [1] == PackageManager.PERMISSION_GRANTED
                    && grantResults [2] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_LONG)
                        .show();
            } else {
                Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_LONG)
                        .show();
            }
        }
    }
}