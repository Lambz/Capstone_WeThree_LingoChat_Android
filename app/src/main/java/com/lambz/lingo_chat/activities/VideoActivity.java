package com.lambz.lingo_chat.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

import com.lambz.lingo_chat.R;

public class VideoActivity extends AppCompatActivity
{
    private VideoView mVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        mVideoView = findViewById(R.id.video_view);
        getSupportActionBar().hide();
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(mVideoView);
        mVideoView.setMediaController(mediaController);
        String url_string = getIntent().getStringExtra("url");
        mVideoView.setVideoPath(url_string);
        mVideoView.start();
    }
    public void backClicked(View view)
    {
        finish();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}