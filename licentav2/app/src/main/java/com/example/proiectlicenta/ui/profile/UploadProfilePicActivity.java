package com.example.proiectlicenta.ui.profile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.proiectlicenta.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Objects;


public class UploadProfilePicActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private ImageView imageViewUploadPic;
    private FirebaseAuth authProfile;
    private StorageReference storageReference;
    private FirebaseUser firebaseUser;
    private static final int PICK_IMAGE_REQUEST=1;
    private Uri uriImage;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_profile_pic);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);


        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        getSupportActionBar().setTitle("");


        Button buttonUploadPicChoose=findViewById(R.id.upload_pic_choose_button);
        Button buttonUploadPic=findViewById(R.id.upload_pic_button);
        progressBar=findViewById(R.id.progressBar);
        imageViewUploadPic=findViewById(R.id.imageView_profile_dp);

        authProfile=FirebaseAuth.getInstance();
        firebaseUser=authProfile.getCurrentUser();

        storageReference= FirebaseStorage.getInstance().getReference("DisplayPics");

        Uri uri=firebaseUser.getPhotoUrl();

        //setam imaginea utilizatorului daca a fost incarcata pana acum

        Picasso.get().load(uri).into(imageViewUploadPic);

        //alegere imagine pentru upload
        buttonUploadPicChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        //upload image
        buttonUploadPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                UploadPic();
            }
        });

    }
    private void openFileChooser(){
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data!=null && data.getData()!=null){
            uriImage=data.getData();
            imageViewUploadPic.setImageURI(uriImage);

        }
    }

    private void UploadPic(){
        if(uriImage != null){
            //salvam imaginea  uid-ului utilizatorului conectat în prezent
            StorageReference fileReference = storageReference.child(Objects.requireNonNull(authProfile.getCurrentUser()).getUid() + "/displaypic." +
                    getFileExtension(uriImage));
            //upload image in storage
            fileReference.putFile(uriImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Uri downloadUri = uri;
                            firebaseUser = authProfile.getCurrentUser();

                            // Save the display image URL to Firebase Realtime Database
                            DatabaseReference profilePicReference = FirebaseDatabase.getInstance().getReference("Registered Users")
                                    .child(firebaseUser.getUid()).child("profilePicUrl");
                            profilePicReference.setValue(downloadUri.toString());

                            //set the display image of the user after upload
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setPhotoUri(downloadUri).build();
                            firebaseUser.updateProfile(profileUpdates);
                        }
                    });

                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(UploadProfilePicActivity.this,"Upload Successful!",
                            Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(UploadProfilePicActivity.this, UserProfileActivity.class);
                    startActivity(intent);
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(UploadProfilePicActivity.this, e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(UploadProfilePicActivity.this, "No file Selected!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    //obtinem extensia imaginii
    private String getFileExtension(Uri uri){
        ContentResolver cR=getContentResolver();
        MimeTypeMap mime= MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));

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
            NavUtils.navigateUpFromSameTask(UploadProfilePicActivity.this);
            return true;
        } else if (itemId == R.id.menu_refresh) {
            startActivity(getIntent());
            finish();
            overridePendingTransition(0, 0);
            return true;
        } else if (itemId == R.id.menu_update_profile) {
            Intent intent = new Intent(UploadProfilePicActivity.this, UpdateProfileActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.menu_update_email) {
            Intent intent = new Intent(UploadProfilePicActivity.this, UpdateEmailActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.menu_change_password) {
            Intent intent = new Intent(UploadProfilePicActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.menu_delete_profile) {
            Intent intent = new Intent(UploadProfilePicActivity.this, DeleteProfileActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.menu_logout) {
            authProfile.signOut();
            Toast.makeText(UploadProfilePicActivity.this, "Logged out", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(UploadProfilePicActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }


}