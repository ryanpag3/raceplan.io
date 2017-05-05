package com.race.planner.data_models;


import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.Date;

/**
 * Created by ryan on 4/7/17.
 */

public class Racer implements Parcelable
{
    private static final String TAG = Racer.class.getName();

    public Date date;

    public String raceType;
    public String experienceLevel;
    public String nameOfPlan;
    public String calendarID; // google api calendar id
    public String calendarName;

    public int year;
    public int month;
    public int day;
    public int databaseID; // sql id

    public Racer(Date d, String r, String e, String n, int id)
    {
        date = d;
        raceType = r;
        experienceLevel = e;
        nameOfPlan = n;
        databaseID = id;
    }

    public Racer()
    {
        raceType = null;
        experienceLevel = null;
    }

    // Getters && Setters
    public String getDate()
    {
        return date.toString();
    }

    public boolean isComplete()
    {
        return year != -1 && month != -1 && day != -1 && raceType != null && experienceLevel != null;
    }

    // Parcelable Implementation
    public Racer(Parcel in)
    {
        // Order needs to be the same as the writeToParcel() method
        date = new Date(in.readLong());
        int[] data = new int[3];
        in.readIntArray(data);
        year = data[0];
        month = data[1];
        day = data[2];

        raceType = in.readString();
        experienceLevel = in.readString();
        nameOfPlan = in.readString();
        calendarID = in.readString();
        calendarName = in.readString();
    }


    public int describeContents()
    {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags)
    {
        out.writeLong(date.getTime());
        out.writeIntArray(new int[]{this.year, this.month, this.day});
        out.writeString(raceType);
        out.writeString(experienceLevel);
        out.writeString(nameOfPlan);
        out.writeString(calendarID);
        out.writeString(calendarName);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator()
    {
        public Racer createFromParcel(Parcel in)
        {
            return new Racer(in);
        }

        public Racer[] newArray(int size)
        {
            return new Racer[size];
        }
    };
}
