package com.example.proiectlicenta.ui.bmi;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class BmiViewModel extends ViewModel {
    private final MutableLiveData<Double> bmiResult = new MutableLiveData<>();
    private String gender = "Male"; // implicit "Male"

    public LiveData<Double> getBmiResult() {
        return bmiResult;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void calculateBmi(double weight, double height) {
        double bmi;
        if ("Male".equals(gender)) {
            bmi = weight / (height * height);
        } else {
            bmi = (weight / (height * height)) * 0.9; // formula diferita pentru femei
        }
        bmiResult.setValue(bmi);
        saveBmiToFirebase(bmi);
    }

    private void saveBmiToFirebase(double bmi) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Registered Users")
                    .child(user.getUid()).child("bmi");
            ref.setValue(bmi);
        }
    }
}