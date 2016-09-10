package com.geekyint.login.roundtrack;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Created by geekyint on 21/7/16.
 */
public class RFBSender extends Service implements Executor {
    private String TAG = "FBSender";
    private double latitude;
    private double longitude;
    private double speed;
    private String time;
    private String data;
    public  RFBSender (String data) {
        this.data = data;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void put (Location location) {

        latitude = location.getLatitude();
        longitude = location.getLongitude();
        speed = location.getSpeed();
        time = DateFormat.getTimeInstance().format(new Date());

        Log.e(TAG, "Entering the run ()");

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        user.getToken(true).addOnCompleteListener(this, new OnCompleteListener<GetTokenResult>() {
            @Override
            public void onComplete(@NonNull Task<GetTokenResult> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "token=" + task.getResult().getToken());
                } else {
                    Log.e(TAG, "exception=" +task.getException().toString());
                }
            }
        });

        final DatabaseReference reference = database.getReference("users/" + user.getUid()
                + "/round_trip/" + data );
        reference.goOnline();


        Map mLocation = new HashMap();
        mLocation.put("latitude", latitude);
        mLocation.put("longitude", longitude);

        reference.push().setValue(mLocation, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    Log.i(TAG, "onComplete: OKAY");
                } else {
                    Log.e(TAG, "onComplete: FAILED " + databaseError.getMessage());
                }
            }
        });
        reference.goOffline();

        Log.e(TAG, "Exiting The run ()");

    }

    @Override
    public void execute(Runnable command) {

    }
}