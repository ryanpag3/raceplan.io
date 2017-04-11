package com.example.ryan.raceplanner;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ryan on 4/7/17.
 */

public class Date implements Parcelable
{
    private int year;
    private int month;
    private int day;

    Date(int y, int m, int d)
    {
        this.year = y;
        this.month = m;
        this.day = d;
    }

    // Getters && Setters
    public int getYear()
    {
        return this.year;
    }

    public int getMonth()
    {
        return this.month;
    }

    public int getDay()
    {
        return this.day;
    }

    public void setYear(int y)
    {
        this.year = y;
    }

    public void setMonth(int m)
    {
        this.month = m;
    }

    public void setDat(int d)
    {
        this.day = d;
    }
    //

    // Parcelable Implementation
    public Date(Parcel in)
    {
        int[] data = new int[3];
        in.readIntArray(data);
        // Order needs to be the same as the writeToParcel() method
        this.year = data[0];
        this.month = data[1];
        this.day = data[2];
    }


    public int describeContents()
    {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags)
    {
        out.writeIntArray(new int[]{this.year, this.month, this.day});
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator()
    {
        public Date createFromParcel(Parcel in)
        {
            return new Date(in);
        }

        public Date[] newArray(int size)
        {
            return new Date[size];
        }
    };
}
