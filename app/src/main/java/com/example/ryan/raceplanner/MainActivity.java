package com.example.ryan.raceplanner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import java.util.Calendar;

import static com.example.ryan.raceplanner.GlobalVariables.*;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener
{
    private static final String TAG = GenerateTrainingPlan.class.getName();
    private boolean createNewCalendar = false;
    Spinner raceTypeSpinner;
    Spinner experienceLevelSpinner;
    RacerInfo racerInfo = new RacerInfo();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissions();
        generateRaceTypeSpinner();
        generateExperienceLevelSpinner();
        generateDatePickerWidget();
        generateNewCalendarSwitch();
        generateGoButton();

    }


    /**
     * This method checks for permissions for the internal calendar API to write and read. This
     * is all that is necessary to run the app if the user does not wish to create a new Calendar
     * to host their training plan on.
     */
    private void requestPermissions()
    {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_CALENDAR},
                    MY_PERMISSIONS_REQUEST_READ_CALENDAR);
        }

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_CALENDAR},
                    MY_PERMISSIONS_REQUEST_WRITE_CALENDAR);
        }

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_SYNC_SETTINGS) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_SYNC_SETTINGS},
                    MY_PERMISSIONS_REQUEST_READ_SYNC_SETTINGS);
        }
    }


    /**
     * This method will set the race type and experience level based on the users choice. It
     * handles both spinners using a switch.
     * @param parent this is the SpinnerView
     * @param view Unused parameter. Defines the individual list item selected.
     * @param pos Position of list item in SpinnerView with [0] being the first location.
     * @param id The row id of the item selected.
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    {
        if (pos != 0)
        {
            switch (parent.getId())
            {
                case R.id.spinner:
                    racerInfo.raceType = (String) parent.getItemAtPosition(pos);
                    Log.i(TAG, "View: " + view.toString());
                    break;

                case R.id.expSpinner:
                    racerInfo.experienceLevel = (String) parent.getItemAtPosition(pos);
                    Log.i(TAG, view.toString());
                    break;
            }
        }
    }

    /**
     * Unused
     * @param parent
     */
    @Override
    public void onNothingSelected(AdapterView<?> parent) { }

    /**
     * Listener class for DatePicker widget. Assigns dateOfRace object to currently selected date
     * on the spinner.
     */
    private class MyOnDateChangedListener implements DatePicker.OnDateChangedListener
    {
        @Override
        public void onDateChanged(DatePicker parent, int year, int month, int day)
        {
            racerInfo.year = year;
            racerInfo.month = month + 1; // zero based index for month
            racerInfo.day = day;
        }

    }

    /**
     * Generates the spinner UI element for selecting race type.
     */
    private void generateRaceTypeSpinner()
    {
        raceTypeSpinner = (Spinner) findViewById(R.id.spinner);
        final ArrayAdapter<CharSequence> raceAdapt = ArrayAdapter.createFromResource(this, R.array.main_race_type_spinner_strings, android.R.layout.simple_spinner_item);
        raceAdapt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        raceTypeSpinner.setAdapter(raceAdapt);
        raceTypeSpinner.setOnItemSelectedListener(this);

    }

    /**
     * Generates the spinner UI element for selecting experience level.
     */
    private void generateExperienceLevelSpinner()
    {
        experienceLevelSpinner = (Spinner) findViewById(R.id.expSpinner);
        ArrayAdapter<CharSequence> expAdapt = ArrayAdapter.createFromResource(this, R.array.main_experience_spinner_strings, android.R.layout.simple_spinner_item);
        expAdapt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        experienceLevelSpinner.setAdapter(expAdapt);
        experienceLevelSpinner.setOnItemSelectedListener(this);
    }

    /**
     * Generates the spinner UI element for selecting the date.
     */
    private void generateDatePickerWidget()
    {
        DatePicker datePicker = (DatePicker) findViewById(R.id.datepicker);
        final Calendar c = Calendar.getInstance();
        datePicker.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), new MyOnDateChangedListener());
    }

    /**
     *
     */
    private void generateNewCalendarSwitch(){
        Switch switchNewCalendar = (Switch) findViewById(R.id.main_switch_new_calendar);
        switchNewCalendar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
                 public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                 {
                     if(isChecked)
                     {
                         createNewCalendar = true;
                     } else
                     {
                         createNewCalendar = false;
                     }
                 }
        });
    }

    /**
     * Generates the button UI element.
     * When the button is pressed and onClick is called, it checks to make sure all selections have
     * been made and then assigns an intent object to be passed to the GenerateTrainingPlan class.
     * Then it starts that activity.
     *
     * If all selections have not been made, it presents a toast to the user to make sure all
     * are selected.
     */
    private void generateGoButton()
    {
        Button button = (Button) findViewById(R.id.generateButton);
        button.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                // check for correct selections
                if (/*racerInfo.isComplete()*/ true)
                {
                    if(createNewCalendar)
                    {
                        Intent intent = new Intent(MainActivity.this, AuthenticateCalendarAPI.class);
                        intent.putExtra(GlobalVariables.RACER_INFO_ID, racerInfo);
                        startActivity(intent);
                    } else
                    {
                        Intent intent = new Intent(MainActivity.this, GenerateTrainingPlan.class);
                        intent.putExtra(GlobalVariables.RACER_INFO_ID, racerInfo);
                        startActivity(intent);
                    }

                } else
                {
                    Toast toast = Toast.makeText(MainActivity.this, "Please select all three options", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 172);
                    toast.show();
                }
            }
        });
    }
}
