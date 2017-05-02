package com.race.raceplanner;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.os.AsyncTask;
import android.widget.TextView;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.io.IOException;

/**
 * DeleteTrainingPlanTask is an AsyncTask much like the inner classes of AuthenticateAndCallAPI.
 */

public class DeleteTrainingPlanTask extends AsyncTask<Void, Void, Void>
{
    private com.google.api.services.calendar.Calendar mService = null;
    private Exception mLastError = null;
    private RacerInfo racerInfo;
    private DatabaseHelper db;
    private TextView mOutput;
    private ProgressDialog mProgress;

    DeleteTrainingPlanTask(GoogleAccountCredential credential, RacerInfo r, Activity context)
    {
        if(android.os.Debug.isDebuggerConnected())
            android.os.Debug.waitForDebugger();
        // mOutput provides debug info
        mOutput = (TextView) context.findViewById(R.id.mOutputText);

        // mProgress is a simple progress spinner
        mProgress = new ProgressDialog(context);
        mProgress.setMessage("Working...");

        // instantiate google api service
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService = new com.google.api.services.calendar.Calendar.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("race-planner")
                .build();

        // update with info of race to-be-deleted
        racerInfo = r;
        // instantiate new database
        db = new DatabaseHelper(context);
    }

    @Override
    protected Void doInBackground(Void... params)
    {
        // query through all events where the database ID matches the training plan database ID
        Cursor c = db.query("SELECT * FROM " + DatabaseHelper.EVENT_ID_TABLE_NAME + " WHERE "
                + DatabaseHelper.EVENT_ID_COL_1 + "= ?", new String[] {String.valueOf(racerInfo.databaseID)});

        while (c.moveToNext())
        {
            // delete by database ID and eventID
            deleteEventByID(c.getString(1), c.getString(2));
        }
        // finally remove the training plan from database
        db.deletePlanFromDatabase(racerInfo.databaseID);
        return null;
    }

    public void deleteEventByID(String ID, String eventID)
    {
        try
        {
            // delete from google calendar using api
            mService.events().delete(eventID, ID).execute();

            // remove event from event table
            db.deleteEventFromDatabase(ID);
        } catch (IOException e)
        { mLastError = e; }
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
    }

    @Override
    protected void onCancelled()
    {
        mProgress.hide();
    }
}