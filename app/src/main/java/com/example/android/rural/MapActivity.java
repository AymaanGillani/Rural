package com.example.android.rural;

import android.Manifest;
import android.app.Application;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MapActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Locations>>, OnMapReadyCallback {

    public static final String LOG_TAG = MapActivity.class.getName();
    private static final String URL="https://maps.googleapis.com/maps/api/place/nearbysearch/json";
    private static final int LOCATION_LOADER_ID = 1;
    private GoogleMap map;
    private ProgressBar pb;
    private TextView loadingTV;
    private ArrayList<Locations> data;
    private double userLongitude;
    private double userLatitude;
    private FrameLayout mapView;
    FusedLocationProviderClient locationProviderClient;
    int PERMISSION_ID = 44;
    String finalUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        permissionRequest();
        locationProviderClient= LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();
        pb = (ProgressBar) findViewById(R.id.progressBar);
        loadingTV = (TextView) findViewById(R.id.loadingTV);
        mapView=(FrameLayout) findViewById(R.id.map);
        mapView.setVisibility(View.GONE);
        ConnectivityManager cm =
                (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        TextView noNetTV = (TextView) findViewById(R.id.noNetTV);
        if (!isConnected) {
            pb.setVisibility(View.GONE);
            loadingTV.setVisibility(View.GONE);
        } else {
            noNetTV.setVisibility(View.GONE);
        }
    }

    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_ID
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }

    private void getLastLocation(){
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                locationProviderClient.getLastLocation().addOnCompleteListener(
                        new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                Location location = task.getResult();
                                if (location == null) {
                                    requestNewLocationData();
                                } else {
                                    userLatitude= location.getLatitude();
                                    userLongitude= location.getLongitude();

                                }
                                LoaderManager loaderManager = getLoaderManager();
                                loaderManager.initLoader(LOCATION_LOADER_ID, null, MapActivity.this);
                            }
                        }
                );
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            requestPermissions();
        }
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }

    @Override
    public void onResume(){
        super.onResume();
        if (checkPermissions()) {
            getLastLocation();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stoploader();
        finish();
    }

    private void requestNewLocationData() {

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationProviderClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        );
    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            userLatitude= mLastLocation.getLatitude();
            userLongitude= mLastLocation.getLongitude();
        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.setMyLocationEnabled(true);
        LatLng user = new LatLng(userLatitude,userLongitude);
        Iterator<Locations> iterator=data.iterator();
        while (iterator.hasNext()){
            Locations loc=iterator.next();
            LatLng marker=new LatLng(loc.getLatitude(),loc.getLongitude());
            Toast.makeText(this,"Works",Toast.LENGTH_LONG).show();
            map.addMarker(new MarkerOptions()
                    .position(marker)
                    .title(loc.getName()));
        }
        pb.setVisibility(View.GONE);
        loadingTV.setVisibility(View.GONE);
        mapView.setVisibility(View.VISIBLE);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(user, 13));
//            map.moveCamera(CameraUpdateFactory.newLatLng(user));
    }

    @Override
    public Loader<List<Locations>> onCreateLoader(int i, Bundle bundle) {
        finalUrl=URL+"?"+"type=hospital"+"&key="+getString(R.string.google_maps_key)+"&radius=1500"+"&location="+userLatitude+","+userLongitude;
//        TextView test=(TextView)findViewById(R.id.locations);
//        test.setText(finalUrl);
        return new LocationAsyncTaskLoader(MapActivity.this,finalUrl);
    }

    @Override
    public void onLoadFinished(android.content.Loader<List<Locations>> loader, List<Locations> locat) {
        data=new ArrayList<>(locat.size());
        data.addAll(locat);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        stoploader();
    }

    private void stoploader(){
        getLoaderManager().destroyLoader(LOCATION_LOADER_ID);
    }

    @Override
    public void onLoaderReset(android.content.Loader<List<Locations>> loader) {
//        data.clear();
    }

    private void permissionRequest() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
        } else {
            ActivityCompat.requestPermissions(this, new String[] {
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION },
                    1);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stoploader();
        finish();
    }
}
