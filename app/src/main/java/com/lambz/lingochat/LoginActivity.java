package com.lambz.lingochat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity
{
    private static final long VIBRATION_DURATION = 500;
    private EditText mEmailEditText, mPasswordEditText;
    private Animation mAnimation;
    private Vibrator mVibrator;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mEmailEditText = findViewById(R.id.email_edittext);
        mPasswordEditText = findViewById(R.id.password_editText);
        mAnimation = AnimationUtils.loadAnimation(this, R.anim.shake);
        mVibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        mAuth = FirebaseAuth.getInstance();
    }

    public void signUpClicked(View view)
    {
        Intent intent = new Intent(this,SignUpActivity.class);
        startActivity(intent);
        finish();
    }

    public void signInClicked(View view)
    {
        String email = mEmailEditText.getText().toString().trim();
        String password = mPasswordEditText.getText().toString();
        if(email.isEmpty())
        {
            mEmailEditText.setError("Email is required!");
            mEmailEditText.requestFocus();
            shakeAndVibrate(mEmailEditText);
            return;
        }
        else if(!Utils.validate(email))
        {
            mEmailEditText.setError("Enter valid email address!");
            mEmailEditText.requestFocus();
            shakeAndVibrate(mEmailEditText);
            return;
        }
        else if(password.isEmpty())
        {
            mPasswordEditText.setError("Password is required!");
            mPasswordEditText.requestFocus();
            shakeAndVibrate(mPasswordEditText);
            return;
        }
        else
        {
            password = Utils.sha256(password);
            login(email,password);
        }
    }

    private void login(String email, String password)
    {
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>()
        {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task)
            {
                if(task.isSuccessful())
                {
                    Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    private void shakeAndVibrate(EditText editText)
    {
        mVibrator.vibrate(VIBRATION_DURATION);
        editText.startAnimation(mAnimation);
    }
}