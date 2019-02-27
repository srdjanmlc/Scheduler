package com.example.scheduler.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TimePicker;

import com.example.scheduler.CalendarContractHelper;
import com.example.scheduler.DatabaseHelper;
import com.example.scheduler.R;

import java.util.Calendar;

public class AddMeetingFragment extends Fragment {


    private AutoCompleteTextView autoCompleteTextView;
    private ListView listView;
    private ImageView imageView;
    private DatabaseHelper db;
    private Cursor dbCursor;
    private SimpleCursorAdapter cursorAdapter;
    private SimpleCursorAdapter listCursorAdapter;
    private String name;
    private StringBuffer dateTimeString;
    private String sqlSelect;
    private String account;
    private String eventTitle;
    private int eventDay, eventMonth, eventYear,eventHour,eventMinute;
    private CalendarContractHelper calendarContractHelper;
    private String[] fromColumns;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_meeting, container, false);
        account = this.getArguments().getString("Account_Name");

        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        autoCompleteTextView = view.findViewById(R.id.autoCompleteTextView);
        imageView = view.findViewById(R.id.contactimageView);
        listView = view.findViewById(R.id.resultListView);

        db = new DatabaseHelper(getContext(), "Contacts");
        calendarContractHelper = new CalendarContractHelper(getContext(), account);



        fromColumns = new String[]{"name"};
        int[] toViews = {R.id.nameTextView};
        cursorAdapter = new SimpleCursorAdapter(getContext(), R.layout.item_autocompletetv, dbCursor, fromColumns, toViews, 0);
        autoCompleteTextView.setAdapter(cursorAdapter);
        autoCompleteTextView.setDropDownWidth(ViewGroup.LayoutParams.WRAP_CONTENT);

        //First calling setFilterQueryProvider. This callback, FilterQueryProvider’s
        // runQuery function is executed everytime when we change something in AutoCompleteTextView.
        // Actually this is the main point.
        cursorAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            public Cursor runQuery(CharSequence str) {
                return getCursor(str);
            }
        });

        /*Secondly, calling setCursorToStringConverter, it is used when we select item, it executes
        this function to convert cursor to String.*/
        cursorAdapter.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {public CharSequence convertToString(Cursor cur) {
                int index = cur.getColumnIndex("name");
                return cur.getString(index);
            }
        });

        /*When I select a name in autocomplete view I get dialog view to enter the new meeting date and
          at the same time list view is populated with all of the previous meeting I had with that person.
          In that way I can have info when was the last meeting and how many meeting we had.
        */
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listAppointments(view);
                newMeeting(view);
            }
        });


    }

    //Lastly, one missing point, we haven’t share the getCursor(CharSequence str) function.
    public Cursor getCursor(CharSequence str) {
        String sqlSelectStatement = "SELECT users.id _id, users.name, users.birthdate FROM users " +
                "LEFT JOIN deleted_users ON deleted_users.name = users.name " +
                "WHERE ((deleted_users.name IS NULL) AND users.name LIKE  \"%" + str + "%\") ORDER BY users.name ASC";
        dbCursor = db.dbQuery(sqlSelectStatement);
        return dbCursor;
    }

    public void listAppointments(View view) {
        imageView.setVisibility(View.VISIBLE);
        InputMethodManager manager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
        name = autoCompleteTextView.getText().toString();
        sqlSelect = "SELECT b.id _id, b.appointmentdate appointmentdate " +
                "FROM appointments b, (SELECT a.id _id, a.name FROM users a WHERE a.name = \"" + name + "\") c " +
                "WHERE b.nameid = c._id ORDER BY b.appointmentdate DESC";
        dbCursor = db.dbQuery(sqlSelect);
        String[] fromColumns = new String[]{"appointmentdate"};
        int[] toViews = {R.id.nameTextView};
        listCursorAdapter = new SimpleCursorAdapter(getContext(), R.layout.item_appointment_list, dbCursor, fromColumns, toViews, 0);
        listView.setAdapter(listCursorAdapter);
    }

    public void newMeeting(View view) {
        eventTitle = autoCompleteTextView.getText().toString();
        datePickerDialog();
    }

    public void datePickerDialog() {

        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        dateTimeString = new StringBuffer();

        final DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), android.R.style.Theme_Holo_Dialog, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                dateTimeString.append(String.format("%02d", dayOfMonth));
                dateTimeString.append("/");
                dateTimeString.append(String.format("%02d", month + 1));
                dateTimeString.append("/");
                dateTimeString.append(year);
                eventDay = dayOfMonth;
                eventMonth = month;
                eventYear = year;
                timePickerDialog(c);
            }
        }, year, month, day);
        datePickerDialog.show();
    }

    public void timePickerDialog(Calendar calendar) {

        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), android.R.style.Theme_Holo_Dialog, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                eventHour = hourOfDay;
                eventMinute = minute;
                dateTimeString.append(" ");
                dateTimeString.append(String.format("%02d", hourOfDay));
                dateTimeString.append(":");
                dateTimeString.append(String.format("%02d", minute));
                Log.i("Time info", String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute));
                db.dbOperations("INSERT INTO appointments (nameid, appointmentdate) VALUES ((SELECT a.id _id FROM users a WHERE a.name = \"" + name + "\"),\"" + dateTimeString.toString() + "\")");
                Cursor newCursor = db.dbQuery("SELECT b.id _id, b.appointmentdate appointmentdate " +
                        "FROM appointments b, (SELECT a.id _id, a.name FROM users a WHERE a.name = \"" + name + "\") c " +
                        "WHERE b.nameid = c._id ORDER BY b.appointmentdate DESC");
                calendarContractHelper.addEvent(eventTitle, eventDay, eventMonth, eventYear, eventHour, eventMinute);
                listCursorAdapter.swapCursor(newCursor);
                listCursorAdapter.notifyDataSetChanged();
            }
        }, hours, minutes, true);
        timePickerDialog.show();

    }

   /*In onStop lifecycle of the fragment I am clearing the autocomplete view.
    Because if I don't do this and navigate away from the fragment next time I return to it autocomplete
    will remain filled but the list view will be empty and don't want that.
    */
    @Override
    public void onStop() {
        super.onStop();
        autoCompleteTextView.setText("");
    }
}
