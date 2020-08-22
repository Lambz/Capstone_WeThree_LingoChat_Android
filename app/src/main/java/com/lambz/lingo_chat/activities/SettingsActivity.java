package com.lambz.lingo_chat.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.lambz.lingo_chat.R;
import com.lambz.lingo_chat.Utils;
import com.lambz.lingo_chat.models.Contact;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import segmented_control.widget.custom.android.com.segmentedcontrol.SegmentedControl;

public class SettingsActivity extends AppCompatActivity
{
    private static final int CAMERA_PERM_CODE = 151;
    private static final int CAMERA_REQUEST_CODE = 152;
    private static final int GALLERY_REQUEST_CODE = 2404;
    private static final long VIBRATION_DURATION = 500;

    private CircleImageView mCircleImageView;
    private EditText mNameEditText;
    private SegmentedControl mSegmentedControl;
    private HashMap<String, Object> mUserDataMap = new HashMap<>();
    private Bitmap mSelectedImage;
    private Animation mAnimation;
    private Vibrator mVibrator;
    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setMemberVariables();
    }

    private void setMemberVariables()
    {
        getSupportActionBar().hide();
        mCircleImageView = findViewById(R.id.image_view);
        mNameEditText = findViewById(R.id.name_editText);
        mSegmentedControl = findViewById(R.id.segmented_control);
        mAnimation = AnimationUtils.loadAnimation(this, R.anim.shake);
        mVibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid()).addValueEventListener(mValueEventListener);
    }

    public void backClicked(View view)
    {
        finish();
    }

    public void saveClicked(View view)
    {
        String name = mNameEditText.getText().toString();
        if (name.isEmpty())
        {
            mNameEditText.setError(getString(R.string.name_required_error));
            shakeAndVibrate(mNameEditText);
            return;
        }
        String strs[] = name.split(" ");
        mUserDataMap.put("first_name", strs[0]);
        if (strs.length > 1)
        {
            mUserDataMap.put("last_name", strs[1]);
        } else
        {
            mUserDataMap.put("last_name", "");
        }
        int lang = mSegmentedControl.getLastSelectedAbsolutePosition();
        mUserDataMap.put("lang", String.valueOf(lang));
        FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid()).updateChildren(mUserDataMap);
        changeAppLanguage(lang);
        if (mSelectedImage != null)
        {
            addDisplayImage(mSelectedImage);
        }
        finish();
    }

    private void addDisplayImage(Bitmap image)
    {
        StorageReference filePath = FirebaseStorage.getInstance().getReference().child("Profile Images").child(mCurrentUser.getUid() + ".jpg");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        filePath.putBytes(data).continueWithTask(task ->
        {
            if (!task.isSuccessful())
            {
                throw task.getException();
            }
            return filePath.getDownloadUrl();
        }).addOnCompleteListener(task ->
        {
            if (task.isSuccessful())
            {
                Uri download_uri = task.getResult();
                mUserDataMap.put("image", download_uri.toString());
                FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid()).updateChildren(mUserDataMap);
            }
        });
    }

    private void changeAppLanguage(int lang)
    {
        String languageToLoad  = Utils.getLanguageCode(lang);
        Locale locale = new Locale(languageToLoad);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
        this.setContentView(R.layout.activity_settings);
    }

    ValueEventListener mValueEventListener = new ValueEventListener()
    {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot)
        {
            for (DataSnapshot ds : snapshot.getChildren())
            {
                mUserDataMap.put(ds.getKey(), String.valueOf(ds.getValue()));
            }
            String imgurl = (String) mUserDataMap.getOrDefault("image", null);
            if(imgurl != null && !imgurl.isEmpty())
            {
                Picasso.get().load(imgurl).error(R.mipmap.placeholder).placeholder(R.mipmap.placeholder).into(mCircleImageView);
            }
            mNameEditText.setText(mUserDataMap.get("first_name") + " " + mUserDataMap.get("last_name"));
            String lang = (String) mUserDataMap.getOrDefault("lang","0");
            if(lang.isEmpty())
            {
                mSegmentedControl.setSelectedSegment(0);
            }
            else
            {
                mSegmentedControl.setSelectedSegment(Integer.parseInt(lang));
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error)
        {

        }
    };

    public void imageClicked(View view)
    {
        String[] arr = {getString(R.string.camera), getString(R.string.gallery)};
        AlertDialog.Builder dialog = new AlertDialog.Builder(SettingsActivity.this);
        dialog.setTitle(R.string.choose_image);
        dialog.setItems(arr, (dialog1, position) ->
        {
            if (position == 0)
            {
                askCameraPermissions();
            } else
            {
                ImagePicker.Companion.with(SettingsActivity.this)
                        .galleryOnly()
                        .compress(1024)
                        .start();
            }
        });
        dialog.setPositiveButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss());
        AlertDialog alert = dialog.create();
        alert.show();
    }

    private void askCameraPermissions()
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_PERM_CODE);
        } else
        {
            openCamera();
        }
    }

    private void openCamera()
    {
        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(camera, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if (requestCode == CAMERA_PERM_CODE)
        {
            if (grantResults.length < 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
            {
                openCamera();
                //dispatchTakePictureIntent();
            } else
            {
                Toast.makeText(this, R.string.camera_permission, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE)
        {
            if (resultCode == RESULT_OK)
            {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                mSelectedImage = bitmap;
                mCircleImageView.setImageBitmap(mSelectedImage);
            }
        } else if (requestCode == GALLERY_REQUEST_CODE)
        {
            if (resultCode == RESULT_OK && data != null && data.getData() != null)
            {
                File file = ImagePicker.Companion.getFile(data);
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                mSelectedImage = bitmap;
                mCircleImageView.setImageBitmap(mSelectedImage);
            }
        }
    }

    private void shakeAndVibrate(EditText editText)
    {
        mVibrator.vibrate(VIBRATION_DURATION);
        editText.startAnimation(mAnimation);
    }

    public void signOutClicked(View view)
    {
        LoginManager.getInstance().logOut();
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, StartupActivity.class);
        startActivity(intent);
        finish();
    }
}