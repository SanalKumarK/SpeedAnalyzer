package com.dyolab.speedanalyzer.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Sanal on 5/28/2017.
 */

public class TripDO implements Serializable {

    private Long tripCode;
    private String tripName;
    private Date startTime;
    private Date endTime;

    private Boolean selected = false;

    public TripDO() {
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public TripDO(Long tripCode, String tripName, Date startTime) {
        this.tripCode = tripCode;
        this.tripName = tripName;
        this.startTime = startTime;
    }

    public Long getTripCode() {
        return tripCode;
    }

    public void setTripCode(Long tripCode) {
        this.tripCode = tripCode;
    }

    public String getTripName() {
        return tripName;
    }

    public void setTripName(String tripName) {
        this.tripName = tripName;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Boolean isSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    @Override
    public String toString() {
        return tripName;
    }
}
