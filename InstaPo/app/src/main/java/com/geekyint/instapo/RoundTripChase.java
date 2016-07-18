package com.geekyint.instapo;

import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RoundTripChase extends AppCompatActivity {

    private Button btnStart;
    private TextView txtLable;
    private final static String TAG = "RoundTripActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_round_trip_chase);
        btnStart = (Button) findViewById(R.id.btnStartIt_trip);
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DatabaseReference reference = database.getReference("users/" + user.getUid()
                        + "/round_trip");
                Map mLocation = new HashMap();
                mLocation.put("latitude", 10);
                mLocation.put("longitude", 10);

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
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
