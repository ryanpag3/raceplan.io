package com.race.planner.activities;

import android.support.v4.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Activity;
import android.os.Bundle;
import android.widget.Spinner;
import java.util.Date;

import com.race.planner.R;
import com.race.planner.data_models.Racer;
import com.race.planner.fragments.SelectRaceTypeFragment;
import com.race.planner.utils.FragmentCommunicator;

public class SelectTrainingPlan extends Activity implements FragmentCommunicator// implements AdapterView.OnItemSelectedListener
{
    private static final String TAG = SelectTrainingPlan.class.getName();
    private boolean createNewCalendar = true;
    Spinner raceTypeSpinner;
    Spinner experienceLevelSpinner;
    Racer racer = new Racer();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_training_plan);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        //fragmentTransaction.setCustomAnimations(R.animator.enter_from_right, R.animator.exit_to_left);
        SelectRaceTypeFragment selectRaceTypeFragment = new SelectRaceTypeFragment();
        fragmentTransaction.add(R.id.fragment_select_race_type, selectRaceTypeFragment);
        fragmentTransaction.commit();





        /**
         * This is the code for the non-dynamic UI, use as backup if dynamic ui breaks.
         */
//        final EditText editText = (EditText) findViewById(R.id.edit_enter_name);
//        // generate raceType spinner UI element
//        raceTypeSpinner = (Spinner) findViewById(R.id.spinner);
//        final ArrayAdapter<CharSequence> raceAdapt = ArrayAdapter.createFromResource(this, R.array.main_race_type_spinner_strings, android.R.layout.simple_spinner_item);
//        raceAdapt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        raceTypeSpinner.setAdapter(raceAdapt);
//        raceTypeSpinner.setOnItemSelectedListener(this);
//
//        // generate experienceLevel spinner UI element
//        experienceLevelSpinner = (Spinner) findViewById(R.id.expSpinner);
//        ArrayAdapter<CharSequence> expAdapt = ArrayAdapter.createFromResource(this, R.array.main_experience_spinner_strings, android.R.layout.simple_spinner_item);
//        expAdapt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        experienceLevelSpinner.setAdapter(expAdapt);
//        experienceLevelSpinner.setOnItemSelectedListener(this);
//
//        // generate DatePicker UI element
//        DatePicker datePicker = (DatePicker) findViewById(R.id.datepicker);
//        final Calendar c = Calendar.getInstance();
//        datePicker.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), new MyOnDateChangedListener());
//
//        // generate switch for creating new calendar
//        Switch switchNewCalendar = (Switch) findViewById(R.id.main_switch_new_calendar);
//        switchNewCalendar.setChecked(true);
//        switchNewCalendar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
//        {
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
//            {
//                createNewCalendar = isChecked;
//            }
//        });
//
//        // button for calling next activity
//        Button button = (Button) findViewById(R.id.generateButton);
//        button.setOnClickListener(new View.OnClickListener()
//        {
//            public void onClick(View v)
//            {
//                // check for correct selections
//                if (racer.isComplete())
//                {
//                    if (editText.getText().toString().matches(""))
//                    {
//                        Log.i(TAG, "edittext empty");
//                        racer.nameOfPlan = "unnamed";
//                    } else
//                    {
//                        Log.i(TAG, " edittext not empty");
//                        racer.nameOfPlan = editText.getText().toString();
//                    }
//
//                    Intent intent = new Intent(SelectTrainingPlan.this, AuthenticateAndCallAPI.class);
//                    intent.putExtra(GlobalVariables.RACER_INFO_ID, racer);
//                    intent.putExtra(GlobalVariables.CREDENTIAL_ACCOUNT_NAME, getIntent()
//                            .getExtras().getString(GlobalVariables.CREDENTIAL_ACCOUNT_NAME));
//                    intent.putExtra(GlobalVariables.CREATE_CALENDAR_BOOL, createNewCalendar);
//                    startActivity(intent);
//
//                } else
//                {
//                    Toast toast = Toast.makeText(SelectTrainingPlan.this, "Please select all three options", Toast.LENGTH_SHORT);
//                    toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 172);
//                    toast.show();
//                }
//            }
//        });
    }
    @Override
    public void passRaceType(String s)
    {
        racer.raceType = s;
    }

    @Override
    public void passDate(Date d)
    {
        // set up racer date object first
    }

    @Override
    public void passExperienceLevel(String e)
    {
        racer.experienceLevel = e;
    }


//
//
//    /**
//     * This method will set the race type and experience level based on the users choice. It
//     * handles both spinners using a switch.
//     * @param parent this is the SpinnerView
//     * @param view Unused parameter. Defines the individual list item selected.
//     * @param pos Position of list item in SpinnerView with [0] being the first location.
//     * @param id The row id of the item selected.
//     */
//    @Override
//    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
//    {
//        if (pos != 0)
//        {
//            switch (parent.getId())
//            {
//                case R.id.spinner:
//                    racer.raceType = (String) parent.getItemAtPosition(pos);
//                    Log.i(TAG, "View: " + view.toString());
//                    break;
//
//                case R.id.expSpinner:
//                    racer.experienceLevel = (String) parent.getItemAtPosition(pos);
//                    Log.i(TAG, view.toString());
//                    break;
//            }
//        }
//    }
//
//    /**
//     * Unused
//     * @param parent
//     */
//    @Override
//    public void onNothingSelected(AdapterView<?> parent) { }
//
//    /**
//     * Listener class for DatePicker widget. Assigns dateOfRace object to currently selected date
//     * on the spinner.
//     */
//    private class MyOnDateChangedListener implements DatePicker.OnDateChangedListener
//    {
//        @Override
//        public void onDateChanged(DatePicker parent, int year, int month, int day)
//        {
//            racer.year = year;
//            racer.month = month; // zero based index for month
//            racer.day = day;
//        }
//
//    }
}
