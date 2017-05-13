package com.race.planner.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Toast;

import com.race.planner.R;
import com.race.planner.activities.MainActivity;
import com.race.planner.utils.FragmentListenerInterface;

public class SelectCalendarFragment extends Fragment
{
    private FragmentListenerInterface mListener;

    public SelectCalendarFragment()
    {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SelectCalendarFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SelectCalendarFragment newInstance(String param1, String param2)
    {
        return new SelectCalendarFragment();
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_select_calendar, container, false);
        final String fragmentName = SelectCalendarFragment.class.getName();

        final CheckBox checkBoxOwnCalendar = (CheckBox) view.findViewById(R.id.checkbox_own_cal);
        final CheckBox checkBoxNewCalendar = (CheckBox) view.findViewById(R.id.checkbox_new_cal);

        checkBoxOwnCalendar.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (checkBoxOwnCalendar.isChecked())
                {
                    mListener.passCalCreatedBool(false);
                    checkBoxNewCalendar.setChecked(false);
                }
            }
        });

        checkBoxNewCalendar.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (checkBoxNewCalendar.isChecked())
                {
                    mListener.passCalCreatedBool(true);
                    checkBoxOwnCalendar.setChecked(false);
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

        ImageButton buttonConfirm = (ImageButton) view.findViewById(R.id.button_confirm);
        buttonConfirm.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (checkBoxNewCalendar.isChecked() || checkBoxOwnCalendar.isChecked())
                {
                    mListener.onFragmentClicked(fragmentName);
                } else
                {
                    Toast.makeText(getActivity(), "please select an option", Toast.LENGTH_LONG).show();
                }
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
