package com.dyolab.speedanalyzer.util;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Sanal on 4/27/2017.
 */

public class DistanceCalculator {

    public static final String METER = "m";
    public static final String KM = "km";

    /**
     Haversine formula:
     a = sin²(Δφ/2) + cos φ1 ⋅ cos φ2 ⋅ sin²(Δλ/2)
     c = 2 ⋅ atan2( √a, √(1−a) )
     d = R ⋅ c

     where	φ is latitude, λ is longitude, R is earth’s radius (mean radius = 6,371km);
     note that angles need to be in radians to pass to trig functions!
     */


    public Double getDistance(Location loc1, Location loc2, String reqUnit) {
        LatLng latLng1 = new LatLng(loc1.getLatitude(),
                loc1.getLongitude());

        LatLng latLng2 = new LatLng(loc2.getLatitude(),
                loc2.getLongitude());

        return getDistance(latLng1, latLng2, reqUnit);
    }

    public Double getDistance(LatLng latLng1, LatLng latLng2, String reqUnit) {

        Double lat1 = latLng1.latitude;
        Double lon1 = latLng1.longitude;
        Double lat2 = latLng2.latitude;
        Double lon2 = latLng2.longitude;

        Double dLat = convertDegreeToRadian(lat2 - lat1);
        Double dLon = convertDegreeToRadian(lon2 - lon1);

        Double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(convertDegreeToRadian(lat1)) * Math.cos(convertDegreeToRadian(lat2)) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);

        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        Double distance = Constant.EARTH_RADIUS * c;

        if(reqUnit.equals(METER)) {
            return distance * 1000; //m
        }
        return distance; //km
    }

    private static Double convertDegreeToRadian(Double deg) {
        return deg * (Math.PI/180);
    }
}
