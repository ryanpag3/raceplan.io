package com.example.ryan.raceplanner;


import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class GenerateTrainingPlan extends AppCompatActivity
{
    private static final String TAG = GenerateTrainingPlan.class.getName();
    List<CalendarInfo> result = new ArrayList<>();
    List<String> namesOfCalendars = new ArrayList<>();
    Date date;
    Long calID;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_training_plan);

        // check position in code, moved up for debugging purposes
        // IF a boolean is set to true on GenerateTrainingPlan, call this activity
        Intent intent = new Intent(GenerateTrainingPlan.this, AuthenticateCalendarAPI.class);
        startActivity(intent);

        // grab date info from MainActivity
        date = getIntent().getExtras().getParcelable(GlobalVariables.DATE_OF_RACE_ID);

        // query list of calendars on device
        getCalendars();

        // create spinner and add calendars to it for selection
        generateCalendarSelectSpinner();

        // create switch and listener
        generateNewCalendarSwitch();

        // button generation
        generateCalendarConfirmButton();

    }

    /**
     *  Generates a list of CalendarInfo objects to be used if the user does not want to make a new
     *  calendar for their training plan.
     */
    public void getCalendars()
    {
        try
        {
            // See "Querying a Calendar"
            // https://developer.android.com/guide/topics/providers/calendar-provider.html
            String[] projection = new String[]{
                    Calendars._ID,
                    Calendars.NAME,
                    Calendars.ACCOUNT_NAME,
                    Calendars.ACCOUNT_TYPE
            };

            // ContentResolver receives a URI to a specific Content Provider
            // Content Providers provide an interface to query content
            // Cursors use ContentResolvers to iterate through
            ContentResolver cr = getContentResolver();
            Uri uri = Calendars.CONTENT_URI;
            Cursor calCursor = cr.query(uri, projection, null, null, null);

            while (calCursor.moveToNext())
            {
                result.add(new CalendarInfo(calCursor.getLong(0), calCursor.getString(1), calCursor.getInt(3)));
                namesOfCalendars.add(calCursor.getString(1));
            }
            calCursor.close();
        } catch (SecurityException e)
        {
            Log.e(TAG, "Permission Denied. Did you set permissions?");
        }
    }

    /**
     * Generates the spinner to select which calendar to export to.
     */
    private void generateCalendarSelectSpinner()
    {
        // create spinner and add calendars to it for selection
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, namesOfCalendars);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner calendarSelect = (Spinner) findViewById(R.id.calendarSelect);
        calendarSelect.setAdapter(adapter);
        calendarSelect.setOnItemSelectedListener(new OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                // set calendar to be edited with training plan
                switch (parent.getId())
                {
                    case R.id.calendarSelect:
                    {
                        TextView textView = (TextView) findViewById(R.id.textView);
                        textView.setText(result.get(position).id.toString() + " | " + result.get(position).name);
                        calID = result.get(position).id;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });
    }

    /**
     * Generates button that when selected will create the events.
     */
    private void generateCalendarConfirmButton()
    {
        Button confirmCalendarButton = (Button) findViewById(R.id.confirmCalendar);
        confirmCalendarButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // create dummy event for testing
                createEvent(GenerateTrainingPlan.this, calID, 0, 0, date);
            }
        });
    }

    /**
     * This creates a new event for the selected calendar. This should be put on an asyncronous
     * thread for optimization.
     * @param curActivity current activity
     * @param id the id of the calendar we are creating the event for
     * @param startM the start time of the event in Milliseconds
     * @param endM the end time of the event in Milliseconds
     * @param date the date of the event
     */
    public void createEvent(Activity curActivity, long id, long startM, long endM, Date date)
    {
        long calID = id;
        long startMillis = startM;
        long endMillis = endM;
        Date dateOfEvent = date;
        Calendar beginTime = Calendar.getInstance();
        beginTime.set(date.getYear(), date.getMonth(), date.getDay(), 0, 0);
        startMillis = beginTime.getTimeInMillis();
        Calendar endTime = Calendar.getInstance();
        endTime.set(date.getYear(), date.getMonth(), date.getDay(), 0, 0);
        endMillis = endTime.getTimeInMillis();

        ContentResolver cr = getContentResolver();
        ContentValues values = new ContentValues();
        values.put(Events.DTSTART, startMillis);
        values.put(Events.DTEND, endMillis);
        values.put(Events.TITLE, "Test Event");
        values.put(Events.DESCRIPTION, "Test Description");
        values.put(Events.CALENDAR_ID, calID);
        values.put(Events.EVENT_TIMEZONE, "America/Los_Angeles");
        try
        {
            // commented out to avoid unnecessary event additions
            //Uri uri = cr.insert(Events.CONTENT_URI, values);
            Log.i(TAG, "Event Created");

        } catch (SecurityException e)
        {
            Log.e(TAG, "Permission Denied");
        }
    }

    /**
     * holds the info of the calendars on the phone.
     */
    private class CalendarInfo
    {
        private Long id;
        private String name;
        private int color;

        private CalendarInfo(Long i, String n, int c)
        {
            this.id = i;
            this.name = n;
            this.color = c;
        }
    }
}
