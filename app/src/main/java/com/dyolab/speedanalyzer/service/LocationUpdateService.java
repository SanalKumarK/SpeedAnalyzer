package com.dyolab.speedanalyzer.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.dyolab.speedanalyzer.R;
import com.dyolab.speedanalyzer.util.SpeedAnalyzerUtil;
import com.dyolab.speedanalyzer.util.SpeedFinder;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LocationUpdateService extends Service implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        SensorEventListener{

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;
    private Date mLastLocKnownTime;

    private Location mCurrentLocation;
    private Date mCurrentLocKnownTime;

    private SpeedFinder speedFinder;

    /** TODO get from resource */
    public static String unit = SpeedAnalyzerUtil.KM_PER_HR;
    /** TODO get from resource */
    long update_interval = 1000 * 30 ;
    long update_fastest_interval = 1000 * 10;

    public final static String LOCATION_UPDATE = "locationUpdate";
    public final static String LOCATION = "location";
    public final static String SPEED = "speed";

    // The entry point to Google Play services, used by the Places API and Fused Location Provider.
    private GoogleApiClient mGoogleApiClient;
    private static final String TAG = LocationUpdateService.class.getSimpleName();

    private SpeedAnalyzerNotificationService mnotificationService;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private TriggerEventListener mTriggerEventListener;

    private boolean mMotionDetected = false;

    public LocationUpdateService() {
        speedFinder = new SpeedFinder();
    }

    // Binder given to clients
    private final IBinder mBinder = new LocationUpdateBinder();

    @Override
    public void onSensorChanged(SensorEvent event) {
        Toast.makeText(getBaseContext(), "SensorEvent " + event.sensor,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Toast.makeText(getBaseContext(), "AccuracyChanged " + accuracy,
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocationUpdateBinder extends Binder {
        public LocationUpdateService getService() {
            // Return this instance of LocalService so clients can call public methods
            return LocationUpdateService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Build the Play services client for use by the Fused Location Provider and the Places API.
        // Use the addApi() method to request the Google Places API and the Fused Location Provider.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
        mGoogleApiClient.connect();

        //Create a notification for the service
        mnotificationService = new SpeedAnalyzerNotificationService(getBaseContext());
    }

    private void startSensorLocationListener() {
        Toast.makeText(getBaseContext(), "Start Listening from Sensors",
                Toast.LENGTH_SHORT).show();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        mTriggerEventListener = new TriggerEventListener() {
            @Override
            public void onTrigger(TriggerEvent event) {
                if(event.sensor.getType() == Sensor.TYPE_SIGNIFICANT_MOTION) {
                    Toast.makeText(getBaseContext(), "TYPE_SIGNIFICANT_MOTION ",
                            Toast.LENGTH_SHORT).show();
                    mMotionDetected = true;
                } else if(event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
                    Toast.makeText(getBaseContext(), "TYPE_LINEAR_ACCELERATION ",
                            Toast.LENGTH_SHORT).show();
                } else if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    Toast.makeText(getBaseContext(), "TYPE_ACCELEROMETER ",
                            Toast.LENGTH_SHORT).show();
                }
            }
        };

        mSensorManager.requestTriggerSensor(mTriggerEventListener, mSensor);
    }

    @Override
    public void onDestroy() {
        stopLocationUpdates();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    public void startLocationUpdates() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(update_interval);
        mLocationRequest.setFastestInterval(update_fastest_interval);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        try  {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        } catch(SecurityException ex) {
            System.err.println("Error in getting location updates " + ex);
        }

        mnotificationService.displayNotification();


        //Listen for Significant motion
        startSensorLocationListener();
    }

    public void stopLocationUpdates() {
        if(mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        }

        //Cancel the notification
        mnotificationService.cancelNotification();
    }

    ArrayList<Location> locations = new ArrayList<>();
    Date prevRes = new Date();
    @Override
    public void onLocationChanged(Location location) {
        Date curTime = new Date();
        long duration = (prevRes.getTime() - curTime.getTime())/1000;
        prevRes = curTime;
        mMotionDetected = true;

        Log.d(TAG, "onLocationChanged - " + location.toString() + " " + duration + " mins");
        if(mMotionDetected && isBetterLocation(location, mLastKnownLocation)) {
            //System.out.println("onLocationChanged - " + location.toString() + " " + ((curTime.getTime() - prevRes.getTime())/1000));
            //set last know location to current location,
            if(locations.size() == 5) {
                if(mCurrentLocation != null) { //to skip initial null possibility
                    mLastKnownLocation = mCurrentLocation;
                    mLastLocKnownTime = mCurrentLocKnownTime;
                }

                /*
                mCurrentLocation = location;
                mCurrentLocKnownTime = new Date();
                */
                //Find the average of location
                mCurrentLocation = getAvgLocation(locations);
                mCurrentLocKnownTime = new Date();
                updateLocationChange();
                locations.clear();
            } else {
                locations.add(location);
            }
        }
    }

    private Location getAvgLocation(@NonNull ArrayList<Location> locations) {
        Location avgLocation = locations.get(locations.size()-1);
        double avgLatitude = 0.0f;
        double avgLongitude = 0.0f;
        for(Location loc : locations) {
            avgLatitude += loc.getLatitude();
            avgLongitude += loc.getLongitude();
        }
        avgLatitude /= locations.size();
        avgLongitude /= locations.size();
        avgLocation.setLatitude(avgLatitude);
        avgLocation.setLongitude(avgLongitude);

        return avgLocation;
    }

    private void updateLocationChange() {

        LatLng curMarkerPos;
        if(mCurrentLocation != null && mCurrentLocation.getLatitude() != 0
                && mCurrentLocation.getLongitude()!= 0) {
            curMarkerPos = new LatLng(mCurrentLocation.getLatitude(),
                    mCurrentLocation.getLongitude());
        } else {
            curMarkerPos = new LatLng(mLastKnownLocation.getLatitude(),
                    mLastKnownLocation.getLongitude());
        }

        //Get the speed of the device, and set the value.
        double speed = 0.0;
        if(mCurrentLocation != null && mCurrentLocKnownTime != null
                && mLastKnownLocation!= null && mLastLocKnownTime!=null) {
            speed = speedFinder.getSpeed(mLastKnownLocation, mLastLocKnownTime,
                    mCurrentLocation,mCurrentLocKnownTime, unit);

            //format the speed
            String speedStr = SpeedAnalyzerUtil.speedFormat.format(speed);
            mnotificationService.updateNotification(speedStr + " " + unit);
            broadCastLastKnownInfo(curMarkerPos, speed);
        } else {
            broadCastLastKnownInfo(curMarkerPos, speed);
        }
    }

    private void broadCastLastKnownInfo(LatLng curMarkerPos, Double speed) {
        Log.d("sender", "Broadcasting updated location");
        Intent intent = new Intent(LOCATION_UPDATE);
        // You can also include some extra data.
        intent.putExtra(SPEED, speed);
        intent.putExtra(LOCATION, curMarkerPos);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "Play services connected");
    }

    /**
     * Handles suspension of the connection to the Google Play services client.
     */
    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "Play services connection suspended");
    }

    /**
     * Handles failure to connect to the Google Play services client.
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        // Refer to the reference doc for ConnectionResult to see what error codes might
        // be returned in onConnectionFailed.
        Log.d(TAG, "Play services connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    public Location getLastKnownLocation() {
        try {
            return LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);
        } catch (SecurityException ex) {
            Log.e(TAG, ex.getMessage());
        }
        return null;
    }

    private static final int DELTA_MINUTES = 1000 * 60;

    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > DELTA_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -DELTA_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

      /*  // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());
*/
        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate) {
            return true;
        }
        return false;
    }

}