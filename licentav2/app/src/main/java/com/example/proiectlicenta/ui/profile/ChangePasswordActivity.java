package com.example.proiectlicenta.ui.profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.proiectlicenta.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;


public class ChangePasswordActivity extends AppCompatActivity {

    private FirebaseAuth authProfile;
    private EditText editTextPwdCurr, editTextPwdNew, editTextPwdConfirmNew;
    private TextView textViewAuthenticated;
    private Button buttonChangePwd, buttonReAuthenticate;
    private ProgressBar progressBar;
    private String userPwdCurr;
    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        getSupportActionBar().setTitle("");

        editTextPwdNew=findViewById(R.id.editText_change_pwd_new);
        editTextPwdCurr=findViewById(R.id.editText_change_pwd_current);
        editTextPwdConfirmNew=findViewById(R.id.editText_change_pwd_new_confirm);
        textViewAuthenticated=findViewById(R.id.textView_change_pwd_authenticated);
        progressBar=findViewById(R.id.progressBar);
        buttonReAuthenticate=findViewById(R.id.button_change_pwd_authenticate);
        buttonChangePwd=findViewById(R.id.button_change_pwd);

        //disable edittext for new password, confirm new password and make change pwd button unclickable till user is authenticated
        editTextPwdNew.setEnabled(false);
        editTextPwdConfirmNew.setEnabled(false);
        buttonChangePwd.setEnabled(false);

        authProfile=FirebaseAuth.getInstance();
        FirebaseUser firebaseUser=authProfile.getCurrentUser();

        if(firebaseUser.equals("")){
            Toast.makeText(ChangePasswordActivity.this,"Something went wrong! User's details not available",
                    Toast.LENGTH_SHORT).show();
            Intent intent=new Intent(ChangePasswordActivity.this, UserProfileActivity.class);
            startActivity(intent);
            finish();
        } else{
            reAuthenticateUser(firebaseUser);
        }
    }

    //reauthenticate user before changing password
    private void reAuthenticateUser(FirebaseUser firebaseUser) {
        buttonReAuthenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userPwdCurr=editTextPwdCurr.getText().toString();

                if(TextUtils.isEmpty(userPwdCurr)){
                    Toast.makeText(ChangePasswordActivity.this,"Password is needed",Toast.LENGTH_SHORT).show();
                    editTextPwdCurr.setError("Please enter your current password to authenticate");
                    editTextPwdCurr.requestFocus();
                }else {
                    progressBar.setVisibility(View.VISIBLE);

                    //reauthenticate user now
                    AuthCredential credential= EmailAuthProvider.getCredential(firebaseUser.getEmail(),userPwdCurr);

                    firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                progressBar.setVisibility(View.GONE);

                                //DISABLE EDITTEXT FOR CURRENT PASSWORD. enable edittext for new password and confirm new password
                                editTextPwdCurr.setEnabled(false);
                                editTextPwdNew.setEnabled(true);
                                editTextPwdConfirmNew.setEnabled(true);

                                //enable change pwd button. disable authenticate button
                                buttonReAuthenticate.setEnabled(false);
                                buttonChangePwd.setEnabled(true);

                                //set textview to show user is authenticated/verified
                                textViewAuthenticated.setText("You are authenticated/verified." +
                                        "You can change password now!");
                                Toast.makeText(ChangePasswordActivity.this,"Password has been verified"+
                                        "Change password now",Toast.LENGTH_SHORT).show();

                                //update color of change password button
                                buttonChangePwd.setBackgroundTintList(ContextCompat.getColorStateList(
                                        ChangePasswordActivity.this,R.color.dark_green));
                                buttonChangePwd.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        changePwd(firebaseUser);
                                    }
                                });
                            } else {
                                try{
                                    throw task.getException();
                                } catch (Exception e){
                                    Toast.makeText(ChangePasswordActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            }
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });
    }

    private void changePwd(FirebaseUser firebaseUser) {
        String userPwdNew=editTextPwdNew.getText().toString();
        String userPwdConfirmNew=editTextPwdConfirmNew.getText().toString();

        if(TextUtils.isEmpty(userPwdNew)){
            Toast.makeText(ChangePasswordActivity.this,"New Password is needed", Toast.LENGTH_SHORT).show();
            editTextPwdNew.setError("Please enter your new password");
            editTextPwdNew.requestFocus();
        } else if(TextUtils.isEmpty(userPwdConfirmNew)){
            Toast.makeText(ChangePasswordActivity.this,"Please confirm your new password", Toast.LENGTH_SHORT).show();
            editTextPwdConfirmNew.setError("Please re-enter your new password");
            editTextPwdConfirmNew.requestFocus();
        } else if (!userPwdNew.matches(userPwdConfirmNew)){
            Toast.makeText(ChangePasswordActivity.this,"Password did not match", Toast.LENGTH_SHORT).show();
            editTextPwdConfirmNew.setError("Please re-enter same password");
            editTextPwdConfirmNew.requestFocus();
        } else if (userPwdCurr.matches(userPwdNew)){
            Toast.makeText(ChangePasswordActivity.this,"New password cannot be same as old password", Toast.LENGTH_SHORT).show();
            editTextPwdNew.setError("Please enter a new password");
            editTextPwdNew.requestFocus();
        } else {
            progressBar.setVisibility(View.VISIBLE);
            firebaseUser.updatePassword(userPwdNew).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(ChangePasswordActivity.this,"Password has been changed", Toast.LENGTH_SHORT).show();
                        Intent intent= new Intent(ChangePasswordActivity.this,UserProfileActivity.class);
                        startActivity(intent);
                        finish();
                    }else {
                        try{
                            throw task.getException();

                        }catch(Exception e){
                            Toast.makeText(ChangePasswordActivity.this,e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
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
            NavUtils.navigateUpFromSameTask(ChangePasswordActivity.this);
            return true;
        } else if (itemId == R.id.menu_refresh) {
            startActivity(getIntent());
            finish();
            overridePendingTransition(0, 0);
            return true;
        } else if (itemId == R.id.menu_update_profile) {
            Intent intent = new Intent(ChangePasswordActivity.this, UpdateProfileActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.menu_update_email) {
            Intent intent = new Intent(ChangePasswordActivity.this, UpdateEmailActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.menu_change_password) {
            Intent intent = new Intent(ChangePasswordActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.menu_delete_profile) {
            Intent intent = new Intent(ChangePasswordActivity.this, DeleteProfileActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.menu_logout) {
            authProfile.signOut();
            Toast.makeText(ChangePasswordActivity.this, "Logged out", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(ChangePasswordActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}