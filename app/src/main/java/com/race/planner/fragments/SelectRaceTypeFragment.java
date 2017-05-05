package com.race.planner.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.race.planner.R;
import com.race.planner.activities.MainActivity;
import com.race.planner.utils.FragmentCommunicator;
import static com.race.planner.data_models.GlobalVariables.*;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentCommunicator} interface
 * to handle interaction events.
 * Use the {@link SelectRaceTypeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SelectRaceTypeFragment extends Fragment
{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private FragmentCommunicator mListener;

    public SelectRaceTypeFragment()
    {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SelectRaceTypeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SelectRaceTypeFragment newInstance(String param1, String param2)
    {
        SelectRaceTypeFragment fragment = new SelectRaceTypeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_select_race_type, container, false);


        // 5k button
        Button button5K = (Button) view.findViewById(R.id.button_5k);
        button5K.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mListener.passRaceType(RACE_5K);
                startNextSelection();
            }
        });

        // 10k button
        Button button10K = (Button) view.findViewById(R.id.button_10k);
        button10K.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mListener.passRaceType(RACE_10K);
                startNextSelection();
            }
        });

        // half-marathon button
        Button buttonHalfMarathon = (Button) view.findViewById(R.id.button_half_marathon);
        buttonHalfMarathon.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mListener.passRaceType(RACE_HALF);
                startNextSelection();
            }
        });

        // marathon button
        Button buttonMarathon = (Button) view.findViewById(R.id.button_marathon);
        buttonMarathon.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mListener.passRaceType(RACE_MARATHON);
                startNextSelection();
            }
        });

        // back button
        Button buttonBack = (Button) view.findViewById(R.id.button_back);
        buttonBack.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);

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
        if (context instanceof FragmentCommunicator)
        {
            mListener = (FragmentCommunicator) context;
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

    public void startNextSelection()
    {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.animator.enter_from_right, R.animator.exit_to_left);
        SelectExperienceLevelFragment selectExperienceLevelFragment = new SelectExperienceLevelFragment();
        fragmentTransaction.replace(R.id.race_type_container, selectExperienceLevelFragment);
        fragmentTransaction.commit();
    }
}
