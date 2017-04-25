package com.example.ryan.raceplanner;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button openSelectTrainingPlanActivity = (Button) findViewById(R.id.button_open_select_training_plan_activity);
        openSelectTrainingPlanActivity.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(MainActivity.this, SelectTrainingPlan.class);
                startActivity(intent);
            }
        });

        Button openListTrainingPlansActivity = (Button) findViewById(R.id.button_open_list_training_plans_activity);
        openListTrainingPlansActivity.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(MainActivity.this, ListTrainingPlans.class);
                startActivity(intent);
            }
        });
    }
}
