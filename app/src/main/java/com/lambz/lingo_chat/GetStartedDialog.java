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
import segmented_control.widget.custom.android.com.segmentedcontrol.SegmentedControl;

public class GetStartedDialog extends Dialog
{
    private CircleImageView mUserImageView;
    private Button mSaveButton;
    private SegmentedControl mSegmentedControl;
//    private Spinner mLanguageSpinner;

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
//        mLanguageSpinner = findViewById(R.id.language_spinner);
        mSegmentedControl = findViewById(R.id.segmented_control);
        mSegmentedControl.setSelectedSegment(0);
    }

    public CircleImageView getUserImageView()
    {
        return mUserImageView;
    }

    public Button getSaveButton()
    {
        return mSaveButton;
    }

    public int getLanguageSelection()
    {
        return mSegmentedControl.getLastSelectedAbsolutePosition();
    }

    public void setImageBitmap(Bitmap image)
    {
        mUserImageView.setImageBitmap(image);
    }
}
