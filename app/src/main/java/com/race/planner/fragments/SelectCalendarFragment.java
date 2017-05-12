package com.race.planner.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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

        Button chooseOwnCalendar = (Button) view.findViewById(R.id.button_choose_own_cal);
        chooseOwnCalendar.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mListener.passCalCreatedBool(false);
                mListener.onFragmentClicked(fragmentName);
            }
        });

        Button chooseNewCalendar = (Button) view.findViewById(R.id.button_choose_new_cal);
        chooseNewCalendar.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mListener.passCalCreatedBool(true);
                mListener.onFragmentClicked(fragmentName);
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
