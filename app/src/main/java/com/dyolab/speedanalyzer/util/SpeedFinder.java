package com.dyolab.speedanalyzer.util;

import android.location.Location;

import java.util.Date;

/**
 * Created by Sanal on 4/28/2017.
 */

public class SpeedFinder {

    private DistanceCalculator distanceCalculator;

    public SpeedFinder() {
        this.distanceCalculator = new DistanceCalculator();
    }

    public Double getSpeed(Location loc1, Date time1, Location loc2, Date time2, String reqUnit ) {
        Double speed = 0.0;

        //Get the distance in req unit
        Double distance = 0.0;
        Double duration = 1.0;

        //TODO, distanceCalculation can be replaced with Location.distanceTo(LocationEnd)
        if(reqUnit.equals(SpeedAnalyzerUtil.METER_PER_SEC)){ //m/sec
            distance = distanceCalculator.getDistance(loc1, loc2, DistanceCalculator.METER);
            duration = Double.valueOf(time2.getTime() - time1.getTime())/ Constant.SEC_IN_MSEC; //Diff In Hours
        } else if (reqUnit.equals(SpeedAnalyzerUtil.KM_PER_HR)) { //km/h
            distance = distanceCalculator.getDistance(loc1, loc2, DistanceCalculator.KM);
            duration = Double.valueOf((time2.getTime() - time1.getTime())) / Constant.HOUR_IN_MSEC; //Diff In Hours
        } else {
            duration = 1.0;
        }

        if(duration > 0 ) {
            speed = distance / duration;
        }
        return speed;
    }
}
