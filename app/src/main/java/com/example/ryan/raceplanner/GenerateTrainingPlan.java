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
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class GenerateTrainingPlan extends AppCompatActivity
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

        ContentResolver cr = getContentResolver();
        Uri uri = Calendars.CONTENT_URI;
        Cursor calCursor = cr.query(uri, projection, null, null, null);
        List<CalendarInfo> result = new ArrayList<>();


        while(calCursor.moveToNext())
        {
            result.add(new CalendarInfo(calCursor.getLong(0), calCursor.getString(1), calCursor.getInt(3)));
        }

        calCursor.close();

        StringBuilder builder = new StringBuilder();
        for (CalendarInfo c : result)
        {
            builder.append(c.name + ", ");
        }

        // pushes calendar names to dummy textView for debug purposes
        TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText(builder);

    }

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
