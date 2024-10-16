package com.example.proiectlicenta.ui.steps;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.core.content.ContextCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.example.proiectlicenta.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class StepsChartActivity extends AppCompatActivity {

    private RadioGroup toggleGroup;
    private TextView textViewWeeklyAverage, textViewMonthlyAverage;
    private TextView textViewTotalStepsWeekly, textViewTotalStepsMonthly;
    private LineChart lineChart;
    private DatabaseReference databaseReference;
    private List<Entry> weeklyEntries;
    private List<Entry> monthlyEntries;
    private List<String> dates;
    private int dailyGoal;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_steps_chart);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);

        toggleGroup = findViewById(R.id.toggleGroup);
        textViewWeeklyAverage = findViewById(R.id.textViewWeeklyAverage);
        textViewMonthlyAverage = findViewById(R.id.textViewMonthlyAverage);
        textViewTotalStepsWeekly = findViewById(R.id.textViewTotalStepsWeekly);
        textViewTotalStepsMonthly = findViewById(R.id.textViewTotalStepsMonthly);
        lineChart = findViewById(R.id.lineChart);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            databaseReference = FirebaseDatabase.getInstance().getReference("Registered Users")
                    .child(user.getUid()).child("steps");
            DatabaseReference goalReference = FirebaseDatabase.getInstance().getReference("Registered Users")
                    .child(user.getUid()).child("dailyGoal");

            loadStepsData();
            goalReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Integer goal = dataSnapshot.getValue(Integer.class);
                    if (goal != null) {
                        dailyGoal = goal;
                    } else {
                        dailyGoal = 0;
                    }
                    updateChart();
                    updateTextColors();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        toggleGroup.setOnCheckedChangeListener((group, checkedId) -> updateChart());

       //selectam implicit pe 7 zile
        toggleGroup.check(R.id.buttonLast7Days);
    }

    private void loadStepsData() {
        if (databaseReference != null) {
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    List<Entry> tempWeeklyEntries = new ArrayList<>();
                    List<Entry> tempMonthlyEntries = new ArrayList<>();
                    dates = new ArrayList<>();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    SimpleDateFormat displaySdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
                    String currentDate = sdf.format(new Date());

                    List<String> allDates = new ArrayList<>();
                    List<Integer> allSteps = new ArrayList<>();

                    for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
                        String date = dateSnapshot.getKey();
                        int totalStepsForDate = 0;
                        for (DataSnapshot deviceSnapshot : dateSnapshot.getChildren()) {
                            Integer deviceSteps = deviceSnapshot.getValue(Integer.class);
                            if (deviceSteps != null) {
                                totalStepsForDate += deviceSteps;
                            }
                        }
                        allDates.add(date);
                        allSteps.add(totalStepsForDate);
                    }

                    int startIndex = Math.max(allDates.size() - 30, 0);

                    for (int i = startIndex; i < allDates.size(); i++) {
                        String date = allDates.get(i);
                        int steps = allSteps.get(i);
                        try {
                            dates.add(displaySdf.format(sdf.parse(date)));
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }

                        if (i >= allDates.size() - 7) {
                            tempWeeklyEntries.add(new Entry(i - startIndex, steps));
                        }
                        tempMonthlyEntries.add(new Entry(i - startIndex, steps));
                    }

                    weeklyEntries = tempWeeklyEntries;
                    monthlyEntries = tempMonthlyEntries;

                    int weeklySum = tempWeeklyEntries.stream().mapToInt(entry -> (int) entry.getY()).sum();
                    int monthlySum = tempMonthlyEntries.stream().mapToInt(entry -> (int) entry.getY()).sum();

                    textViewWeeklyAverage.setText(getString(R.string.weekly_average, weeklySum / Math.max(tempWeeklyEntries.size(), 1)));
                    textViewMonthlyAverage.setText(getString(R.string.monthly_average, monthlySum / Math.max(tempMonthlyEntries.size(), 1)));
                    textViewTotalStepsWeekly.setText(getString(R.string.total_steps_weekly, weeklySum));
                    textViewTotalStepsMonthly.setText(getString(R.string.total_steps_monthly, monthlySum));

                    updateChart();
                    updateTextColors();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
    }


    private void updateChart() {
        List<Entry> entries;
        if (toggleGroup.getCheckedRadioButtonId() == R.id.buttonLast7Days) {
            entries = weeklyEntries;
        } else {
            entries = monthlyEntries;
        }

        if (entries == null || entries.isEmpty()) {
            return;
        }

        LineDataSet dataSet = new LineDataSet(entries, "Steps");
        dataSet.setColor(ContextCompat.getColor(this,R.color.button_colorbmi)); //  line color
        dataSet.setLineWidth(2.5f); //  line width
        dataSet.setCircleColor(ContextCompat.getColor(this,R.color.blue)); //  circle color
        dataSet.setCircleRadius(5f); //  circle radius
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(ContextCompat.getColor(this,R.color.black));
        dataSet.setDrawFilled(true); // fill area
        dataSet.setFillColor(ContextCompat.getColor(this,R.color.blue)); //  fill color

        LineData lineData = new LineData(dataSet);
        lineData.setValueFormatter(new ValueFormatter() {
            @Override
            public String getPointLabel(Entry entry) {
                return String.valueOf((int) entry.getY());
            }
        });

        lineChart.setData(lineData);
        lineChart.invalidate();

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                int index = (int) value;
                if (index >= 0 && index < dates.size()) {
                    return dates.get(index);
                } else {
                    return "";
                }
            }
        });

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setAxisMinimum(0);
        leftAxis.removeAllLimitLines();

        // ajustez y-ul ca sa se vada goalul
        float maxY = Math.max(dailyGoal, getMaxValue(entries));
        leftAxis.setAxisMaximum(maxY + (maxY * 0.1f)); // 10% padding

        if (dailyGoal > 0) {
            LimitLine goalLine = new LimitLine(dailyGoal, getString(R.string.goal));
            goalLine.setLineColor(ContextCompat.getColor(this,R.color.dark_green));
            goalLine.setLineWidth(2f);
            goalLine.enableDashedLine(10f, 10f, 0f);
            leftAxis.addLimitLine(goalLine);
        }
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getDescription().setEnabled(false); // Disable the description label
    }


    private void updateTextColors() {
        int weeklyAverage = Integer.parseInt(textViewWeeklyAverage.getText().toString().replaceAll("\\D", ""));
        int monthlyAverage = Integer.parseInt(textViewMonthlyAverage.getText().toString().replaceAll("\\D", ""));

        if (weeklyAverage >= dailyGoal) {
            textViewWeeklyAverage.setTextColor(ContextCompat.getColor(this,R.color.dark_green));
        } else {
            textViewWeeklyAverage.setTextColor(ContextCompat.getColor(this,R.color.red));
        }

        if (monthlyAverage >= dailyGoal) {
            textViewMonthlyAverage.setTextColor(ContextCompat.getColor(this,R.color.dark_green));
        } else {
            textViewMonthlyAverage.setTextColor(ContextCompat.getColor(this,R.color.red));
        }
    }

    private float getMaxValue(List<Entry> entries) {
        float max = 0;
        for (Entry entry : entries) {
            if (entry.getY() > max) {
                max = entry.getY();
            }
        }
        return max;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }else if (item.getItemId() == R.id.action_share) {
            shareStepResultAsPdf();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void shareStepResultAsPdf() {

        View content = findViewById(R.id.content_view);
        content.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(content.getDrawingCache());
        content.setDrawingCacheEnabled(false);

        //  PDF document
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(bitmap.getWidth(), bitmap.getHeight(), 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        canvas.drawBitmap(bitmap, 0, 0, null);
        document.finishPage(page);

        // save the PDF
        File pdfFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Steps.pdf");
        try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
            document.writeTo(fos);
            document.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // share PDF
        Uri pdfUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", pdfFile);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/pdf");
        shareIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Share steps"));
    }
}
