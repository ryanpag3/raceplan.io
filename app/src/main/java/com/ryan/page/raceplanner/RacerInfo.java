package com.ryan.page.raceplanner;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ryan on 4/7/17.
 */

public class RacerInfo implements Parcelable
{
    private static final String TAG = AuthenticateCalendarAPI.class.getName();

    String raceType;
    String experienceLevel;
    String nameOfPlan;
    String calendarID; // google api calendar id
    String calendarName;

    int year;
    int month;
    int day;
    int databaseID; // sql id

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
        calendarID = in.readString();
        calendarName = in.readString();
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
        out.writeString(calendarID);
        out.writeString(calendarName);
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
