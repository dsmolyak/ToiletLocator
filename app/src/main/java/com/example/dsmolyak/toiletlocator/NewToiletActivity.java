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
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NewToiletActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    GoogleApiClient mGoogleApiClient;
    Button mAddToilet, mInputCurrLocation;
    Location mLastLoc= MapsActivity.mLastLocation;
    TextView mLatitudeText, mLongitudeText;
    EditText mLatitude, mLongitude;
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference mRef = db.getReference();
    static final int REQUEST_LOCATION=1;
    static Integer mSize;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(mSize!=null){
            mSize=1;
        }
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
                if(!(mLatitude.getText().equals("") || mLongitude.getText().equals(""))) {
//                    DatabaseReference newLocationRef;
//                    if(mSize!=null) {
//                        newLocationRef = mRef.child("location" + mSize);
//                    }
//                    else{
//                        newLocationRef=mRef.child("location1");
//                        mSize=1;
//                    }
                    GeoFire loc=new GeoFire(mRef);
//                    newLocationRef.setValue(new LatLng(Double.parseDouble(mLatitude.getText().toString()),
//                            Double.parseDouble(mLongitude.getText().toString())));
                    if(mSize!=null)
                        loc.setLocation("location" + mSize, new GeoLocation(Double.parseDouble(mLatitude.getText().toString()),
                            Double.parseDouble(mLongitude.getText().toString())));
                    else{
                        loc.setLocation("location1", new GeoLocation(Double.parseDouble(mLatitude.getText().toString()),
                                Double.parseDouble(mLongitude.getText().toString())));
                        mSize=1;
                    }
                    mLatitude.setText("");
                    mLongitude.setText("");
                    mSize++;
                }
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
    public void onConnectionSuspended(int i) {

    }

    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
