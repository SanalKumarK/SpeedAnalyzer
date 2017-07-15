package com.dyolab.speedanalyzer.db.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.dyolab.speedanalyzer.model.TripDO;
import com.dyolab.speedanalyzer.model.TripSpeedDetailsDO;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Sanal on 4/28/2017.
 */

public class SpeedAnalyzerDbHelper extends SQLiteOpenHelper {

    private static final String SQL_CREATE_TRIP_INFO_TABLE =
            "CREATE TABLE " + SpeedAnalyzerDbContract.TripInfo.TRIP_INFO_TABLE_NAME+ " (" +
                    SpeedAnalyzerDbContract.TripInfo.COLUMN_TRIP_CODE + " INTEGER PRIMARy KEY AUTOINCREMENT," +
                    SpeedAnalyzerDbContract.TripInfo.COLUMN_TRIP_NAME  + " TEXT," +
                    SpeedAnalyzerDbContract.TripInfo.COLUMN_TRIP_START_TIME + " INTEGER," +
                    SpeedAnalyzerDbContract.TripInfo.COLUMN_TRIP_END_TIME + " INTEGER)";

    private static final String SQL_CREATE_TRIP_SPEED_INFO_TABLE =
            "CREATE TABLE " + SpeedAnalyzerDbContract.TripSpeedEntry.TRIP_SPEED_INFO_TABLE_NAME + " (" +
                    SpeedAnalyzerDbContract.TripSpeedEntry.COLUMN_NAME_TRIP_CODE + " INTEGER," +
                    SpeedAnalyzerDbContract.TripSpeedEntry.COLUMN_NAME_TIME + " INTEGER," +
                    SpeedAnalyzerDbContract.TripSpeedEntry.COLUMN_NAME_LOCATION  + " TEXT," +
                    SpeedAnalyzerDbContract.TripSpeedEntry.COLUMN_NAME_SPEED+ " INT2, "+
                    "PRIMARY KEY("+ SpeedAnalyzerDbContract.TripSpeedEntry.COLUMN_NAME_TRIP_CODE+"," +
                    SpeedAnalyzerDbContract.TripSpeedEntry.COLUMN_NAME_TIME +"))";

    private static final String SQL_DELETE_TRIP_SPEED_INFO_TABLE =
            "DROP TABLE IF EXISTS " + SpeedAnalyzerDbContract.TripSpeedEntry.TRIP_SPEED_INFO_TABLE_NAME;

    private static final String SQL_DELETE_TRIP_INFO_TABLE =
            "DROP TABLE IF EXISTS " + SpeedAnalyzerDbContract.TripInfo.TRIP_INFO_TABLE_NAME;

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "DyoEye.db";

