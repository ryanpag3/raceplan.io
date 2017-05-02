package com.ryan.page.raceplanner;

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
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import java.util.Calendar;

import static com.ryan.page.raceplanner.GlobalVariables.*;


public class SelectTrainingPlan extends AppCompatActivity implements AdapterView.OnItemSelectedListener
{
    private static final String TAG = GenerateTrainingPlan.class.getName();
    private boolean createNewCalendar = true;
    Spinner raceTypeSpinner;
    Spinner experienceLevelSpinner;
    RacerInfo racerInfo = new RacerInfo();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_training_plan);
        final EditText editText = (EditText) findViewById(R.id.edit_enter_name);


        // generate raceType spinner UI element
        raceTypeSpinner = (Spinner) findViewById(R.id.spinner);
        final ArrayAdapter<CharSequence> raceAdapt = ArrayAdapter.createFromResource(this, R.array.main_race_type_spinner_strings, android.R.layout.simple_spinner_item);
        raceAdapt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        raceTypeSpinner.setAdapter(raceAdapt);
        raceTypeSpinner.setOnItemSelectedListener(this);

        // generate experienceLevel spinner UI element
        experienceLevelSpinner = (Spinner) findViewById(R.id.expSpinner);
        ArrayAdapter<CharSequence> expAdapt = ArrayAdapter.createFromResource(this, R.array.main_experience_spinner_strings, android.R.layout.simple_spinner_item);
        expAdapt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        experienceLevelSpinner.setAdapter(expAdapt);
        experienceLevelSpinner.setOnItemSelectedListener(this);

        // generate DatePicker UI element
        DatePicker datePicker = (DatePicker) findViewById(R.id.datepicker);
        final Calendar c = Calendar.getInstance();
        datePicker.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), new MyOnDateChangedListener());

        // generate switch for creating new calendar
        Switch switchNewCalendar = (Switch) findViewById(R.id.main_switch_new_calendar);
        switchNewCalendar.setChecked(true);
        switchNewCalendar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                createNewCalendar = isChecked;
            }
        });

        // button for calling next activity
        Button button = (Button) findViewById(R.id.generateButton);
        button.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                // check for correct selections
                if (racerInfo.isComplete())
                {
                    if (editText.getText().toString().matches(""))
                    {
                        Log.i(TAG, "edittext empty");
                        racerInfo.nameOfPlan = "unnamed";
                    } else
                    {
                        Log.i(TAG, " edittext not empty");
                        racerInfo.nameOfPlan = editText.getText().toString();
                    }

                    Intent intent = new Intent(SelectTrainingPlan.this, AuthenticateCalendarAPI.class);
                    intent.putExtra(GlobalVariables.RACER_INFO_ID, racerInfo);
                    intent.putExtra(GlobalVariables.CREDENTIAL_ACCOUNT_NAME, getIntent()
                            .getExtras().getString(GlobalVariables.CREDENTIAL_ACCOUNT_NAME));
                    intent.putExtra(GlobalVariables.CREATE_CALENDAR_BOOL, createNewCalendar);
                    startActivity(intent);

                } else
                {
                    Toast toast = Toast.makeText(SelectTrainingPlan.this, "Please select all three options", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 172);
                    toast.show();
                }
            }
        });
    }


    /**
     * This method checks for permissions for the internal calendar API to write and read. This
     * is all that is necessary to run the app if the user does not wish to create a new Calendar
     * to host their training plan on.
     */
    private void requestPermissions()
    {
        if (ContextCompat.checkSelfPermission(SelectTrainingPlan.this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(SelectTrainingPlan.this,
                    new String[]{Manifest.permission.READ_CALENDAR},
                    MY_PERMISSIONS_REQUEST_READ_CALENDAR);
        }

        if (ContextCompat.checkSelfPermission(SelectTrainingPlan.this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(SelectTrainingPlan.this,
                    new String[]{Manifest.permission.WRITE_CALENDAR},
                    MY_PERMISSIONS_REQUEST_WRITE_CALENDAR);
        }

        if (ContextCompat.checkSelfPermission(SelectTrainingPlan.this, Manifest.permission.READ_SYNC_SETTINGS) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(SelectTrainingPlan.this,
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
            racerInfo.month = month; // zero based index for month
            racerInfo.day = day;
        }

    }
}
