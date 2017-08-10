package com.race.planner.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.race.planner.R;
import com.race.planner.activities.MainActivity;
import com.race.planner.utils.ActivityListenerInferface;
import com.race.planner.utils.FragmentListenerInterface;
import com.race.planner.data_models.GlobalVariables;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.microedition.khronos.opengles.GL;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentListenerInterface} interface
 * to handle interaction events.
 * Use the {@link SelectDateFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SelectDateFragment extends Fragment implements ActivityListenerInferface
{
    FragmentListenerInterface mListener;
    private DatePicker datePicker;
    private String raceType;
    private String experienceLevel;
    Date date;

    public SelectDateFragment()
    {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     */
    public static SelectDateFragment newInstance(String param1, String param2)
    {
        SelectDateFragment fragment = new SelectDateFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_select_date, container, false);
        raceType = this.getArguments().getString("raceType");
        experienceLevel = this.getArguments().getString("experienceLevel");

        // get current date
        final Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        date = cal.getTime();

        // adjust default datepicker widget date to X amount of weeks from current date based on race
        date.setTime(date.getTime() + getPlanLengthInMillis(raceType, experienceLevel));
        cal.setTime(date);
        year = cal.get(Calendar.YEAR); // kinda looks confusing but saves memory usage
        month = cal.get(Calendar.MONTH);
        day = cal.get(Calendar.DAY_OF_MONTH);

        // display toast explaining the chosen date
        Toast toast = Toast.makeText(getActivity(), "Date has been set to the minimum recommended training plan length. Feel free to adjust if necessary.", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER|Gravity.BOTTOM, 0, 0);
        toast.show();


        // set default race date to avoid crashing on pressing back button
        mListener.passDate(date);

        datePicker = (DatePicker) view.findViewById(R.id.date_picker_widget);
        datePicker.init(year, month, day, new DatePicker.OnDateChangedListener()
        {
            @Override
            public void onDateChanged(DatePicker datePicker, int i, int i1, int i2)
            {
                date = new GregorianCalendar(i, i1, i2).getTime();
                mListener.passDate(date);
                Log.i(getActivity().getLocalClassName(), date.toString());
                // do something with the date here
            }
        });

        ImageButton okButton = (ImageButton) view.findViewById(R.id.ok_confirm);
        okButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (trainingPlanHasTime())
                {
                    mListener.onFragmentClicked(SelectDateFragment.class.getName());
                } else
                {
                    // dialog display
                    promptYesNoDialog();
                }
            }

        });


        // back button
        ImageButton buttonBack = (ImageButton) view.findViewById(R.id.button_back);
        buttonBack.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mListener.onBackButtonClicked();
            }
        });

        // restart button
        ImageButton buttonRestart = (ImageButton) view.findViewById(R.id.button_restart);
        buttonRestart.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
            }
        });
        return view;
    }

    private void promptYesNoDialog()
    {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                switch(which)
                {
                    case DialogInterface.BUTTON_POSITIVE:
                        mListener.onFragmentClicked(SelectDateFragment.class.getName());
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Selecting this date might not give you enough time to properly train for the selected race. Would you like to continue?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }


    private boolean trainingPlanHasTime()
    {
        long trainingPlanLength = getPlanLengthInMillis(raceType, experienceLevel);
        Date currentDate = new Date();
        // if race date - current date is greater than the training plan length
        // 60 secs added to allow user time to decide
        return (date.getTime() - currentDate.getTime() + 60000) > trainingPlanLength;
    }

    private long getPlanLengthInMillis(String raceType, String experienceLevel)
    {
        long planLengthInMillis = 0;
        switch (raceType)
        {
            case GlobalVariables.RACE_5K:
                planLengthInMillis = GlobalVariables.WEEKS_OF_TRAINING_5K * 604800000L;
                break;
            case GlobalVariables.RACE_10K:
                planLengthInMillis = GlobalVariables.WEEKS_OF_TRAINING_10K * 604800000L;
                break;
            case GlobalVariables.RACE_HALF:
                planLengthInMillis = GlobalVariables.WEEKS_OF_TRAINING_HALF * 604800000L;
                break;
            case GlobalVariables.RACE_MARATHON:
                if (experienceLevel.equals(GlobalVariables.EXPERIENCE_BEGINNER))
                {
                    planLengthInMillis = GlobalVariables.WEEKS_OF_TRAINING_MARATHON_BEGINNER * 604800000L;
                } else
                {
                    planLengthInMillis = GlobalVariables.WEEKS_OF_TRAINING_MARATHON * 604800000L;
                }
                break;

        }

        return planLengthInMillis;
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if (context instanceof FragmentListenerInterface)
        {
            mListener = (FragmentListenerInterface) context;
        } else
        {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void passName(String n)
    {
        // do nothing
    }

    @Override
    public void passRaceType(String r)
    {
        raceType = r;
    }

    @Override
    public void passExperienceLevel(String e)
    {
        experienceLevel = e;
    }

    @Override
    public void passDate(Date d)
    {
        // do nothing
    }
}
