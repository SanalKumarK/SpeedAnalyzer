package com.dyolab.speedanalyzer.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.dyolab.speedanalyzer.MapActivity;
import com.dyolab.speedanalyzer.R;

/**
 * Created by Sanal on 6/8/2017.
 */

public class SpeedAnalyzerNotificationService {

    Context context;
    // Sets an ID for the notification, so it can be updated
    int notifyID = 1;
    NotificationManager mNotificationManager ;
    NotificationCompat.Builder mBuilder;

    public SpeedAnalyzerNotificationService(Context context) {
        this.context = context;
    }

    public void displayNotification() {

        mBuilder = new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(context.getString(R.string.notify_title))
                        .setContentText(context.getString(R.string.notify_desc) + " 0.0 km/h");

    // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(context, MapActivity.class);

    // The stack builder object will contain an artificial back stack for the
    // started Activity.
    // This ensures that navigating backward from the Activity leads out of
    // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
    // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MapActivity.class);
    // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    // mId allows you to update the notification later on.
        mNotificationManager.notify(notifyID , mBuilder.build());
    }

    public void cancelNotification() {
        if(mNotificationManager != null) {
            mNotificationManager.cancel(notifyID);
        }
    }

    public void updateNotification(String speed) {
        if(mBuilder != null) {
            mBuilder.setContentText(context.getString(R.string.notify_desc) + " " + speed);
            // Because the ID remains unchanged, the existing notification is
            // updated.
            mNotificationManager.notify(
                    notifyID,
                    mBuilder.build());
        }
    }
}