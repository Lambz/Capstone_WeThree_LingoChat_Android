package com.lambz.lingo_chat.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.lambz.lingo_chat.R;

public class SplashScreenActivity extends AppCompatActivity
{

    private LinearLayout mLinearLayout;
    private Animation mFadeInAnimation;
    private Animation mFadeOutAnimation;
    private final int WAIT_TIME = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        getSupportActionBar().hide();
//        Intent intent = new Intent(this,StartupActivity.class);
//        startActivity(intent);
//        finish();
        mLinearLayout = findViewById(R.id.linear_layout);
        mFadeInAnimation = AnimationUtils.loadAnimation(this,R.anim.fade_in);
        mFadeOutAnimation = AnimationUtils.loadAnimation(this,R.anim.fade_out);
        mFadeInAnimation.setAnimationListener(mFadeInAnimationListener);
        mFadeOutAnimation.setAnimationListener(mFadeOutAnimationListener);
        mLinearLayout.startAnimation(mFadeInAnimation);
    }

    private Animation.AnimationListener mFadeInAnimationListener = new Animation.AnimationListener()
    {
        @Override
        public void onAnimationStart(Animation animation)
        {

        }

        @Override
        public void onAnimationEnd(Animation animation)
        {
            new Handler().postDelayed(() -> mLinearLayout.startAnimation(mFadeOutAnimation),WAIT_TIME);
        }

        @Override
        public void onAnimationRepeat(Animation animation)
        {

        }
    };

    private Animation.AnimationListener mFadeOutAnimationListener = new Animation.AnimationListener()
    {
        @Override
        public void onAnimationStart(Animation animation)
        {

        }

        @Override
        public void onAnimationEnd(Animation animation)
        {
            mLinearLayout.setVisibility(View.GONE);
            Intent intent = new Intent(SplashScreenActivity.this, StartupActivity.class);
            startActivity(intent);
            finish();
        }

        @Override
        public void onAnimationRepeat(Animation animation)
        {

        }
    };
}