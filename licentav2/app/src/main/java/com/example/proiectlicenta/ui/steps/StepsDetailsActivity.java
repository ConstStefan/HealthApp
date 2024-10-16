package com.example.proiectlicenta.ui.steps;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.proiectlicenta.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.highlight.Highlight;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class StepsDetailsActivity extends AppCompatActivity {

    private TextView textViewDailyGoal, textViewStepsToday;
    private ProgressBar progressBarSteps;
    private BarChart barChartStepsHistory;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private DatabaseReference goalReference;
    private List<BarEntry> barEntries;
    private List<String> dates;
    private int dailyGoal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_details);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        getSupportActionBar().setTitle("Steps");
        textViewDailyGoal = findViewById(R.id.textViewDailyGoal);
        textViewStepsToday = findViewById(R.id.textViewStepsToday);
        progressBarSteps = findViewById(R.id.progressBarSteps);
        barChartStepsHistory = findViewById(R.id.barChartStepsHistory);

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        barEntries = new ArrayList<>();
        dates = new ArrayList<>();

        if (user != null) {
            databaseReference = FirebaseDatabase.getInstance().getReference("Registered Users")
                    .child(user.getUid()).child("steps");
            goalReference = FirebaseDatabase.getInstance().getReference("Registered Users")
                    .child(user.getUid()).child("dailyGoal");
            loadStepsData();
            loadDailyGoal();
        }

        textViewDailyGoal.setOnClickListener(v -> showEditGoalDialog());
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_steps_details, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (itemId == R.id.action_set_daily_goal) {
            showEditGoalDialog();
            return true;
        }
        else if (itemId == R.id.action_show_chart) {
            Intent intent = new Intent(StepsDetailsActivity.this, StepsChartActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadStepsData() {
        if (databaseReference != null) {
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    barEntries.clear();
                    dates.clear();
                    int index = 0;
                    for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
                        String date = dateSnapshot.getKey();
                        int totalStepsForDate = 0;
                        for (DataSnapshot deviceSnapshot : dateSnapshot.getChildren()) {
                            Integer deviceSteps = deviceSnapshot.getValue(Integer.class);
                            if (deviceSteps != null) {
                                totalStepsForDate += deviceSteps;
                            }
                        }
                        barEntries.add(new BarEntry(index, totalStepsForDate));
                        dates.add(formatDate(date));

                        assert date != null;
                        if (date.equals(getTodayDate())) {
                            textViewStepsToday.setText("Steps Today: " + totalStepsForDate);
                            progressBarSteps.setProgress(totalStepsForDate);
                        }
                        index++;
                    }
                    updateBarChart();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }


    private void loadDailyGoal() {
        if (goalReference != null) {
            goalReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        dailyGoal = dataSnapshot.getValue(Integer.class);
                        textViewDailyGoal.setText("Daily Goal: " + dailyGoal + " steps");
                        progressBarSteps.setMax(dailyGoal);
                        updateBarChart();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void updateBarChart() {
        BarDataSet barDataSet = new BarDataSet(barEntries, "");
        barDataSet.setColors(getBarColors());
        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(0.9f);

        barChartStepsHistory.setData(barData);
        barChartStepsHistory.setFitBars(true);
        barChartStepsHistory.invalidate(); // refresh the chart
        barChartStepsHistory.getDescription().setEnabled(false);
        barChartStepsHistory.getLegend().setEnabled(false);


        XAxis xAxis = barChartStepsHistory.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return dates.size() > (int) value ? dates.get((int) value) : "";
            }
        });
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis leftAxis = barChartStepsHistory.getAxisLeft();
        leftAxis.setAxisMinimum(0);
        leftAxis.removeAllLimitLines();

        int maxSteps = getMaxSteps(barEntries);
        leftAxis.setAxisMaximum(Math.max(maxSteps, dailyGoal) * 1.1f); // add 10% for visibility

        LimitLine goalLine = new LimitLine(dailyGoal, "Goal");
        goalLine.setLineColor(Color.GREEN);
        goalLine.setLineWidth(2f);
        goalLine.enableDashedLine(10f, 10f, 0f);
        leftAxis.addLimitLine(goalLine);

        LimitLine halfGoalLine = new LimitLine((float) dailyGoal / 2, "Half Goal");
        halfGoalLine.setLineColor(Color.GRAY);
        halfGoalLine.setLineWidth(2f);
        halfGoalLine.enableDashedLine(10f, 10f, 0f);
        leftAxis.addLimitLine(halfGoalLine);

        barChartStepsHistory.getAxisRight().setEnabled(false);

        barChartStepsHistory.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                int steps = (int) e.getY();
                textViewStepsToday.setText("Steps Today: " + steps);
                progressBarSteps.setProgress(steps);
            }

            @Override
            public void onNothingSelected() {
            }
        });
    }

    private int[] getBarColors() {
        int[] colors = new int[barEntries.size()];
        for (int i = 0; i < barEntries.size(); i++) {
            if (barEntries.get(i).getY() >= dailyGoal) {
                colors[i] = Color.GREEN;
            } else {
                colors[i] = Color.GRAY;
            }
        }
        return colors;
    }

    private String formatDate(String date) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
            Date parsedDate = inputFormat.parse(date);
            return parsedDate != null ? outputFormat.format(parsedDate) : date;
        } catch (Exception e) {
            return date;
        }
    }

    private void showEditGoalDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Daily Goal");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            int newGoal = Integer.parseInt(input.getText().toString());
            goalReference.setValue(newGoal);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private String getTodayDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    private int getMaxSteps(List<BarEntry> entries) {
        int max = 0;
        for (BarEntry entry : entries) {
            if (entry.getY() > max) {
                max = (int) entry.getY();
            }
        }
        return max;
    }
}