    public SpeedAnalyzerDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TRIP_INFO_TABLE);
        db.execSQL(SQL_CREATE_TRIP_SPEED_INFO_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL(SQL_DELETE_TRIP_INFO_TABLE);
        db.execSQL(SQL_DELETE_TRIP_SPEED_INFO_TABLE);

        // Create tables again
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
    }

    public List<TripSpeedDetailsDO> getTripSpeedInfo(Long tripCode) {
        ArrayList<TripSpeedDetailsDO> tripSpeedDetailsDOs = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

    // Define a projection that specifies which columns from the database
    // you will actually use after this query.
        String[] projection = {
                SpeedAnalyzerDbContract.TripSpeedEntry.COLUMN_NAME_TRIP_CODE,
                SpeedAnalyzerDbContract.TripSpeedEntry.COLUMN_NAME_TIME,
                SpeedAnalyzerDbContract.TripSpeedEntry.COLUMN_NAME_LOCATION,
                SpeedAnalyzerDbContract.TripSpeedEntry.COLUMN_NAME_SPEED
        };

        // Filter results WHERE "title" = 'My Title'
        String selection = SpeedAnalyzerDbContract.TripSpeedEntry.COLUMN_NAME_TRIP_CODE + " = ?";
        String[] selectionArgs = { String.valueOf(tripCode )};

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                SpeedAnalyzerDbContract.TripSpeedEntry.COLUMN_NAME_TIME + " ASC";

        Cursor cursor = db.query(
                SpeedAnalyzerDbContract.TripSpeedEntry.TRIP_SPEED_INFO_TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        while(cursor.moveToNext()) {
            TripSpeedDetailsDO tripDO = new TripSpeedDetailsDO();
            tripDO.setTripCode(cursor.getLong(0));
            tripDO.setTime(new Date(cursor.getLong(1)));
            tripDO.setLocation(cursor.getString(2));
            tripDO.setSpeed(cursor.getDouble(3));
            tripSpeedDetailsDOs.add(tripDO);
        }
        cursor.close();

        return tripSpeedDetailsDOs;
    }

    public long addTripSpeedDetails(TripSpeedDetailsDO tripSpeedDetailsDO) {
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(SpeedAnalyzerDbContract.TripSpeedEntry.COLUMN_NAME_TRIP_CODE, tripSpeedDetailsDO.getTripCode());
        values.put(SpeedAnalyzerDbContract.TripSpeedEntry.COLUMN_NAME_TIME, String.valueOf(tripSpeedDetailsDO.getTime()));
        values.put(SpeedAnalyzerDbContract.TripSpeedEntry.COLUMN_NAME_LOCATION, tripSpeedDetailsDO.getLocation());
        values.put(SpeedAnalyzerDbContract.TripSpeedEntry.COLUMN_NAME_SPEED, tripSpeedDetailsDO.getSpeed());

        // Insert the new row, returning the primary key value of the new row
        return db.insert(SpeedAnalyzerDbContract.TripSpeedEntry.TRIP_SPEED_INFO_TABLE_NAME, null, values);
    }

    public void deleteTripSpeedDetails(long tripCode) {
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();

        // Define 'where' part of query.
        String selection = SpeedAnalyzerDbContract.TripSpeedEntry.COLUMN_NAME_TRIP_CODE + " LIKE ?";
        // Specify arguments in placeholder order.
        String[] selectionArgs = { String.valueOf(tripCode) };
        // Issue SQL statement.
        db.delete(SpeedAnalyzerDbContract.TripSpeedEntry.TRIP_SPEED_INFO_TABLE_NAME, selection, selectionArgs);
    }


    /**Trip Queries start **/
    public long addTripDetails(String tripName) {
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        //values.put(SpeedAnalyzerDbContract.TripInfo.COLUMN_TRIP_CODE, tripDO.getTripCode());
        values.put(SpeedAnalyzerDbContract.TripInfo.COLUMN_TRIP_NAME, tripName);
        //values.put(SpeedAnalyzerDbContract.TripInfo.COLUMN_TRIP_START_TIME, String.valueOf(new Date().getTime()));
        //values.put(SpeedAnalyzerDbContract.TripInfo.COLUMN_TRIP_END_TIME, String.valueOf(tripDO.getEndTime().getTime()));

        // Insert the new row, returning the primary key value of the new row
        return db.insert(SpeedAnalyzerDbContract.TripInfo.TRIP_INFO_TABLE_NAME, null, values);
    }

    public void deleteTripInfo(long tripCode) {
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();

        // Define 'where' part of query.
        String selection = SpeedAnalyzerDbContract.TripInfo.COLUMN_TRIP_CODE + " LIKE ?";
        // Specify arguments in placeholder order.
        String[] selectionArgs = { String.valueOf(tripCode) };
        // Issue SQL statement.
        db.delete(SpeedAnalyzerDbContract.TripInfo.TRIP_INFO_TABLE_NAME, selection, selectionArgs);
    }

    public List<TripDO> getAllTripInfo() {
        ArrayList<TripDO> tripDOs = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                SpeedAnalyzerDbContract.TripInfo.COLUMN_TRIP_CODE,
                SpeedAnalyzerDbContract.TripInfo.COLUMN_TRIP_NAME,
                SpeedAnalyzerDbContract.TripInfo.COLUMN_TRIP_START_TIME,
                SpeedAnalyzerDbContract.TripInfo.COLUMN_TRIP_END_TIME
        };



        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                SpeedAnalyzerDbContract.TripInfo.COLUMN_TRIP_START_TIME+ " DESC";

        Cursor cursor = db.query(
                SpeedAnalyzerDbContract.TripInfo.TRIP_INFO_TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        while(cursor.moveToNext()) {
            TripDO tripDO = new TripDO();
            tripDO.setTripCode(cursor.getLong(0));
            tripDO.setTripName(cursor.getString(1));
            if(cursor.getLong(2) != 0) {
                tripDO.setStartTime(new Date(cursor.getLong(2)));
            }

            if(cursor.getLong(3) != 0) {
                tripDO.setEndTime(new Date(cursor.getLong(3)));
            }

            //To add not started trip to the top of the list
            if(tripDO.getStartTime() == null) {
                tripDOs.add(0,tripDO);
            } else {
                tripDOs.add(tripDO);
            }
        }
        cursor.close();

        return tripDOs;
    }


    public int updateTripInfo(TripDO trip) {
        SQLiteDatabase db = this.getReadableDatabase();

        // New value for one column
        ContentValues values = new ContentValues();

        if(trip.getStartTime() != null) {
            values.put(SpeedAnalyzerDbContract.TripInfo.COLUMN_TRIP_START_TIME, trip.getStartTime().getTime());
        }

        if(trip.getEndTime() != null) {
            values.put(SpeedAnalyzerDbContract.TripInfo.COLUMN_TRIP_END_TIME, trip.getEndTime().getTime());
        }

        // Which row to update, based on the title
        String selection = SpeedAnalyzerDbContract.TripInfo.COLUMN_TRIP_CODE + " LIKE ?";
        String[] selectionArgs = { trip.getTripCode().toString() };

        return db.update(
                SpeedAnalyzerDbContract.TripInfo.TRIP_INFO_TABLE_NAME,
                values,
                selection,
                selectionArgs);
    }
    //Trip Queries End
}
