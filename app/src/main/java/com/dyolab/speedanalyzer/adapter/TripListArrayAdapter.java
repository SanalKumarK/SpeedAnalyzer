package com.dyolab.speedanalyzer.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;


import com.dyolab.speedanalyzer.MapActivity;
import com.dyolab.speedanalyzer.R;
import com.dyolab.speedanalyzer.TripActivity;
import com.dyolab.speedanalyzer.TripAnalyzerActivity;
import com.dyolab.speedanalyzer.model.TripDO;

import java.util.List;

/**
 * Created by Sanal on 6/1/2017.
 */

public class TripListArrayAdapter extends ArrayAdapter<TripDO> {

    private static final String TAG = TripListArrayAdapter.class.getSimpleName();

    List<TripDO> tripDOs = null;
    TripActivity activity;

    public TripListArrayAdapter(Activity activity, @NonNull List<TripDO> tripDOs) {
        super(activity.getBaseContext(), R.layout.trip_list_row, tripDOs);
        this.activity = (TripActivity) activity;
        this.tripDOs = tripDOs;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final TripDO trip = tripDOs.get(position);
        LayoutInflater inflater = activity.getLayoutInflater();
        convertView = inflater.inflate(R.layout.trip_list_row, parent, false);
        ImageButton ctrlBtn ;
        if(trip.getStartTime() == null ) { //Trip not started
            ctrlBtn = (ImageButton) convertView.findViewById(R.id.start_trip);
            ctrlBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    startTripTraking(v, trip);
                }
            });
        }
        else { //Trip started
            if(trip.getEndTime() == null) {
                ctrlBtn = (ImageButton) convertView.findViewById(R.id.stop_trip);
                ctrlBtn.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        stopTripTraking(v, trip);
                    }
                });
            } else {
                ctrlBtn = (ImageButton) convertView.findViewById(R.id.trip_report);
                ctrlBtn.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        showReport(v, trip);
                    }
                });
            }
        }
        ctrlBtn.setVisibility(View.VISIBLE);

        TextView tripName = (TextView) convertView.findViewById(R.id.text1);
        tripName.setText(trip.getTripName());

        //to set the selected colors dynamically
        if(trip.isSelected()) {
            convertView.setBackgroundColor(Color.LTGRAY);
        } else {
            convertView.setBackground(null);
        }

        return convertView;
    }

    public void startTripTraking(View view, TripDO trip) {
        Log.d(TAG,"Start Tracking trip : " + trip.getTripName());

        activity.locationUpdateService.startLocationUpdates();
        //start the trip
        if(trip.getStartTime() == null) {
            //If trip is already started, skip starting it again
            activity.getSpeedAnalyzerModelProvider().startTrip(trip);
        }
        Intent intent = new Intent(view.getContext(), MapActivity.class);
        intent.putExtra(TripActivity.TRIP, trip);
        view.getContext().startActivity(intent);
    }

    public void stopTripTraking(View v, TripDO trip) {
        Log.d(TAG,"Stop Tracking trip : " + trip.getTripName());

        activity.locationUpdateService.stopLocationUpdates();

        //stop the trip
        activity.getSpeedAnalyzerModelProvider().stopTrip(trip);
        activity.refreshTripsList();
    }

    public void showReport(View view, TripDO trip) {
        Log.d(TAG,"Show Trip Report : " + trip.getTripName());

        Intent intent = new Intent(view.getContext(), TripAnalyzerActivity.class);
        intent.putExtra(TripActivity.TRIP, trip);
        view.getContext().startActivity(intent);
    }


}
