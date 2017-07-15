package com.dyolab.speedanalyzer.util;

import android.support.annotation.NonNull;
import android.support.annotation.Size;

import com.dyolab.speedanalyzer.model.TripAnalyzerReport;
import com.dyolab.speedanalyzer.model.TripSpeedDetailsDO;

import java.util.ArrayList;

/**
 * Created by Sanal on 5/17/2017.
 */

public class TripAnalyzer {

    public TripAnalyzerReport getTripAnalyzeReport(@NonNull @Size(min=1) ArrayList<TripSpeedDetailsDO> speedDetailsDOs) {
        TripAnalyzerReport report = new TripAnalyzerReport();
        if(speedDetailsDOs.size() == 0) {
            report.setMaxSpeed(0.0);
            report.setMinSpeed(0.0);
            report.setAvgSpeed(0.0);
            report.setIdleTime(0);
        } else {
            report.setMaxSpeed(speedDetailsDOs.get(0).getSpeed());
            report.setMinSpeed(speedDetailsDOs.get(0).getSpeed());
            Double avgSpeed = 0.0;
            long idleDuration = 0;
            TripSpeedDetailsDO prevSpeed = null;
            for (TripSpeedDetailsDO speedDetail: speedDetailsDOs) {
                //For finding avg Speed
                avgSpeed += speedDetail.getSpeed();
                //For idle time
                if(speedDetail.getSpeed() == 0 && prevSpeed != null) {
                    idleDuration += (speedDetail.getTime().getTime() - prevSpeed.getTime().getTime());
                }
                //Find max speed
                else if(report.getMaxSpeed() < speedDetail.getSpeed()){
                    report.setMaxSpeed(speedDetail.getSpeed());
                }
                //Find min speed
                else if(report.getMinSpeed() > speedDetail.getSpeed()){
                    report.setMinSpeed(speedDetail.getSpeed());
                }
                prevSpeed = speedDetail;
            }
            avgSpeed = avgSpeed/speedDetailsDOs.size();
            idleDuration = idleDuration/Constant.MIN_IN_MSEC;

            report.setAvgSpeed(avgSpeed);
            report.setIdleTime(idleDuration);
        }
        return report;
    }
}
