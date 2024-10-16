package com.example.proiectlicenta.ui.steps;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.preference.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class StepCounterService extends Service implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor stepSensor;
    private boolean running = false;
    private int currentSteps = 0;
    private int previousTotalSteps = 0;
    private int initialSteps = 0;
    private int lastTotalSteps = 0;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private SharedPreferences sharedPreferences;
    private String todayDate;
    private String deviceID;

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        auth = FirebaseAuth.getInstance();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        deviceID = getDeviceID();
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            todayDate = getTodayDate();
            databaseReference = FirebaseDatabase.getInstance().getReference("Registered Users")
                    .child(user.getUid()).child("steps").child(todayDate).child(deviceID);
            loadStepCounts();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
            running = true;
        } else {
            Toast.makeText(this, "No Step Sensor!", Toast.LENGTH_SHORT).show();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (running) {
            sensorManager.unregisterListener(this);
            saveStepCounts();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (running) {
            checkDateChange();
            int totalSteps = (int) event.values[0];

            // daca pasii initiali nu sunt setati, ii setam la totalsteps
            if (initialSteps == 0) {
                initialSteps = totalSteps;
                saveInitialSteps();
            }

            // in cazul unui reboot
            if (totalSteps < lastTotalSteps) {
                previousTotalSteps += lastTotalSteps;
            }

            currentSteps = totalSteps - initialSteps + previousTotalSteps;
            lastTotalSteps = totalSteps;

            saveStepCounts();
            saveStepCountToFirebase();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void saveStepCounts() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("currentSteps", currentSteps);
        editor.putInt("previousTotalSteps", previousTotalSteps);
        editor.putInt("initialSteps", initialSteps);
        editor.putInt("lastTotalSteps", lastTotalSteps);
        editor.putString("lastSavedDate", todayDate);
        editor.putString("deviceID", deviceID);
        editor.apply();
    }

    private void loadStepCounts() {
        currentSteps = sharedPreferences.getInt("currentSteps", 0);
        previousTotalSteps = sharedPreferences.getInt("previousTotalSteps", 0);
        initialSteps = sharedPreferences.getInt("initialSteps", 0);
        lastTotalSteps = sharedPreferences.getInt("lastTotalSteps", 0);
        todayDate = sharedPreferences.getString("lastSavedDate", getTodayDate());

        // reset la o noua zi
        if (!todayDate.equals(getTodayDate())) {
            currentSteps = 0;
            previousTotalSteps = 0;
            initialSteps = 0;
            lastTotalSteps = 0;
            todayDate = getTodayDate();
            saveStepCounts();
        }
    }

    private void saveInitialSteps() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("initialSteps", initialSteps);
        editor.apply();
    }

    private String getDeviceID() {
        String deviceID = sharedPreferences.getString("deviceID", null);
        if (deviceID == null) {
            deviceID = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("deviceID", deviceID);
            editor.apply();
        }
        return deviceID;
    }

    private String getTodayDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }


    private void checkDateChange() {
        String currentDate = getTodayDate();
        if (!currentDate.equals(todayDate)) {
            todayDate = currentDate;
            currentSteps = 0;
            previousTotalSteps = 0;
            initialSteps = 0;
            lastTotalSteps = 0;
            saveStepCounts();
            FirebaseUser currentUser = auth.getCurrentUser();
            if (currentUser != null) {
                databaseReference = FirebaseDatabase.getInstance().getReference("Registered Users")
                        .child(currentUser.getUid()).child("steps").child(todayDate).child(deviceID);
            } else {
                databaseReference = null;
            }
        }
    }

    private void saveStepCountToFirebase() {
        if (databaseReference != null) {
            databaseReference.setValue(currentSteps);
        }
    }
}
