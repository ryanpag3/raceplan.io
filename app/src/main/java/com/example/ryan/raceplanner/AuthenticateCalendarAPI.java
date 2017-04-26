package com.example.ryan.raceplanner;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;


public class AuthenticateCalendarAPI extends Activity
{
    private static final String[] SCOPES = { CalendarScopes.CALENDAR };
    private static final String TAG = AuthenticateCalendarAPI.class.getName();
    GoogleAccountCredential mCredential;
    ProgressDialog mProgress;
    String calID;
    Boolean buttonPressed = false;
    Boolean calCreated = false;
    RacerInfo racerInfo;
    Date raceDate;
    private TextView mOutputText;
    DatabaseHelper db;

    CalendarTask c;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authenticate_calendar_api);

        racerInfo  = getIntent().getExtras().getParcelable(GlobalVariables.RACER_INFO_ID);

        Log.i(TAG, racerInfo.nameOfPlan);
        db = new DatabaseHelper(this);
        c = new CalendarTask(mCredential, racerInfo, this);

        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.YEAR, racerInfo.year);
        cal.set(java.util.Calendar.MONTH, racerInfo.month);
        cal.set(java.util.Calendar.DAY_OF_MONTH, racerInfo.day);
        raceDate = cal.getTime();

        mOutputText = (TextView) findViewById(R.id.mOutputText);
        mOutputText.setMovementMethod(new ScrollingMovementMethod());
        mOutputText.setText("");

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Calling Google Calendar API ...");

        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        mCredential.setSelectedAccountName(getIntent().getExtras().getString(GlobalVariables.CREDENTIAL_ACCOUNT_NAME));

        final Button buttonCreateCal = (Button) findViewById(R.id.button_create_calendar);
        buttonCreateCal.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                    try
                    {
                        createCalendar();
                        buttonPressed = true;
                    } catch (IOException e)
                    {
                    }
            }
        });

        Button buttonDeleteCal = (Button) findViewById(R.id.button_delete_calendar);
        buttonDeleteCal.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                    try
                    {
                        deleteCalendar();
                        buttonPressed = true;
                    } catch (IOException e)
                    {
                        Log.e(TAG, "IOException" + e.toString());
                    }
            }
        });

        Button buttonCreateEvent = (Button) findViewById(R.id.button_create_training_plan);
        buttonCreateEvent.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                    try
                    {
                        createTrainingPlan();
                        buttonPressed = true;
                    } catch (IOException e)
                    {
                        Log.e(TAG, "IOException" + e.toString());
                    }

            }
        });

        Button buttonDeleteTrainingPlan = (Button) findViewById(R.id.button_delete_training_plan);
        buttonDeleteTrainingPlan.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                    try
                    {
                        deleteTrainingPlan();
                        buttonPressed = true;
                    } catch (IOException e)
                    {
                        Log.e(TAG, "IOException" + e.toString());
                    }
            }
        });

        Button buttonFinishActivity = (Button) findViewById(R.id.button_finish_authenticate_api);
        buttonFinishActivity.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(AuthenticateCalendarAPI.this, GenerateTrainingPlan.class);
                intent.putExtra(GlobalVariables.RACER_INFO_ID, getIntent().getExtras().getParcelable(GlobalVariables.RACER_INFO_ID));
                intent.putExtra(GlobalVariables.CALENDAR_CREATED_ID, calCreated);
                intent.putExtra(GlobalVariables.CALENDAR_ID, calID);
                startActivity(intent);
            }
        });


        // FOR DEBUGGING
        Button buttonQueryDatabase = (Button) findViewById(R.id.button_query_database);
        buttonQueryDatabase.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Cursor c = db.query("SELECT * FROM " + DatabaseHelper.TRAINING_PLAN_TABLE_NAME, null);
                while (c.moveToNext())
                {
                    Log.i(TAG, c.getString(0) + " " + c.getString(1));
                }

                c = db.query("SELECT * FROM " + DatabaseHelper.EVENT_ID_TABLE_NAME, null);
                while (c.moveToNext())
                {
                    Log.i(TAG, c.getString(0) + " " + c.getString(1));
                }
                c.close();

                c = db.query("SELECT * FROM " + DatabaseHelper.TRAINING_PLAN_TABLE_NAME, null);
                while (c.moveToNext())
                {
                    Log.i(TAG, c.getString(0) + " " + c.getString(1));
                }
                c.close();
            }
        });

        Button buttonDeleteAllTestPlans = (Button) findViewById(R.id.button_delete_all_test_plans);
        buttonDeleteAllTestPlans.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                db.deleteDatabases();
            }
        });
        // DEBUGGING END
    }

    /**
     * If prereqs are met, calls a new AsyncTask with proper ID
     */
    private void createCalendar() throws IOException
    {
            new CalendarTask(mCredential, racerInfo, this).execute("createCalendar");
            mOutputText.setText("Calendar created");
    }


    /**
     * If prereqs are met, calls a new AsyncTask with proper ID
     */
    private void deleteCalendar() throws IOException
    {
            new CalendarTask(mCredential, racerInfo, this).execute("deleteCalendar");
            mOutputText.setText("Calendar deleted");

    }

    /**
     * If prereqs are met, calls a new AsyncTask with proper ID
     */
    private void createTrainingPlan() throws IOException
    {
            new CalendarTask(mCredential, racerInfo, this).execute("createTrainingPlan");
            //new CalendarTask(mCredential, racerInfo).execute(R.id.button_create_training_plan);
            mOutputText.setText("Training plan created.");

    }

    /**
     * If prereqs are met, calls a new AsyncTask with proper ID
     */
    private void deleteTrainingPlan() throws IOException
    {
            new CalendarTask(mCredential, racerInfo, this).execute("deleteTrainingPlan");
            //new CalendarTask(mCredential, racerInfo).execute(R.id.button_delete_training_plan);
            mOutputText.setText("Training plan events deleted.");
    }
}
