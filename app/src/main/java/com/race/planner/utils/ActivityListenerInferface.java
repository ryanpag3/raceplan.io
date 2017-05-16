package com.race.planner.utils;

import java.util.Date;

/**
 * Created by dev on 5/16/2017.
 */

public interface ActivityListenerInferface
{
    void passName(String n);
    void passRaceType(String r);
    void passExperienceLevel(String e);
    void passDate(Date d);
}
