package com.example.ryan.raceplanner;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ListTrainingPlans extends AppCompatActivity
{
    ArrayList<String> results = new ArrayList<>();
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
                        results.add(c.getString(1));
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
        fromColumns = results.toArray(fromColumns);
        for (int i = 0; i < fromColumns.length; i++)
        {
            Log.i("blah", results.get(i));
        }
        int[] toViews = {android.R.id.text1};
        final SimpleCursorAdapter mAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, null, fromColumns, toViews, 0);
        ListView listView = (ListView) findViewById(R.id.list_view_created_plans);
        listView.setAdapter(mAdapter);
    }
}
