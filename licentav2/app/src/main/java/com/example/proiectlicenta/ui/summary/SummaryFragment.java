package com.example.proiectlicenta.ui.summary;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.proiectlicenta.ui.profile.MainActivity;
import com.example.proiectlicenta.R;
import com.example.proiectlicenta.ui.profile.UserProfileActivity;
import com.example.proiectlicenta.ui.steps.StepsDetailsActivity;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class SummaryFragment extends Fragment  {

    private TextView textViewBmi, textViewCalorieNeeds, textViewBodyFat, stepsTextView, textViewDailyGoal, textViewProgressPercentage, textViewCaloriesBurned;
    private ProgressBar progressBarSteps;
    private CardView cardViewSteps;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference, goalReference;
    private int dailyGoal = 6000; // Valoare implicita
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle drawerToggle;
    private Toolbar toolbar;
    private DatabaseReference profilePicReference;
    private final DecimalFormat twoDecimalFormat = new DecimalFormat("#.00");
    private final DecimalFormat noDecimalFormat = new DecimalFormat("#");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_summary, container, false);
        textViewBmi = root.findViewById(R.id.textViewBmi);
        textViewCalorieNeeds = root.findViewById(R.id.textViewCalorieNeeds);
        textViewBodyFat = root.findViewById(R.id.textViewBodyFat);
        stepsTextView = root.findViewById(R.id.stepsTextView);
        textViewDailyGoal = root.findViewById(R.id.textViewDailyGoal);
        textViewProgressPercentage = root.findViewById(R.id.textViewProgressPercentage);
        progressBarSteps = root.findViewById(R.id.progressBarSteps);
        cardViewSteps = root.findViewById(R.id.cardViewSteps);
        toolbar = root.findViewById(R.id.toolbar);
        drawerLayout = root.findViewById(R.id.drawer_layout);
        navigationView = root.findViewById(R.id.nav_view);
        textViewCaloriesBurned = root.findViewById(R.id.textViewCaloriesBurned);

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);

        drawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        if (user != null) {
            String todayDate = getTodayDate();
            databaseReference = FirebaseDatabase.getInstance().getReference("Registered Users")
                    .child(user.getUid()).child("steps").child(todayDate);
            goalReference = FirebaseDatabase.getInstance().getReference("Registered Users")
                    .child(user.getUid()).child("dailyGoal");
            profilePicReference = FirebaseDatabase.getInstance().getReference("Registered Users")
                    .child(user.getUid()).child("profilePicUrl");

            addStepCountListener();
            addDailyGoalListener();
            loadBmiFromFirebase();
            loadCalorieNeedsFromFirebase();
            loadBodyFatFromFirebase();
            updateNavigationHeader(user);
            addProfilePicListener(); // Adaugare listener pentru poza de profil
            addUserGenderListener();
        }

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(getActivity(), UserProfileActivity.class);
                startActivity(intent);
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            } else if (id == R.id.nav_about) {
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            } else if (id == R.id.nav_logout) {
                auth.signOut();
                Toast.makeText(getActivity(), "Logged out", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                if (getActivity() != null) {
                    getActivity().finish();
                }
                return true;
            }
            return false;
        });

        cardViewSteps.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), StepsDetailsActivity.class);
            startActivity(intent);
        });

        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            updateNavigationHeader(user);
        }
    }

    private void addStepCountListener() {
        if (databaseReference != null) {
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!isAdded()) {
                        return;
                    }
                    if (dataSnapshot.exists()) {
                        int todayTotalSteps = 0;
                        for (DataSnapshot deviceSnapshot : dataSnapshot.getChildren()) {
                            Integer deviceSteps = deviceSnapshot.getValue(Integer.class);
                            if (deviceSteps != null) {
                                todayTotalSteps += deviceSteps;
                            }
                        }
                        stepsTextView.setText(String.valueOf(todayTotalSteps));
                        progressBarSteps.setProgress(todayTotalSteps);
                        int progressPercentage = (int) (((float) todayTotalSteps / dailyGoal) * 100);
                        textViewProgressPercentage.setText(progressPercentage + "%");
                        double caloriesBurned = todayTotalSteps * 0.04; // Assuming 0.04 calories per step
                        textViewCaloriesBurned.setText(getString(R.string.calories_burned, caloriesBurned));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }


    private void addDailyGoalListener() {
        if (goalReference != null) {
            goalReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        dailyGoal = dataSnapshot.getValue(Integer.class);
                        textViewDailyGoal.setText("/" + dailyGoal + " pași");
                        progressBarSteps.setMax(dailyGoal);
                        // Actualizam procentajul de progres
                        String stepsText = stepsTextView.getText().toString();
                        if (!stepsText.isEmpty()) {
                            int todayStepCount = Integer.parseInt(stepsText);
                            int progressPercentage = (int) (((float) todayStepCount / dailyGoal) * 100);
                            textViewProgressPercentage.setText(progressPercentage + "%");
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private String getTodayDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    private void loadBmiFromFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Registered Users")
                    .child(user.getUid()).child("bmi");
            ref.get().addOnSuccessListener(dataSnapshot -> {
                if (dataSnapshot.exists()) {
                    double bmi = dataSnapshot.getValue(Double.class);
                    textViewBmi.setText("BMI: " + twoDecimalFormat.format(bmi));
                }
            });
        }
    }

    private void loadCalorieNeedsFromFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Registered Users")
                    .child(user.getUid()).child("calorieNeeds");
            ref.get().addOnSuccessListener(dataSnapshot -> {
                if (dataSnapshot.exists()) {
                    double calorieNeeds = dataSnapshot.getValue(Double.class);
                    textViewCalorieNeeds.setText("Calorie Needs: " + noDecimalFormat.format(calorieNeeds));
                }
            });
        }
    }

    private void loadBodyFatFromFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Registered Users")
                    .child(user.getUid()).child("bodyFat");
            ref.get().addOnSuccessListener(dataSnapshot -> {
                if (dataSnapshot.exists()) {
                    double bodyFat = dataSnapshot.getValue(Double.class);
                    textViewBodyFat.setText("Body Fat: " + twoDecimalFormat.format(bodyFat));
                }
            });
        }
    }

    private void addProfilePicListener() {
        if (profilePicReference != null) {
            profilePicReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String profilePicUrl = snapshot.getValue(String.class);
                        if (profilePicUrl != null) {
                            updateNavigationHeader(profilePicUrl);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void updateNavigationHeader(String profilePicUrl) {
        View headerView = navigationView.getHeaderView(0);
        ImageView profileImageView = headerView.findViewById(R.id.profileImageView);

        if (profilePicUrl != null && isAdded()) { // Verificam dacă fragmentul este atasat
            Glide.with(this)
                    .load(profilePicUrl)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .circleCrop()
                    .into(profileImageView);
        } else {
            profileImageView.setImageResource(R.drawable.ic_profile_placeholder);
        }
    }

    private void updateNavigationHeader(FirebaseUser user) {
        View headerView = navigationView.getHeaderView(0);
        //ImageView profileImageView = headerView.findViewById(R.id.profileImageView);
        TextView navHeaderDisplayName = headerView.findViewById(R.id.textViewDisplayName);
        TextView navHeaderEmail = headerView.findViewById(R.id.textViewEmail);

        navHeaderDisplayName.setText(user.getDisplayName());
        navHeaderEmail.setText(user.getEmail());

    }

    private void addUserGenderListener() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Registered Users")
                    .child(user.getUid()).child("gender");
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String userGender = snapshot.getValue(String.class);
                    if (userGender != null) {
                        // daca schimbam genul, recalculam necesarul caloric
                        fetchCalorieNeedsData(userGender);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }

    //  metoda pentru a obtine datele necesare recalcularii
    private void fetchCalorieNeedsData(String userGender) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Registered Users")
                    .child(user.getUid());

            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Double weight = snapshot.child("weight").getValue(Double.class);
                    Double height = snapshot.child("height").getValue(Double.class);
                    Integer age = snapshot.child("age").getValue(Integer.class);
                    if (weight != null && height != null && age != null) {
                        double calorieNeeds = calculateCalorieNeeds(weight, height, age, userGender);
                        textViewCalorieNeeds.setText("Calorie Needs: " + noDecimalFormat.format(calorieNeeds));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }

    // metoda pentru a calcula necesarul de calorii
    private double calculateCalorieNeeds(double weight, double height, int age, String gender) {
        double bmr;
        if (gender.equals("Male")) {
            bmr = 10 * weight + 6.25 * height - 5 * age + 5;
        } else if (gender.equals("Female")) {
            bmr = 10 * weight + 6.25 * height - 5 * age - 161;
        } else {
            bmr = 10 * weight + 6.25 * height - 5 * age;
        }

        return bmr;
    }
}