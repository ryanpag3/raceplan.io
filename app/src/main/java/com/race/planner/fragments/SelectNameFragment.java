package com.race.planner.fragments;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.app.Fragment;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.race.planner.R;
import com.race.planner.activities.MainActivity;
import com.race.planner.utils.FragmentListenerInterface;

public class SelectNameFragment extends Fragment
{
    private FragmentListenerInterface mListener;
    private EditText editText;
    private String editTextEntry = "";

    public SelectNameFragment()
    {
        // Required empty public constructor
    }

    public static SelectNameFragment newInstance()
    {
        return new SelectNameFragment();
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
        View view = inflater.inflate(R.layout.fragment_select_name, container, false);

        editText = (EditText) view.findViewById(R.id.edit_text);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                // close keyboard on enter key pressed
                if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
                {
                    final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    // assign current name to variable, variable is saved to backstack and used for
                    // preventing keyboard opening on back button pressed
                    editTextEntry = editText.getText().toString();
                    imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });

        // force open keyboard on fragment call
        // editTextEntry is defined when the enter key on the keyboard is pressed
        if (editTextEntry.equals(""))
        {
            ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }

        ImageButton buttonConfirm = (ImageButton) view.findViewById(R.id.button_confirm);
        buttonConfirm.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mListener.passName(editText.getText().toString());
//                    Toast toast = Toast.makeText(getActivity(), "Calendar name set to: " + editText.getText().toString(), Toast.LENGTH_SHORT);
//                    toast.setGravity(Gravity.CENTER|Gravity.TOP, 0, 0);
//                    toast.show();
                mListener.onFragmentClicked(SelectNameFragment.class.getName());
            }
        });

        // back button
        ImageButton buttonBack = (ImageButton) view.findViewById(R.id.button_back);
        buttonBack.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);            }
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

//    @Override
//    public void onDetach()
//    {
//        super.onDetach();
//        mListener = null;
//    }
//
//    @Override
//    public void onResume()
//    {
//        super.onResume();
//        editText.post(new Runnable()
//        {
//            @Override
//            public void run()
//            {
//                editText.requestFocus();
//                InputMethodManager imgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//                imgr.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
//            }
//        });
//    }
}
