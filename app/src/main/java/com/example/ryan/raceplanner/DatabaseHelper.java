package com.example.ryan.raceplanner;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by ryan on 4/21/17.
 */

public class DatabaseHelper extends SQLiteOpenHelper
{
    public static final String DATABASE_NAME = "training_plans.db";
    public static final String TABLE_NAME = "training_plans_table";
    public static final String COL_1 = "ID";
    public static final String COL_2 = "EVENT_ID";

    public DatabaseHelper(Context context)
    {
        super(context, DATABASE_NAME, null, 1);
        SQLiteDatabase db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL("create table " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, EVENT_ID TEXT)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
