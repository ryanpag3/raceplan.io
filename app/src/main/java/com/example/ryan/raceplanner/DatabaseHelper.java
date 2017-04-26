package com.example.ryan.raceplanner;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by ryan on 4/21/17.
 */

public class DatabaseHelper extends SQLiteOpenHelper
{
    public static final String DATABASE_NAME = "training_plans.db";
    public static final String TRAINING_PLAN_TABLE_NAME = "training_plans_table";
    public static final String TRAINING_PLAN_COL_1 = "id";
    public static final String TRAINING_PLAN_COL_2 = "name";
    public static final String TRAINING_PLAN_COL_3 = "race_date";
    public static final String TRAINING_PLAN_COL_4 = "race_type";
    public static final String TRAINING_PLAN_COL_5 = "experience_level";
    public static final String TRAINING_PLAN_COL_6 = "calendar";


    public static final String EVENT_ID_TABLE_NAME = "event_id_table";
    public static final String EVENT_ID_COL_1 = "training_plan_id";
    public static final String EVENT_ID_COL_2 = "event_id";
    public static final String EVENT_ID_COL_3 = "calendar_id";


    public DatabaseHelper(Context context)
    {
        super(context, DATABASE_NAME, null, 4);
        SQLiteDatabase db = this.getWritableDatabase();

    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL("create table " + TRAINING_PLAN_TABLE_NAME
                + " (" + TRAINING_PLAN_COL_1 + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + TRAINING_PLAN_COL_2 + " TEXT NOT NULL, "
                        + TRAINING_PLAN_COL_3 + " TEXT NOT NULL, "
                        + TRAINING_PLAN_COL_4 + " TEXT NOT NULL, "
                        + TRAINING_PLAN_COL_5 + " TEXT NOT NULL, "
                        + TRAINING_PLAN_COL_6 + " TEXT NOT NULL)");

        db.execSQL("create table " + EVENT_ID_TABLE_NAME
                + " ( " + EVENT_ID_COL_1 + " INT NOT NULL, "
                        + EVENT_ID_COL_2 + " TEXT, "
                        + EVENT_ID_COL_3 + " TEXT )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS " + TRAINING_PLAN_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + EVENT_ID_TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(int training_plan_id, String event_id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(EVENT_ID_COL_1, training_plan_id);
        values.put(EVENT_ID_COL_2, event_id);
        long result = db.insert(EVENT_ID_TABLE_NAME, null, values);
        return result != -1;
    }

    public Cursor query(String query, String[] selectionArgs)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery(query, selectionArgs);
        return c;
    }

    /**
     * TODO: check if plan with name already exists
     * @param name
     * @return
     */
    public boolean insertNewPlanToDatabase(String name, String raceDate, String raceType,
                                           String experienceLevel, String calendarName)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TRAINING_PLAN_COL_2, name);
        values.put(TRAINING_PLAN_COL_3, raceDate);
        values.put(TRAINING_PLAN_COL_4, raceType);
        values.put(TRAINING_PLAN_COL_5, experienceLevel);
        values.put(TRAINING_PLAN_COL_6, calendarName);
        long result = db.insert(TRAINING_PLAN_TABLE_NAME, null, values);
        return result != -1;
    }

    public void deletePlanFromDatabase(int id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TRAINING_PLAN_TABLE_NAME, TRAINING_PLAN_COL_1 + "=" + id, null);
        db.delete(EVENT_ID_TABLE_NAME, EVENT_ID_COL_1 + "=" + id, null);

    }

    public void deletePlanFromDatabase(String name)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TRAINING_PLAN_TABLE_NAME, TRAINING_PLAN_COL_2 + "=" + name, null);
        db.delete(EVENT_ID_TABLE_NAME, null, null);
    }

    public boolean insertEventToDatabase(int plan_id, String event_id, String calID)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(EVENT_ID_COL_1, plan_id);
        values.put(EVENT_ID_COL_2, event_id);
        values.put(EVENT_ID_COL_3, calID);
        long result = db.insert(EVENT_ID_TABLE_NAME, null, values);
        return result != -1;
    }

    public void deleteDatabases()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TRAINING_PLAN_TABLE_NAME, null, null);
        db.delete(EVENT_ID_TABLE_NAME, null, null);
    }
}
