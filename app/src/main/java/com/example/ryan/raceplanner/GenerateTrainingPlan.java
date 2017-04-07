package com.example.ryan.raceplanner;


import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract.*;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.AdapterView.*;

import java.util.ArrayList;
import java.util.List;

/**
 *  See Trello for TO-DO
 */

public class GenerateTrainingPlan extends AppCompatActivity implements OnItemSelectedListener
{
    private static final int MY_PERMISSIONS_REQUEST_READ_CALENDAR = 1;

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
        List<CalendarInfo> result = new ArrayList<>();
        List<String> namesOfCalendars = new ArrayList<>();

        // iterate through query
        while(calCursor.moveToNext())
        {
            result.add(new CalendarInfo(calCursor.getLong(0), calCursor.getString(1), calCursor.getInt(3)));
            namesOfCalendars.add(calCursor.getString(1));
        }
        calCursor.close();

        // create spinner and add calendars to it for selection
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(GenerateTrainingPlan.this, android.R.layout.simple_spinner_item, namesOfCalendars);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner calendarSelect = (Spinner) findViewById(R.id.calendarSelect);
        calendarSelect.setAdapter(adapter);

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

    @Override
    public void onItemSelected(AdapterView<?> parent, View v, int pos, long id)
    {
        // set calendar to be edited with training plan
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {
        // TODO?
    }
}
