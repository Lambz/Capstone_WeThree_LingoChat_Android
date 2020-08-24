package com.lambz.lingo_chat.activities;

import androidx.annotation.NonNull;
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

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.facebook.FacebookSdk;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lambz.lingo_chat.R;
import com.lambz.lingo_chat.Utils;

import org.json.JSONException;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity
{
    private static final long VIBRATION_DURATION = 500;
    private EditText mEmailEditText, mPasswordEditText;
    private Animation mAnimation;
    private Vibrator mVibrator;
    private LoginButton mLoginButton;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private int RC_SIGN_IN = 121;
    private String TAG = "LoginActivity";
    private CallbackManager mCallbackManager;
    private DatabaseReference mDatabaseReference;
    private String mName;
    private String mEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        FacebookSdk.fullyInitialize();
        setContentView(R.layout.activity_login);
        mEmailEditText = findViewById(R.id.email_edittext);
        mPasswordEditText = findViewById(R.id.password_editText);
        mAnimation = AnimationUtils.loadAnimation(this, R.anim.shake);
        mLoginButton = findViewById(R.id.login_button);
        mVibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mCallbackManager = CallbackManager.Factory.create();
        mLoginButton = findViewById(R.id.login_button);
        mLoginButton.setReadPermissions("email", "public_profile");

        mLoginButton.registerCallback(mCallbackManager, mFacebookCallback);

        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setOnClickListener(mGoogleSignInClicked);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        getSupportActionBar().hide();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null)
        {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void signUpClicked(View view)
    {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
        finish();
    }

    public void signInClicked(View view)
    {
        String email = mEmailEditText.getText().toString().trim();
        String password = mPasswordEditText.getText().toString();
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
        } else
        {
//            password = Utils.sha256(password);
            login(email, password);
        }
    }

    private void login(String email, String password)
    {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task ->
        {
            if (task.isSuccessful())
            {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void shakeAndVibrate(EditText editText)
    {
        mVibrator.vibrate(VIBRATION_DURATION);
        editText.startAnimation(mAnimation);
    }

    SignInButton.OnClickListener mGoogleSignInClicked = new SignInButton.OnClickListener()
    {

        @Override
        public void onClick(View view)
        {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        }
    };

//    public void gmailClicked(View view)
//    {
//        System.out.println("clicking");
//        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
//        startActivityForResult(signInIntent, RC_SIGN_IN);
//    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN)
        {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try
            {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken(), account.getDisplayName(), account.getEmail());
            } catch (ApiException e)
            {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e.getCause());
                // ...
            }
        }
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void firebaseAuthWithGoogle(String idToken, String name, String email)
    {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task ->
                {
                    if (task.isSuccessful())
                    {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");
                        //FirebaseUser user = mAuth.getCurrentUser();
//                        String current_user_id = mAuth.getCurrentUser().getUid();
//                        mDatabaseReference.child("Users").child(current_user_id).setValue("");
                        mName = name;
                        mEmail = email;
//                        mDatabaseReference.child("Users").child(current_user_id).addValueEventListener(mValueEventListener);
                        mDatabaseReference.child("Users").addValueEventListener(mValueEventListener);
                        sendUserToMainActivity();
                        //updateUI(user);
                    } else
                    {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Snackbar.make(LoginActivity.this.findViewById(android.R.id.content).getRootView(), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
//                            updateUI(null);
                    }

                    // ...
                });
    }

    FacebookCallback<LoginResult> mFacebookCallback = new FacebookCallback<LoginResult>()
    {
        @Override
        public void onSuccess(LoginResult loginResult)
        {
            Log.d(TAG, "facebook:onSuccess:" + loginResult);
            handleFacebookAccessToken(loginResult.getAccessToken());
        }

        @Override
        public void onCancel()
        {
            Log.d(TAG, "facebook:onCancel");
            // ...
        }

        @Override
        public void onError(FacebookException error)
        {
            Log.d(TAG, "facebook:onError", error);
            // ...
        }
    };

    private void handleFacebookAccessToken(AccessToken token)
    {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task ->
                {
                    if (task.isSuccessful())
                    {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");
//                            FirebaseUser user = mAuth.getCurrentUser();
                        String current_user_id = mAuth.getCurrentUser().getUid();
                        //mDatabaseReference.child("Users").child(current_user_id).setValue("");
                        getNameFromFacebook(token);
                        sendUserToMainActivity();
//                            updateUI(user);
                    } else
                    {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
//                            updateUI(null);
                    }

                    // ...
                });
    }

    private void getNameFromFacebook(AccessToken access_token)
    {
        GraphRequest request = GraphRequest.newMeRequest(access_token, (object, response) ->
        {
            String name = null;
            String email = null;
            try
            {
                mName = object.getString("name");
                mEmail = object.getString("email");
//                mDatabaseReference.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(mValueEventListener);
                mDatabaseReference.child("Users").addValueEventListener(mValueEventListener);
                //                addUserInfo(name, email);
            } catch (JSONException e)
            {
                e.printStackTrace();
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "name,email");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void sendUserToMainActivity()
    {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void addUserInfo(String name, String email, HashMap<String, Object> profile_data)
    {
        for(String key: profile_data.keySet())
        {
            Log.v(TAG,key+": "+profile_data.get(key));
        }
        String[] str = name.split(" ");
        profile_data.put("first_name", str[0]);
        if (str.length > 1)
        {
            profile_data.put("last_name", str[1]);
        } else
        {
            profile_data.put("last_name","");
        }
        profile_data.put("email", email);
        if(!profile_data.containsKey("lang"))
        {
            profile_data.put("lang", "");
        }
        if(!profile_data.containsKey("image"))
        {
            profile_data.put("image", "");
        }
        mDatabaseReference.child("Users").child(mAuth.getCurrentUser().getUid()).updateChildren(profile_data);
    }

    ValueEventListener mValueEventListener = new ValueEventListener()
    {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot)
        {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            if(!snapshot.hasChild(uid))
            {
                mDatabaseReference.child("Users").child(uid).setValue("");
            }
            snapshot = snapshot.child(uid);
            HashMap<String, Object> profile_data = new HashMap<>();
            for (DataSnapshot dataSnapshot : snapshot.getChildren())
            {
                profile_data.put(dataSnapshot.getKey(), dataSnapshot.getValue().toString());
            }
            addUserInfo(mName, mEmail, profile_data);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error)
        {

        }
    };

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