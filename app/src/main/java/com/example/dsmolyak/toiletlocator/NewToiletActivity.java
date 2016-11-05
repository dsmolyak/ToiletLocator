package com.example.dsmolyak.toiletlocator;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Permission;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NewToiletActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Button mAddToilet, mInputCurrLocation;
    TextView mLatitudeText, mLongitudeText;
    EditText mLatitude, mLongitude;
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference mRef = db.getReference();
    static final int REQUEST_LOCATION=1;
    int mSize = 0;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                    DatabaseReference newLocationRef = mRef.child("location" + mSize);
                    newLocationRef.setValue(new LatLng(Double.parseDouble(mLatitude.getText().toString()),
                            Double.parseDouble(mLongitude.getText().toString())));
                    mLatitude.setText("");
                    mLongitude.setText("");
                    mSize++;
                }
            }
        });

        mInputCurrLocation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mLatitude.setText(String.valueOf(mLastLocation.getLatitude()));
                mLongitude.setText(String.valueOf(mLastLocation.getLongitude()));
            }

        });

    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    public void onConnected(Bundle connectionHint) {
      askLocation();
    }

    public void askLocation(){
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)){
                Toast.makeText(this, "Need Location to Find Toilets", Toast.LENGTH_SHORT).show();
            }

            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);

            return;
        }

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
    }
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        Log.d("here", "here1");
        if(requestCode==REQUEST_LOCATION){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED){
                if (mLastLocation != null) {
                    mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
                    mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
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
