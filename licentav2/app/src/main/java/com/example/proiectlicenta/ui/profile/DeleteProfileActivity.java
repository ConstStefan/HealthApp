package com.example.proiectlicenta.ui.profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;


public class DeleteProfileActivity extends AppCompatActivity {

    private FirebaseAuth authProfile;
    private FirebaseUser firebaseUser;
    private EditText editTextUserPwd;
    private TextView textViewAuthenticated;
    private ProgressBar progressBar;
    private String userPwd;
    private Button buttonReAuthenticate, buttonDeleteUser;
    private static final String TAG= "DeleteProfileActivity";
    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_profile);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        progressBar=findViewById(R.id.progressBar);
        editTextUserPwd=findViewById(R.id.editText_delete_user_pwd);
        textViewAuthenticated=findViewById(R.id.textView_delete_user_authenticated);
        buttonDeleteUser=findViewById(R.id.button_delete_user);
        buttonReAuthenticate=findViewById(R.id.button_delete_user_authenticate);

        //disable delte user button until user is authenticated
        buttonDeleteUser.setEnabled(false);
        authProfile=FirebaseAuth.getInstance();
        firebaseUser=authProfile.getCurrentUser();

        if(firebaseUser.equals("")){
            Toast.makeText(DeleteProfileActivity.this,"Something went wrong!"+
                    "User details are not available at the moment.",Toast.LENGTH_SHORT).show();
            Intent intent= new Intent(DeleteProfileActivity.this, UserProfileActivity.class);
            startActivity(intent);
            finish();
        } else {
            reAuthenticateUser(firebaseUser);
        }

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        getSupportActionBar().setTitle("");
    }

    //reauthenticate user before changing password
    private void reAuthenticateUser(FirebaseUser firebaseUser) {
        buttonReAuthenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userPwd=editTextUserPwd.getText().toString();

                if(TextUtils.isEmpty(userPwd)){
                    Toast.makeText(DeleteProfileActivity.this,"Password is needed",Toast.LENGTH_SHORT).show();
                    editTextUserPwd.setError("Please enter your current password to authenticate");
                    editTextUserPwd.requestFocus();
                }else {
                    progressBar.setVisibility(View.VISIBLE);

                    //reauthenticate
                    AuthCredential credential= EmailAuthProvider.getCredential(firebaseUser.getEmail(),userPwd);

                    firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                progressBar.setVisibility(View.GONE);

                                //disable edittext pentru parola.
                                editTextUserPwd.setEnabled(false);


                                //enable delete user button. disable authenticate button
                                buttonReAuthenticate.setEnabled(false);
                                buttonDeleteUser.setEnabled(true);

                                //set textview to show user is authenticated/verified
                                textViewAuthenticated.setText("You are authenticated/verified." +
                                        "You can delete your profile now!");
                                Toast.makeText(DeleteProfileActivity.this,"Password has been verified"+
                                        "You can delete your profile now. Be careful, this action is irreversible",Toast.LENGTH_SHORT).show();

                                //update color of change password button
                                buttonDeleteUser.setBackgroundTintList(ContextCompat.getColorStateList(
                                        DeleteProfileActivity.this,R.color.dark_green));
                                buttonDeleteUser.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        showAlertDialog();
                                    }
                                });
                            } else {
                                try{
                                    throw task.getException();
                                } catch (Exception e){
                                    Toast.makeText(DeleteProfileActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            }
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });
    }

    private void showAlertDialog() {
        //setup the alert builder

        AlertDialog.Builder builder=new AlertDialog.Builder(DeleteProfileActivity.this);
        builder.setTitle("Delete user and related data?");
        builder.setMessage("Do you really want to delete your profile and related data? This action is irreversible");


        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteUserData(firebaseUser);
            }
        });

        //return to user profile activity if user presses cancel button
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent=new Intent(DeleteProfileActivity.this, UserProfileActivity.class);
                startActivity(intent);
                finish();
            }
        });




        AlertDialog alertDialog=builder.create();

        //change color of continue button
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.red));
            }
        });

        //show the alert dialog
        alertDialog.show();
    }

    private void deleteUser() {
        firebaseUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    authProfile.signOut();
                    Toast.makeText(DeleteProfileActivity.this,"User has been deleted!",
                            Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(DeleteProfileActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    try{
                        throw task.getException();
                    } catch (Exception e) {
                        Toast.makeText(DeleteProfileActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }


    //delete user data
    private void deleteUserData(FirebaseUser firebaseUser) {

        //delete display pic. verificam daca userul a incarcat vreo poza inainte sa dam delete
        if(firebaseUser.getPhotoUrl() !=null){
            FirebaseStorage firebaseStorage=FirebaseStorage.getInstance();
            StorageReference storageReference=firebaseStorage.getReferenceFromUrl(firebaseUser.getPhotoUrl().toString());
            storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Log.d(TAG,"imagine stearsa");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG,e.getMessage());
                    Toast.makeText(DeleteProfileActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            });
        }


        //Delete data from realtime database
        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("Registered Users");
        databaseReference.child(firebaseUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG,"user data deleted");

                //finally delete the user
                deleteUser();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG,e.getMessage());
                Toast.makeText(DeleteProfileActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
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
            NavUtils.navigateUpFromSameTask(DeleteProfileActivity.this);
            return true;
        } else if (itemId == R.id.menu_refresh) {
            startActivity(getIntent());
            finish();
            overridePendingTransition(0, 0);
            return true;
        } else if (itemId == R.id.menu_update_profile) {
            Intent intent = new Intent(DeleteProfileActivity.this, UpdateProfileActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.menu_update_email) {
            Intent intent = new Intent(DeleteProfileActivity.this, UpdateEmailActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.menu_change_password) {
            Intent intent = new Intent(DeleteProfileActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.menu_delete_profile) {
            Intent intent = new Intent(DeleteProfileActivity.this, DeleteProfileActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.menu_logout) {
            authProfile.signOut();
            Toast.makeText(DeleteProfileActivity.this, "Logged out", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(DeleteProfileActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }


}