package com.hng.task;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class Forget_password extends AppCompatActivity {
    private Button   mResetPasswordSendEmailButton;
    private EditText mResetEmailInput;
    //firebase variables
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        //init firebase
        mAuth = FirebaseAuth.getInstance();

        //init widgets
        mResetPasswordSendEmailButton = findViewById(R.id.btn_reset_password);
        mResetEmailInput = findViewById(R.id.reset_email);

        mResetPasswordSendEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userEmail= mResetEmailInput.getText().toString();

                if (TextUtils.isEmpty(userEmail))
                {
                    Toast.makeText(Forget_password.this,"Please write your Valid email address first...",Toast.LENGTH_SHORT).show();

                }
                else {
                    mAuth.sendPasswordResetEmail(userEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()){
                                Toast.makeText(Forget_password.this,"Please check your Email Account,if you want to reset your password...",Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(Forget_password.this,Sign_in.class));
                            }
                            else {
                                String message=task.getException().getMessage();
                                Toast.makeText(Forget_password.this,"Error Occured"+message,Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
}
