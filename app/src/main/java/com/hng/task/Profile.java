package com.hng.task;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hng.task.models.User;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends AppCompatActivity {

    private static final String TAG = "Profile";
    private static final int PICTURE_RESULT = 42;

    //widgets
    private EditText profileName;
    private EditText profileEmail;
    private EditText profileContact;
    private CircleImageView profileImage;
    private TextView logout;
    private TextView saveDetails;
    private String g;

    //firebase variables
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference user;
    private DatabaseReference users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //init firebase
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        g = mAuth.getCurrentUser().getUid();
        user = mFirebaseDatabase.getReference("users").child(g);
        users = mFirebaseDatabase.getReference("users");

        //init widgets
        profileName = findViewById(R.id.profileName);
        profileEmail = findViewById(R.id.profileEmail);
        profileContact = findViewById(R.id.profileContact);
        profileImage = findViewById(R.id.profilePicture);
        logout = findViewById(R.id.logout);
        saveDetails = findViewById(R.id.save_details);


        disableWidgets();


        user.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null){
                    User u = dataSnapshot.getValue(User.class);
                    profileName.setText(u.getFullName());
                    profileEmail.setText(u.getEmail());
                    profileContact.setText(u.getPhone());
                } else {
                    Toast.makeText(Profile.this, "Please Add Your Details", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mAuth.getCurrentUser() != null){
                    mAuth.signOut();
                    Intent mine = new Intent(Profile.this, Sign_in.class);
                    Toast.makeText(Profile.this, "Signed out", Toast.LENGTH_LONG).show();
                    startActivity(mine);
                    finish();
                }
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(intent.createChooser(intent, "Insert Picture"), PICTURE_RESULT);
            }
        });

        saveDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(saveDetails.getText().toString().equalsIgnoreCase("save details")){
                    if(TextUtils.isEmpty(profileName.getText().toString()) && profileName.getText().toString().length() < 3){
                        Toast.makeText(Profile.this, "Please Enter a valid Name", Toast.LENGTH_LONG).show();
                        return;
                    } else if(TextUtils.isEmpty(profileEmail.getText().toString()) && !profileEmail.getText().toString().contains("@")){
                        Toast.makeText(Profile.this, "Please Enter a valid Name", Toast.LENGTH_LONG).show();
                        return;
                    } else if(TextUtils.isEmpty(profileContact.getText().toString()) && profileContact.getText().toString().length() < 9){
                        Toast.makeText(Profile.this, "Please Enter a valid Phone Number", Toast.LENGTH_LONG).show();
                        return;
                    } else {
                        User user = new User();
                        user.setFullName(profileName.getText().toString());
                        user.setEmail(profileEmail.getText().toString());
                        user.setPhone(profileContact.getText().toString());

                        users.child(mAuth.getCurrentUser().getUid())
                                .setValue(user)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(Profile.this, "Update Successful", Toast.LENGTH_LONG).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(Profile.this, "Update Unsuccessful", Toast.LENGTH_LONG).show();
                                    }
                                });
                    }
                    saveDetails.setText("Update Profile");
                    disableWidgets();
                } else {
                    saveDetails.setText("Save Details");
                    enableWidgets();
                }
            }
        });
    }

    private void disableWidgets() {
        profileName.setEnabled(false);
        profileEmail.setEnabled(false);
        profileContact.setEnabled(false);
    }

    private void enableWidgets() {
        profileName.setEnabled(true);
        profileEmail.setEnabled(true);
        profileContact.setEnabled(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICTURE_RESULT && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();
            final StorageReference ref = FirebaseStorage.getInstance().getReference().child(g + "/image");
            ref.putFile(imageUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                    firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String url = uri.toString();
                            String pictureName = taskSnapshot.getStorage().getPath();
                            Log.d(TAG, "name: " + pictureName);
                            Log.d(TAG, "url: " + url);
                            showImage(url);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Toast.makeText(Profile.this, "Failed to Upload"+exception.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }

    private void showImage(String url){
        if(url != null && url.isEmpty() == false){
            int width = Resources.getSystem().getDisplayMetrics().widthPixels;
            Picasso.with(this)
                    .load(url)
                    .resize(width, width*2/3)
                    .centerCrop()
                    .into(profileImage);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        StorageReference ref = FirebaseStorage.getInstance().getReference().child(g + "/image");

        try {
            final File localFile = File.createTempFile("pic", "jpg");
            ref.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    profileImage.setImageURI(Uri.fromFile(localFile));
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(Profile.this, "Please Set Your Profile Image from the profile section", Toast.LENGTH_LONG).show();
                    return;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
