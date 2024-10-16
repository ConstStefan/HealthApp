package com.example.proiectlicenta.ui.summary;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;


import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.proiectlicenta.R;
import com.example.proiectlicenta.ui.bmi.BmiFragment;
import com.example.proiectlicenta.ui.calorie.CalorieFragment;
import com.example.proiectlicenta.ui.bodyfat.BodyFatCalculatorFragment;
import com.example.proiectlicenta.ui.steps.StepCounterService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class SummaryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, StepCounterService.class);
        startService(intent);
        setContentView(R.layout.activity_summary);


        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setLabelVisibilityMode(NavigationBarView.LABEL_VISIBILITY_LABELED);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_bmi) {
                selectedFragment = new BmiFragment();
            } else if (itemId == R.id.navigation_calorie) {
                selectedFragment = new CalorieFragment();
            } else if (itemId == R.id.navigation_bodyfat) {
                selectedFragment = new BodyFatCalculatorFragment();
            } else if (itemId == R.id.navigation_summary) {
                selectedFragment = new SummaryFragment();
            }
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(R.id.nav_host_fragment, selectedFragment).commit();
            }
            return true;
        });

        // setam fragmentul implicit
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.navigation_summary);
        }
    }
}