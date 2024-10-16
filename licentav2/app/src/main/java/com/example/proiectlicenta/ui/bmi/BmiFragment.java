package com.example.proiectlicenta.ui.bmi;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.proiectlicenta.R;

public class BmiFragment extends Fragment {

    private BmiViewModel viewModel;
    private TextView heightValue;
    private EditText weightValue, ageValue;
    private CardView cardMale, cardFemale;
    private SeekBar heightSeekBar;
    private int weight = 67;
    private int age = 20;
    private int height = 87;
    private Button buttonCalculate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bmi, container, false);

        heightValue = view.findViewById(R.id.height_value);
        weightValue = view.findViewById(R.id.weight_value);
        ageValue = view.findViewById(R.id.age_value);
        cardMale = view.findViewById(R.id.card_male);
        cardFemale = view.findViewById(R.id.card_female);
        heightSeekBar = view.findViewById(R.id.height_seekbar);
        buttonCalculate = view.findViewById(R.id.buttonCalculate);

        heightValue.setText(height + " cm");
        weightValue.setText(String.valueOf(weight));
        ageValue.setText(String.valueOf(age));

        Button weightDecrease = view.findViewById(R.id.weight_decrease);
        Button weightIncrease = view.findViewById(R.id.weight_increase);
        Button ageDecrease = view.findViewById(R.id.age_decrease);
        Button ageIncrease = view.findViewById(R.id.age_increase);

        viewModel = new ViewModelProvider(this).get(BmiViewModel.class);

        viewModel.setGender("Male");
        cardMale.setCardBackgroundColor(ContextCompat.getColor(requireContext(),R.color.black));
        cardFemale.setCardBackgroundColor(ContextCompat.getColor(requireContext(),R.color.summarygrey));

        cardMale.setOnClickListener(v -> {
            viewModel.setGender("Male");
            cardMale.setCardBackgroundColor(ContextCompat.getColor(requireContext(),R.color.black));
            cardFemale.setCardBackgroundColor(ContextCompat.getColor(requireContext(),R.color.summarygrey));
        });

        cardFemale.setOnClickListener(v -> {
            viewModel.setGender("Female");
            cardMale.setCardBackgroundColor(ContextCompat.getColor(requireContext(),R.color.summarygrey));
            cardFemale.setCardBackgroundColor(ContextCompat.getColor(requireContext(),R.color.black));
        });

        buttonCalculate.setOnClickListener(v -> {
            if (!validateInputFields()) {
                return;
            }

            double weightDouble = weight;
            double heightDouble = height / 100.0;  // convertim în metri
            viewModel.calculateBmi(weightDouble, heightDouble);

            viewModel.getBmiResult().observe(getViewLifecycleOwner(), result -> {
                // cream un intent pentru a deschide BmiResultActivity
                Intent intent = new Intent(getActivity(), BmiResultActivity.class);
                intent.putExtra("BMI_RESULT", result);
                intent.putExtra("AGE", age);
                startActivity(intent);
            });
        });

        weightDecrease.setOnClickListener(v -> {
            weight = Math.max(weight - 1, 0);
            weightValue.setText(String.valueOf(weight));
        });

        weightIncrease.setOnClickListener(v -> {
            weight = weight + 1;
            weightValue.setText(String.valueOf(weight));
        });

        ageDecrease.setOnClickListener(v -> {
            if (age > 18) {
                age = age - 1;
                ageValue.setText(String.valueOf(age));
            }
        });

        ageIncrease.setOnClickListener(v -> {
            age = age + 1;
            ageValue.setText(String.valueOf(age));
        });

        heightSeekBar.setProgress(height);
        heightSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                height = progress;
                heightValue.setText(height + " cm");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        weightValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        ageValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});

        weightValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    weight = Integer.parseInt(s.toString());
                }
            }
        });

        ageValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    age = Integer.parseInt(s.toString());
                }
            }
        });

        return view;
    }

    private boolean validateInputFields() {
        String weightText = weightValue.getText().toString();
        String ageText = ageValue.getText().toString();

        boolean isWeightValid = !weightText.isEmpty() && Integer.parseInt(weightText) > 0;
        boolean isAgeValid = !ageText.isEmpty() && Integer.parseInt(ageText) >= 18;

        if (!isWeightValid) {
            showErrorDialog("Please enter a valid weight.");
            return false;
        }

        if (!isAgeValid) {
            showErrorDialog("Please enter an age of 18 or older.");
            return false;
        }

        return true;
    }

    private void showErrorDialog(String message) {
        new AlertDialog.Builder(getContext())
                .setTitle("Invalid input")
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .setCancelable(false)
                .show();
    }
}
