package com.lambz.lingo_chat;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;

import de.hdodenhof.circleimageview.CircleImageView;

public class GetStartedDialog extends Dialog
{
    private CircleImageView mUserImageView;
    private Button mSaveButton;
    private Spinner mLanguageSpinner;

    public GetStartedDialog(@NonNull Context context)
    {
        super(context);
        this.setContentView(R.layout.user_start);
        this.setCanceledOnTouchOutside(false);
        this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        Window window = this.getWindow();
        window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
        mUserImageView = findViewById(R.id.user_imageview);
        mSaveButton = findViewById(R.id.save_btn);
        mLanguageSpinner = findViewById(R.id.language_spinner);
    }

    public CircleImageView getmUserImageView()
    {
        return mUserImageView;
    }

    public Button getmSaveButton()
    {
        return mSaveButton;
    }

    public int getLanguageSelection()
    {
        return mLanguageSpinner.getSelectedItemPosition();
    }

    public void setImageBitmap(Bitmap image)
    {
        System.out.println("setting");
        mUserImageView.setImageBitmap(image);
    }
}
