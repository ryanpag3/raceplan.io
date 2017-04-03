package com.example.ryan.raceplanner;

import android.app.DatePickerDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    Spinner raceTypeSpinner;
    Spinner experienceLevelSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        raceTypeSpinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> raceAdapt = ArrayAdapter.createFromResource(this, R.array.race_type, android.R.layout.simple_spinner_item);
        raceAdapt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        raceTypeSpinner.setAdapter(raceAdapt);
        raceTypeSpinner.setOnItemSelectedListener(this);

        experienceLevelSpinner = (Spinner) findViewById(R.id.expSpinner);
        ArrayAdapter<CharSequence> expAdapt = ArrayAdapter.createFromResource(this, R.array.experience, android.R.layout.simple_spinner_item);
        expAdapt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        experienceLevelSpinner.setAdapter(expAdapt);
        experienceLevelSpinner.setOnItemSelectedListener(this);


    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }



}
