package com.dyolab.speedanalyzer.service;

import android.content.Context;

import com.dyolab.speedanalyzer.db.helper.SpeedAnalyzerDbHelper;
import com.dyolab.speedanalyzer.model.TripDO;
import com.dyolab.speedanalyzer.model.TripSpeedDetailsDO;

import java.util.Date;
import java.util.List;

/**
 * Created by Sanal on 6/2/2017.
 */

public class SpeedAnalyzerModelProvider {

    SpeedAnalyzerDbHelper dbHelper ;

    public SpeedAnalyzerModelProvider(Context context) {
        dbHelper = new SpeedAnalyzerDbHelper(context);
    }

    public long addTripSpeedDetails(long tripCode, String location, Date time, double speed) {
        TripSpeedDetailsDO tsdDO = new TripSpeedDetailsDO(tripCode, location, time , speed);
        return dbHelper.addTripSpeedDetails(tsdDO);
    }

    public List<TripSpeedDetailsDO> getTripSpeedDetailsInfo(long tripCode) {
        return dbHelper.getTripSpeedInfo(tripCode);
    }

    public long addTripInfo(String tripName) {
        return dbHelper.addTripDetails(tripName);
    }

    public List<TripDO> getAllTrips() {
        return dbHelper.getAllTripInfo();
    }

    public void deleteTripDetails(TripDO tripDO) {
        if(tripDO != null) {
            dbHelper.deleteTripSpeedDetails(tripDO.getTripCode());
            dbHelper.deleteTripInfo(tripDO.getTripCode());
        }
    }

    public void startTrip(TripDO trip) {
        //To start the trip, set start time to current time
        trip.setStartTime(new Date());
        dbHelper.updateTripInfo(trip);
    }

    public void stopTrip(TripDO trip) {
        //To End the trip, set End time to current time
        trip.setEndTime(new Date());
        dbHelper.updateTripInfo(trip);
    }
}
