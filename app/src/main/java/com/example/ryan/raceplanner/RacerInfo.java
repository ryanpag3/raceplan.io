package com.example.ryan.raceplanner;


import android.icu.util.Calendar;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ryan on 4/7/17.
 */

public class RacerInfo implements Parcelable
{
    private static final String TAG = AuthenticateCalendarAPI.class.getName();

    String raceType;
    String experienceLevel;
    String nameOfPlan;
    Date date;
    int year;
    int month;
    int day;
    int databaseID;

    RacerInfo(int y, int m, int d, String r, String e, String n, int id)
    {
        year = y;
        month = m;
        day = d;
        raceType = r;
        experienceLevel = e;
        nameOfPlan = n;
        databaseID = id;
    }

    RacerInfo()
    {
        year = -1;
        month = -1;
        day = -1;
        raceType = null;
        experienceLevel = null;
    }

    // Getters && Setters
    String getDate()
    {
        return year + "-" + month + "-" + day;
    }

    boolean isComplete()
    {
        return year != -1 && month != -1 && day != -1 && raceType != null && experienceLevel != null;
    }

    // Parcelable Implementation
    public RacerInfo(Parcel in)
    {
        // Order needs to be the same as the writeToParcel() method
        int[] data = new int[3];
        in.readIntArray(data);
        year = data[0];
        month = data[1];
        day = data[2];

        raceType = in.readString();
        experienceLevel = in.readString();
        nameOfPlan = in.readString();
    }


    public int describeContents()
    {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags)
    {
        out.writeIntArray(new int[]{this.year, this.month, this.day});
        out.writeString(raceType);
        out.writeString(experienceLevel);
        out.writeString(nameOfPlan);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator()
    {
        public RacerInfo createFromParcel(Parcel in)
        {
            return new RacerInfo(in);
        }

        public RacerInfo[] newArray(int size)
        {
            return new RacerInfo[size];
        }
    };
}
