package com.race.planner.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.race.planner.R;
import com.race.planner.activities.MainActivity;
import com.race.planner.utils.*;

import static com.race.planner.data_models.GlobalVariables.*;

public class SelectExperienceLevelFragment extends Fragment
{
    private FragmentListenerInterface mListener;

    public SelectExperienceLevelFragment()
    {
        // Required empty public constructor
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
        View view = inflater.inflate(R.layout.fragment_select_experience_level, container, false);
        final String fragmentName = SelectExperienceLevelFragment.class.getName();

        // beginner button
        Button buttonExpBeginner = (Button) view.findViewById(R.id.button_expBeginner);
        buttonExpBeginner.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mListener.passExperienceLevel(EXPERIENCE_BEGINNER);
                mListener.onFragmentClicked(fragmentName);

            }
        });

        // intermediate button
        Button buttonExpIntermediate = (Button) view.findViewById(R.id.button_expIntermediate);
        buttonExpIntermediate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mListener.passExperienceLevel(EXPERIENCE_INTERMEDIATE);
                mListener.onFragmentClicked(fragmentName);
            }
        });

        // expert button
        Button buttonExpExpert = (Button) view.findViewById(R.id.button_expExpert);
        buttonExpExpert.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mListener.passExperienceLevel(EXPERIENCE_EXPERT);
                mListener.onFragmentClicked(fragmentName);
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

        // Inflate the layout for this fragment
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
