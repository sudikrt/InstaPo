package com.geekyint.login;

import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.geekyint.login.roundtrack.RBackGroundLocation;
import com.geekyint.login.track.BackgroundLocation;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Date;

import butterknife.Bind;


public class MainActivity extends ActionBarActivity {

    private ImageButton btnSignOut;
    private ImageButton btnStart;
    private ImageButton btnRound;
    private TextView txtView;
    private TextView txtRound;

    private static FirebaseAuth auth;

    public static String DATE;

    private static String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_main);

        /*
            Turn On GPS
         */
        turnGPSOn();
        /*
            Get the references
         */
        txtView = (TextView) findViewById(R.id.txtbtnstart);
        btnSignOut = (ImageButton) findViewById(R.id.btnStop);
        btnStart = (ImageButton) findViewById(R.id.btnstart);
        txtRound = (TextView) findViewById(R.id.txtRound);
        btnRound = (ImageButton) findViewById(R.id.btnRound);




         /*
            Assign the listeners for the buttons.
         */
        btnRound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isMyServiceRunning(RBackGroundLocation.class)) {
                    Log.e(TAG, "Yes Round-Service is running");
                    stopService(new Intent(MainActivity.this, RBackGroundLocation.class));
                    Log.e(TAG, "Round-Service Stopped");
                    btnRound.setImageResource(R.drawable.round_trip);
                    txtRound.setText("Start My Journey");
                } else {
                    if (!chkStatus()) {
                        Toast.makeText(getApplicationContext(),
                                "You are not connected to network switch on internet",
                                Toast.LENGTH_LONG)
                                .show();
                        return;
                    }
                    if (isReadLocationAllowed () ) {
                        DATE = new Date().toString();
                        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(MainActivity.this);
                        View mView = layoutInflaterAndroid.inflate(R.layout.dialogtxtinput, null);
                        AlertDialog.Builder alertDialogBuilderUserInput = new
                                                                AlertDialog.Builder(MainActivity.this);
                        alertDialogBuilderUserInput.setView(mView);

                        final EditText userInputDialogEditText =
                                (EditText) mView.findViewById(R.id.userInputDialog);

                        alertDialogBuilderUserInput
                        .setCancelable(false)
                        .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                    DATE = userInputDialogEditText.getText().toString();
                                    startService(new Intent(MainActivity.this,
                                            RBackGroundLocation.class)
                                            .putExtra("data", String.valueOf("Hell")));
                                    Log.e(TAG, "Round-Service started");
                                    btnRound.setImageResource(R.drawable.stop);
                                    txtRound.setText("Yup, I m done");
                                    return;
                            }
                        })

                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialogBox, int id) {
                                        dialogBox.cancel();
                                    }
                                });

                        final AlertDialog alertDialogAndroid = alertDialogBuilderUserInput.create();
                        alertDialogAndroid.show();

                        alertDialogAndroid.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

                        userInputDialogEditText.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                if (s.length() == 0) {
                                    alertDialogAndroid.getButton(AlertDialog.BUTTON_POSITIVE)
                                                                                .setEnabled(false);
                                } else if (s.length() > 0) {
                                    alertDialogAndroid.getButton(AlertDialog.BUTTON_POSITIVE)
                                                                                .setEnabled(true);
                                }
                            }

                            @Override
                            public void afterTextChanged(Editable s) {

                            }
                        });
                    }
                    else {
                        requestLocationPermission ();
                        Toast.makeText(getApplicationContext(),
                                "Now you press Login",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                }


            }
        });

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isMyServiceRunning(BackgroundLocation.class)) {
                    Log.e(TAG, "Yes Service is running");
                    stopService(new Intent(MainActivity.this, BackgroundLocation.class));
                    Log.e("MainActivity", "Service Stopped");
                    btnStart.setImageResource(R.drawable.run);
                    txtView.setText("Start Chasing me");
                } else {
                    if (!chkStatus()) {
                        Toast.makeText(getApplicationContext(),
                                "You are not connected to network switch on internet",
                                Toast.LENGTH_LONG)
                                .show();
                        return;
                    }
                    if (isReadLocationAllowed () ) {
                        startService(new Intent(MainActivity.this, BackgroundLocation.class).putExtra("data", String.valueOf("Hell")));
                        Log.e(TAG, "Service started");
                        btnStart.setImageResource(R.drawable.stop);
                        txtView.setText("Stop Chasing me");
                        return;
                    }
                    else {
                        requestLocationPermission ();
                        Toast.makeText(getApplicationContext(),
                                "Now you press Login",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                }
            }
        });


        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();
                stopService(new Intent(MainActivity.this, BackgroundLocation.class));
                Log.e("MainActivity", "Service Stopped");
                btnStart.setImageResource(R.drawable.run);
                txtView.setText("Start Chasing me");
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

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

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
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
}
