package com.example.ryan.raceplanner;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.example.ryan.raceplanner.CalendarTask;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;

import android.text.format.DateFormat;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class ListTrainingPlans extends AppCompatActivity
{
    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    private static final String BUTTON_TEXT = "Call Google Calendar API";
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { CalendarScopes.CALENDAR };

    ArrayList<String> results = new ArrayList<>();
    ViewHolder mViewHolder = new ViewHolder();
    private final String TAG = this.getClass().getSimpleName();
    GoogleAccountCredential mCredential;
    MyArrayAdapter myArrayAdapter;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_training_plans);

        myArrayAdapter = new MyArrayAdapter(this);

        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        mCredential.setSelectedAccountName(getIntent().getExtras().getString(GlobalVariables.CREDENTIAL_ACCOUNT_NAME));

        Button refreshButton = (Button) findViewById(R.id.button_refresh);
        refreshButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                myArrayAdapter = new MyArrayAdapter(ListTrainingPlans.this);
                listView.setAdapter(myArrayAdapter);
                Log.i(TAG, "set adapter");
            }
        });

        openAndQueryDatabase();
        displayTrainingPlanList();
    }

    private void openAndQueryDatabase()
    {
        try
        {
            DatabaseHelper db = new DatabaseHelper(this);
            Cursor c = db.query("SELECT * FROM " + DatabaseHelper.TRAINING_PLAN_TABLE_NAME, null);
            if (c != null)
            {
                if (c.moveToFirst())
                {
                    do
                    {
                        Log.i(TAG, c.getString(1));
                        mViewHolder.idList.add(c.getInt(0));
                        mViewHolder.nameList.add(c.getString(1));
                        mViewHolder.raceDateList.add(c.getString(2));
                        mViewHolder.raceTypeList.add(c.getString(3));
                        mViewHolder.experienceLevelList.add(c.getString(4));
                        mViewHolder.calendarList.add(c.getString(5));

                    } while (c.moveToNext());
                }
            }
        } catch (SQLiteException sqe)
        {
            Log.e(getClass().getSimpleName(), "Could not retrieve or open database");
        }
    }

    private void displayTrainingPlanList()
    {
        String[] fromColumns = new String[results.size()];
        int[] toViews = {android.R.id.text1};
        listView = (ListView) findViewById(R.id.list_view_created_plans);
        listView.setAdapter(myArrayAdapter);
    }


    /**
     * Custom ArrayAdapter for populating the ListView
     */
    private class MyArrayAdapter extends BaseAdapter
    {
        private LayoutInflater inflater;

        public MyArrayAdapter(Context context)
        {
            inflater = LayoutInflater.from(context);
        }

        public int getCount()
        {
            return mViewHolder.nameList.size();
        }

        public Object getItem(int position)
        {
            return position;
        }

        public long getItemId(int position)
        {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent)
        {
            final DatabaseHelper db = new DatabaseHelper(ListTrainingPlans.this);

            convertView = inflater.inflate(R.layout.layout_training_plan_row, parent, false);

            mViewHolder.name = (TextView) convertView.findViewById(R.id.row_text_plan_name);
            mViewHolder.raceDate = (TextView) convertView.findViewById(R.id.row_text_race_date);
            mViewHolder.raceType = (TextView) convertView.findViewById(R.id.row_text_race_type);
            mViewHolder.experienceLevel = (TextView) convertView.findViewById(R.id.row_text_experience_level);
            mViewHolder.calendar = (TextView) convertView.findViewById(R.id.row_text_calendar);
            mViewHolder.deleteTrainingPlanButton = (Button) convertView.findViewById(R.id.row_button_delete_training_plan);

            convertView.setTag(mViewHolder);

            mViewHolder.deleteTrainingPlanButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                        // parse date string
                        String dateString = mViewHolder.raceDateList.get(position);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        Date date = new Date();
                        try
                        {
                            date = dateFormat.parse(dateString);
                        } catch (ParseException e)
                        {
                            e.printStackTrace();
                        }

                        // create new RacerInfo object to pass to AsyncTask
                        int year = Integer.parseInt((String) DateFormat.format("yyyy", date));
                        int month = Integer.parseInt((String) DateFormat.format("MM", date));
                        int day = Integer.parseInt((String) DateFormat.format("dd", date));

                        int id = mViewHolder.idList.get(position);
                        String name = mViewHolder.nameList.get(position);
                        String raceType = mViewHolder.raceTypeList.get(position);
                        String experienceLevel = mViewHolder.experienceLevelList.get(position);
                        String calendarName = mViewHolder.calendarList.get(position);

                        RacerInfo racerInfo = new RacerInfo(year, month, day, raceType, experienceLevel, name, id);

                        new CalendarTask(mCredential, racerInfo, ListTrainingPlans.this).execute("deleteTrainingPlan");
                        mViewHolder.idList.remove(position);
                    }
            });

            mViewHolder.name.setText(mViewHolder.nameList.get(position));
            mViewHolder.raceDate.setText(mViewHolder.raceDateList.get(position));
            mViewHolder.raceType.setText(mViewHolder.raceTypeList.get(position));
            mViewHolder.experienceLevel.setText(mViewHolder.experienceLevelList.get(position));
            mViewHolder.calendar.setText(mViewHolder.calendarList.get(position));

            return convertView;

        }
    }

    private class ViewHolder
    {
        public ArrayList<Integer> idList = new ArrayList<>();
        public ArrayList<String> nameList = new ArrayList<>();
        public ArrayList<String> calendarList = new ArrayList<>();
        public ArrayList<String> raceDateList = new ArrayList<>();
        public ArrayList<String> raceTypeList = new ArrayList<>();
        public ArrayList<String> experienceLevelList = new ArrayList<>();

        private TextView name;
        private TextView calendar;
        private TextView raceDate;
        private TextView raceType;
        private TextView experienceLevel;
        private Button deleteTrainingPlanButton;
    }
}
