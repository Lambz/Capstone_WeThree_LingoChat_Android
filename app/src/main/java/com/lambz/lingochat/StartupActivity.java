package com.lambz.lingochat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class StartupActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);
    }

    public void signUpClicked(View view)
    {
        Intent intent = new Intent(this,SignUpActivity.class);
        startActivity(intent);
        finish();
    }

    public void signInClicked(View view)
    {
        Intent intent = new Intent(this,LoginActivity.class);
        startActivity(intent);
        finish();
    }
}