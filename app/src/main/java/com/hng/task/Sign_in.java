package com.hng.task;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hng.task.models.User;

public class Sign_in extends AppCompatActivity {

    //widgets
    private TextView signUpLink;
    private EditText mEmail;
    private EditText mPassword;
    private TextView mforgotPassword;
    private ProgressBar mBar;

    //firebase variables
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        //init firebase
        mAuth = FirebaseAuth.getInstance();

        //init widgets
        signUpLink = findViewById(R.id.sign_up);
        mEmail = findViewById(R.id.txt_username);
        mPassword = findViewById(R.id.txt_password);
        mforgotPassword=findViewById(R.id.forgot_password);
        mBar = findViewById(R.id.progressBar);

        mBar.setVisibility(View.INVISIBLE);



        signUpLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Sign_in.this,Sign_up.class));
                finish();
            }
        });
        mforgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Sign_in.this,Forget_password.class));
                finish();
            }
        });

    }

    public void signIn(View view) {
        if(TextUtils.isEmpty(mEmail.getText().toString()) && !mEmail.getText().toString().contains("@")){
            Toast.makeText(Sign_in.this, "Please Enter a valid Email Address", Toast.LENGTH_LONG).show();
            return;
        } else if(TextUtils.isEmpty(mPassword.getText().toString())){
            Toast.makeText(Sign_in.this, "Please Enter a valid Password", Toast.LENGTH_LONG).show();
            return;
        } else {
            //Register User
            mBar.setVisibility(View.VISIBLE);
            mAuth.signInWithEmailAndPassword(mEmail.getText().toString(), mPassword.getText().toString())
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            startActivity(new Intent(Sign_in.this,Chat_screen.class));
                            finish();
                            mBar.setVisibility(View.INVISIBLE);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Sign_in.this, "Authentication Failed: "  + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
            ;
        }
    }
}
