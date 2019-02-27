package com.example.scheduler;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Srdjan on 12-Mar-18.
 */

public class DatabaseHelper {

    Context mContext;
    String mDbName;
    private SQLiteDatabase db;

    //default constructor
    public DatabaseHelper(Context context, String dbName) {
        this.mContext = context;
        this.mDbName = dbName;
        db = mContext.openOrCreateDatabase(dbName, mContext.MODE_PRIVATE, null);
    }

    public void dbOperations(String sqlStatement) {
        db.execSQL(sqlStatement);
    }

    /*Here I decided to use rawQuery method. tThis is not a commercial app.
    For commercial app parameterised SQL query might be a better an safer solution.*/
    public Cursor dbQuery(String sqlStatement) {
        Cursor c = null;
        try {
            c = db.rawQuery(sqlStatement, null);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }

    public void close() {
        db.close();
    }
}