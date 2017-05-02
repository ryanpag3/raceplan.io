package com.ryan.page.raceplanner;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


public class AuthenticateCalendarAPI extends Activity
{
    private com.google.api.services.calendar.Calendar mService = null;
    private static final String[] SCOPES = { CalendarScopes.CALENDAR };
    private static final String TAG = AuthenticateCalendarAPI.class.getName();
    GoogleAccountCredential mCredential;
    ProgressDialog mProgress;
    String calID;
    Boolean buttonPressed = false;
    Boolean createCal = false;
    RacerInfo racerInfo;
    Date raceDate;
    private TextView mOutputText;
    Button buttonCreatePlanOnSelected;
    Spinner calendarSelect;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authenticate_calendar_api);

        racerInfo  = getIntent().getExtras().getParcelable(GlobalVariables.RACER_INFO_ID);
        createCal = getIntent().getExtras().getBoolean(GlobalVariables.CREATE_CALENDAR_BOOL);
        calendarSelect = (Spinner) findViewById(R.id.spinner_calendar_select_2);
        calendarSelect.setVisibility(View.GONE);


        Log.i(TAG, racerInfo.nameOfPlan);

        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();



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

        mService = new com.google.api.services.calendar.Calendar.Builder(
                transport, jsonFactory, mCredential)
                .setApplicationName("race-planner")
                .build();


        if (createCal)
        {
            try
            {
                createCalendar();
                createTrainingPlan();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        } else
        {
            calendarSelect.setVisibility(View.VISIBLE);
            chooseCalendar();
        }

        buttonCreatePlanOnSelected = (Button) findViewById(R.id.button_create_calendar_on_selected);
        buttonCreatePlanOnSelected.setVisibility(View.GONE);
        buttonCreatePlanOnSelected.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    createCustomTrainingPlan();
                } catch (IOException e)
                {
                    Log.e(TAG, e.getMessage());
                }
            }
        });


        Button buttonFinishActivity = (Button) findViewById(R.id.button_finish_authenticate_api);
        buttonFinishActivity.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(AuthenticateCalendarAPI.this, MainActivity.class);
                intent.putExtra(GlobalVariables.RACER_INFO_ID, getIntent().getExtras().getParcelable(GlobalVariables.RACER_INFO_ID));
                intent.putExtra(GlobalVariables.CALENDAR_CREATED_ID, createCal);
                intent.putExtra(GlobalVariables.CALENDAR_ID, calID);
                startActivity(intent);
            }
        });
    }

    private void chooseCalendar()
    {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>()
        {
            List<String> names = new ArrayList<>();
            List<String> calendarIDs = new ArrayList<>();

            @Override
            protected Void doInBackground(Void... params)
            {
                List<CalendarListEntry> items = new ArrayList<>();

                if(android.os.Debug.isDebuggerConnected())
                    android.os.Debug.waitForDebugger();
                try
                {
                    String pageToken = null;
                    do
                    {
                        CalendarList calendarList = mService.calendarList().list().setPageToken(pageToken).execute();
                        items = calendarList.getItems();

                        for (CalendarListEntry calendarListEntry : items)
                        {
                            names.add(calendarListEntry.getSummary());
                            calendarIDs.add(calendarListEntry.getId());
                            Log.i(TAG, calendarListEntry.getSummary());
                        }
                        pageToken = calendarList.getNextPageToken();
                    } while (pageToken != null);
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
                return null;
            }

            protected void onPostExecute(Void result)
            {
                Log.i(TAG, String.valueOf(names.size()));
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(AuthenticateCalendarAPI.this, android.R.layout.simple_spinner_item, names);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                calendarSelect = (Spinner) findViewById(R.id.spinner_calendar_select_2);
                calendarSelect.setAdapter(adapter);
                calendarSelect.setEnabled(true);
                calendarSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
                {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
                    {
                        racerInfo.calendarName = names.get(position);
                        racerInfo.calendarID = calendarIDs.get(position);
                        buttonCreatePlanOnSelected.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent)
                    {

                    }
                });

            }
        };

        task.execute();
    }

    /**
     * If prereqs are met, calls a new AsyncTask with proper ID
     */
    private void createCalendar() throws IOException
    {
            new CalendarTask(mCredential, racerInfo, this).execute("createCalendar");
    }


    /**
     * If prereqs are met, calls a new AsyncTask with proper ID
     */
    private void deleteCalendar() throws IOException
    {
            new CalendarTask(mCredential, racerInfo, this).execute("deleteCalendar");

    }

    /**
     * If prereqs are met, calls a new AsyncTask with proper ID
     */
    private void createTrainingPlan() throws IOException
    {
            new CalendarTask(mCredential, racerInfo, this).execute("createTrainingPlan");

    }

    private void createCustomTrainingPlan() throws  IOException
    {
        new CalendarTask(mCredential, racerInfo, this).execute("createCustomTrainingPlan");
    }

    /**
     * If prereqs are met, calls a new AsyncTask with proper ID
     */
    private void deleteTrainingPlan() throws IOException
    {
            new CalendarTask(mCredential, racerInfo, this).execute("deleteTrainingPlan");
    }
}
