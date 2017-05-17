package com.race.planner.activities;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import com.race.planner.R;
import com.race.planner.fragments.DisplayFinalInfo;
import com.race.planner.utils.*;
import com.race.planner.data_models.GlobalVariables;
import com.race.planner.data_models.Racer;

public class AuthenticateAndCallAPI extends Activity implements EasyPermissions.PermissionCallbacks, FragmentListenerInterface
{
    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    private static final String PREF_ACCOUNT_NAME = "accountName";

    private com.google.api.services.calendar.Calendar mService = null;
    private static final String[] SCOPES = {CalendarScopes.CALENDAR};
    private static final String TAG = AuthenticateAndCallAPI.class.getName();

    private int TASK_ID = -1;
    private static final int CREATE_CALENDAR_ID = 1;
    private static final int CHOOSE_CALENDAR_ID = 2;
    private static final int CREATE_PLAN_ID = 3;

    GoogleAccountCredential mCredential;
    ProgressDialog mProgressBarDialog;
    ProgressDialog mProgressCircle;
    Boolean createCal = false;
    Racer racer;
    Date raceDate;
    Button buttonCreatePlanOnSelected;
    Spinner calendarSelect;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authenticate_calendar_api);

        racer = getIntent().getExtras().getParcelable(GlobalVariables.RACER_INFO_ID);
        createCal = getIntent().getExtras().getBoolean(GlobalVariables.CREATE_CALENDAR_BOOL);
        calendarSelect = (Spinner) findViewById(R.id.spinner_calendar_select_2);
        calendarSelect.setVisibility(View.INVISIBLE);
        // set task ID for callAPI switch
        // create calendar == 1
        // choose calendar == 2
        if (createCal)
        {
            TASK_ID = CREATE_CALENDAR_ID;
        } else
        {
            //calendarSelect.setVisibility(View.VISIBLE);
            TASK_ID = CHOOSE_CALENDAR_ID;
        }

        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.YEAR, racer.year);
        cal.set(java.util.Calendar.MONTH, racer.month);
        cal.set(java.util.Calendar.DAY_OF_MONTH, racer.day);
        raceDate = racer.date;

        mProgressCircle = new ProgressDialog(this);
        mProgressCircle.setMessage("Creating calendar...");

        mProgressBarDialog = new ProgressDialog(this);
        mProgressBarDialog.setMessage("Generating training runs...");
        mProgressBarDialog.setIndeterminate(false);
        mProgressBarDialog.setProgressNumberFormat(null);
        mProgressBarDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressBarDialog.setProgress(0);


        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());



        Button buttonUndo = (Button) findViewById(R.id.button_undo);
        buttonUndo.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                DatabaseHelper db = new DatabaseHelper(AuthenticateAndCallAPI.this);
                // deletes training plan based on racer info
                new DeleteTrainingPlanTask(mCredential, racer, AuthenticateAndCallAPI.this).execute();
                // deletes plan from the database by database id
                db.deletePlanFromDatabase(racer.databaseID);
            }
        });

        Button buttonMainMenu = (Button) findViewById(R.id.button_main_menu);
        buttonMainMenu.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(AuthenticateAndCallAPI.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra(GlobalVariables.CREDENTIAL_ACCOUNT_NAME, mCredential.getSelectedAccountName());
                startActivity(intent);
            }
        });

        // TASK_ID is set and UI is created, ready to begin API calls.
        callAPI();
    }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount()
    {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS))
        {
            String accountName = PreferenceManager.getDefaultSharedPreferences(this)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null)
            {
                mCredential.setSelectedAccountName(accountName);
                Log.e(TAG, "Create Calendar inside chooseAccount called");
                callAPI();

            } else
            {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else
        {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     *
     * @param requestCode  The request code passed in
     *                     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     *
     * @param requestCode The request code associated with the requested
     *                    permission
     * @param list        The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list)
    {
        // do nothing
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     *
     * @param requestCode The request code associated with the requested
     *                    permission
     * @param list        The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list)
    {
        // Do nothing.
    }

    /**
     * callAPI runs a series of checks and finally calls an AsyncTask based on the value of TASK_ID
     * TASK_ID is adjusted in OnCreate based on the choice of the user to create a new calendar or not.
     */
    private void callAPI()
    {
        if (!isGooglePlayServicesAvailable())
        {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null)
        {
            chooseAccount();
        } else if (!isDeviceOnline())
        {
            Log.e(TAG, "No network connection available...");
        } else
        {
            Log.e(TAG, "Call API executed...");
            //
            switch (TASK_ID)
            {
                case CREATE_CALENDAR_ID:
                    new CreateCalendarTask(mCredential).execute();
                    break;
                case CHOOSE_CALENDAR_ID:
                    new ChooseCalendarTask(mCredential).execute();
                    break;

            }

        }
    }


    /**
     * These methods are overloaded from the ListenerInterface. It might be more efficient to
     * create new listener for just the FinalInfoFragment so you dont have all this code bloat.
     */
    @Override
    public void moveProgressIconLeft()
    {
        // do nothing
    }

    @Override
    public void moveProgressIconRight()
    {
        // do nothing
    }

    @Override
    public void onFragmentClicked(String s)
    {
        // do nothing
    }

    @Override
    public void onBackButtonClicked()
    {
        // do nothing
    }

    @Override
    public void passName(String n)
    {
        // do nothing
    }

    @Override
    public void passRaceType(String s)
    {
        // do nothing
    }

    @Override
    public void passDate(Date d)
    {
        // do nothing
    }

    @Override
    public void passExperienceLevel(String e)
    {
        // do nothing
    }

    @Override
    public void passCalCreatedBool(Boolean b)
    {
        // do nothing
    }

    /**
     * Asynchronous task for choosing a calendar to create a plan on.
     */
    public class ChooseCalendarTask extends AsyncTask<Void, Void, Void>
    {
        List<String> names = new ArrayList<>();
        List<String> calendarIDs = new ArrayList<>();

        ChooseCalendarTask(GoogleAccountCredential credential)
        {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("race-planner")
                    .build();
        }

        @Override
        protected Void doInBackground(Void... params)
        {
            List<CalendarListEntry> items;

            if (android.os.Debug.isDebuggerConnected())
                android.os.Debug.waitForDebugger();
            // needs abstraction
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
            AlertDialog.Builder b = new AlertDialog.Builder(AuthenticateAndCallAPI.this);
            b.setTitle("Select Calendar");
            String[] sNames = new String[names.size()];
            sNames = names.toArray(sNames);

            b.setItems(sNames, new DialogInterface.OnClickListener()
            {

                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.dismiss();
                    racer.calendarName = names.get(which);
                    racer.calendarID = calendarIDs.get(which);
                    new CreatePlanTask(mCredential).execute();
                }

            });

            b.show();
        }
    }

    public class CreateCalendarTask extends AsyncTask<Void, Void, Void>
    {
        private com.google.api.services.calendar.Calendar mService = null;
        private Exception mLastError = null;

        CreateCalendarTask(GoogleAccountCredential credential)
        {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("race-planner")
                    .build();
        }

        @Override
        protected Void doInBackground(Void... params)
        {
            if (android.os.Debug.isDebuggerConnected())
                android.os.Debug.waitForDebugger();

            try
            {
                checkForDeprecatedCalendars();

                if (!isRacePlanCalendarCreated())
                {
                    // TODO: turn calendarname and summary into hardcoded string value
                    Log.e(TAG, "createCalendarInAPI called.");
                    // BUG: when removing library definition and adding an import, calls the wrong constructor
                    // WORKAROUND: defined all manually
                    com.google.api.services.calendar.model.Calendar calendar = new com.google.api.services.calendar.model.Calendar();
                    calendar.setSummary(GlobalVariables.DEFAULT_CAL_NAME); // GlobalVariables
                    calendar.setTimeZone("America/Los_Angeles");
                    com.google.api.services.calendar.model.Calendar createdCalendar = mService.calendars().insert(calendar).execute();

                    racer.calendarID = createdCalendar.getId();
                    racer.calendarName = GlobalVariables.DEFAULT_CAL_NAME;
                }
            } catch (Exception e)
            {
                mLastError = e;
                cancel(true);
                return null;
            }

            return null;
        }

        /**
         * Checks for outdated versions of the calendar and deletes if necessary
         */
        private void checkForDeprecatedCalendars()
        {
            String pageToken = null;
            do
            {
                try
                {
                    CalendarList calendarList = mService.calendarList().list().setPageToken(pageToken).execute();
                    List<CalendarListEntry> items = calendarList.getItems();

                    for (CalendarListEntry calendarListEntry : items)
                    {
                        if (calendarListEntry.getSummary().equals("race-planner"))
                        {
                            mService.calendars().delete(calendarListEntry.getId()).execute();
                        }

                        if (calendarListEntry.getSummary().equals("run-planner"))
                        {
                            mService.calendars().delete(calendarListEntry.getId()).execute();
                        }
                    }
                    pageToken = calendarList.getNextPageToken();
                } catch (IOException e)
                {
                    Log.e(TAG, "IOException:", e);
                }
            } while (pageToken != null);

        }

        public boolean isRacePlanCalendarCreated()
        {

            // iterate through entries in calendar list
            String pageToken = null;
            do
            {
                try
                {
                    CalendarList calendarList = mService.calendarList().list().setPageToken(pageToken).execute();
                    List<CalendarListEntry> items = calendarList.getItems();

                    for (CalendarListEntry calendarListEntry : items)
                    {
                        if (calendarListEntry.getSummary().equals(GlobalVariables.DEFAULT_CAL_NAME))
                        {

                            racer.calendarID = calendarListEntry.getId();
                            racer.calendarName = GlobalVariables.DEFAULT_CAL_NAME;
                            Log.e(TAG, "inside israceplancreated: " + racer.calendarID);
                            return true;
                        }
                    }
                    pageToken = calendarList.getNextPageToken();
                } catch (IOException e)
                {

                }
            } while (pageToken != null);
            Log.i(TAG, "isRacePlannerCrated returned false");
            return false;
        }

        @Override
        protected void onPreExecute()
        {
            // set ui as non-intractable
            mProgressCircle.setCancelable(false);
            mProgressCircle.show();


        }

        @Override
        protected void onPostExecute(Void output)
        {
            // set ui as interactable
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            mProgressCircle.hide();

            // set callAPI to create training plan now
            TASK_ID = CREATE_PLAN_ID;

            // display completed toast
            Toast toast = Toast.makeText(AuthenticateAndCallAPI.this, "Calendar created successfully!", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER | Gravity.TOP, 0, 0);
            toast.show();

            new CreatePlanTask(mCredential).execute();
        }

        @Override
        protected void onCancelled()
        {
            // make ui interactable again
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            mProgressBarDialog.hide();
            if (mLastError != null)
            {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException)
                {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException)
                {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            MainActivity.REQUEST_AUTHORIZATION);
                } else
                {
                    Log.e(TAG, "The following error occurred:\n"
                            + mLastError.getMessage());
                }
            } else
            {
                Log.e(TAG, "Request cancelled.");
            }
        }
    }

    private class CreatePlanTask extends AsyncTask<Void, Void, Void>
    {
        private com.google.api.services.calendar.Calendar mService = null;
        private Exception mLastError = null;
        DatabaseHelper db;
        private int progress;

        CreatePlanTask(GoogleAccountCredential credential)
        {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("race-planner")
                    .build();

            db = new DatabaseHelper(AuthenticateAndCallAPI.this);
        }

        @Override
        protected Void doInBackground(Void... params)
        {
            try
            {
                createPlan(racer.calendarID);
            } catch (IOException e)
            {
                mLastError = e;
            }
            return null;
        }

        private void createPlan(String calID) throws IOException
        {
            double startingMiles = -1;
            double goalMiles = 5;
            double tuesdayMiles;
            double wednesdayMiles;
            double thursdayMiles;
            double sundayMiles = -1;
            double bumpMileageUp = -1;
            int tuesThursMileCap = -1;
            int wedMileCap = -1;
            int sunMileCap = -1;
            long dayInMillis = 86400000;
            int weeksOfTraining = -1;
            Date startDate; // amount of millis in a week * 8 weeks

            Log.e(TAG, "inside createPlan: " + racer.calendarID);
            db.insertNewPlanToDatabase(racer);

            Cursor c = db.query("SELECT * FROM " + DatabaseHelper.TRAINING_PLAN_TABLE_NAME, null);

            while (c.moveToNext())
            {
                Log.i(TAG, "plan name: " + c.getString(1));
            }

            c.moveToLast();
            racer.databaseID = c.getInt(0);
            c.close();
            db.close();

            switch (racer.experienceLevel)
            {
                case GlobalVariables.EXPERIENCE_BEGINNER:
                    startingMiles = 1;
                    sundayMiles = 2;
                    break;
                case GlobalVariables.EXPERIENCE_INTERMEDIATE:
                    startingMiles = 3;
                    sundayMiles = 3;
                    break;
                case GlobalVariables.EXPERIENCE_EXPERT:
                    startingMiles = 3;
                    sundayMiles = 5;
                    break;
            }

            switch (racer.raceType)
            {
                case GlobalVariables.RACE_5K:
                    mProgressBarDialog.setMax(GlobalVariables.PROGRESS_MAX_5K);
                    weeksOfTraining = GlobalVariables.WEEKS_OF_TRAINING_5K;
                    goalMiles = 5;
                    tuesThursMileCap = 2;
                    wedMileCap = 3;
                    sunMileCap = 4;
                    break;
                case GlobalVariables.RACE_10K:
                    mProgressBarDialog.setMax(GlobalVariables.PROGRESS_MAX_10K);
                    weeksOfTraining = GlobalVariables.WEEKS_OF_TRAINING_10K;
                    goalMiles = 7;
                    tuesThursMileCap = 3;
                    wedMileCap = 4;
                    sunMileCap = 5;
                    break;
                case GlobalVariables.RACE_HALF:
                    mProgressBarDialog.setMax(GlobalVariables.PROGRESS_MAX_HALF);
                    weeksOfTraining = GlobalVariables.WEEKS_OF_TRAINING_HALF;
                    goalMiles = 13;
                    tuesThursMileCap = 5;
                    wedMileCap = 7;
                    sunMileCap = 12;
                    break;
                case GlobalVariables.RACE_MARATHON:
                    mProgressBarDialog.setMax(GlobalVariables.PROGRESS_MAX_MARATHON);
                    weeksOfTraining = GlobalVariables.WEEKS_OF_TRAINING_MARATHON;
                    // increase training length if beginner
                    if (racer.experienceLevel.equals(GlobalVariables.EXPERIENCE_BEGINNER))
                    {
                        weeksOfTraining = GlobalVariables.WEEKS_OF_TRAINING_MARATHON_BEGINNER;
                        mProgressBarDialog.setMax(GlobalVariables.PROGRESS_MAX_MARATHON + 16);
                    }
                    goalMiles = 26;
                    tuesThursMileCap = 6;
                    wedMileCap = 10;
                    sunMileCap = 22;
                    break;
            }

            startDate = new Date(racer.date.getTime() - (604800000L * weeksOfTraining));
            Date tuesday = startDate;
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE");

            // push tuesday to correct day of week
            while (!sdf.format(tuesday).equals("Tuesday"))
            {
                tuesday = new Date(tuesday.getTime() + 86400000);
            }

            Date wednesday = new Date(tuesday.getTime() + dayInMillis);
            Date thursday = new Date(wednesday.getTime() + dayInMillis);
            Date sunday = new Date(thursday.getTime() + (dayInMillis * 3));

            // adjust first week values based on experience
            tuesdayMiles = startingMiles;
            wednesdayMiles = startingMiles + 1;
            thursdayMiles = startingMiles;

            createRunsForDayOfWeek(tuesday, weeksOfTraining, tuesdayMiles, tuesThursMileCap, goalMiles, .5);
            createRunsForDayOfWeek(wednesday, weeksOfTraining, wednesdayMiles, wedMileCap, goalMiles, .5);
            createRunsForDayOfWeek(thursday, weeksOfTraining, thursdayMiles, tuesThursMileCap, goalMiles, .5);
            createRunsForDayOfWeek(sunday, weeksOfTraining, sundayMiles, sunMileCap, goalMiles, 1);

            mProgressBarDialog.setProgress(++progress);
            // create event for race
            createEventInAPI(calID, raceDate, "RACE DAY!!!");
            Log.e(TAG, "TOTAL PROGRESS: " + progress);
        }

        private void createRunsForDayOfWeek(Date date, int weeksOfTraining, double tuesdayMiles,
                                            int tuesThursMileCap, double goalMiles, double increment)
        {
            String calID = racer.calendarID;
            Date tuesday = date;

            // TUESDAY
            int recovery = 0;
            for (int i = 0; i < weeksOfTraining; i++)
            {
                try
                {
                    if (i == weeksOfTraining - 1) // decrease mileage week before race
                    {
                        tuesdayMiles = tuesdayMiles / 2;
                        mProgressBarDialog.setProgress(++progress);
                        createEventInAPI(calID, tuesday, Double.toString(tuesdayMiles) + "M");
                    } else if (i % 3 == 0 && tuesday.getTime() < raceDate.getTime())
                    {
                        double recoveryMiles = tuesdayMiles - recovery;
                        mProgressBarDialog.setProgress(++progress);
                        createEventInAPI(calID, tuesday, Double.toString(recoveryMiles) + "M");
                        if (recovery < (tuesThursMileCap / 2))
                        {
                            recovery++;
                        }

                    } else if (tuesday.getTime() < raceDate.getTime())
                    {// check if event takes place before race date
                        mProgressBarDialog.setProgress(++progress);
                        createEventInAPI(calID, tuesday, Double.toString(tuesdayMiles) + "M");
                    }

                    if (tuesdayMiles < goalMiles && tuesdayMiles < tuesThursMileCap)
                    {
                        tuesdayMiles = tuesdayMiles + increment;
                    }
                    tuesday = getOneWeekLater(tuesday);
                    if (tuesday.getTime() > raceDate.getTime()) break;
                } catch (IOException e)
                {
                    Log.e(TAG, "IOException: ", e);
                }
            }
        }

        private Date getOneWeekLater(Date date)
        {
            return new Date(date.getTime() + 604800000L);
        }

        private void createEventInAPI(String calID, Date date, String eventName) throws IOException
        {
            Date currentDate = new Date();
            try
            {
                Event event = new Event()
                        .setSummary(eventName)
                        .setDescription("raceplan.io");

                Date startDate = date;
                Date endDate = new Date(startDate.getTime() + 86400000);

                // skip events that take place before the present
                if (currentDate.getTime() < startDate.getTime())
                {
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

                    DateTime startDateTime = new DateTime(dateFormat.format(startDate));
                    DateTime endDateTime = new DateTime(dateFormat.format(endDate));

                    EventDateTime startEventDateTime = new EventDateTime().setDate(startDateTime);
                    EventDateTime endEventDateTime = new EventDateTime().setDate(endDateTime);

                    event.setStart(startEventDateTime);
                    event.setEnd(endEventDateTime);
                    // insert event into Calendar via API
                    event = mService.events().insert(racer.calendarID, event).execute();

                    // insert event info into database
                    db.insertEventToDatabase(racer.databaseID, event.getId(), racer.calendarID);
                } else
                {
                    Log.i(TAG, "Current date is after run date, skipping...");
                }

            } catch (IOException e)
            {
                Log.e(TAG, "IOException: ", e);
            }
        }

        @Override
        protected void onPreExecute()
        {
            // set UI not interactable
            mProgressBarDialog.setCancelable(false);
            mProgressBarDialog.show();
        }

        @Override
        protected void onPostExecute(Void output)
        {

            // set ui as interactable
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            mProgressBarDialog.hide();

            callFinalInfoFragment();

            Toast toast = Toast.makeText(AuthenticateAndCallAPI.this, "Training plan created succesfully!" +
                    " Refresh your google calendar by using the top right button on the app to see changes!", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER | Gravity.TOP, 0, 0);
            toast.show();
        }

        private void callFinalInfoFragment()
        {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.animator.enter_from_right, R.animator.exit_to_left, R.animator.enter_from_left, R.animator.exit_to_right);
            DisplayFinalInfo displayFinalInfo = new DisplayFinalInfo();
            String tag = displayFinalInfo.toString();
            fragmentTransaction.replace(R.id.fragment_container, displayFinalInfo, tag);
            fragmentTransaction.addToBackStack(tag);
            fragmentTransaction.commit();
            Log.e(TAG, "callFinalInfoFragment");
        }

        @Override
        protected void onCancelled()
        {
            // make ui interactive again
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            mProgressBarDialog.hide();
            if (mLastError != null)
            {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException)
                {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException)
                {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            MainActivity.REQUEST_AUTHORIZATION);
                } else
                {
                    Log.e(TAG, "The following error occurred:\n"
                            + mLastError.getMessage());
                }
            } else
            {
                Log.e(TAG, "Request cancelled.");
            }
        }
    }


    /**
     * Checks whether the device currently has a network connection.
     *
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline()
    {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     *
     * @return true if Google Play Services is available and up to
     * date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable()
    {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices()
    {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode))
        {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     *
     * @param connectionStatusCode code describing the presence (or lack of)
     *                             Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode)
    {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                AuthenticateAndCallAPI.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     *
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode  code indicating the result of the incoming
     *                    activity result.
     * @param data        Intent (containing result data) returned by incoming
     *                    activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK)
                {
                    Log.e(TAG,
                            "This app requires Google Play Services. Please install " +
                                    "Google Play Services on your device and relaunch this app.");
                } else
                {
                    callAPI();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null)
                {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null)
                    {
                        SharedPreferences settings =
                                PreferenceManager.getDefaultSharedPreferences(this);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        callAPI();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK)
                {
                    callAPI();
                }
                break;
        }
    }
}
