package com.example.proiectlicenta.ui.calorie;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.proiectlicenta.data.model.Article;
import com.example.proiectlicenta.data.repository.ArticleRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class CalorieViewModel extends ViewModel {
    private final MutableLiveData<Double> calorieNeedsResult = new MutableLiveData<>();
    private final MutableLiveData<String> goalQuery = new MutableLiveData<>();
    private final ArticleRepository articleRepository = new ArticleRepository();

    private String weightGoal;
    private String activityLevel;
    private boolean calculatePressed = false; // vriabilă pentru a urmari daca butonul Calculate a fost apasat

    public LiveData<Double> getCalorieNeedsResult() {
        return calorieNeedsResult;
    }

    public LiveData<List<Article>> getArticles() {
        return Transformations.switchMap(goalQuery, query -> articleRepository.getArticles(query));
    }

    public void calculateCalorieNeeds(double weight, double height, int age, String gender) {
        double bmr;
        if (gender.equals("Male")) {
            bmr = 10 * weight + 6.25 * height - 5 * age + 5;
        } else if (gender.equals("Female")) {
            bmr = 10 * weight + 6.25 * height - 5 * age - 161;
        } else {
            bmr = 10 * weight + 6.25 * height - 5 * age;
        }

        if (weightGoal != null) {
            switch (weightGoal) {
                case "Lose 1 kg per week":
                    bmr -= 1100;
                    break;
                case "Lose 0.75 kg per week":
                    bmr -= 825;
                    break;
                case "Lose 0.5 kg per week":
                    bmr -= 550;
                    break;
                case "Lose 0.25 kg per week":
                    bmr -= 275;
                    break;
                case "Gain 0.25 kg per week":
                    bmr += 275;
                    break;
                case "Gain 0.5 kg per week":
                    bmr += 550;
                    break;
                case "Gain 0.75 kg per week":
                    bmr += 825;
                    break;
                case "Gain 1 kg per week":
                    bmr += 1100;
                    break;
                default:
                    break;
            }
        }

        if (activityLevel != null) {
            switch (activityLevel) {
                case "Sedentary (little or no exercise)":
                    bmr *= 1.2;
                    break;
                case "Lightly active (exercise 1–3 days/week)":
                    bmr *= 1.375;
                    break;
                case "Moderately active (exercise 3–5 days/week)":
                    bmr *= 1.55;
                    break;
                case "Active (exercise 6–7 days/week)":
                    bmr *= 1.725;
                    break;
                case "Very active (hard exercise 6–7 days/week)":
                    bmr *= 1.9;
                    break;
                default:
                    break;
            }
        }

        calorieNeedsResult.setValue(bmr);
        saveCalorieNeedsToFirebase(bmr);

        if (calculatePressed) {
            updateGoalQuery();
        }
    }

    public void applyWeightGoal(String goal) {
        weightGoal = goal;
    }

    public void applyActivityLevel(String level) {
        activityLevel = level;
    }

    private void updateGoalQuery() {
        if (weightGoal != null) {
            String query;
            if (weightGoal.contains("Lose")) {
                query = "weight loss";
            } else if (weightGoal.contains("Gain")) {
                query = "weight gain";
            } else {
                query = "weight maintenance";
            }
            goalQuery.setValue(query);
        }
    }

    public void setCalculatePressed(boolean pressed) {
        calculatePressed = pressed;
    }

    private void saveCalorieNeedsToFirebase(double bmr) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Registered Users")
                    .child(user.getUid()).child("calorieNeeds");
            ref.setValue(bmr);
        }
    }
}
