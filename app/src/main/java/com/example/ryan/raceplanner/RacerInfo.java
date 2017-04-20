package com.example.ryan.raceplanner;


import android.icu.util.Calendar;
import android.os.Parcel;
import android.os.Parcelable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ryan on 4/7/17.
 */

public class RacerInfo implements Parcelable
{
    String raceType;
    String experienceLevel;
    Date date;
    int year;
    int month;
    int day;

    RacerInfo(int y, int m, int d, String r, String e)
    {

        date = new Date(y, m, d);
        raceType = r;
        experienceLevel = e;
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
    int getYear()
    {
        return year;
    }

    int getMonth()
    {
        return month;
    }

    int getDay()
    {
        return day;
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
