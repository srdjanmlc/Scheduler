package com.example.scheduler.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/*using the applandeo calendar view instead af built in calendar view because it offers much more possibilities,
precisely a symbol can be set below the date in which we have meeting */
import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;
import com.example.scheduler.CalendarContractHelper;
import com.example.scheduler.DatabaseHelper;
import com.example.scheduler.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class FirstFragment extends Fragment {

    private ListView scheduleListView;
    private CoordinatorLayout coordinatorLayout;
    private CalendarContractHelper calendarContractHelper;
    private String account;
    private CalendarView calendarView;
    private FloatingActionButton fab;
    private HashMap<String, String> eventTitleMap;
    private ArrayList<String> eventTitle, eventFull;
    private ArrayAdapter<String> arrayAdapter;
    private List<EventDay> eventDayList;
    private Snackbar snackbar;




    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_main, container, false);

        account = this.getArguments().getString("Account_Name");
        scheduleListView = view.findViewById(R.id.scheduleListView);
        calendarView = view.findViewById(R.id.calendarView);
       // fab = view.findViewById(R.id.fab);
        coordinatorLayout = view.findViewById(R.id.coordinatorLayout);


        calendarContractHelper = new CalendarContractHelper(getActivity().getApplicationContext(), account);
        calendarView.setEvents(calendarContractHelper.queryAllEvents());

/*       setting on day click listener where I query the google calendar *with the help of mz custom CalendarContractHelper class)
        and then populate listview with the result . At the same time I am setting the date a the applandeo calendar.*/
        calendarView.setOnDayClickListener(new OnDayClickListener() {
            @Override
            public void onDayClick(EventDay eventDay) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(eventDay.getCalendar().getTime());
              //Here I have used HashMap as it is easier for me to do it like that.
              // I have pair of full name of the event and event title.
                eventTitleMap = new HashMap<>();
                eventTitleMap = calendarContractHelper.queryEvent(calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR));
                eventTitle = new ArrayList<>(eventTitleMap.values());
                eventFull = new ArrayList<>(eventTitleMap.keySet());
                arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, eventFull);
                scheduleListView.setAdapter(arrayAdapter);
            }
        });

        /*If I want to delete the event I set the long Click listener on Listview. At the same time
         I am deleting the event form the local database, google calendar and applandeo calendar.*/

        scheduleListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                calendarContractHelper.deleteEvent(eventTitle.get(position));
                deleteDatabaseEntry(eventFull.get(position));
                eventFull.remove(position);
                eventTitle.remove(position);
                arrayAdapter.notifyDataSetChanged();

                //Snackbar to inform the user of the action taken.
                snackbar = Snackbar.make(coordinatorLayout, "Entry deleted!", Snackbar.LENGTH_LONG);
                snackbar.show();
                return true;
            }
        });


        return view;

    }

    public void deleteDatabaseEntry(String fullEventName){

        DatabaseHelper db;
        String[] parsedString;
        String startTime, titleName,nameId;
        Cursor dbCursor;
        parsedString = fullEventName.split("[:\n]+");
        titleName = parsedString[1].replaceFirst(" ","");
        startTime = parsedString[3].replaceFirst(" ","") + ":" + parsedString[4];

        db = new DatabaseHelper(getContext(),"Contacts");
        dbCursor = db.dbQuery("select id from users where name = \"" + titleName + "\"");
        dbCursor.moveToFirst();
        nameId = dbCursor.getString(dbCursor.getColumnIndex("id"));
        db.dbOperations("DELETE from appointments where (nameid="+ Integer.valueOf(nameId) +" and appointmentdate =\"" + startTime + "\")");

    }

}

