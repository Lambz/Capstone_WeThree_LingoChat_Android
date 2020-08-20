package com.lambz.lingo_chat.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.lambz.lingo_chat.GetStartedDialog;
import com.lambz.lingo_chat.R;
import com.lambz.lingo_chat.Utils;
import com.lambz.lingo_chat.adapters.ContactMessagedAdapter;
import com.lambz.lingo_chat.models.Contact;
import com.lambz.lingo_chat.models.Message;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

public class MainActivity extends AppCompatActivity
{

    private static final int CAMERA_PERM_CODE = 151;
    private static final int CAMERA_REQUEST_CODE = 152;
    private static final int GALLERY_REQUEST_CODE = 2404;
    private static final String TAG = "MainActivity";
    private static final int NEW_MESSAGE_REQUEST_CODE = 1010;
    private GetStartedDialog mDialog;
    private Bitmap mSelectedImage;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mCurrentUser;
    private StorageReference mStorageReference;
    private HashMap<String, String> mUserInfo;
    private RecyclerView mRecyclerView;
    private ArrayList<Message> mMessageList;
    private ContactMessagedAdapter mContactMessagedAdapter;
    private Translate mTranslate;
    private Locale mLocale;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        setMemberVariables();
        setupRecyclerView();
    }

    private void setupRecyclerView()
    {
        mContactMessagedAdapter = new ContactMessagedAdapter(mMessageList, this, mTranslate);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mContactMessagedAdapter);
        OverScrollDecoratorHelper.setUpOverScroll(mRecyclerView, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);
    }

    private void setMemberVariables()
    {
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getUid());
        mDatabaseReference.addValueEventListener(mValueEventListener);
        mStorageReference = FirebaseStorage.getInstance().getReference().child("Profile Images");
        mUserInfo = new HashMap<>();
        mRecyclerView = findViewById(R.id.recyclerview);
        mTranslate = TranslateOptions.newBuilder().setApiKey(getString(R.string.google_translate_api_key)).build().getService();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        mMessageList = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference().child("Messages").child(mCurrentUser.getUid()).addValueEventListener(mUsersValueEventListener);
    }

    public void newMessageClicked(View view)
    {
        Intent intent = new Intent(this, ContactsActivity.class);
        startActivityForResult(intent, NEW_MESSAGE_REQUEST_CODE);
    }

    private void showGetStartedDialog()
    {
        mDialog = new GetStartedDialog(this);
        mDialog.show();
        mDialog.getSaveButton().setOnClickListener(view ->
        {
            saveInfo(mSelectedImage, mDialog.getLanguageSelection());
            mDialog.dismiss();
        });

        mDialog.getUserImageView().setOnClickListener(view ->
        {
            String[] arr = {getString(R.string.camera), getString(R.string.gallery)};
            AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
            dialog.setTitle(getString(R.string.choose_image));
            dialog.setItems(arr, (dialog1, position) ->
            {
                if (position == 0)
                {
                    askCameraPermissions();
                } else
                {
                    ImagePicker.Companion.with(MainActivity.this)
                            .galleryOnly()
                            .compress(1024)
                            .start();
                }
            });
            dialog.setPositiveButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss());
            AlertDialog alert = dialog.create();
            alert.show();
        });
    }

    private void saveInfo(Bitmap selected_image, int languageSelection)
    {
        changeLanguage(languageSelection);
        changeAppLanguage(languageSelection);
        if (selected_image != null)
        {
            addDisplayImage(selected_image);
        }
    }

    private void changeLanguage(int languageSelection)
    {
        mUserInfo.put("lang", String.valueOf(languageSelection));
        mDatabaseReference.setValue(mUserInfo);
    }



    private void addDisplayImage(Bitmap image)
    {
        StorageReference filePath = mStorageReference.child(mCurrentUser.getUid() + ".jpg");
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
                mUserInfo.put("image", download_uri.toString());
                mDatabaseReference.setValue(mUserInfo);
            }
        });
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
                Toast.makeText(this, "Camera is Required to Use Camera", Toast.LENGTH_SHORT).show();
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
                mDialog.setImageBitmap(mSelectedImage);
            }
        } else if (requestCode == GALLERY_REQUEST_CODE)
        {
            if (resultCode == RESULT_OK && data != null && data.getData() != null)
            {
                File file = ImagePicker.Companion.getFile(data);
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                mSelectedImage = bitmap;
                mDialog.setImageBitmap(mSelectedImage);
            }
        } else if (requestCode == NEW_MESSAGE_REQUEST_CODE)
        {
            System.out.println(resultCode);
            if (resultCode == RESULT_OK && data != null)
            {
                Contact contact = (Contact) data.getSerializableExtra("contact");
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                intent.putExtra("contact", contact);
                startActivity(intent);
                Log.v(TAG, "onActivityResult: name: " + contact.getName());
            }
        }
    }

    public void signOutClicked(View view)
    {
        mAuth.signOut();
        Intent intent = new Intent(this, StartupActivity.class);
        startActivity(intent);
        finish();
    }

    ValueEventListener mValueEventListener = new ValueEventListener()
    {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot)
        {
            mMessageList = new ArrayList<>();
            for (DataSnapshot ds : snapshot.getChildren())
            {
                mUserInfo.put(ds.getKey(), String.valueOf(ds.getValue()));
            }
            if (snapshot.exists() && snapshot.hasChild("lang") && !snapshot.child("lang").getValue().equals(""))
            {
                changeAppLanguage(Integer.valueOf(String.valueOf(snapshot.child("lang").getValue())));
                //Has
            } else
            {
                showGetStartedDialog();
            }
            Utils.setUserData(mUserInfo);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error)
        {

        }
    };


    public void settingsClicked(View view)
    {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    ValueEventListener mUsersValueEventListener = new ValueEventListener()
    {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot)
        {
            for (DataSnapshot ds : snapshot.getChildren())
            {
                Message message = getLastElement(ds).getValue(Message.class);
                mMessageList.add(message);
            }
            mContactMessagedAdapter.setMessageList(mMessageList);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error)
        {

        }

        public DataSnapshot getLastElement(final DataSnapshot dataSnapshot) {
            Iterator<DataSnapshot> itr = dataSnapshot.getChildren().iterator();
            DataSnapshot lastElement = itr.next();
            Message lastMessage = lastElement.getValue(Message.class);
            while(itr.hasNext()) {
                lastElement = itr.next();
            }
            return lastElement;
        }
    };

    private void changeAppLanguage(int lang)
    {
        String languageToLoad = Utils.getLanguageCode(lang);
        Log.v(TAG,"original: "+getResources().getConfiguration().locale+" changing to: "+Utils.getLanguageCode(lang));
        boolean shouldRestart = !Utils.getLanguageCode(lang).equals(getResources().getConfiguration().locale.toString());
        Locale locale = new Locale(languageToLoad);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());

        if(shouldRestart)
        {
            restartApplication();
        }
    }

    private void restartApplication()
    {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        Log.v(TAG,"updated config");
        if (mLocale != null)
        {
            newConfig.locale = mLocale;
            Locale.setDefault(mLocale);
            getBaseContext().getResources().updateConfiguration(newConfig, getBaseContext().getResources().getDisplayMetrics());
        }
    }
}