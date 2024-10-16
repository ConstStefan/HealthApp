package com.example.proiectlicenta.ui.profile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.proiectlicenta.R;
import com.example.proiectlicenta.ui.login.LoginActivity;
import com.example.proiectlicenta.ui.steps.StepCounterService;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        //open login activity
        Button buttonLogin=findViewById(R.id.button_login);
        buttonLogin.setOnClickListener(v -> {
            Intent intent1 =new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent1);
        });
        //open register activity
        Button buttonRegister=findViewById(R.id.button_register);
        buttonRegister.setOnClickListener(v -> {
            Intent intent12 =new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent12);
        });


    }
}