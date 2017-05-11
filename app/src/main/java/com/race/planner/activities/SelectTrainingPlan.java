package com.race.planner.activities;

import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Spinner;

import java.util.Date;

import com.race.planner.R;
import com.race.planner.data_models.GlobalVariables;
import com.race.planner.data_models.Racer;
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

        progressIcon = (ImageView) findViewById(R.id.logo_icon);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        SelectNameFragment selectNameFragment = new SelectNameFragment();
        String tag = selectNameFragment.toString();
        fragmentTransaction.add(R.id.fragment_swap, selectNameFragment, tag);
        //fragmentTransaction.addToBackStack(tag);
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
    public void onFragmentClicked(String nameOfCurrentFragment)
    {
        String tag;
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.animator.enter_from_right, R.animator.exit_to_left, R.animator.enter_from_left, R.animator.exit_to_right);
        moveProgressIconRight();


        if (nameOfCurrentFragment.equals(SelectNameFragment.class.getName()))
        {
            SelectRaceTypeFragment selectRaceTypeFragment = new SelectRaceTypeFragment();
            tag = selectRaceTypeFragment.toString();
            fragmentTransaction.replace(R.id.fragment_swap, selectRaceTypeFragment, tag);
            fragmentTransaction.addToBackStack(tag);
            fragmentTransaction.commit();

        } else if (nameOfCurrentFragment.equals(SelectRaceTypeFragment.class.getName()))
        {
            SelectExperienceLevelFragment selectExperienceLevelFragment = new SelectExperienceLevelFragment();
            tag = selectExperienceLevelFragment.toString();
            fragmentTransaction.replace(R.id.fragment_swap, selectExperienceLevelFragment, tag);
            fragmentTransaction.addToBackStack(tag);
            fragmentTransaction.commit();
        } else if (nameOfCurrentFragment.equals(SelectExperienceLevelFragment.class.getName()))
        {
            Fragment selectDateFragment = new SelectDateFragment();
            tag = selectDateFragment.toString();
            fragmentTransaction.replace(R.id.fragment_swap, selectDateFragment, tag);
            fragmentTransaction.addToBackStack(tag);
            fragmentTransaction.commit();
        } else
        {
            Intent intent = new Intent(SelectTrainingPlan.this, AuthenticateAndCallAPI.class);
            intent.putExtra(GlobalVariables.RACER_INFO_ID, racer);
            intent.putExtra(GlobalVariables.CREDENTIAL_ACCOUNT_NAME, getIntent()
                    .getExtras().getString(GlobalVariables.CREDENTIAL_ACCOUNT_NAME));
            intent.putExtra(GlobalVariables.CREATE_CALENDAR_BOOL, createNewCalendar);
            startActivity(intent);
        }

    }

    @Override
    public void onBackButtonClicked()
    {
        moveProgressIconLeft();
        getFragmentManager().popBackStack();
    }

    @Override
    public void moveProgressIconRight()
    {
        ObjectAnimator animX = ObjectAnimator.ofFloat(progressIcon, "x", progressIcon.getX()+300);
        animX.start();

    }

    @Override
    public void moveProgressIconLeft()
    {
        ObjectAnimator animX = ObjectAnimator.ofFloat(progressIcon, "x", progressIcon.getX()-300);
        animX.start();

    }
}
