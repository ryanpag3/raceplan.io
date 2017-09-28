package com.race.planner.activities;

import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.Image;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.Spinner;

import java.util.Date;

import com.race.planner.R;
import com.race.planner.data_models.GlobalVariables;
import com.race.planner.data_models.Racer;
import com.race.planner.fragments.SelectCalendarFragment;
import com.race.planner.fragments.SelectDateFragment;
import com.race.planner.fragments.SelectExperienceLevelFragment;
import com.race.planner.fragments.SelectNameFragment;
import com.race.planner.fragments.SelectRaceTypeFragment;
import com.race.planner.utils.FragmentListenerInterface;

public class SelectTrainingPlan extends Activity implements FragmentListenerInterface// implements AdapterView.OnItemSelectedListener
{
    private static final String TAG = SelectTrainingPlan.class.getName();
    private boolean createNewCalendar = true;
    Racer racer = new Racer();
    ImageView progressIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_training_plan);

//        progressIcon = (ImageView) findViewById(R.id.logo_icon);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        SelectNameFragment selectNameFragment = new SelectNameFragment();
        String tag = selectNameFragment.toString();
        fragmentTransaction.replace(R.id.fragment_swap, selectNameFragment, tag);
        // don't add first fragment to back stack so we don't see blank activity
        // fragmentTransaction.addToBackStack(tag);
        fragmentTransaction.commit();
    }

    @Override
    public void passName(String n)
    {
        racer.nameOfPlan = n;
    }

    @Override
    public void passRaceType(String s)
    {
        racer.raceType = s;
    }

    @Override
    public void passDate(Date d)
    {
        racer.date = d;
    }

    @Override
    public void passExperienceLevel(String e)
    {
        racer.experienceLevel = e;
    }

    @Override
    public void passCalCreatedBool(Boolean b)
    {
        createNewCalendar = b;
    }

    @Override
    public void onFragmentClicked(String nameOfCurrentFragment)
    {
        String tag = null;
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.animator.enter_from_right, R.animator.exit_to_left, R.animator.enter_from_left, R.animator.exit_to_right);
//        moveProgressIconRight();


        if (nameOfCurrentFragment.equals(SelectNameFragment.class.getName()))
        {
            SelectRaceTypeFragment selectRaceTypeFragment = new SelectRaceTypeFragment();
            tag = selectRaceTypeFragment.toString();
            fragmentTransaction.replace(R.id.fragment_swap, selectRaceTypeFragment, tag);
        } else if (nameOfCurrentFragment.equals(SelectRaceTypeFragment.class.getName()))
        {
            SelectExperienceLevelFragment selectExperienceLevelFragment = new SelectExperienceLevelFragment();
            tag = selectExperienceLevelFragment.toString();
            fragmentTransaction.replace(R.id.fragment_swap, selectExperienceLevelFragment, tag);
        } else if (nameOfCurrentFragment.equals(SelectExperienceLevelFragment.class.getName()))
        {
            // pass racetype and experiencelevel to fragment so it can check that the date is correct
            Bundle args = new Bundle();
            args.putString("raceType", racer.raceType);
            args.putString("experienceLevel", racer.experienceLevel);

            SelectDateFragment selectDateFragment = new SelectDateFragment();
            selectDateFragment.setArguments(args);
            tag = selectDateFragment.toString();
            fragmentTransaction.replace(R.id.fragment_swap, selectDateFragment, tag);

        } else if (nameOfCurrentFragment.equals(SelectDateFragment.class.getName()))
        {
            SelectCalendarFragment selectCalendarFragment = new SelectCalendarFragment();
            tag = selectCalendarFragment.toString();
            fragmentTransaction.replace(R.id.fragment_swap, selectCalendarFragment, tag);
        } else
        {
            Intent intent = new Intent(SelectTrainingPlan.this, AuthenticateAndCallAPI.class);
            intent.putExtra(GlobalVariables.RACER_INFO_ID, racer);
            intent.putExtra(GlobalVariables.CREDENTIAL_ACCOUNT_NAME, getIntent()
                    .getExtras().getString(GlobalVariables.CREDENTIAL_ACCOUNT_NAME));
            intent.putExtra(GlobalVariables.CREATE_CALENDAR_BOOL, createNewCalendar);
            startActivity(intent);
        }

        fragmentTransaction.addToBackStack(tag);
        String t = String.valueOf(getFragmentManager().getBackStackEntryCount());
        Log.e(TAG, "backstack count " + t);
        fragmentTransaction.commit();

    }

    @Override
    public void onBackButtonClicked()
    {
//        moveProgressIconLeft();
        String t = String.valueOf(getFragmentManager().getBackStackEntryCount());
        getFragmentManager().popBackStack();
    }

    @Override
    public void moveProgressIconRight()
    {
        ObjectAnimator animXIcon = ObjectAnimator.ofFloat(progressIcon, "x",
                getPositionInDp(progressIcon.getX()) + (getScreenWidthInDp()));
        animXIcon.start();
    }

    @Override
    public void moveProgressIconLeft()
    {
        ObjectAnimator animXIcon = ObjectAnimator.ofFloat(progressIcon, "x",
                getPositionInDp(progressIcon.getX()) - (getScreenWidthInDp()));
        animXIcon.start();
    }

    // helper methods
    private float getScreenWidthInDp()
    {
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        return dpWidth;
    }

    private float getPositionInDp(float pos)
    {
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        float xPosInDp = pos / displayMetrics.density;
        return xPosInDp;
    }

    private float getYPositionInDp(float pos)
    {
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        float yPosInDp = pos / displayMetrics.density;
        return yPosInDp;
    }
}
