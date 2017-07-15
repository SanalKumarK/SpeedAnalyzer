package com.dyolab.speedanalyzer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

import com.dyolab.speedanalyzer.model.TripDO;
import com.dyolab.speedanalyzer.service.LocationUpdateService;
import com.dyolab.speedanalyzer.service.SpeedAnalyzerModelProvider;
import com.dyolab.speedanalyzer.util.SpeedAnalyzerUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Date;

import static com.dyolab.speedanalyzer.util.SpeedAnalyzerUtil.unit;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final String TAG = MapActivity.class.getSimpleName();

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;

    LocationUpdateService locationUpdateService ;
    boolean mBound = false;

    private TripDO tripInfoDO;
    private TextView speedInfo;

    private SpeedAnalyzerModelProvider modelProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        modelProvider = new SpeedAnalyzerModelProvider(this);

        setContentView(R.layout.activity_map);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        tripInfoDO = (TripDO) intent.getSerializableExtra(TripActivity.TRIP);

       /* FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.stop_tracking_btn);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });*/

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        speedInfo = (TextView) findViewById(R.id.speedTxt);

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
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        // Turn on the My Location layer and the related control on the map.
        checkLocationPermissionGranted();
    }


    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void checkLocationPermissionGranted() {
        if (mMap == null) {
            return;
        }

        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            updateMapUI(mLocationPermissionGranted);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            mLocationPermissionGranted = false;
            updateMapUI(mLocationPermissionGranted);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            updateMapUI(mLocationPermissionGranted);
        }
    }

    private void updateMapUI(boolean isLocPermissionGranted ) {
        try{
            if(isLocPermissionGranted) {
                startLocationListener();
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
            }
        } catch (SecurityException ex) {
            Log.d(TAG, ex.getMessage());
        }
    }
    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void setLastKnownLocation() {
        Date mLastLocKnownTime;
        CameraPosition mCameraPosition = null;
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        if (mLocationPermissionGranted) {
            mLastKnownLocation = locationUpdateService.getLastKnownLocation();
            mLastLocKnownTime = new Date();
        }

        // Set the map's camera position to the current location of the device.
        if (mCameraPosition != null) {
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
        } else if (mLastKnownLocation != null) {
            updateInfoInMap(new LatLng(mLastKnownLocation.getLatitude(),
                    mLastKnownLocation.getLongitude()), 0);

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(),
                    mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
        } else {
            Log.d(TAG, "Current location is null. Using defaults.");
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }

    private Marker marker;
    private void updateInfoInMap(LatLng curMarkerPos, double speed) {
        if(marker != null ) {
            marker.remove();
        }
        marker = mMap.addMarker(new MarkerOptions().position(curMarkerPos));//.title((speed != 0 ? speedFormat.format(speed) : speed) + " " + unit));
        speedInfo.setText((speed != 0 ? SpeedAnalyzerUtil.speedFormat.format(speed) : speed) + " " + unit);
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(curMarkerPos, DEFAULT_ZOOM));
    }


    // Our handler for received Intents. This will be called whenever an Intent
    // with an action named "custom-event-name" is broadcasted.
    private BroadcastReceiver locationUpdater = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            LatLng location = intent.getParcelableExtra(LocationUpdateService.LOCATION);
            double speed = intent.getDoubleExtra(LocationUpdateService.SPEED, 0);

            updateInfoInMap(location, speed);

            //Add speed info of the trip to the db
            if(tripInfoDO != null) {
                modelProvider.addTripSpeedDetails(tripInfoDO.getTripCode(), location.toString(), new Date(), speed);
            }
        }
    };

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocationUpdateService.LocationUpdateBinder binder = (LocationUpdateService.LocationUpdateBinder) service;
            locationUpdateService = binder.getService();
            setLastKnownLocation();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    /** For Updating the location of the map*/
    private void startLocationListener() {
        if(mLocationPermissionGranted) {
            /*Intent intent = new Intent(this, LocationUpdateService.class);
            startService(intent);*/

            // Register to receive messages.
            // We are registering an observer (mMessageReceiver) to receive Intents
            // with actions named "custom-event-name".
            LocalBroadcastManager.getInstance(this).registerReceiver(
                    locationUpdater , new IntentFilter(LocationUpdateService.LOCATION_UPDATE));

            bindLocationListenerService();
        }
    }


    private void bindLocationListenerService() {
        //Register LocationListenerService
        Intent intent = new Intent(this, LocationUpdateService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }
}
