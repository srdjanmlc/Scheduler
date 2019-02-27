package com.example.scheduler;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

/**
 * Created by Srdjan on 13-Mar-18.
 */

public class Contacts {

    private Context mContext;
    private DatabaseHelper db;

    //Default constructor
    public Contacts(Context context, DatabaseHelper dbHelper) {
        this.mContext = context;
        this.db = dbHelper;
    }


    public void readAllContactsWrite2Db() {

        //reading contacts
        Cursor cursor1;
        //list Columns to retrieve
        String col[] = {ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME};
        cursor1 = mContext.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, col, null, null, ContactsContract.Contacts.DISPLAY_NAME);
        if (cursor1 != null) {
            if (cursor1.getCount() > 0) {
                cursor1.moveToFirst();
                while (cursor1.moveToNext()) {
                    String id = cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    if (id != null && (name != "" || name != null)) {
                        Cursor cursor2 = mContext.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
                        if (cursor2 != null) {
                            while (cursor2.moveToNext()) {
                                //Preventing to send message to the same person if he / she has multiple numbers.
                                // Only one SMS to one number will be sent to avoid spamming the user on multiple numbers.
                                if (cursor2.getCount() > 1) {
                                    cursor2.moveToLast();
                                }
                                //Inserting extracted contact into local db
                                try {

                                /*Important note here: This list is always kept up to date because every time
                                  the app is stated and if there is new contact added in the address book it will
                                  be added in the local database also like some kind of incremental addition.
                                  This incremental addition is possible because I have made users table like so
                                  that I defined name column as indexing column (thus it is unique), so insert
                                  statements where the user is already in the db table will not pass (will be errors)
                                  and the rest will pass inserting new user in the table. */

                                    db.dbOperations("INSERT INTO users (name,  birthdate) VALUES (\"" + name + "\",\"\")");
                                } catch (Exception e) {
                                    //e.printStackTrace();
                                    continue;
                                }

                            }
                            cursor2.close();
                        }
                    }
                }
            }
            cursor1.close();
            db.close();
        }
    }

}
