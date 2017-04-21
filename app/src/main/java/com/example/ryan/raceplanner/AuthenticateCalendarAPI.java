package com.example.ryan.raceplanner;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
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
import com.google.api.services.calendar.Calendar;
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

public class AuthenticateCalendarAPI extends Activity implements EasyPermissions.PermissionCallbacks
{
    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    private static final String TAG = AuthenticateCalendarAPI.class.getName();
    private static final String BUTTON_TEXT = "Call Google Calendar API";
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { CalendarScopes.CALENDAR };
    GoogleAccountCredential mCredential;
    ProgressDialog mProgress;
    String calID;
    Boolean buttonPressed = false;
    Boolean calCreated = false;
    RacerInfo racerInfo;
    private TextView mOutputText;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authenticate_calendar_api);

        racerInfo  = getIntent().getExtras().getParcelable(GlobalVariables.RACER_INFO_ID);

        mOutputText = (TextView) findViewById(R.id.mOutputText);
        mOutputText.setMovementMethod(new ScrollingMovementMethod());
        mOutputText.setText(
                "Click the \'" + BUTTON_TEXT +"\' button to test the API.");

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Calling Google Calendar API ...");

        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        final Button buttonCreateCal = (Button) findViewById(R.id.button_create_calendar);
        buttonCreateCal.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                getResultsFromApi();
                buttonPressed = true;
                Log.i(TAG, "Button create called.");

            }
        });

        Button buttonDeleteCal = (Button) findViewById(R.id.button_delete_calendar);
        buttonDeleteCal.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                deleteResultsFromAPI();
                buttonPressed = true;
                Log.i(TAG, "Button delete called.");
            }
        });

        Button buttonCreateEvent = (Button) findViewById(R.id.button_create_training_plan);
        buttonCreateEvent.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                createEventsInAPI();
                Log.i(TAG, "event creator called");
            }
        });

        Button buttonFinishActivity = (Button) findViewById(R.id.button_finish_authenticate_api);
        buttonFinishActivity.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (buttonPressed)
                {
                    Intent intent = new Intent(AuthenticateCalendarAPI.this, GenerateTrainingPlan.class);
                    intent.putExtra(GlobalVariables.RACER_INFO_ID, getIntent().getExtras().getParcelable(GlobalVariables.RACER_INFO_ID));
                    intent.putExtra(GlobalVariables.CALENDAR_CREATED_ID, calCreated);
                    intent.putExtra(GlobalVariables.CALENDAR_ID, calID);
                    startActivity(intent);
                } else {
                    Log.i(TAG, "calendar not created yet");
                }
            }
        });
    }

    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    private void getResultsFromApi()
    {
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount(1);
        } else if (! isDeviceOnline()) {
            mOutputText.setText("No network connection available.");
        } else {
            new MakeCalendarTask(mCredential).execute(R.id.button_create_calendar);
            mOutputText.setText("Calendar created");
        }
    }

    private void deleteResultsFromAPI()
    {
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount(2);
        } else if (! isDeviceOnline()) {
            mOutputText.setText("No network connection available.");
        } else {
            new MakeCalendarTask(mCredential).execute(R.id.button_delete_calendar);
            mOutputText.setText("Calendar deleted");
        }
    }

    private void createEventsInAPI()
    {
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount(3);
        } else if (! isDeviceOnline()) {
            mOutputText.setText("No network connection available.");
        } else {
            new MakeCalendarTask(mCredential).execute(R.id.button_create_training_plan);
            mOutputText.setText("Training plan created.");
        }

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
    private void chooseAccount(int i) {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                // i = identifier for calling original method
                switch (i)
                {
                    case 1:
                        getResultsFromApi();
                        break;
                    case 2:
                        deleteResultsFromAPI();
                        break;
                    case 3:
                        createEventsInAPI();
                        break;
                }
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    mOutputText.setText(
                            "This app requires Google Play Services. Please install " + "Google Play Services on your device and relaunch this app.");
                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     * @param requestCode The request code passed in
     *     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
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
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }


    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                AuthenticateCalendarAPI.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    /**
     * An asynchronous task that handles the Google Calendar API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */

    private class MakeCalendarTask extends AsyncTask<Integer, Integer, Void>
    {
        private com.google.api.services.calendar.Calendar mService = null;
        private Exception mLastError = null;

        MakeCalendarTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("race-planner")
                    .build();
        }

        /**
         * Background task to call Google Calendar API.
         */
        @Override
        protected Void doInBackground(Integer... id) {
            try {
                switch (id[0])
                {
                    // commented out API calls to avoid getting timed out
                    case R.id.button_create_calendar:
                        createCalendarInAPI();
                        //cancel(true);
                        Log.i(TAG, id[0].toString());
                        break;
                    case R.id.button_delete_calendar:
                        deleteCalendarFromAPI();
                        //cancel(true);
                        Log.i(TAG, id[0].toString());
                        break;
                    case R.id.button_create_training_plan:
                    {
                        //createTrainingPlan(racerInfo);
                        createEventInAPI();
                        //cancel(true);
                        Log.i(TAG, id[0].toString());
                    }

                }
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
            }
            return null;
        }

        /**
         * Creates new Calendar for events to be placed in.
         * @throws IOException throws exception if input is missing
         * TODO: calID can only check for
         */
        private void createCalendarInAPI() throws IOException {
            if (!isRacePlannerCalendarCreated())
            {
                // BUG: when removing library definition and adding an import, calls the wrong constructor
                // WORKAROUND: defined all manually
                com.google.api.services.calendar.model.Calendar calendar = new com.google.api.services.calendar.model.Calendar();
                calendar.setSummary("race-planner");
                calendar.setTimeZone("America/Los_Angeles");
                com.google.api.services.calendar.model.Calendar createdCalendar = mService.calendars().insert(calendar).execute();
                calCreated = true;

                calID = createdCalendar.getId();
                Log.i(TAG, createdCalendar.getId());
            } else
            {
                //mOutputText.setText("Calendar already created.");
            }
        }

        private boolean isRacePlannerCalendarCreated() throws IOException
        {
            // iterate through entries in calendar list
            String pageToken = null;
            do
            {
                Log.i(TAG, mCredential.toString());
                CalendarList calendarList = mService.calendarList().list().setPageToken(pageToken).execute();
                List<CalendarListEntry> items = calendarList.getItems();

                for (CalendarListEntry calendarListEntry : items)
                {
                    String temp = calendarListEntry.getSummary();
                    if (calendarListEntry.getSummary().equals("race-planner"))
                    {
                        calID = calendarListEntry.getId();
                        Log.i(TAG, "isRacePlannerCalendarCreated returned true");
                        return true;
                    }
                    Log.i(TAG, "Summary: " + calendarListEntry.getSummary());
                    Log.i(TAG, "ID: " + calendarListEntry.getId());
                }
                pageToken = calendarList.getNextPageToken();
            } while (pageToken != null);
            Log.i(TAG, "isRacePlannerCrated returned false");
            return false;
        }

        /**
         * Deletes the created Calendar
         * @throws IOException
         */
        private void deleteCalendarFromAPI() throws IOException
        {
            if (calID != null){
                mService.calendars().delete(calID).execute();
                calCreated = false;
                Log.i(TAG, calID);
            }
            else
            {
                mOutputText.setText("No calendar to delete.");
            }
        }

