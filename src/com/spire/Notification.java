package com.spire;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.spire.model.IntentActions;
import com.spire.model.struct.Parking;
import com.spire.parking_details.ParkingDetails;

/**
 * Created with IntelliJ IDEA.
 * User: volodymyr
 * Date: 03.07.13
 * Time: 14:30
 * To change this template use File | Settings | File Templates.
 */

public class Notification {

    private String mTitle;
    private String mText;
    private Context mContext;
    private Parking currentParking;

    public Notification ( Context context, Parking parking ){


        if ( context != null && parking != null ){

            this.mTitle = context.getString(R.string.notif_title);//parking.getParkid();
            this.mText = parking.getParkid() + ". " + context.getString(R.string.notif_message);
            this.mContext = context;
            currentParking = parking;

            createNotification();
        }
    }

    private void createNotification( ) {

        Intent emailIntent = new Intent( android.content.Intent.ACTION_SEND );

        String aEmailList[] = { "your.feedbackaddress@yourlistserver.com" }; // INIT_CONFIG: The email address, where user-supplied feedback is sent.


        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, aEmailList);
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "User feedback");
        emailIntent.setType("plain/text");


        PendingIntent pIntent = PendingIntent.getActivity( this.mContext, 0, emailIntent, PendingIntent.FLAG_UPDATE_CURRENT ); /**/

        Intent mReportStatus = new Intent ( this.mContext, ParkingDetails.class);
        mReportStatus.setAction(IntentActions.ACTION_SHOW_AVAILABILITY_OF_SPACE);

        Intent mReportStatusContent = new Intent ( this.mContext, ParkingDetails.class);
        mReportStatusContent.putExtra("parking", currentParking);
        PendingIntent pReportStatusContent = PendingIntent.getActivity( this.mContext, 0, mReportStatusContent, PendingIntent.FLAG_UPDATE_CURRENT );



        mReportStatus.putExtra("parking", currentParking);

        PendingIntent pReportStatus = PendingIntent.getActivity( this.mContext, 0, mReportStatus, PendingIntent.FLAG_UPDATE_CURRENT );



        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this.mContext)
                .setContentTitle(mTitle)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(mText))
                .setContentText(mText)
                .setSmallIcon(R.drawable.gray_icon)
                .addAction(R.drawable.av_upload, mContext.getString(R.string.btn_notif_report), pReportStatus)
                .addAction( R.drawable.content_email, mContext.getString(R.string.btn_notif_feedback), pIntent )
                .setAutoCancel(true);    /**/

        mBuilder.setContentIntent( pReportStatusContent );

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSound(alarmSound);


        NotificationManager notificationManager;
        notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);


        notificationManager.notify ( 0, mBuilder.build() );
    }
}
