package com.example.proiectlicenta.ui.bmi;

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
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import com.example.proiectlicenta.R;
import com.example.proiectlicenta.ui.summary.SummaryActivity;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

public class BmiResultActivity extends AppCompatActivity {

    private TextView textViewBmiResult, textViewBmiRange, textViewAge, textViewSuggestion, textViewBmiStatus;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmi_result);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        getSupportActionBar().setTitle("BMI");

        textViewBmiResult = findViewById(R.id.textViewBmiResult);
        textViewBmiRange = findViewById(R.id.textViewBmiRange);
        textViewAge = findViewById(R.id.textViewAge);
        textViewSuggestion = findViewById(R.id.textViewSuggestion);
        textViewBmiStatus = findViewById(R.id.textViewBmiStatus);

        double bmiResult = getIntent().getDoubleExtra("BMI_RESULT", 0);
        int age = getIntent().getIntExtra("AGE", 0);

        textViewBmiResult.setText(String.format("%.1f", bmiResult));
        textViewAge.setText(String.format("your Age: %d (Adult)", age));
        textViewAge.setTextColor(ContextCompat.getColor(this,R.color.black)); // Set age text color to green


        if (bmiResult < 15) {
            textViewBmiStatus.setText("Severe Thinness");
            textViewBmiStatus.setTextColor(ContextCompat.getColor(this,R.color.red));
            textViewSuggestion.setText("Suggestion: You are severely underweight. Please consult a healthcare provider.");
            textViewSuggestion.setTextColor(ContextCompat.getColor(this,R.color.red));
        } else if (bmiResult < 16) {
            textViewBmiStatus.setText("Moderate Thinness");
            textViewBmiStatus.setTextColor(ContextCompat.getColor(this,R.color.red));
            textViewSuggestion.setText("Suggestion: You are moderately underweight. Consider gaining weight for better health.");
            textViewSuggestion.setTextColor(ContextCompat.getColor(this,R.color.red));
        } else if (bmiResult < 18.5) {
            textViewBmiStatus.setText("Mild Thinness");
            textViewBmiStatus.setTextColor(ContextCompat.getColor(this,R.color.progress_secondary));
            textViewSuggestion.setText("Suggestion: You are mildly underweight. Consider gaining weight for better health.");
            textViewSuggestion.setTextColor(ContextCompat.getColor(this,R.color.red));
        } else if (bmiResult < 25) {
            textViewBmiStatus.setText("Normal (healthy weight)");
            textViewBmiStatus.setTextColor(ContextCompat.getColor(this,R.color.dark_green));
            textViewSuggestion.setText("Suggestion: You have a healthy weight. Keep maintaining your current lifestyle.");
            textViewSuggestion.setTextColor(ContextCompat.getColor(this,R.color.black));
        } else if (bmiResult < 30) {
            textViewBmiStatus.setText("Overweight");
            textViewBmiStatus.setTextColor(ContextCompat.getColor(this,R.color.progress_secondary));
            textViewSuggestion.setText("Suggestion: You are overweight. Consider losing weight for better health.");
            textViewSuggestion.setTextColor(ContextCompat.getColor(this,R.color.red));
        } else {
            textViewBmiStatus.setText("Obese Classes");
            textViewBmiStatus.setTextColor(ContextCompat.getColor(this,R.color.red));
            textViewSuggestion.setText("Suggestion: You are obese. Please consult a healthcare provider for advice.");
            textViewSuggestion.setTextColor(ContextCompat.getColor(this,R.color.red));
        }

        textViewBmiRange.setText("Normal BMI range:\n18.5 - 25 kg/m²");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_share) {
            shareBmiResultAsPdf();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(BmiResultActivity.this, SummaryActivity.class);
        intent.putExtra("SELECTED_FRAGMENT", R.id.navigation_bmi);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void shareBmiResultAsPdf() {

        View content = findViewById(R.id.content_view);
        content.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(content.getDrawingCache());
        content.setDrawingCacheEnabled(false);

        // create PDF
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(bitmap.getWidth(), bitmap.getHeight(), 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        canvas.drawBitmap(bitmap, 0, 0, null);
        document.finishPage(page);

        // save PDF
        File pdfFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "BMI_Result.pdf");
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
        startActivity(Intent.createChooser(shareIntent, "Share BMI Result"));
    }
}
