package com.race.planner;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by ryan on 4/25/17.
 */

public class CalendarTask extends AsyncTask<String, Void, Void>
{
    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    private static final String BUTTON_TEXT = "Call Google Calendar API";
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { CalendarScopes.CALENDAR };
    private int TRAINING_PLAN_ID = -1;
    GoogleAccountCredential mCredential;
    Boolean buttonPressed = false;
    Boolean calCreated = false;
    Date raceDate;
    Context context;


    private com.google.api.services.calendar.Calendar mService = null;
    private Exception mLastError = null;
    private static final String TAG = CalendarTask.class.getSimpleName();
    private TextView mOutput;
    private ProgressDialog mProgress;

    String calID;
    RacerInfo racerInfo;
    DatabaseHelper db;

    CalendarTask(GoogleAccountCredential credential, RacerInfo racerInfo, Activity context) {
        if(android.os.Debug.isDebuggerConnected())
            android.os.Debug.waitForDebugger();
        mOutput = (TextView) context.findViewById(R.id.mOutputText);
        mProgress = new ProgressDialog(context);
        mProgress.setMessage("Working...");

        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mCredential = credential;
        mService = new com.google.api.services.calendar.Calendar.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("race-planner")
                .build();
        this.racerInfo = racerInfo;
        this.context = context;
        db = new DatabaseHelper(context);

        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.YEAR, racerInfo.year);
        cal.set(java.util.Calendar.MONTH, racerInfo.month);
        cal.set(java.util.Calendar.DAY_OF_MONTH, racerInfo.day);
        raceDate = cal.getTime();
    }

    /**
     * Background task to call Google Calendar API.
     */
    @Override
    protected Void doInBackground(String... methodCall) {
        if(android.os.Debug.isDebuggerConnected())
            android.os.Debug.waitForDebugger();

            try
            {
                switch (methodCall[0])
                {
                    case "createCalendar":
                        createCalendarInAPI();
                        break;
                    case "deleteCalendar":
                        deleteCalendarFromAPI();
                        break;
                    case "createTrainingPlan":
                        createPlan();
                        break;
                    case "createCustomTrainingPlan":
                        Log.i(TAG, racerInfo.calendarID);
                        createPlan(racerInfo.calendarID);
                        break;
                    case "deleteTrainingPlan":
                        deleteTrainingPlanTask();
                        break;
                }
            } catch (Exception e)
            {
                mLastError = e;
                Log.e(TAG, "Exception: ", e);
                cancel(true);
            }
        return null;
    }


    /**
     * Creates new Calendar for events to be placed in.
     * @throws IOException throws exception if input is missing
     */
    public void createCalendarInAPI() throws IOException {
        Log.e(TAG, "createCalendarInAPI called.");
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
        } else
        {
            Toast toast = Toast.makeText(context, "race-planner calendar already exists! Creating new plan there.", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    /**
     * Deletes the created Calendar
     * @throws IOException
     */
    public void deleteCalendarFromAPI() throws IOException
    {
        if (isRacePlannerCalendarCreated()){
            mService.calendars().delete(calID).execute();
            calCreated = false;
        }
        else
        {
            mOutput.setText("No calendar to delete.");
        }
    }

    /**
     * Deletes the training plan from the database, then removes calendar events
     * by stored eventIDs.
     * @throws IOException
     */
    public void deleteTrainingPlanTask() throws IOException
    {
//        Cursor c = db.query("SELECT * FROM " + DatabaseHelper.TRAINING_PLAN_TABLE_NAME, null);
//        c.moveToLast();
//        TRAINING_PLAN_ID = c.getInt(0);
        //DatabaseHelper db = new DatabaseHelper(context);
        Cursor c = db.query("SELECT * FROM " + DatabaseHelper.EVENT_ID_TABLE_NAME + " WHERE "
                + DatabaseHelper.EVENT_ID_COL_1 + "= ?", new String[] {String.valueOf(racerInfo.databaseID)});

        while (c.moveToNext())
        {
            deleteEventByID(c.getString(1), c.getString(2));
        }
        db.deletePlanFromDatabase(racerInfo.databaseID);
    }


    public void deleteEventByID(String ID, String calID) throws IOException
    {
        //isRacePlannerCalendarCreated();
        //DatabaseHelper db = new DatabaseHelper(context);
        mService.events().delete(calID, ID).execute();
        db.deleteEventFromDatabase(ID);
    }

    public boolean isRacePlannerCalendarCreated()
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
                        String temp = calendarListEntry.getSummary();
                        if (calendarListEntry.getSummary().equals("race-planner"))
                        {
                            calID = calendarListEntry.getId();
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

    public void createPlan() throws IOException
    {
        racerInfo.calendarName = "race-planner";
        if (isRacePlannerCalendarCreated()){
            createPlan(calID);}
        else
        {
            createCalendarInAPI();
            createPlan(calID);
        }
    }

    public void createPlan(String calID) throws IOException
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
        long dayInMillis = 86400000;
        int weeksOfTraining = -1;
        Date startDate; // amount of millis in a week * 8 weeks
        Date tuesday;
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
        Log.e(TAG, "YOOOOOOO THIS IS THE NAME: " + racerInfo.nameOfPlan);
        //DatabaseHelper db = new DatabaseHelper(context);
        Log.i(TAG, racerInfo.nameOfPlan + " " + racerInfo.getDate() + " " + racerInfo.raceType + " " + racerInfo.experienceLevel);
        db.insertNewPlanToDatabase(racerInfo);

        Cursor c = db.query("SELECT * FROM " + DatabaseHelper.TRAINING_PLAN_TABLE_NAME, null);

        while(c.moveToNext())
        {
            Log.i(TAG, "plan name: " + c.getString(1));
        }

        c.moveToLast();
        racerInfo.databaseID = c.getInt(0);
        Log.e(TAG, "YOOOOOOO THIS IS THE ID: " + racerInfo.databaseID);
        c.close();
        db.close();

        switch (racerInfo.experienceLevel)
        {
            case "Beginner":
                startingMiles = 1;
                sundayMiles = 2;
                bumpMileageUp = 1;
                break;
            case "Intermediate":
                startingMiles = 3;
                sundayMiles = 3;
                bumpMileageUp = 1;
                break;
            case "Expert":
                startingMiles = 3;
                sundayMiles = 5;
                bumpMileageUp = 1;
                break;
        }

        switch(racerInfo.raceType)
        {
            case "5k":
                weeksOfTraining = 8;
                goalMiles = 5;
                tuesThursMileCap = 2;
                wedMileCap = 3;
                break;
            case "10k":
                weeksOfTraining = 12;
                goalMiles = 7;
                tuesThursMileCap = 3;
                wedMileCap = 5;
                break;
            case "Half-Marathon":
                weeksOfTraining = 12;
                goalMiles = 13;
                tuesThursMileCap = 5;
                wedMileCap = 7;
                break;
            case "Marathon":
                weeksOfTraining = 18;
                goalMiles = 26;
                tuesThursMileCap = 5;
                wedMileCap = 10;
                break;
        }

        startDate = new Date(raceDate.getTime() - (604800000L * weeksOfTraining));
        tuesday = startDate;

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

        for (int i = 0; i < weeksOfTraining; i++)
        {
            if (tuesday.getTime()   < raceDate.getTime())createEventInAPI(calID, tuesday, Double.toString(tuesdayMiles));
            if (tuesdayMiles < goalMiles && tuesdayMiles < tuesThursMileCap) { tuesdayMiles = tuesdayMiles + (bumpMileageUp / 2); }
            tuesday   = getOneWeekLater(tuesday);
            if (tuesday.getTime() > raceDate.getTime()) break;
        }

        for (int i = 0; i < weeksOfTraining; i++)
        {
            if (wednesday.getTime()   < raceDate.getTime())createEventInAPI(calID, wednesday, Double.toString(wednesdayMiles));
            if (wednesdayMiles < goalMiles && wednesdayMiles < wedMileCap) { wednesdayMiles = wednesdayMiles + (bumpMileageUp / 2); }
            wednesday   = getOneWeekLater(wednesday);
            if (wednesday.getTime() > raceDate.getTime()) break;
        }

        for (int i = 0; i < weeksOfTraining; i++)
        {
            if (thursday.getTime()   < raceDate.getTime())createEventInAPI(calID, thursday, Double.toString(thursdayMiles));
            if (thursdayMiles < goalMiles && thursdayMiles < tuesThursMileCap) { thursdayMiles = thursdayMiles + (bumpMileageUp / 2); }
            thursday   = getOneWeekLater(thursday);
            if (thursday.getTime() > raceDate.getTime()) break;
        }

        for (int i = 0; i < weeksOfTraining; i++)
        {
            if (sunday.getTime()   < raceDate.getTime())createEventInAPI(calID, sunday, Double.toString(sundayMiles));
            if (sundayMiles < goalMiles) { sundayMiles = sundayMiles + 1; }
            sunday   = getOneWeekLater(sunday);
            if (sunday.getTime() > raceDate.getTime()) break;
        }
    }

    private Date getOneWeekLater(Date date)
    {
        return new Date(date.getTime() + 604800000L);
    }


    private void createEventInAPI(String calID, Date date, String mileage) throws IOException
    {
        if (isRacePlannerCalendarCreated())
        {
            try
            {
                Event event = new Event()
                        .setSummary(mileage + "M")
                        .setDescription("race-planner");

                Date startDate = date;
                Date endDate = new Date(startDate.getTime() + 86400000);

                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

                DateTime startDateTime = new DateTime(dateFormat.format(startDate));
                DateTime endDateTime = new DateTime(dateFormat.format(endDate));

                EventDateTime startEventDateTime = new EventDateTime().setDate(startDateTime);
                EventDateTime endEventDateTime = new EventDateTime().setDate(endDateTime);

                event.setStart(startEventDateTime);
                event.setEnd(endEventDateTime);
                // insert event into Calendar via API
                event = mService.events().insert(calID, event).execute();

                // insert event info into database
                db.insertEventToDatabase(racerInfo.databaseID, event.getId(), calID);

            } catch (IOException e)
            {
                Log.e(TAG, "IOException: ", e);
            }
        }
    }


    @Override
    protected void onPreExecute()
    {
        mProgress.show();
    }

    @Override
    protected void onPostExecute(Void output)
    {
        mProgress.hide();
        Toast toast = Toast.makeText(context, "All finished! Check your google calendar to see what changed!", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER|Gravity.TOP,0,0);
        toast.show();
    }

    @Override
    protected void onCancelled()
    {
        mProgress.hide();
    }
}

