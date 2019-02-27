package com.example.scheduler;



import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;

import com.applandeo.materialcalendarview.EventDay;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;


//Custom helper class for handling querying the google calendar

public class CalendarContractHelper{

    private Context mContext;
    private String account;
    private Cursor cursor;
    private long startDate;
    private Calendar calendar;
    private List<EventDay> eventList;
    private ArrayList<String> eventArray, eventArrayTitle;
    private String[] EVENT_PROJECTION;


    // The indices for the projection array above.
    private int PROJECTION_CALENDAR_ID, PROJECTION_TITLE, PROJECTION_START_INDEX, PROJECTION_END_INDEX;


    //Contractor
    public CalendarContractHelper(Context context, String accountName) {
        this.account = accountName;
        this.mContext = context;
        eventList = new ArrayList<>();
        eventArray = new ArrayList<>();
        eventArrayTitle = new ArrayList<>();
        EVENT_PROJECTION = new String[]{
                CalendarContract.Events.CALENDAR_ID,
                CalendarContract.Events.TITLE,
                CalendarContract.Events.DTSTART,
                CalendarContract.Events.DTEND
        };
        PROJECTION_CALENDAR_ID = 0;
        PROJECTION_TITLE = 1;
        PROJECTION_START_INDEX = 2;
        PROJECTION_END_INDEX = 3;
    }

    public List<EventDay> queryAllEvents() {

        calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH, Calendar.HOUR, Calendar.MINUTE);

        ContentResolver cr = mContext.getContentResolver();
        Uri uri = CalendarContract.Events.CONTENT_URI;

        // Selection criteria to query Event table
        String selection = "((" + CalendarContract.Events.DTSTART + " >= ?) AND (" + CalendarContract.Events.CALENDAR_DISPLAY_NAME + " = ?))";
        String[] selectionArgs = new String[]{String.valueOf(calendar.getTimeInMillis()), account};

        // Submit the query
        cursor = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);


        while (cursor.moveToNext()) {
            startDate = cursor.getLong(PROJECTION_START_INDEX);
            Calendar calendarStart = Calendar.getInstance();
            calendarStart.setTimeInMillis(startDate);
            eventList.add(new EventDay(calendarStart, R.drawable.sample_icon));
        }
        cursor.close();

        return eventList;
    }

    public HashMap<String, String> queryEvent(int dayVariable, int monthVariable, int yearVariable) {

        HashMap<String, String> output = new HashMap<>();

        eventArray = new ArrayList<>();
        eventArrayTitle = new ArrayList<>();
        String title, event;
        long startDate, endDate, startMillis, endMillis;

        // 0 = January, 1 = February, ...
        // event instances
        Calendar beginTime = Calendar.getInstance();
        beginTime.set(yearVariable, monthVariable, dayVariable, 0, 0);
        startMillis = beginTime.getTimeInMillis();
        Calendar endTime = Calendar.getInstance();
        endTime.set(yearVariable, monthVariable, dayVariable, 23, 59);
        endMillis = endTime.getTimeInMillis();

        ContentResolver cr = mContext.getContentResolver();
        Uri uri = CalendarContract.Events.CONTENT_URI;

        // Selection criteria to query event table
        String selection = "((" + CalendarContract.Events.DTSTART + " >= ?) AND (" + CalendarContract.Events.DTEND + " <= ?) AND (" + CalendarContract.Events.CALENDAR_DISPLAY_NAME + " = ?))";
        String[] selectionArgs = new String[]{String.valueOf(startMillis), String.valueOf(endMillis), account};

        // Submit the query
        cursor = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);

        while (cursor.moveToNext()) {

            title = cursor.getString(PROJECTION_TITLE);
            startDate = cursor.getLong(PROJECTION_START_INDEX);
            endDate = cursor.getLong(PROJECTION_END_INDEX);

            Calendar calendarStart = Calendar.getInstance();
            Calendar calendarEnd = Calendar.getInstance();
            calendarStart.setTimeInMillis(startDate);
            calendarEnd.setTimeInMillis(endDate);
            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            event = "Event: " + title + "\nStart Date: " + formatter.format(calendarStart.getTime()) + "\nEnd Date: " + formatter.format(calendarEnd.getTime());
            output.put(event, title);
        }

        return output;
    }

    public ArrayList<String> deleteEvent(String eventName) {

        int updCount;

        Uri uri = CalendarContract.Events.CONTENT_URI;
        String mSelectionClause = "((" + CalendarContract.Events.TITLE + " = ?) AND (" + CalendarContract.Events.CALENDAR_DISPLAY_NAME + " = ?))";
        String[] mSelectionArgs = {eventName, account};

        mContext.getContentResolver().delete(uri, mSelectionClause, mSelectionArgs);



        eventArray.remove(eventName);

        return eventArray;
    }

    public List<EventDay> addEvent(String title,int day, int month, int year,int hour, int minute) {

        List<EventDay> events = new ArrayList<>();

        int calendarId = 0; //initial value to prevent error in values.put
        String[] EVENT_PROJECTION = new String[]{
                CalendarContract.Events.CALENDAR_ID,      // 0

        };

        // The indices for the projection array above.
        int PROJECTION_CALENDAR_ID = 0;

        ContentResolver cr = mContext.getContentResolver();
        Uri uri = CalendarContract.Events.CONTENT_URI;

        // The ID of the recurring event whose instances you are searching
        // for in the Instances table
        String selection = CalendarContract.Events.CALENDAR_DISPLAY_NAME + " = ?";
        String[] selectionArgs = new String[]{account};

        // Submit the query
        cursor = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);

        while (cursor.moveToNext()) {
            calendarId = cursor.getInt(PROJECTION_CALENDAR_ID);
        }

        Calendar beginTime = Calendar.getInstance();
        beginTime.set(year, month, day, hour, minute);

        Calendar endTime = Calendar.getInstance();
        endTime.set(year, month, day ,hour+1, minute);

        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, beginTime.getTimeInMillis());
        values.put(CalendarContract.Events.DTEND, endTime.getTimeInMillis());
        values.put(CalendarContract.Events.TITLE, title);
        values.put(CalendarContract.Events.CALENDAR_ID, calendarId);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());

        cr.insert(CalendarContract.Events.CONTENT_URI, values);

        events.add(new EventDay(beginTime, R.drawable.sample_icon));

        return events;

    }
}


