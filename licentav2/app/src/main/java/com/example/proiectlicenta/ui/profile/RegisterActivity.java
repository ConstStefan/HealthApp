package com.example.proiectlicenta.ui.profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.proiectlicenta.R;
import com.example.proiectlicenta.ui.summary.SummaryActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextRegisterFullName, editTextRegisterEmail, editTextRegisterDoB, editTextRegisterMobile, editTextRegisterPwd, editTextRegisterConfirmPwd;
    private ProgressBar progressBar;
    private RadioGroup radioGroupRegisterGender;
    private RadioButton radioButtonRegisterGenderSelected;
    private DatePickerDialog picker;
    private static final String TAG="RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        Toast.makeText(RegisterActivity.this,"You can register now",Toast.LENGTH_LONG).show();

        progressBar = findViewById(R.id.progressBar);
        editTextRegisterFullName=findViewById(R.id.editText_register_full_name);
        editTextRegisterEmail=findViewById(R.id.editText_register_email);
        editTextRegisterDoB=findViewById(R.id.editText_register_dob);
        editTextRegisterMobile=findViewById(R.id.editText_register_mobile);
        editTextRegisterPwd=findViewById(R.id.editText_register_password);
        editTextRegisterConfirmPwd=findViewById(R.id.editText_register_confirm_password);

        //RadioButton pentru gen
        radioGroupRegisterGender=findViewById(R.id.radio_group_register_gender);
        radioGroupRegisterGender.clearCheck();

        //Setam un datepicker
        editTextRegisterDoB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar=Calendar.getInstance();
                int day=calendar.get(Calendar.DAY_OF_MONTH);
                int month=calendar.get(Calendar.MONTH);
                int year=calendar.get(Calendar.YEAR);

                //Date picker dialog
                picker=new DatePickerDialog(RegisterActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        editTextRegisterDoB.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    }
                },year, month, day);
                picker.show();
            }
        });

        Button buttonRegister=findViewById(R.id.button_register);
        buttonRegister.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                int selectedGenderId=radioGroupRegisterGender.getCheckedRadioButtonId();
                radioButtonRegisterGenderSelected=findViewById(selectedGenderId);


                String textFullName=editTextRegisterFullName.getText().toString();
                String textEmail=editTextRegisterEmail.getText().toString();
                String textDoB=editTextRegisterDoB.getText().toString();
                String textMobile=editTextRegisterMobile.getText().toString();
                String textPwd=editTextRegisterPwd.getText().toString();
                String textConfirmPwd=editTextRegisterConfirmPwd.getText().toString();
                String textGender;

                //Validam numarul folosin matcher si pattern
                String mobileRegex="[0][0-9]{9}";
                Matcher mobileMatcher;
                Pattern mobilePattern=Pattern.compile(mobileRegex);
                mobileMatcher=mobilePattern.matcher(textMobile);


                if(TextUtils.isEmpty(textFullName)){
                    Toast.makeText(RegisterActivity.this,"Please enter your full name",Toast.LENGTH_LONG).show();
                    editTextRegisterFullName.setError("Full name is required");
                    editTextRegisterFullName.requestFocus();
                } else if (TextUtils.isEmpty(textEmail)){
                    Toast.makeText(RegisterActivity.this,"Please enter your email",Toast.LENGTH_LONG).show();
                    editTextRegisterEmail.setError("Email is required");
                    editTextRegisterEmail.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(textEmail).matches()){
                    Toast.makeText(RegisterActivity.this,"Please re-enter your email",Toast.LENGTH_LONG).show();
                    editTextRegisterEmail.setError("Valid email is required");
                    editTextRegisterEmail.requestFocus();
                } else if (TextUtils.isEmpty(textDoB)){
                    Toast.makeText(RegisterActivity.this,"Please enter your date of birth",Toast.LENGTH_LONG).show();
                    editTextRegisterDoB.setError("Date of birth is required");
                    editTextRegisterDoB.requestFocus();
                } else if (radioGroupRegisterGender.getCheckedRadioButtonId()== -1){
                    Toast.makeText(RegisterActivity.this,"Please select your gender",Toast.LENGTH_LONG).show();
                    radioButtonRegisterGenderSelected.setError("Gender is required");
                    radioGroupRegisterGender.requestFocus();
                } else if (TextUtils.isEmpty(textMobile)) {
                    Toast.makeText(RegisterActivity.this,"Please enter your mobile number",Toast.LENGTH_LONG).show();
                    editTextRegisterMobile.setError("Mobile number is required");
                    editTextRegisterMobile.requestFocus();
                } else if (textMobile.length() !=10) {
                    Toast.makeText(RegisterActivity.this,"Please re-enter your mobile no.",Toast.LENGTH_LONG).show();
                    editTextRegisterMobile.setError("Mobile no. should be 10 digits ");
                    editTextRegisterMobile.requestFocus();
                } else if(!mobileMatcher.find()) {
                    Toast.makeText(RegisterActivity.this,"Please re-enter your mobile no.",Toast.LENGTH_LONG).show();
                    editTextRegisterMobile.setError("Mobile no. is not valid ");
                    editTextRegisterMobile.requestFocus();

                } else if (TextUtils.isEmpty(textPwd)) {
                    Toast.makeText(RegisterActivity.this,"Please enter your password",Toast.LENGTH_LONG).show();
                    editTextRegisterPwd.setError("Password is required");
                    editTextRegisterPwd.requestFocus();
                } else if (textPwd.length() <6) {
                    Toast.makeText(RegisterActivity.this,"Password should be at least 6 digits",Toast.LENGTH_LONG).show();
                    editTextRegisterPwd.setError("Password too weak");
                    editTextRegisterPwd.requestFocus();
                } else if (TextUtils.isEmpty(textConfirmPwd)) {
                    Toast.makeText(RegisterActivity.this,"Please confirm your password",Toast.LENGTH_LONG).show();
                    editTextRegisterConfirmPwd.setError("Password confirmation is required");
                    editTextRegisterConfirmPwd.requestFocus();
                } else if (!textPwd.equals(textConfirmPwd)) {
                    Toast.makeText(RegisterActivity.this,"Please enter same password",Toast.LENGTH_LONG).show();
                    editTextRegisterConfirmPwd.setError("Password confirmation is required");
                    editTextRegisterConfirmPwd.requestFocus();
                    //clear
                    editTextRegisterPwd.clearComposingText();
                    editTextRegisterConfirmPwd.clearComposingText();
                } else {
                    textGender=radioButtonRegisterGenderSelected.getText().toString();
                    progressBar.setVisibility(View.VISIBLE);
                    registerUser(textFullName, textEmail, textDoB,textGender, textMobile, textPwd);

                }

            }
        });
    }

    //register user
    private void registerUser(String textFullName, String textEmail, String textDoB, String textGender, String textMobile, String textPwd) {
        FirebaseAuth auth= FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(textEmail, textPwd).addOnCompleteListener(RegisterActivity.this,
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){

                            FirebaseUser firebaseUser= auth.getCurrentUser();

                            //update display name
                            UserProfileChangeRequest profileChangeRequest=new UserProfileChangeRequest.Builder().setDisplayName(textFullName).build();
                            firebaseUser.updateProfile(profileChangeRequest);

                            //punem informatiile despre utilizator in firebase realtime database
                            ReadWriteUserDetails writeUserDetails=new ReadWriteUserDetails(textDoB,textGender,textMobile);

                            //extragem referinta userului din baza de date
                            DatabaseReference referenceProfile= FirebaseDatabase.getInstance().getReference("Registered Users");
                            referenceProfile.child(firebaseUser.getUid()).setValue(writeUserDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful()){
                                        //trimite email de verificare
                                        firebaseUser.sendEmailVerification();

                                        Toast.makeText(RegisterActivity.this, "User registered successfully. Please verify your email",
                                                Toast.LENGTH_LONG).show();

                                         //deschidere SummaryActivity dupa inregistrare
                                        Intent intent=new Intent(RegisterActivity.this, SummaryActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                                    Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();

                                    }else {
                                        Toast.makeText(RegisterActivity.this, "User register failed. Please try again",
                                                Toast.LENGTH_LONG).show();
                                    }

                                    progressBar.setVisibility(View.GONE );
                                }
                            });
                        } else {
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthWeakPasswordException e){
                                editTextRegisterPwd.setError("Your password is too weak. Kindly use a mix of alphabets, numbers and special characaters");
                                editTextRegisterPwd.requestFocus();
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                editTextRegisterPwd.setError("Your email is invalid or already in use.");
                                editTextRegisterPwd.requestFocus();
                            } catch (FirebaseAuthUserCollisionException e) {
                                editTextRegisterEmail.setError("User is already registered with this email.");
                                editTextRegisterEmail.requestFocus();
                            } catch (Exception e) {
                                Log.e(TAG,e.getMessage());
                                Toast.makeText(RegisterActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
                            }

                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
    }
}