package com.lambz.lingochat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SignUpActivity extends AppCompatActivity
{
    private static final long VIBRATION_DURATION = 500;
    private static final String TAG = "SignUpActivity";
    private EditText mEmailEditText, mPasswordEditText, mConfirmPasswordEditText, mNameEditText;
    private Animation mAnimation;
    private Vibrator mVibrator;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mNameEditText = findViewById(R.id.name_edittext);
        mEmailEditText = findViewById(R.id.email_edittext);
        mPasswordEditText = findViewById(R.id.password_editText);
        mConfirmPasswordEditText = findViewById(R.id.cpassword_editText);
        mAnimation = AnimationUtils.loadAnimation(this, R.anim.shake);
        mVibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        mAuth = FirebaseAuth.getInstance();
    }

    public void signInClicked(View view)
    {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void signUpClicked(View view)
    {
        String email = mEmailEditText.getText().toString().trim();
        String password = mPasswordEditText.getText().toString();
        String cpassword = mConfirmPasswordEditText.getText().toString();
        String name = mNameEditText.getText().toString();
        if (name.isEmpty())
        {
            mNameEditText.setError("Name is required!");
            mNameEditText.requestFocus();
            shakeAndVibrate(mNameEditText);
            return;
        }
        if (email.isEmpty())
        {
            mEmailEditText.setError("Email is required!");
            mEmailEditText.requestFocus();
            shakeAndVibrate(mEmailEditText);
            return;
        } else if (!Utils.validate(email))
        {
            mEmailEditText.setError("Enter valid email address!");
            mEmailEditText.requestFocus();
            shakeAndVibrate(mEmailEditText);
            return;
        } else if (password.isEmpty())
        {
            mPasswordEditText.setError("Password is required!");
            mPasswordEditText.requestFocus();
            shakeAndVibrate(mPasswordEditText);
            return;
        } else if (password.length() < 6)
        {
            mPasswordEditText.setError("Password must be at least 6 characters long!");
            mPasswordEditText.requestFocus();
            shakeAndVibrate(mPasswordEditText);
            return;
        } else if (cpassword.isEmpty())
        {
            mConfirmPasswordEditText.setError("Confirm Password is required!");
            mConfirmPasswordEditText.requestFocus();
            shakeAndVibrate(mConfirmPasswordEditText);
            return;
        } else if (cpassword.length() < 6)
        {
            mConfirmPasswordEditText.setError("Confirm Password must be at least 6 characters long!");
            mConfirmPasswordEditText.requestFocus();
            shakeAndVibrate(mConfirmPasswordEditText);
            return;
        } else if (!password.equals(cpassword))
        {
            mConfirmPasswordEditText.setError("Confirm Password does not match Password!");
            mConfirmPasswordEditText.requestFocus();
            shakeAndVibrate(mConfirmPasswordEditText);
            return;
        } else
        {
            password = Utils.sha256(password);
            signUp(email, password, name);
        }
    }

    private void signUp(String email, String password, final String name)
    {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task ->
                {
                    if (task.isSuccessful())
                    {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success");
                        //FirebaseUser user = mAuth.getCurrentUser();
                        addName(task.getResult().getUser(), name);
                        startNextActivity();
                        //updateUI(user);
                    } else
                    {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(SignUpActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                        //updateUI(null);
                    }

                    // ...
                });
    }

    private void startNextActivity()
    {
        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void addName(FirebaseUser user, String name)
    {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(task ->
                {
                    if (task.isSuccessful())
                    {
                        Log.d(TAG, "User profile updated.");
                    }
                });
    }

    private void shakeAndVibrate(EditText editText)
    {
        mVibrator.vibrate(VIBRATION_DURATION);
        editText.startAnimation(mAnimation);
    }
}