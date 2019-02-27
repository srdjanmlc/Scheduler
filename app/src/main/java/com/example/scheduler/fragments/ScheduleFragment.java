package com.example.scheduler.fragments;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.scheduler.AlarmReceiver;
import com.example.scheduler.R;

import java.util.Calendar;

public class ScheduleFragment extends Fragment {

    private CoordinatorLayout coordinatorLayoutNotification;
    private SharedPreferences sharedPreferences;
    private Boolean alarmUp,alarmFlag;
    private Button setNotMessageBtn, setBirthMessageBtn, startNotificationsBtn;
    private TextView textView;
    private Snackbar snackbar;


    /* Fragment to Schedule appointment. Here I set the appointment and birthday messages and save
    them using the shared preferences (this is ideal for this as this is small amount of data.
    Also time for which notifications are scheduled is also saved in the shared preferences, adn flag to indicate are
    the notification currently set or not. I need this because I extract them in the AlarmBootReceiver class (permission set in manifest RECEIVE_BOOT_COMPLETED)
    to be able to set the notification again after phone boots up (for example user switched off the phone or phone turned
    off because of the low battery. Notifications are set using the AlarmReceiver class which extends Broadcast receiver
    class (I implement onReceive method).
    Notification have interval of 24hours, and are checking each day at the set time if there is any meetings scheduled
    for tomorrow and is there a contact which have birthday that day and send the message to remind them about meeting or to
    congratulate them birthday.

    Times for the meeting are extracted from the database and are replaced in the message you specify. The replacement character is ?.
    For example: Template message "The meeting is scheduled for ? hours tomorrow." So If the time is set to 11.30,
    the sent sms message wil be "The meeting is scheduled for 11.30 hours tomorrow."

    The layout for this fragment wrapped inside Scrollbar to make it more responsive
    (either to different screen sizes or for the rotation of the phone.


    */

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_schedule, container, false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {


        sharedPreferences = getContext().getSharedPreferences("com.example.scheduler", Context.MODE_PRIVATE);
        coordinatorLayoutNotification = view.findViewById(R.id.coordinatorLayoutNotification);
        setNotMessageBtn = view.findViewById(R.id.setNotificationBtn);
        setBirthMessageBtn = view.findViewById(R.id.setBirthBtn);
        startNotificationsBtn = view.findViewById(R.id.startNotificationBtn);
        textView = view.findViewById(R.id.notificationTextView);
        int hour = sharedPreferences.getInt("setHours",0);
        int minutes = sharedPreferences.getInt("setMinutes",0);
        alarmFlag = sharedPreferences.getBoolean("setFlag",false);

        //check if the alarm manager with the appropriate Pending intent is already running
       // alarmUp = (PendingIntent.getBroadcast(getContext(), 0, new Intent(getContext(), AlarmReceiver.class), PendingIntent.FLAG_NO_CREATE) != null);

        //Checking if the notification are stated or not.
        if (alarmFlag) {
            textView.setText("Notification service started and scheduled for " + String.format("%02d",hour) + ":" + String.format("%02d",minutes));
            startNotificationsBtn.setText("Stop Notification Service");
        } else {
            textView.setText("Notification service not running!");
            startNotificationsBtn.setText("Start Notification Service");
        }


        //Setting messages
        setNotMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeAlertDialog("smsMeetingMessage");
            }
        });

        setBirthMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeAlertDialog("smsBirthdayMessage");
            }
        });

        //Starting notification
        startNotificationsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alarmFlag = sharedPreferences.getBoolean("setFlag",false);

                if (alarmFlag) {
                   stopNotificationService();
                } else {
                   startNotificationService();
                }

            }
        });


    }

    // I am using modified AlertDialog to enter the messages.
    public void makeAlertDialog(final String sharedPreferenceName) {
        AlertDialog.Builder newDialog = new AlertDialog.Builder(getContext());
        newDialog.setIcon(R.drawable.icon_sms);
        newDialog.setTitle("SMS content");
        final EditText editText = new EditText(getContext());
        editText.setSingleLine(false);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        editText.setLines(4);
        editText.setMaxLines(5);
        editText.setGravity(Gravity.START | Gravity.TOP);
        editText.setHorizontalScrollBarEnabled(false);
        editText.setText(sharedPreferences.getString(sharedPreferenceName, ""));
        newDialog.setView(editText);
        newDialog.setCancelable(false);
        newDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sharedPreferences.edit().putString(sharedPreferenceName, editText.getText().toString()).apply();
            }
        });
        newDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        newDialog.show();
    }

    public void startNotificationService() {

        //initialing Alarm manager object
        final AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);

        /*Checking if the user has set messages. If the messages are not set, user will
          see snackbar message to set them first*/

        if (!(sharedPreferences.getString("smsMeetingMessage", "").equals("")) || !(sharedPreferences.getString("smsBirthdayMessage", "").equals(""))) {

            /* Set the alarm to start at time you choose from TimePicker Dialog */
            final Calendar calendar = Calendar.getInstance();
            int hours = calendar.get(Calendar.HOUR_OF_DAY);
            int minutes = calendar.get(Calendar.MINUTE) + 1;

            TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), android.R.style.Theme_Holo_Dialog, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    sharedPreferences.edit().putInt("setHours", hourOfDay).apply();
                    sharedPreferences.edit().putInt("setMinutes", minute).apply();
                    sharedPreferences.edit().putBoolean("setFlag", true).apply();
                    /* Repeating on every repeatIntervalMinutes interval */
                    /* Retrieve a PendingIntent that will perform a broadcast */
                    Intent alarmIntent = new Intent(getContext(), AlarmReceiver.class);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0, alarmIntent, 0);
                    //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 1000 * 60 * repeatIntervalMinutes, pendingIntent);
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
                    textView.setText("Notification service started and scheduled for " + String.format("%02d",hourOfDay) + ":" + String.format("%02d",minute));
                    startNotificationsBtn.setText("Stop Notification Service");
                    snackbar = Snackbar.make(coordinatorLayoutNotification,"Notification Service Started!",Snackbar.LENGTH_LONG);
                    snackbar.show();

                }
            }, hours, minutes, true);
            timePickerDialog.show();

        } else {
            snackbar = Snackbar.make(coordinatorLayoutNotification,"Please set SMS message!",Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }

    public void stopNotificationService() {
        AlarmManager alarmManagerCancel = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntentCancel = new Intent(getContext(), AlarmReceiver.class);
        PendingIntent pendingIntentCancel = PendingIntent.getBroadcast(getContext(), 0, alarmIntentCancel, 0);
        pendingIntentCancel.cancel();
        alarmManagerCancel.cancel(pendingIntentCancel);
        sharedPreferences.edit().putBoolean("setFlag", false).apply();
        startNotificationsBtn.setText("Start Notification Service");
        textView.setText("Notification service not running!");
        snackbar = Snackbar.make(coordinatorLayoutNotification,"Notification Service Stopped!",Snackbar.LENGTH_LONG);
        snackbar.show();

    }

}
