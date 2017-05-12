package com.race.planner.data_models;

import android.app.Application;

/**
 * Created by ryan on 4/7/17.
 */

public class GlobalVariables extends Application
{
    public static final String RACER_INFO_ID = "RACER_INFO_ID";
    public static final String CALENDAR_CREATED_ID = "CALENDAR_CREATED_ID";
    public static final String CALENDAR_ID = "CALENDAR_ID";
    public static final String CREATE_CALENDAR_BOOL = "CREATE_CALENDAR_BOOL";
    public static final String CREDENTIAL_ACCOUNT_NAME = "CREDENTIAL_ACCOUNT_NAME";
    public static final String CREDENTIAL = "CREDENTIAL";
    public static final int MY_PERMISSIONS_REQUEST_READ_CALENDAR = 1;
    public static final int MY_PERMISSIONS_REQUEST_WRITE_CALENDAR = 2;
    public static final int MY_PERMISSIONS_REQUEST_READ_SYNC_SETTINGS = 3;

    public static final String RACE_5K = "5k";
    public static final String RACE_10K = "10k";
    public static final String RACE_HALF = "Half Marathon";
    public static final String RACE_MARATHON = "Marathon";

    public static final String EXPERIENCE_BEGINNER = "Beginner";
    public static final String EXPERIENCE_INTERMEDIATE = "Intermediate";
    public static final String EXPERIENCE_EXPERT = "Expert";

    public static final int PROGRESS_MAX_5K = 31;
    public static final int PROGRESS_MAX_10K = 47;
    public static final int PROGRESS_MAX_HALF = 47;
    public static final int PROGRESS_MAX_MARATHON = 71;
}
