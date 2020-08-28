package com.lambz.lingo_chat.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lambz.lingo_chat.R;
import com.lambz.lingo_chat.Utils;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity
{
    private static final long VIBRATION_DURATION = 500;
    private static final String TAG = "SignUpActivity";
    private EditText mEmailEditText, mPasswordEditText, mConfirmPasswordEditText, mNameEditText;
    private Animation mAnimation;
    private Vibrator mVibrator;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseReference;

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
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        getSupportActionBar().hide();
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
            mNameEditText.setError(getString(R.string.name_required_error));
            mNameEditText.requestFocus();
            shakeAndVibrate(mNameEditText);
            return;
        }
        if (email.isEmpty())
        {
            mEmailEditText.setError(getString(R.string.email_required_error));
            mEmailEditText.requestFocus();
            shakeAndVibrate(mEmailEditText);
            return;
        } else if (!Utils.validate(email))
        {
            mEmailEditText.setError(getString(R.string.invalid_email_error));
            mEmailEditText.requestFocus();
            shakeAndVibrate(mEmailEditText);
            return;
        } else if (password.isEmpty())
        {
            mPasswordEditText.setError(getString(R.string.password_error));
            mPasswordEditText.requestFocus();
            shakeAndVibrate(mPasswordEditText);
            return;
        } else if (password.length() < 8)
        {
            mPasswordEditText.setError(getString(R.string.short_password_error));
            mPasswordEditText.requestFocus();
            shakeAndVibrate(mPasswordEditText);
            return;
        } else if (cpassword.isEmpty())
        {
            mConfirmPasswordEditText.setError(getString(R.string.cpassword_error));
            mConfirmPasswordEditText.requestFocus();
            shakeAndVibrate(mConfirmPasswordEditText);
            return;
        } else if (cpassword.length() < 8)
        {
            mConfirmPasswordEditText.setError(getString(R.string.short_cpassword_error));
            mConfirmPasswordEditText.requestFocus();
            shakeAndVibrate(mConfirmPasswordEditText);
            return;
        } else if (!password.equals(cpassword))
        {
            mConfirmPasswordEditText.setError(getString(R.string.password_mismatch_error));
            mConfirmPasswordEditText.requestFocus();
            shakeAndVibrate(mConfirmPasswordEditText);
            return;
        } else
        {
//            password = Utils.sha256(password);
            Log.v(TAG,"signUpClicked: email:"+email+" password:"+password+" name:"+name);
            signUp(email, password, name);
        }
    }

    private void signUp(String email, String password, final String name)
    {
        Log.v(TAG,"signUpClicked: email:"+email+" password:"+password+" name:"+name);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task ->
                {
                    if (task.isSuccessful())
                    {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success");
                        //FirebaseUser user = mAuth.getCurrentUser();
                        String current_user_id = mAuth.getCurrentUser().getUid();
                        mDatabaseReference.child("Users").child(current_user_id).setValue("");
                        Log.v(TAG,"signUp: email:"+email+" password:"+password+" name:"+name);
                        addName(task.getResult().getUser(), name,email);
                        sendUserToMainActivity();
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

    private void sendUserToMainActivity()
    {
        Intent intent = new Intent(SignUpActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void addName(FirebaseUser user, String name, String email)
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

        Log.v(TAG,"addName: email:"+email+" name:"+name);
        addUserInfo(name,email);
    }

    private void shakeAndVibrate(EditText editText)
    {
        mVibrator.vibrate(VIBRATION_DURATION);
        editText.startAnimation(mAnimation);
    }

    private void addUserInfo(String name, String email)
    {
        Log.v(TAG,"addUserInfo: email:"+email+" name:"+name);
        HashMap<String, String > profile_data = new HashMap<String,String>();
        String strs [] = name.split(" ");
        profile_data.put("first_name",strs[0]);
        if(strs.length>1)
        {
            profile_data.put("last_name",strs[1]);
        }
        else
        {
            profile_data.put("last_name","");
        }
        profile_data.put("lang","");
        profile_data.put("image","");
        profile_data.put("email", email);
        mDatabaseReference.child("Users").child(mAuth.getCurrentUser().getUid()).setValue(profile_data);
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(this,StartupActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event)
    {
        if (event.getAction() == MotionEvent.ACTION_DOWN)
        {
            View v = getCurrentFocus();
            if (v instanceof EditText)
            {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY()))
                {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }
}