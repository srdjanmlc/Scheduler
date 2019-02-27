package com.example.scheduler.fragments;


import android.app.DatePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.example.scheduler.DatabaseHelper;
import com.example.scheduler.R;


import java.util.Calendar;


public class ContactListFragment extends Fragment implements ListView.OnItemClickListener, ListView.OnItemLongClickListener, DatePickerDialog.OnDateSetListener {

    private ListView contactsListView;
    private static DatabaseHelper db;
    static String sqlSelectStatement = "SELECT users.id _id, users.name, users.birthdate FROM users " +
            "LEFT JOIN deleted_users ON deleted_users.name = users.name " +
            "WHERE deleted_users.name IS NULL ORDER BY users.name ASC";
    private static SimpleCursorAdapter cursorAdapter;
    static Cursor dbCursor;
    private static String name;
    private static Cursor newCursor;
    private DatePickerDialog datePickerDialog;
    private Calendar calendar;
    private EditText searchEditText;
    private String filter;

    /*This is the fragment where I have list of all contact I acquired from the address book and put them in the local app database.
    They are listed in the list view and I can add birthdays for each one of them.
    As I wanted some sort of listview filtering and the input data is cursor (from database) not an array, I have set setFilterQueryProvider
    on a cursorAdapter (simple cursor adapter).*/

    /*One note: This list is always kept up to date. Detailed explanation is in Contact class. */

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_contact_list, container, false);

        contactsListView = view.findViewById(R.id.contactsListView);
        searchEditText = view.findViewById(R.id.searchEditText);
        db = new DatabaseHelper(getActivity().getApplicationContext(), "Contacts");
        //Query all contacts and their birthdays form the database.
        dbCursor = db.dbQuery(sqlSelectStatement);
        /*As the cursor is the result of the db query I have user Simple Cursor Adapter
         to adapt the data to the custom view (list_selector layout). */
        String[] fromColumns = new String[]{"name", "birthdate"};
        int[] toViews = {R.id.contactNameTextView, R.id.birthdayTextView};
        cursorAdapter = new SimpleCursorAdapter(getActivity().getApplicationContext(), R.layout.list_selector, dbCursor, fromColumns, toViews, 0);
        contactsListView.setAdapter(cursorAdapter);

        contactsListView.setOnItemClickListener(this);
        contactsListView.setOnItemLongClickListener(this);

        //setting the Filtering condition for the cursor adapter.
        //That is basically query with LIKE condition and the inputs are characters typed within edittext view.
        //The filtering is triggered on onTextChanged of the addTextChangedListener on the edittext view.
        cursorAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {

                String sql  = "SELECT users.id _id, users.name, users.birthdate FROM users LEFT JOIN deleted_users ON deleted_users.name = users.name WHERE deleted_users.name IS NULL AND users.name LIKE '" + constraint.toString() +  "%' ORDER BY users.name ASC";

                return db.dbQuery(sql);
            }
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter = s.toString();
                cursorAdapter.getFilter().filter(filter);
                cursorAdapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return view;

    }

    //When it is clicked on listview item Date picker dialog is shown where user can enter the date.
    // The entry for that contact is then updated with the birthdate in the database.
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Window window;
        TextView nameTextView;

        nameTextView = view.findViewById(R.id.contactNameTextView);
        name=nameTextView.getText().toString();
        calendar = Calendar.getInstance();
        datePickerDialog = new DatePickerDialog(getContext(), android.R.style.Theme_Holo_Dialog, this,
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.setTitle("Birthday for " + name);
        window = datePickerDialog.getWindow();
        window.setGravity(Gravity.CENTER);
        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        datePickerDialog.show();
        datePickerDialog.setOnDateSetListener(this);
    }

    //This allows to delete the contact we don't need.
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        dbCursor.moveToPosition(position);
        name = dbCursor.getString(dbCursor.getColumnIndex("name"));
        db.dbOperations("INSERT INTO deleted_users (name) VALUES (\"" + name + "\")");
        dbCursor = db.dbQuery(sqlSelectStatement);
        cursorAdapter.swapCursor(dbCursor);
        cursorAdapter.notifyDataSetChanged();
        return true;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String dateString = String.valueOf(year) + "-" + String.format("%02d", month + 1) + "-" + String.format("%02d", dayOfMonth);
        db.dbOperations("UPDATE users SET birthdate = \"" + dateString + "\" WHERE name = \"" + name + "\"");
        newCursor = db.dbQuery(sqlSelectStatement);
        cursorAdapter.getFilter().filter(filter);
        cursorAdapter.notifyDataSetChanged();
    }


}

