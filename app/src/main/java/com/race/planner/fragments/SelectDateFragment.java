package com.race.planner.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import com.race.planner.R;
import com.race.planner.activities.MainActivity;
import com.race.planner.activities.SelectTrainingPlan;
import com.race.planner.utils.FragmentListenerInterface;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentListenerInterface} interface
 * to handle interaction events.
 * Use the {@link SelectDateFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SelectDateFragment extends Fragment
{
    FragmentListenerInterface mListener;
    private DatePicker datePicker;
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
        final String fragmentName = SelectDateFragment.class.getName();
        final Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        date = cal.getTime();
        // set default race date to avoid crashing on pressing back button
        mListener.passDate(date);



        datePicker = (DatePicker) view.findViewById(R.id.date_picker_widget);
        datePicker.init(year, month, day, new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker datePicker, int i, int i1, int i2) {
                date = new GregorianCalendar(i, i1, i2).getTime();
                mListener.passDate(date);
                Log.i(getActivity().getLocalClassName(), date.toString());
                // do something with the date here
            }
        });

        Button okButton = (Button) view.findViewById(R.id.ok_clickable_textview);
        okButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mListener.onFragmentClicked(SelectDateFragment.class.getName());
            }
        });


        // back button
        Button buttonBack = (Button) view.findViewById(R.id.button_back);
        buttonBack.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mListener.onBackButtonClicked();
            }
        });

        // restart button
        Button buttonRestart = (Button) view.findViewById(R.id.button_restart);
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
}
