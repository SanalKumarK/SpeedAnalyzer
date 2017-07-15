package com.dyolab.speedanalyzer.db.helper;

import android.provider.BaseColumns;

/**
 * Created by Sanal on 4/28/2017.
 */

public final class SpeedAnalyzerDbContract {
    private SpeedAnalyzerDbContract() {
    }

    /* TripSpeedInfo Table properties*/
    public static class TripSpeedEntry implements BaseColumns{
        public static final String TRIP_SPEED_INFO_TABLE_NAME = "tripspeed";
        //Columns
        public static final String COLUMN_NAME_TRIP_CODE = "tripcode";
        public static final String COLUMN_NAME_TIME = "time";
        public static final String COLUMN_NAME_LOCATION = "location";
        public static final String COLUMN_NAME_SPEED = "speed";
    }

    /** Trip Info table properties */
    public static class TripInfo implements BaseColumns {
        public static final String TRIP_INFO_TABLE_NAME = "trip";
        //Columns
        public static final String COLUMN_TRIP_CODE = "tripcode";
        public static final String COLUMN_TRIP_NAME = "tripname";
        public static final String COLUMN_TRIP_START_TIME = "starttime";
        public static final String COLUMN_TRIP_END_TIME = "endtime";
    }
}
