package com.lambz.lingo_chat.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.lambz.lingo_chat.R;
import com.squareup.picasso.Picasso;

public class ImageViewerActivity extends AppCompatActivity
{
    private ImageView mImageView;
    private String mImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        getSupportActionBar().hide();
        mImageView = findViewById(R.id.imageview);
        mImageUrl = getIntent().getStringExtra("url");
        Picasso.get().load(mImageUrl).placeholder(R.mipmap.placeholder_image).error(R.drawable.image_error).into(mImageView);
    }

    public void backClicked(View view)
    {
        finish();
    }
}