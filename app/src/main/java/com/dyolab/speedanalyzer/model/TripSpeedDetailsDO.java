package com.dyolab.speedanalyzer.model;

import java.util.Date;

/**
 * Created by Sanal on 4/28/2017.
 */

public class TripSpeedDetailsDO {
    private Long tripCode;
    private String location;
    private Date time;
    private Double speed;

    public TripSpeedDetailsDO() {
    }

    public TripSpeedDetailsDO(long tripCode, String location, Date time, double speed) {
        this.tripCode = tripCode;
        this.location = location;
        this.time = time;
        this.speed = speed;
    }

    public Long getTripCode() {
        return tripCode;
    }

    public void setTripCode(Long tripCode) {
        this.tripCode = tripCode;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }
}
