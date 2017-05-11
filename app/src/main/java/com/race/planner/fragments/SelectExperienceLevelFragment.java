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
import com.race.planner.utils.*;

import static com.race.planner.data_models.GlobalVariables.*;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SelectExperienceLevelFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SelectExperienceLevelFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SelectExperienceLevelFragment extends Fragment
{
    private FragmentListenerInterface mListener;

    public SelectExperienceLevelFragment()
    {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SelectExperienceLevelFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SelectExperienceLevelFragment newInstance(String param1, String param2)
    {
        return new SelectExperienceLevelFragment();
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
