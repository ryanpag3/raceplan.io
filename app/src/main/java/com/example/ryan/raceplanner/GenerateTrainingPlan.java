package com.example.ryan.raceplanner;


import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract.*;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.AdapterView.*;
import android.widget.TextView;

import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import com.example.ryan.raceplanner.MainActivity.*;

/**
 *  See Trello for TO-DO
 */

public class GenerateTrainingPlan extends AppCompatActivity
{
    private static final int MY_PERMISSIONS_REQUEST_READ_CALENDAR = 1;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_CALENDAR = 2;
    List<CalendarInfo> result = new ArrayList<>();
    List<String> namesOfCalendars = new ArrayList<>();
    Date date;
    Long calID;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_training_plan);

        // Get READ_CALENDAR permissions
        if (ContextCompat.checkSelfPermission(GenerateTrainingPlan.this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(GenerateTrainingPlan.this,
                    new String[]{Manifest.permission.READ_CALENDAR},
                    MY_PERMISSIONS_REQUEST_READ_CALENDAR);
        }

        if (ContextCompat.checkSelfPermission(GenerateTrainingPlan.this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(GenerateTrainingPlan.this,
                    new String[]{Manifest.permission.WRITE_CALENDAR},
                    MY_PERMISSIONS_REQUEST_WRITE_CALENDAR);
        }

        date = (Date) getIntent().getExtras().getParcelable(GlobalVariables.DATE_OF_RACE_ID);

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

        // iterate through query
        while(calCursor.moveToNext())
        {
            result.add(new CalendarInfo(calCursor.getLong(0), calCursor.getString(1), calCursor.getInt(3)));
            namesOfCalendars.add(calCursor.getString(1));
        }
        calCursor.close();

        // create spinner and add calendars to it for selection
        generateCalendarSelectSpinner();

        // button generation
        generateCalendarConfirmButton();

    }

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


    public void createEvent(Activity curActivity, long id, long startM, long endM, Date date)
    {
        long calID = id;
        long startMillis = startM;
        long endMillis = endM;
        Date dateOfEvent = date;
        Calendar beginTime = Calendar.getInstance();
        beginTime.set(date.getYear(), date.getMonth(), date.getDay(), 0,0);
        startMillis = beginTime.getTimeInMillis();
        Calendar endTime= Calendar.getInstance();
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
            Uri uri = cr.insert(Events.CONTENT_URI, values);
        } catch (SecurityException e)
        {
            Log.e("PermissionDenied", "Permission Denied");
        }
        Log.i("createEvent", "Event Created");

    }

    // Idk if there's a better way to do this.
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
