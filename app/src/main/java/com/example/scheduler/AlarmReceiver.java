package com.example.scheduler;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Srdjan on 06-Mar-18.
 */

public class AlarmReceiver extends BroadcastReceiver {

    Context mContext;
    SharedPreferences sharedPreferences;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        proccesAndSend();
    }

    public void proccesAndSend() {

        sharedPreferences = mContext.getSharedPreferences("com.example.scheduler", Context.MODE_PRIVATE);
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String dateStringMeeting =  String.format("%02d",day+1) +  "/" + String.format("%02d",month+1) + "/" + year ;
        String dateStringBirthday = String.format("%02d",day+1) +  "/" + String.format("%02d",month+1) + "/" + year;
        ArrayList<ArrayList<String>> name_number_time_all = new ArrayList<>();
        ArrayList<ArrayList<String>> name_number_all = new ArrayList<>();
        String meetingMessage,meetingMessageRegex,birthdayMessage;

        meetingMessage = sharedPreferences.getString("smsMeetingMessage","");
        birthdayMessage = sharedPreferences.getString("smsBirthdayMessage","");

        SendSms sendSms = new SendSms(mContext);

        DatabaseHelper db = new DatabaseHelper(mContext, "Contacts");
        Cursor dbCursorMeeting = db.dbQuery("select a.id _id, a.name name, b.appointmentdate appointmentdate \n" +
                "from users a \n" +
                "join\n" +
                "(SELECT id _id, appointments.nameid, appointments.appointmentdate\n" +
                "FROM appointments \n" +
                "where appointments.appointmentdate like \"" + dateStringMeeting +"%\" ) b\n" +
                "on a.id = b.nameid");
        Cursor dbCursorBirthday = db.dbQuery("select id _id, name, birthdate from users where birthdate like \"" + dateStringBirthday + "\"" );

        //ensure there is result in the cursor

            if (dbCursorMeeting != null && dbCursorMeeting.getCount() > 0) {
                name_number_time_all = returnTuple(dbCursorMeeting, true);
                for (ArrayList<String> item : name_number_time_all) {
                     meetingMessageRegex = meetingMessage.replace("?", item.get(2));
                     Log.i("Meeting message",meetingMessageRegex);
                     sendSms.sendSMS(item.get(0),item.get(1),meetingMessageRegex);
                      Log.i("Name", item.get(0));
                      Log.i("Phone Number", item.get(1));
                       Log.i("Appoitment Time", item.get(2));

                }
            }


        if (dbCursorBirthday != null && dbCursorBirthday.getCount() > 0) {
            name_number_all = returnTuple(dbCursorBirthday, false);
            for (ArrayList<String> item : name_number_all) {
                Log.i("Birthday message",birthdayMessage);
                sendSms.sendSMS(item.get(0).toString(),item.get(1),birthdayMessage);
                Log.i("Birthday data", " ");
                Log.i("Name", item.get(0));
                Log.i("Phone Number", item.get(1));
            }
        }


        dbCursorMeeting.close();
        dbCursorBirthday.close();
        db.close();

    }

    public ArrayList<ArrayList<String>> returnTuple(Cursor cDatabase, boolean meeting) {

        String pNumber = "";
        String nameColumn_db = "";
        String[] appoitment_datetime = {};
        String appoitment_time = "";
        ArrayList<ArrayList<String>> name_number_final = new ArrayList<>();
        ArrayList<String> name_number_tuple;

        Cursor contactsCursor,cursor2;
        String col[] = {ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME};
        contactsCursor = mContext.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, col, null, null, ContactsContract.Contacts.DISPLAY_NAME);

        //cDatabase.moveToFirst();
        while(cDatabase.moveToNext()){
            nameColumn_db = cDatabase.getString(cDatabase.getColumnIndex("name"));
            if (meeting) {
                appoitment_datetime = cDatabase.getString(cDatabase.getColumnIndex("appointmentdate")).split(" ");
                appoitment_time = appoitment_datetime[1];
            }
            if (contactsCursor !=null && contactsCursor.getCount()>0) {
                contactsCursor.moveToFirst();
                while (contactsCursor.moveToNext()) {
                    String contacts_id = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts._ID));
                    String contacts_Name = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    if (nameColumn_db.equals(contacts_Name)) {
                        cursor2 = mContext.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{contacts_id}, null);
                        if (cursor2 != null) {
                            while (cursor2.moveToNext()) {
                                cursor2.moveToFirst();
                                //Preventing to send message to the same person if he / she has multiple numbers. Only one SMS to one number will be sent
                                if (cursor2.getCount() > 1) {
                                    cursor2.moveToLast();
                                }
                                pNumber = cursor2.getString(cursor2.getColumnIndex((ContactsContract.CommonDataKinds.Phone.NUMBER)));
                                name_number_tuple = new ArrayList<>();
                                name_number_tuple.add(nameColumn_db);
                                name_number_tuple.add(pNumber);
                                if (meeting){
                                    name_number_tuple.add(appoitment_time);
                                }
                                name_number_final.add(name_number_tuple);
                            }
                            cursor2.close();
                        }

                    }
                }

            }
        }


        contactsCursor.close();
        cDatabase.close();
        return name_number_final;
    }

}
