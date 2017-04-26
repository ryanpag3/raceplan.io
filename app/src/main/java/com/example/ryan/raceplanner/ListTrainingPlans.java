package com.example.ryan.raceplanner;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
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
import java.util.ArrayDeque;
import java.util.ArrayList;

import com.example.ryan.raceplanner.CalendarTask;

public class ListTrainingPlans extends AppCompatActivity
{
    ArrayList<String> results = new ArrayList<>();
    ViewHolder mViewHolder = new ViewHolder();
    private final String TAG = this.getClass().getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_training_plans);
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
        ListView listView = (ListView) findViewById(R.id.list_view_created_plans);
        MyArrayAdapter myArrayAdapter = new MyArrayAdapter(this);
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

                    db.deletePlanFromDatabase(mViewHolder.idList.get(position));
                    mViewHolder.idList.remove(position);
                    notifyDataSetChanged();

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
