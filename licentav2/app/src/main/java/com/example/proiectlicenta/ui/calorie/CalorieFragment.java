package com.example.proiectlicenta.ui.calorie;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proiectlicenta.R;
import com.example.proiectlicenta.data.model.Article;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CalorieFragment extends Fragment {

    private CalorieViewModel viewModel;
    private EditText editTextWeight;
    private EditText editTextHeight;
    private EditText editTextAge;
    private TextView textViewResult;
    private Button buttonSetGoal, buttonCalculate, buttonSetActivityLevel;
    private Button weightDecrease, weightIncrease, heightDecrease, heightIncrease, ageDecrease, ageIncrease;
    private boolean isGoalSet = false;
    private boolean isActivityLevelSet = false;
    private String userGender;
    private int selectedGoalIndex = -1;
    private int selectedActivityLevelIndex = -1;
    private String selectedGoal = "";
    private DatabaseReference genderRef;
    private RecyclerView recyclerViewArticles;
    private ArticleAdapter articleAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calorie, container, false);

        editTextWeight = view.findViewById(R.id.editTextWeight);
        editTextHeight = view.findViewById(R.id.editTextHeight);
        editTextAge = view.findViewById(R.id.editTextAge);
        buttonCalculate = view.findViewById(R.id.buttonCalculate);
        buttonSetGoal = view.findViewById(R.id.buttonSetGoal);
        buttonSetActivityLevel = view.findViewById(R.id.buttonSetActivityLevel);
        textViewResult = view.findViewById(R.id.textViewResult);
        weightDecrease = view.findViewById(R.id.weight_decrease);
        weightIncrease = view.findViewById(R.id.weight_increase);
        heightDecrease = view.findViewById(R.id.height_decrease);
        heightIncrease = view.findViewById(R.id.height_increase);
        ageDecrease = view.findViewById(R.id.age_decrease);
        ageIncrease = view.findViewById(R.id.age_increase);
        recyclerViewArticles = view.findViewById(R.id.recyclerViewArticles);

        viewModel = new ViewModelProvider(this).get(CalorieViewModel.class);

        buttonCalculate.setEnabled(false);
        buttonCalculate.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(),R.color.summarygrey));

        fetchUserGenderFromFirebase();

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkInputFields();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        editTextWeight.addTextChangedListener(textWatcher);
        editTextHeight.addTextChangedListener(textWatcher);
        editTextAge.addTextChangedListener(textWatcher);

        buttonCalculate.setOnClickListener(v -> {
            String weightStr = editTextWeight.getText().toString();
            String heightStr = editTextHeight.getText().toString();
            String ageStr = editTextAge.getText().toString();
            if (!weightStr.isEmpty() && !heightStr.isEmpty() && !ageStr.isEmpty() && isGoalSet && isActivityLevelSet) {
                if (userGender != null) {
                    double weight = Double.parseDouble(weightStr);
                    double height = Double.parseDouble(heightStr);
                    int age = Integer.parseInt(ageStr);
                    viewModel.setCalculatePressed(true); // Setăm variabila când apasă pe Calculate
                    viewModel.calculateCalorieNeeds(weight, height, age, userGender);
                    Log.d("CalorieFragment", "calcul calorii: " + weight + ", height: " + height + ", age: " + age + ", gender: " + userGender);
                    fetchAndFilterArticles(selectedGoal); // Adăugăm filtrarea articolelor aici
                } else {
                    Toast.makeText(getContext(), "User gender not set. Please try again later.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Please fill in all fields, set a goal, and set an activity level", Toast.LENGTH_SHORT).show();
            }
        });

        buttonSetGoal.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Select Your Goal");

            String[] goals = {"Lose 1 kg per week", "Lose 0.75 kg per week", "Lose 0.5 kg per week", "Lose 0.25 kg per week",
                    "Maintain my current weight", "Gain 0.25 kg per week", "Gain 0.5 kg per week"};

            builder.setSingleChoiceItems(goals, selectedGoalIndex, (dialog, which) -> {
                selectedGoalIndex = which;
                selectedGoal = goals[which];
                viewModel.applyWeightGoal(selectedGoal);
                isGoalSet = true;
                checkInputFields();
                Log.d("CalorieFragment", "Goal set to: " + selectedGoal);
                // reseteaza articolele vizibile cand se schimba obiectivul fara a apasa calculate
                articleAdapter.setArticles(new ArrayList<>());
                dialog.dismiss();
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        });

        buttonSetActivityLevel.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Select Your Activity Level");

            String[] activityLevels = {"Sedentary (little or no exercise)", "Lightly active (exercise 1–3 days/week)",
                    "Moderately active (exercise 3–5 days/week)", "Active (exercise 6–7 days/week)",
                    "Very active (hard exercise 6–7 days/week)"};

            builder.setSingleChoiceItems(activityLevels, selectedActivityLevelIndex, (dialog, which) -> {
                selectedActivityLevelIndex = which;
                String selectedActivityLevel = activityLevels[which];
                viewModel.applyActivityLevel(selectedActivityLevel);
                isActivityLevelSet = true;
                checkInputFields();
                Log.d("CalorieFragment", "Activity level set to: " + selectedActivityLevel);
                // reseteaza articolele vizibile cand se schimbă nivelul de activitate fara a apasa calculate
                articleAdapter.setArticles(new ArrayList<>());
                dialog.dismiss();
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        });

        viewModel.getCalorieNeedsResult().observe(getViewLifecycleOwner(), result -> {
            textViewResult.setText(String.format("Daily Calorie Needs: %.2f", result));
            textViewResult.setVisibility(View.VISIBLE);
        });

        weightDecrease.setOnClickListener(v -> updateValue(editTextWeight, -1));
        weightIncrease.setOnClickListener(v -> updateValue(editTextWeight, 1));
        heightDecrease.setOnClickListener(v -> updateValue(editTextHeight, -1));
        heightIncrease.setOnClickListener(v -> updateValue(editTextHeight, 1));
        ageDecrease.setOnClickListener(v -> updateValue(editTextAge, -1));
        ageIncrease.setOnClickListener(v -> updateValue(editTextAge, 1));

        articleAdapter = new ArticleAdapter();
        recyclerViewArticles.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewArticles.setAdapter(articleAdapter);

        return view;
    }

    private void updateValue(EditText editText, int increment) {
        String valueStr = editText.getText().toString();
        int value = valueStr.isEmpty() ? 0 : Integer.parseInt(valueStr);
        value += increment;
        if (value < 0) value = 0;
        editText.setText(String.valueOf(value));
    }

    private void checkInputFields() {
        String weight = editTextWeight.getText().toString();
        String height = editTextHeight.getText().toString();
        String age = editTextAge.getText().toString();

        boolean fieldsFilled = !weight.isEmpty() && !height.isEmpty() && !age.isEmpty();
        boolean enableCalculateButton = fieldsFilled && isGoalSet && isActivityLevelSet;

        buttonCalculate.setEnabled(enableCalculateButton);
        if (enableCalculateButton) {
            buttonCalculate.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.button_colorbmi));

        } else {
            buttonCalculate.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(),R.color.summarygrey));
        }
    }

    private void fetchUserGenderFromFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            genderRef = FirebaseDatabase.getInstance().getReference("Registered Users")
                    .child(user.getUid()).child("gender");
            genderRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    userGender = snapshot.getValue(String.class);
                    if (userGender != null) {
                        Log.d("CalorieFragment", "genul extras din firebase: " + userGender);
                        recalculateCalorieNeeds();
                    } else {
                        Log.d("CalorieFragment", "genul este null");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("CalorieFragment", "eroare obtinere gen", error.toException());
                }
            });
        }
    }

    private void recalculateCalorieNeeds() {
        String weightStr = editTextWeight.getText().toString();
        String heightStr = editTextHeight.getText().toString();
        String ageStr = editTextAge.getText().toString();
        if (!weightStr.isEmpty() && !heightStr.isEmpty() && !ageStr.isEmpty() && isGoalSet && isActivityLevelSet) {
            double weight = Double.parseDouble(weightStr);
            double height = Double.parseDouble(heightStr);
            int age = Integer.parseInt(ageStr);
            viewModel.calculateCalorieNeeds(weight, height, age, userGender);
        }
    }

    private void fetchAndFilterArticles(String goal) {
        Log.d("CalorieFragment", "extragere si filtrare dupa goal: " + goal);
        viewModel.getArticles().observe(getViewLifecycleOwner(), articles -> {
            List<Article> filteredArticles;
            if (goal.toLowerCase().contains("lose")) {
                filteredArticles = articles.stream()
                        .filter(article -> article.getCategory() != null && article.getCategory().equalsIgnoreCase("lose"))
                        .collect(Collectors.toList());
            } else if (goal.toLowerCase().contains("gain")) {
                filteredArticles = articles.stream()
                        .filter(article -> article.getCategory() != null && article.getCategory().equalsIgnoreCase("gain"))
                        .collect(Collectors.toList());
            } else {
                // daca goal-ul nu contine nici "lose" nici "gain", nu afisam nimic
                filteredArticles = new ArrayList<>();
            }
            Log.d("CalorieFragment", "Filtered articles size: " + filteredArticles.size());
            articleAdapter.setArticles(filteredArticles);
        });
    }
}
