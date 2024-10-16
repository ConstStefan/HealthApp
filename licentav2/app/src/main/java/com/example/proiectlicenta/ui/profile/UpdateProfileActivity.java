package com.example.proiectlicenta.ui.profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.proiectlicenta.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateProfileActivity extends AppCompatActivity {

    private EditText editTextUpdateName, editTextUpdateDoB,editTextUpdateMobile;
    private RadioGroup radioGroupUpdateGender;
    private RadioButton radioButtonUpdateGenderSelected;
    private String textFullName, textDoB, textGender, textMobile;
    private FirebaseAuth authProfile;
    private ProgressBar progressBar;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);


        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        getSupportActionBar().setTitle("");

        progressBar=findViewById(R.id.progressBar);
        editTextUpdateName=findViewById(R.id.editText_update_profile_name);
        editTextUpdateDoB=findViewById(R.id.editText_update_profile_dob);
        editTextUpdateMobile=findViewById(R.id.editText_update_profile_mobile);

        radioGroupUpdateGender=findViewById(R.id.radio_group_update_gender);

        authProfile=FirebaseAuth.getInstance();
        FirebaseUser firebaseUser=authProfile.getCurrentUser();

        //Show profile data
        showProfile(firebaseUser);

        //Upload profile pic
        Button buttonUploadProfilePic=findViewById(R.id.button_upload_profile_pic);


        buttonUploadProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(UpdateProfileActivity.this, UploadProfilePicActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //Update email
        Button buttonUpdateEmail=findViewById(R.id.button_profile_update_email);
        buttonUpdateEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(UpdateProfileActivity.this, UpdateEmailActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //update data de nastere
        editTextUpdateDoB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //extragem  dd,mm,yyyy salvate in diferite variabile creand un vector delimitat  "/"
                String textSADoB[]=textDoB.split("/");

                int day=Integer.parseInt(textSADoB[0]);
                int month=Integer.parseInt(textSADoB[1]) - 1;
                int year=Integer.parseInt(textSADoB[2]);

                DatePickerDialog picker;

                //date picker dialog
                picker=new DatePickerDialog(UpdateProfileActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        editTextUpdateDoB.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    }
                },year, month, day);
                picker.show();
            }
        });

        //Update profile
        Button buttonUpdateProfile=findViewById(R.id.button_update_profile);
        buttonUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile(firebaseUser);
            }
        });
    }

    //Update Profile
    private void updateProfile(FirebaseUser firebaseUser) {
        int selectedGenderID = radioGroupUpdateGender.getCheckedRadioButtonId();
        radioButtonUpdateGenderSelected = findViewById(selectedGenderID);

        // Validam numarul utilizand matcher si pattern
        String mobileRegex = "[0][0-9]{9}";
        Matcher mobileMatcher;
        Pattern mobilePattern = Pattern.compile(mobileRegex);
        mobileMatcher = mobilePattern.matcher(textMobile);

        if (TextUtils.isEmpty(textFullName)) {
            Toast.makeText(UpdateProfileActivity.this, "Please enter your full name", Toast.LENGTH_LONG).show();
            editTextUpdateName.setError("Full name is required");
            editTextUpdateName.requestFocus();
        } else if (TextUtils.isEmpty(textDoB)) {
            Toast.makeText(UpdateProfileActivity.this, "Please enter your date of birth", Toast.LENGTH_LONG).show();
            editTextUpdateDoB.setError("Date of birth is required");
            editTextUpdateDoB.requestFocus();
        } else if (TextUtils.isEmpty(radioButtonUpdateGenderSelected.getText())) {
            Toast.makeText(UpdateProfileActivity.this, "Please select your gender", Toast.LENGTH_LONG).show();
            radioButtonUpdateGenderSelected.setError("Gender is required");
            radioButtonUpdateGenderSelected.requestFocus();
        } else if (TextUtils.isEmpty(textMobile)) {
            Toast.makeText(UpdateProfileActivity.this, "Please enter your mobile number", Toast.LENGTH_LONG).show();
            editTextUpdateMobile.setError("Mobile number is required");
            editTextUpdateMobile.requestFocus();
        } else if (textMobile.length() != 10) {
            Toast.makeText(UpdateProfileActivity.this, "Please re-enter your mobile no.", Toast.LENGTH_LONG).show();
            editTextUpdateMobile.setError("Mobile no. should be 10 digits ");
            editTextUpdateMobile.requestFocus();
        } else if (!mobileMatcher.find()) {
            Toast.makeText(UpdateProfileActivity.this, "Please re-enter your mobile no.", Toast.LENGTH_LONG).show();
            editTextUpdateMobile.setError("Mobile no. is not valid ");
            editTextUpdateMobile.requestFocus();
        } else {
            // Obtinem date introduce de utilizator
            textGender = radioButtonUpdateGenderSelected.getText().toString();
            textFullName = editTextUpdateName.getText().toString();
            textDoB = editTextUpdateDoB.getText().toString();
            textMobile = editTextUpdateMobile.getText().toString();

            // cream un hashmap pentru a updata datele
            Map<String, Object> userUpdates = new HashMap<>();
            userUpdates.put("dob", textDoB);
            userUpdates.put("gender", textGender);
            userUpdates.put("mobile", textMobile);

            // Extract user reference from database for registered users
            DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
            String userID = firebaseUser.getUid();

            progressBar.setVisibility(View.VISIBLE);

            referenceProfile.child(userID).updateChildren(userUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        // Setam noul nume
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(textFullName).build();
                        firebaseUser.updateProfile(profileUpdates);

                        Toast.makeText(UpdateProfileActivity.this, "Update Successful!", Toast.LENGTH_LONG).show();

                        // oprim utilizatorul sa se intoarca la UpdateProfilActivity la apasarea butonului inapoi si inchidem activitatea
                        Intent intent = new Intent(UpdateProfileActivity.this, UserProfileActivity.class);
                        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

                        startActivity(intent);
                        finish();
                    } else {
                        try {
                            throw task.getException();
                        } catch (Exception e) {
                            Toast.makeText(UpdateProfileActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
    }

    //extragem datele din Firebase si le afisam
    private void showProfile(FirebaseUser firebaseUser) {
        String userIDofRegistered=firebaseUser.getUid();

        //extragem referinta din Firebase
        DatabaseReference referenceProfile= FirebaseDatabase.getInstance().getReference("Registered Users");

        progressBar.setVisibility(View.VISIBLE);

        referenceProfile.child(userIDofRegistered).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadWriteUserDetails readUserDetails=snapshot.getValue(ReadWriteUserDetails.class);
                if(readUserDetails!=null){
                    textFullName=firebaseUser.getDisplayName();
                    textDoB=readUserDetails.dob;
                    textGender=readUserDetails.gender;
                    textMobile=readUserDetails.mobile;

                    editTextUpdateName.setText(textFullName);
                    editTextUpdateDoB.setText(textDoB);
                    editTextUpdateMobile.setText(textMobile);

                    //show gender through radio radioButton
                    if(textGender.equals("Male")){
                        radioButtonUpdateGenderSelected=findViewById(R.id.radio_male);

                    } else {
                        radioButtonUpdateGenderSelected=findViewById(R.id.radio_female);
                    }
                    radioButtonUpdateGenderSelected.setChecked(true);
                } else {
                    Toast.makeText(UpdateProfileActivity.this,"Something went wrong!",Toast.LENGTH_LONG).show();

                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UpdateProfileActivity.this,"Something went wrong!",Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.common_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(UpdateProfileActivity.this);
            return true;
        } else if (itemId == R.id.menu_refresh) {
            startActivity(getIntent());
            finish();
            overridePendingTransition(0, 0);
            return true;
        } else if (itemId == R.id.menu_update_profile) {
            Intent intent = new Intent(UpdateProfileActivity.this, UpdateProfileActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.menu_update_email) {
            Intent intent = new Intent(UpdateProfileActivity.this, UpdateEmailActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.menu_change_password) {
            Intent intent = new Intent(UpdateProfileActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.menu_delete_profile) {
            Intent intent = new Intent(UpdateProfileActivity.this, DeleteProfileActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.menu_logout) {
            authProfile.signOut();
            Toast.makeText(UpdateProfileActivity.this, "Logged out", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(UpdateProfileActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}