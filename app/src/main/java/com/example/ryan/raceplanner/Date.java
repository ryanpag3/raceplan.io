package com.example.ryan.raceplanner;

import android.app.Application;

/**
 * Created by ryan on 4/7/17.
 */

public class Date
{
    private int year;
    private int month;
    private int day;

    Date(int y, int m, int d)
    {
        year = y;
        month = m;
        day = d;
    }

    Date()
    {
        year = 0;
        month = 0;
        day = 0;
    }
}
