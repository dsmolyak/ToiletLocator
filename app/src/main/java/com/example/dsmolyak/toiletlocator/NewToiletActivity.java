package com.example.dsmolyak.toiletlocator;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NewToiletActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    GoogleApiClient mGoogleApiClient;
    Button mAddToilet, mInputCurrLocation;
    Location mLastLoc= MapsActivity.mLastLocation;
    TextView mLatitudeText, mLongitudeText;
    static EditText mLatitude;
    static EditText mLongitude;
    RatingBar mRatingBar;
    static FirebaseDatabase db = FirebaseDatabase.getInstance();
    static DatabaseReference mRef = db.getReference();
    static final int REQUEST_LOCATION=1;
    static int mSize = 0;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRatingBar = (RatingBar) findViewById(R.id.ratingBar);


        setContentView(R.layout.activity_new_toilet);

        mAddToilet = (Button) findViewById(R.id.addToiletButton);
        mInputCurrLocation=(Button) findViewById(R.id.addLocationButton);
        mLatitudeText = (TextView) findViewById(R.id.showLatitude);
        mLongitudeText = (TextView) findViewById(R.id.showLongitude);
        mLatitude = (EditText) findViewById(R.id.latitudeTextField);
        mLongitude = (EditText) findViewById(R.id.longitudeTextField);
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        final Button button = (Button) findViewById(R.id.backToMap);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent toilet = new Intent(button.getContext(), MapsActivity.class);
                startActivity(toilet);
            }
        });
    }

    protected void onStart() {
        mGoogleApiClient.connect();

        super.onStart();



        mAddToilet.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                float rating = 1;
                //float rating = mRatingBar.getRating();
                Log.d("rating", ""+ rating);
                try {
                    addToilet(Double.parseDouble(mLatitude.getText().toString()), Double.parseDouble(mLongitude.getText().toString()), rating);
                }catch(NumberFormatException e){
                    Toast.makeText(NewToiletActivity.this, "Please enter a valid location", Toast.LENGTH_SHORT).show();
                }
                mLatitude.setText("");
                mLongitude.setText("");
            }
        });

        mInputCurrLocation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if(mLastLoc == null) {
                    askLocation();
                }
                int permissionCheck = ContextCompat.checkSelfPermission(NewToiletActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
                if(permissionCheck == PackageManager.PERMISSION_GRANTED) {
                    mLatitude.setText(String.valueOf(mLastLoc.getLatitude()));
                    mLongitude.setText(String.valueOf(mLastLoc.getLongitude()));
                }
            }

        });

    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    public void onConnected(Bundle connectionHint) {
        Log.d("test", "test");

        askLocation();
    }

    public void askLocation(){
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
                Toast.makeText(this, "Need Location to Find Toilets", Toast.LENGTH_SHORT).show();
            }

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);

            return;
        }

        mLastLoc = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        Log.d("here", "here1");
        if(requestCode==REQUEST_LOCATION){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED){
                if (mLastLoc != null) {
                    mLatitudeText.setText(String.valueOf(mLastLoc.getLatitude()));
                    mLongitudeText.setText(String.valueOf(mLastLoc.getLongitude()));
                }
            }else{
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    public static void addToilet(double lat, double lon, float rating) {
        Log.d("add?", "Trying to add toilet");

        GeoFire loc = new GeoFire(mRef);
        ValueEventListener numValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mSize = dataSnapshot.getValue(Integer.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting numValues failed, log a message
                Log.w("numValues:onCancelled", databaseError.toException());
            }
        };
        mRef.child("numValues").addValueEventListener(numValueListener);
        //mRef.child("numValues").child("init").setValue("1");

        String tag = "location" + (mSize + 1);
        loc.setLocation(tag, new GeoLocation(lat, lon));//add to the database

        //mRef.child(tag).child("Rating").setValue(rating);

        mSize++;

        mRef.child("numValues").setValue(mSize);



    }

    public void onConnectionSuspended(int i) {

    }

    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


}