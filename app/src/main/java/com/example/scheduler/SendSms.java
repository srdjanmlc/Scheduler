package com.example.scheduler;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.widget.Toast;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Srdjan on 09-Mar-18.
 */

public class SendSms {

    private Context mContext;
    private AtomicInteger notification_id;

    //Default constructor
    public SendSms (Context context) {
        this.mContext = context;
        this.notification_id =  new AtomicInteger(0);
    }


    public void sendSMS(String name, String phoneNo, String msg) {

        try {

            //Using sms manager to send sms messages.
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, msg, null, null);

            /* As I am targeting Android 7 and above I am using the notification manager to display notification
               for sent SMS messages. */
            NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

//            Building the notification message
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext.getApplicationContext(), "default");
            mBuilder.setSmallIcon(R.drawable.notification_icon);
            mBuilder.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.icon_sms));
            mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(msg));
            mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
            mBuilder.setContentTitle("SMS message sent to " + name);


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel("default", "Default Channel", NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
            }

            // notificationID allows you to update the notification later on.
            notificationManager.notify(notification_id.incrementAndGet(), mBuilder.build());

        } catch (Exception ex) {
            Toast.makeText(mContext.getApplicationContext(), ex.getMessage().toString(), Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }

    }


}
