package com.example.proiectlicenta.ui.bodyfat;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.proiectlicenta.data.model.BodyFatModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class BodyFatCalculatorViewModel extends ViewModel {
    private final MutableLiveData<Double> bodyFatResult = new MutableLiveData<>();
    private final BodyFatModel bodyFatModel = new BodyFatModel();
    private final Handler handler = new Handler(Looper.getMainLooper());

    public LiveData<Double> getBodyFatResult() {
        return bodyFatResult;
    }

    public void calculateBodyFat(double abdomen, double weightKg) {
        handler.postDelayed(() -> {
            double bodyFat = bodyFatModel.calculateBodyFat(abdomen, weightKg);
            Log.d("BodyFatCalculatorViewModel", "body fatul este " + bodyFat);
            bodyFatResult.setValue(bodyFat);
            saveBodyFatToFirebase(bodyFat);
        }, 1000); // delay 2 sec
    }

    private void saveBodyFatToFirebase(double bodyFat) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Registered Users")
                    .child(user.getUid()).child("bodyFat");
            ref.setValue(bodyFat);
            Log.d("BodyFatCalculatorViewModel", "valoarea salvata este " + bodyFat);
        }
    }
}
