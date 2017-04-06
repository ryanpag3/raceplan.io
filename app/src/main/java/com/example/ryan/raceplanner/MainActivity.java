package com.example.ryan.raceplanner;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Calendar;

/**
 * TODO:
 * Develop calendar generator using raceType, experienceLevel, dateOfRace
 * Push to google calendar API
 */


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener
{
    public static final String EXTRA_MESSAGE = "com.example.RacePlanner.MESSAGE";
    Spinner raceTypeSpinner;
    Spinner experienceLevelSpinner;
    String raceType;
    String experienceLevel;
    String dateOfRace;

    // getters
    public String returnRaceType()
    {
        return this.raceType;
    }

    public String returnExperienceLevel()
    {
        return this.experienceLevel;
    }

    public String returnDateOfRace()
    {
        return this.dateOfRace;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // adapter for raceType spinner

        raceTypeSpinner = (Spinner) findViewById(R.id.spinner);
        final ArrayAdapter<CharSequence> raceAdapt = ArrayAdapter.createFromResource(this, R.array.race_type, android.R.layout.simple_spinner_item);
        raceAdapt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        raceTypeSpinner.setAdapter(raceAdapt);
        raceTypeSpinner.setOnItemSelectedListener(this);

        // adapter for experience level spinner
        experienceLevelSpinner = (Spinner) findViewById(R.id.expSpinner);
        ArrayAdapter<CharSequence> expAdapt = ArrayAdapter.createFromResource(this, R.array.experience, android.R.layout.simple_spinner_item);
        expAdapt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        experienceLevelSpinner.setAdapter(expAdapt);
        experienceLevelSpinner.setOnItemSelectedListener(this);

        // grabbing reference to datepicker widget
        DatePicker datePicker = (DatePicker) findViewById(R.id.datepicker);
        final Calendar c = Calendar.getInstance();
        datePicker.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), new MyOnDateChangedListener());

        // button generation
        Button button = (Button) findViewById(R.id.generateButton);
        button.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                // checks to make sure values have been set
                if (raceType != null && experienceLevel != null && dateOfRace != null)
                {
                    Intent intent = new Intent(MainActivity.this, GenerateTrainingPlan.class);
                    startActivity(intent);
                }
            }
        });

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    {
        Toast toast;
        if (pos != 0)
        {
            switch (parent.getId())
            {
                // assigns spinner selection to variable
                // not sure what scope these variables need to be, might need to make them global
                // need to also change spinner XML ids to match their variable names

                    case R.id.spinner:
                        raceType = (String) parent.getItemAtPosition(pos);
                        break;

                    case R.id.expSpinner:
                        experienceLevel = (String) parent.getItemAtPosition(pos);
                        break;
                }
            }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {
        // TODO...?
    }

    // Listener class for DatePicker widget
    private class MyOnDateChangedListener implements DatePicker.OnDateChangedListener
    {
        @Override
        public void onDateChanged(DatePicker parent, int year, int month, int day)
        {
            // I dont think we will need to convert these values to a string and then back.
            // Probably will just be a method call to Calendar or some shit for the google api
            dateOfRace = (month+1) + "-" + day + "-" + year;
        }
    }
}
