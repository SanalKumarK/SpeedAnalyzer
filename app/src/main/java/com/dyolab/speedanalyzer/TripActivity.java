package com.dyolab.speedanalyzer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.dyolab.speedanalyzer.adapter.TripListArrayAdapter;
import com.dyolab.speedanalyzer.dialog.EditTripNameDialog;
import com.dyolab.speedanalyzer.model.TripDO;
import com.dyolab.speedanalyzer.service.LocationUpdateService;
import com.dyolab.speedanalyzer.service.SpeedAnalyzerModelProvider;

import java.util.ArrayList;


public class TripActivity extends AppCompatActivity implements EditTripNameDialog.EditTripNameDialogListener {

    public static final String TRIP = "Trip";

    private SpeedAnalyzerModelProvider speedAnalyzerModelProvider;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;
    public LocationUpdateService locationUpdateService;

    private ListView mlistView;
    private TripListArrayAdapter mAdapter;

    private ArrayList<TripDO> mSelTrips = new ArrayList<>();

    private MenuItem mDelMenuItem;

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocationUpdateService.LocationUpdateBinder binder = (LocationUpdateService.LocationUpdateBinder) service;
            locationUpdateService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip);

        speedAnalyzerModelProvider = new SpeedAnalyzerModelProvider(getBaseContext());

        refreshTripsList();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        startLocationListenerService();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_trip, menu);

        mDelMenuItem = menu.findItem(R.id.action_delete_trip);
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

        if (id == R.id.action_add_trip) {
            showEditTripDialog();
        }

        if (id == R.id.action_delete_trip) {
            if (mSelTrips.size() > 0) {
                deleteSelectedTrips();
            } else {
                Toast.makeText(this, getString(R.string.msg_sel_none),
                        Toast.LENGTH_SHORT).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void deleteSelectedTrips() {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.delete_dlg_message))
                .setPositiveButton(getString(R.string.delete_dlg_yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        for (TripDO tripDO : mSelTrips) {
                            speedAnalyzerModelProvider.deleteTripDetails(tripDO);
                            mAdapter.remove(tripDO);
                        }
                        mAdapter.notifyDataSetChanged();

                        mDelMenuItem.setVisible(false);
                    }
                })
                .setNegativeButton(getString(R.string.delete_dlg_no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object and return it
        Dialog dlg = builder.create();
        dlg.show();
    }

    public void showEditTripDialog() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new EditTripNameDialog();
        dialog.show(getSupportFragmentManager(), "EditTripNameDialog");
    }

    public void refreshTripsList() {
        mAdapter = new TripListArrayAdapter(
                this, new SpeedAnalyzerModelProvider(getBaseContext()).getAllTrips());
        mlistView = (ListView) findViewById(R.id.triplistview);
        mlistView.setAdapter(mAdapter);

        mlistView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                setToSelectionMode();
                return false;
            }
        });

        mlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TripDO selTrip = (TripDO) mlistView.getItemAtPosition(position);

                if (mlistView.getChoiceMode() == ListView.CHOICE_MODE_MULTIPLE) {
                    if (!selTrip.isSelected()) {
                        selTrip.setSelected(true);
                        if (!mSelTrips.contains(selTrip)) {
                            mSelTrips.add(selTrip);
                        }
                    } else {
                        selTrip.setSelected(false);
                        if (mSelTrips.contains(selTrip)) {
                            mSelTrips.remove(selTrip);
                        }
                        //To resetSelectionMode, when nothing selected
                        if (mSelTrips.isEmpty()) {
                            resetFromSelectionMode();
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                } else {
                    if (selTrip.getStartTime() != null && selTrip.getEndTime() != null) { //Trip Ended

                    } else if (selTrip.getStartTime() != null && selTrip.getEndTime() == null) { //Trip Started but not Ended
                        ((TripListArrayAdapter) (mlistView.getAdapter())).startTripTraking(view, selTrip);
                    } else {
                        Toast.makeText(view.getContext(), getString(R.string.msg_start_trip),
                                Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
    }

    private void addTripToDb(String tripName) {
        speedAnalyzerModelProvider.addTripInfo(tripName);
    }

    @Override
    public void onFinishEditDialog(String inputText) {
        addTripToDb(inputText);
        refreshTripsList();
    }

    public SpeedAnalyzerModelProvider getSpeedAnalyzerModelProvider() {
        return speedAnalyzerModelProvider;
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void startLocationListenerService() {
            /*
             * Request location permission, so that we can get the location of the
             * device. The result of the permission request is handled by a callback,
             * onRequestPermissionsResult.
             */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            startService();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            mLocationPermissionGranted = false;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            startService();
        }
    }


    /**
     * For Updating the location of the map
     */
    private void startService() {
        if (mLocationPermissionGranted) {
            Intent intent = new Intent(this, LocationUpdateService.class);
            startService(intent);

            bindLocationListenerService();
        }
    }

    private void bindLocationListenerService() {
        //Register LocationListenerService
        Intent intent = new Intent(this, LocationUpdateService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private void resetFromSelectionMode() {
        mlistView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mDelMenuItem.setVisible(false);
        for (TripDO tripDO : mSelTrips) {
            tripDO.setSelected(false);
        }
        mAdapter.notifyDataSetChanged();
        mSelTrips.clear();
    }

    private void setToSelectionMode() {
        mlistView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        mDelMenuItem.setVisible(true);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mlistView.getChoiceMode() == ListView.CHOICE_MODE_MULTIPLE) {
            resetFromSelectionMode();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
