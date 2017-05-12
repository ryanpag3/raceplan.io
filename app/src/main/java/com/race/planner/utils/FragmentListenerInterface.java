package com.race.planner.utils;

import java.util.Date;

/**
 * Created by dev on 5/4/2017.
 */

public interface FragmentListenerInterface
{
    void moveProgressIconLeft();
    void moveProgressIconRight();
    void onFragmentClicked(String s);
    void onBackButtonClicked();
    void passName(String n);
    void passRaceType(String s);
    void passDate(Date d);
    void passExperienceLevel(String e);
    void passCalCreatedBool(Boolean b);
}
