package com.race.planner.utils;

import java.util.Date;

/**
 * Created by dev on 5/4/2017.
 */

public interface FragmentCommunicator
{
    void passRaceType(String s);
    void passDate(Date d);
    void passExperienceLevel(String e);
}
