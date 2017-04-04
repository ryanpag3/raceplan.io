package com.example.ryan.raceplanner;

import android.app.DatePickerDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * TODO:
 * Figure out how to assign variable/object through the datePicker
 * Develop calendar generator using raceType, experienceLevel, dateOfRace
 * Push to google calendar API
 */


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener
{
    Spinner raceTypeSpinner;
    Spinner experienceLevelSpinner;
    String raceType;
    String experienceLevel;
    String dateOfRace;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // adapter for raceType spinner
        raceTypeSpinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> raceAdapt = ArrayAdapter.createFromResource(this, R.array.race_type, android.R.layout.simple_spinner_item);
        raceAdapt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        raceTypeSpinner.setAdapter(raceAdapt);
        raceTypeSpinner.setOnItemSelectedListener(this);

        // adapter for experience level spinner
        experienceLevelSpinner = (Spinner) findViewById(R.id.expSpinner);
        ArrayAdapter<CharSequence> expAdapt = ArrayAdapter.createFromResource(this, R.array.experience, android.R.layout.simple_spinner_item);
        expAdapt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        experienceLevelSpinner.setAdapter(expAdapt);
        experienceLevelSpinner.setOnItemSelectedListener(this);

        // debug log, checking spinner id's
        Log.w("rts id: ", Integer.toString(raceTypeSpinner.getId()));
        Log.w("els id: ", Integer.toString(experienceLevelSpinner.getId()));


    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    {
        Toast toast;
        switch (parent.getId())
        {
            // assigns spinner selection to variable
            // not sure what scope these variables need to be, might need to make them global
            // also displays toasts to confirm spinner selection
            // might be able to remove duplicate code for the toasts

            case R.id.spinner:
                raceType = (String) parent.getItemAtPosition(pos);
                toast = Toast.makeText(MainActivity.this, parent.getItemAtPosition(pos).toString(), Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.TOP, 0, 0);
                toast.show();
                break;
            case R.id.expSpinner:
                experienceLevel = (String) parent.getItemAtPosition(pos);
                toast = Toast.makeText(MainActivity.this, parent.getItemAtPosition(pos).toString(), Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.TOP, 0, 0);
                toast.show();
                break;

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView)
    {
        // TODO: 4/4/17
    }


}
