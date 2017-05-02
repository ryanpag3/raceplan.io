package com.ryan.page.raceplanner;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;

import android.text.format.DateFormat;

public class ListTrainingPlans extends AppCompatActivity
{
    private static final String[] SCOPES = { CalendarScopes.CALENDAR };
    private final String TAG = this.getClass().getSimpleName();
    GoogleAccountCredential mCredential;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_training_plans);

        // instantiate credential object used for API authentication
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        // instantiate DatabaseHelper object for accessing sqlite databases
        DatabaseHelper db = new DatabaseHelper(this);

        // cursor object to be able to iterate through db
        Cursor c = db.query("SELECT * FROM " + DatabaseHelper.TRAINING_PLAN_TABLE_NAME, null);

        // listView for displaying created training plans
        listView = (ListView) findViewById(R.id.list_view_created_plans);
        // MyCursorAdapter holds the custom layout for each training plan
        final MyCursorAdapter cursorAdapter = new MyCursorAdapter(this, c);
        listView.setAdapter(cursorAdapter); // ties the custom adapter to the listview

        // store account name as intent for next activity
        mCredential.setSelectedAccountName(getIntent().getExtras().getString(GlobalVariables.CREDENTIAL_ACCOUNT_NAME));

        // instantiate button for refreshing listview
        Button refreshButton = (Button) findViewById(R.id.button_refresh);
        refreshButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                DatabaseHelper db = new DatabaseHelper(ListTrainingPlans.this);
                Cursor t = db.query("SELECT * FROM " + DatabaseHelper.TRAINING_PLAN_TABLE_NAME, null);
                cursorAdapter.changeCursor(t);
            }
        });

    }


    /**
     * adapter for custom listview layout
     */
    private class MyCursorAdapter extends CursorAdapter
    {
        // override super constructor
        public MyCursorAdapter(Context context, Cursor c)
        {
            super(context, c, 0);
        }

        // inflates the custom layout to the current parent listview and context
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent)
        {
            return LayoutInflater.from(context).inflate(R.layout.layout_training_plan_row, parent, false);
        }

        // binds data to the custom layout
        @Override
        public void bindView(View view, Context context, Cursor cursor)
        {
            TextView tvName = (TextView) view.findViewById(R.id.row_text_plan_name);
            TextView tvRaceDate = (TextView) view.findViewById(R.id.row_text_race_date);
            TextView tvRaceType = (TextView) view.findViewById(R.id.row_text_race_type);
            TextView tvExperienceLevel = (TextView) view.findViewById(R.id.row_text_experience_level);
            TextView tvCalendar = (TextView) view.findViewById(R.id.row_text_calendar);
            Button bDeleteTrainingPlan = (Button) view.findViewById(R.id.row_button_delete_training_plan);
            // assigns values based on cursor object position
            final int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.TRAINING_PLAN_COL_1));
            final String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.TRAINING_PLAN_COL_2));
            final String raceDate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.TRAINING_PLAN_COL_3));
            final String raceType = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.TRAINING_PLAN_COL_4));
            final String experienceLevel = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.TRAINING_PLAN_COL_5));
            String calendar = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.TRAINING_PLAN_COL_6));

            // assign values to ui elements
            tvName.setText(name);
            tvRaceDate.setText(raceDate);
            tvRaceType.setText(raceType);
            tvExperienceLevel.setText(experienceLevel);
            tvCalendar.setText(calendar);
            bDeleteTrainingPlan.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    // parse date string
                    DatabaseHelper db = new DatabaseHelper(ListTrainingPlans.this);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = new Date();
                    try
                    {
                        date = dateFormat.parse(raceDate);
                    } catch (ParseException e)
                    {
                        e.printStackTrace();
                    }

                    // create new RacerInfo object to pass to AsyncTask
                    int year = Integer.parseInt((String) DateFormat.format("yyyy", date));
                    int month = Integer.parseInt((String) DateFormat.format("MM", date));
                    int day = Integer.parseInt((String) DateFormat.format("dd", date));

                    RacerInfo racerInfo = new RacerInfo(year, month, day, raceType, experienceLevel, name, id);
                    // creates new asynctask for deleting a training plan
                    new CalendarTask(mCredential, racerInfo, ListTrainingPlans.this).execute("deleteTrainingPlan");
                    // deletes plan from the database
                    db.deletePlanFromDatabase(id);
                }
            });
        }
    }
}