//        private void createTrainingPlan(RacerInfo r)
//        {
//            String raceType = r.raceType;
//            Date date = r.date;
//            int startMiles;
//            int finishMiles;
//
//            switch (r.experienceLevel)
//            {
//
//            }
//        }

        private void createEventInAPI() throws IOException
        {
            if (isRacePlannerCalendarCreated())
            {
                try
                {
                    Event event = new Event()
                            .setSummary("Test Event")
                            .setDescription("Test Description");
                    Log.i(TAG, racerInfo.experienceLevel);
                    Log.i(TAG, racerInfo.raceType);

                    java.util.Calendar cal = java.util.Calendar.getInstance();
                    cal.set(java.util.Calendar.YEAR, racerInfo.year);
                    cal.set(java.util.Calendar.MONTH, racerInfo.month);
                    cal.set(java.util.Calendar.DAY_OF_MONTH, racerInfo.day);
                    Date date = cal.getTime();


                    Date startDate = date;
                    Date endDate = new Date(startDate.getTime() + 86400000);

                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    Log.i(TAG, dateFormat.format(startDate));
                    Log.i(TAG, dateFormat.format(endDate));

                    DateTime startDateTime = new DateTime(dateFormat.format(startDate));
                    DateTime endDateTime = new DateTime(dateFormat.format(endDate));

                    EventDateTime startEventDateTime = new EventDateTime().setDate(startDateTime);
                    EventDateTime endEventDateTime = new EventDateTime().setDate(endDateTime);

                    event.setStart(startEventDateTime);
                    event.setEnd(endEventDateTime);

                    event = mService.events().insert(calID, event).execute();
                } catch (IOException e)
                {
                    Log.e(TAG, "IOException: ", e);
                }

            }
        }


        @Override
        protected void onPreExecute()
        {
            mOutputText.setText("");
            mProgress.show();
        }

        @Override
        protected void onPostExecute(Void output)
        {
            mProgress.hide();
        }

        @Override
        protected void onCancelled()
        {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            AuthenticateCalendarAPI.REQUEST_AUTHORIZATION);
                } else {
                    mOutputText.setText("The following error occurred:\n" + mLastError.getMessage());
                }
            } else {
                mOutputText.setText("Request cancelled.");
            }
        }
    }

}
