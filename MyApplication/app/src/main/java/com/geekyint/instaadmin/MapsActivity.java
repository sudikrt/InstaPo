package com.geekyint.instaadmin;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthProvider;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Iterator;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private final static String TAG = "MapsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            Log.e(TAG, "Already Login");
            do_further();

        } else {
            auth.signInWithEmailAndPassword("geeky@mail.com", "123456")
                    .addOnCompleteListener(MapsActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                Log.e (TAG, "Problem in login");
                            } else {
                                Log.e (TAG, "Successful login");
                                do_further();
                            }
                        }
                    });
        }

    }
    public void do_further () {
        database = FirebaseDatabase.getInstance();
        final DatabaseReference reference = database.getReference().child("users");
        Log.e(TAG, "I m in further");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mMap.clear();
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    Log.e(TAG, "KEy: "+ snapshot.getKey());


                    Map<String, Object> vechicle = (Map <String, Object>) snapshot.getValue();
                    Map<String, Object> v_det = (Map<String, Object>) vechicle.get("vehicles");
                    Map<String, Object> user_det = (Map<String, Object>) vechicle.get("userInfo");

                    LatLng latLng = new LatLng(Double.parseDouble(v_det.get("latitude").toString()),
                                            Double.parseDouble(v_det.get("longitude").toString()));

                    mMap.addMarker(new MarkerOptions().position(latLng)
                                        .title(user_det.get("name").toString())
                                        .snippet("Speed: " + v_det.get("speed").toString()
                                                + "\nLat: " + v_det.get("latitude").toString()));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

                    Log.e(TAG, v_det.get("latitude").toString());
                    Log.e(TAG, v_det.get("longitude").toString());
                    Log.e(TAG, v_det.get("time").toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        // Add a marker in Sydney and move the camera
        mMap.addMarker(new MarkerOptions().position(new LatLng(13.1818, 74.9413)).title("Nitte"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(13.1818, 74.9413) , 17.0f));
    }
}
