package com.lambz.lingo_chat.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.lambz.lingo_chat.R;
import com.lambz.lingo_chat.Utils;
import com.lambz.lingo_chat.adapters.MessageAdapter;
import com.lambz.lingo_chat.models.Contact;
import com.lambz.lingo_chat.models.Message;
import com.lambz.lingo_chat.models.UserLocation;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

public class ChatActivity extends AppCompatActivity
{
    private static final String TAG = "ChatActivity";
    private static final int SELECT_FILE = 438;
    private static final int MAPS_REQUEST_CODE = 678;
    private ImageView mUserImageView;
    private TextView mUserNameTextView;
    private EditText mMessageEditText;
    private Contact mContact;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mCurrentUser;
    private RecyclerView mRecyclerView;
    private List<Message> mMessageList = new ArrayList<>();
    private MessageAdapter mMessageAdapter;
    private Translate mTranslate;
    private String mFileType = "", mURL = "";
    private Uri mFileUri;
    private StorageTask mUploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        getSupportActionBar().hide();
        setMemberVariables();
        setupRecyclerView();
        setupBlurView();
    }

    private void setupBlurView()
    {
        float radius = 20f;

        View decorView = getWindow().getDecorView();
        //ViewGroup you want to start blur from. Choose root as close to BlurView in hierarchy as possible.
        ViewGroup rootView = (ViewGroup) decorView.findViewById(android.R.id.content);
        //Set drawable to draw in the beginning of each blurred frame (Optional).
        //Can be used in case your layout has a lot of transparent space and your content
        //gets kinda lost after after blur is applied.
        Drawable windowBackground = decorView.getBackground();

        ((BlurView) findViewById(R.id.blurView)).setupWith(rootView)
                .setFrameClearDrawable(windowBackground)
                .setBlurAlgorithm(new RenderScriptBlur(this))
                .setBlurRadius(radius)
                .setHasFixedTransformationMatrix(true);

        ((BlurView) findViewById(R.id.texts_blurview)).setupWith(rootView)
                .setFrameClearDrawable(windowBackground)
                .setBlurAlgorithm(new RenderScriptBlur(this))
                .setBlurRadius(radius)
                .setHasFixedTransformationMatrix(true);
    }

    private void setupRecyclerView()
    {
        mMessageAdapter = new MessageAdapter(mMessageList, this, mTranslate);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mMessageAdapter);
        OverScrollDecoratorHelper.setUpOverScroll(mRecyclerView, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);
    }

    private void setMemberVariables()
    {
        mUserImageView = findViewById(R.id.user_imageview);
        mUserNameTextView = findViewById(R.id.user_name_textview);
        mMessageEditText = findViewById(R.id.message_edittext);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mContact = (Contact) getIntent().getSerializableExtra("contact");
        if (!mContact.getImage().isEmpty())
        {
            Picasso.get().load(mContact.getImage()).placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder).into(mUserImageView);
        }
        mUserNameTextView.setText(mContact.getName());
        mRecyclerView = findViewById(R.id.recyclerview);
        mTranslate = TranslateOptions.newBuilder().setApiKey(getString(R.string.google_translate_api_key)).build().getService();
        new Thread(() ->
        {
            Translation translation = mTranslate.translate(mContact.getName(), Translate.TranslateOption.targetLanguage(Utils.getLanguageCode()));
            new Handler(Looper.getMainLooper()).post(() -> mUserNameTextView.setText(translation.getTranslatedText()));
        }).start();
    }

    public void backClicked(View view)
    {
        finish();
    }

    public void sendClicked(View view)
    {
        String message = mMessageEditText.getText().toString();
        if (message.isEmpty())
        {
            return;
        }

        String messageSenderRef = "Messages/" + mCurrentUser.getUid() + "/" + mContact.getUid();
        String messageReceiverRef = "Messages/" + mContact.getUid() + "/" + mCurrentUser.getUid();

        DatabaseReference databaseReference = mDatabaseReference.child("Messages").child(mContact.getUid()).child(mCurrentUser.getUid()).push();

        String message_key = databaseReference.getKey();

        HashMap<String, String> message_data = new HashMap<>();
        message_data.put("text", message);
        message_data.put("type", "text");
        message_data.put("from", mCurrentUser.getUid());
        message_data.put("lang", Utils.getLanguageCode());
        message_data.put("link", "");
        message_data.put("to", mContact.getUid());
        message_data.put("id",message_key);

        Map message_body_details = new HashMap();
        message_body_details.put(messageSenderRef + "/" + message_key, message_data);
        message_body_details.put(messageReceiverRef + "/" + message_key, message_data);

        mDatabaseReference.updateChildren(message_body_details).addOnCompleteListener(task ->
        {
            if (task.isSuccessful())
            {
                Log.v(TAG, "sendClicked: Message Pushed");
            } else
            {
                Log.v(TAG, "sendClicked: Error");
            }
            mMessageEditText.setText("");
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        mDatabaseReference.child("Messages").child(mCurrentUser.getUid()).child(mContact.getUid()).addChildEventListener(mChildEventListener);
    }

    ChildEventListener mChildEventListener = new ChildEventListener()
    {
        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName)
        {
            Message message = snapshot.getValue(Message.class);
            Log.v(TAG,"onChildAdded called");
            boolean cond = true;
            for(Message message1: mMessageList)
            {
                if(message1.equals(message))
                {
                    cond = false;
                    break;
                }
            }
            if(cond)
            {
                mMessageList.add(message);
            }
            mMessageAdapter.setMessageList(mMessageList);
            mRecyclerView.smoothScrollToPosition(mRecyclerView.getAdapter().getItemCount());
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName)
        {

        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot snapshot)
        {

        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName)
        {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError error)
        {

        }
    };

    public void sendFilesClicked(View view)
    {
        CharSequence options [] = new CharSequence[]{getString(R.string.images),getString(R.string.pdf_files),getString(R.string.ms_word_files),getString(R.string.location)};
        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
        builder.setTitle(R.string.select_the_file);
        builder.setItems(options, (dialogInterface, i) ->
        {
            if (i == 0)
            {
                mFileType = "image";

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent.createChooser(intent,getString(R.string.select_image)), SELECT_FILE);
            }
            else if(i == 1)
            {
                mFileType = "pdf";

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("application/pdf");
                startActivityForResult(intent.createChooser(intent,getString(R.string.select_pdf_file)), SELECT_FILE);
            }
            else if(i == 2)
            {
                mFileType = "docx";

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
                startActivityForResult(intent.createChooser(intent,getString(R.string.select_word_file)), SELECT_FILE);
            }
            else if(i == 3)
            {
                mFileType = "location";
                Intent intent = new Intent(ChatActivity.this,MapsActivity.class);
                startActivityForResult(intent,MAPS_REQUEST_CODE);
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SELECT_FILE && resultCode == RESULT_OK && data!= null && data.getData() != null)
        {
            mFileUri = data.getData();

            if(!mFileType.equals("image"))
            {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Documents");
                String messageSenderRef = "Messages/" + mCurrentUser.getUid() + "/" + mContact.getUid();
                String messageReceiverRef = "Messages/" + mContact.getUid() + "/" + mCurrentUser.getUid();

                DatabaseReference databaseReference = mDatabaseReference.child("Messages").child(mContact.getUid()).child(mCurrentUser.getUid()).push();

                String message_key = databaseReference.getKey();
                StorageReference filePath = storageReference.child(message_key+"."+mFileType);

                filePath.putFile(mFileUri).continueWithTask((Continuation) task ->
                {
                    if(!task.isSuccessful())
                    {
                        Log.v(TAG,"then: "+task.getException().getMessage());
                    }
                    return filePath.getDownloadUrl();
                }).addOnCompleteListener((OnCompleteListener<Uri>) task ->
                {
                    Uri downloadUrl = task.getResult();
                    mURL = downloadUrl.toString();

                    HashMap<String, String> message_data = new HashMap<>();
                    message_data.put("text", "");
                    message_data.put("type", mFileType);
                    message_data.put("from", mCurrentUser.getUid());
                    message_data.put("lang", Utils.getIntLanguageCode());
                    message_data.put("link", mURL);
                    message_data.put("fileName", mFileUri.getLastPathSegment());
                    message_data.put("to", mContact.getUid());
                    message_data.put("id",message_key);

                    Map message_body_details = new HashMap();
                    message_body_details.put(messageSenderRef + "/" + message_key, message_data);
                    message_body_details.put(messageReceiverRef + "/" + message_key, message_data);

                    mDatabaseReference.updateChildren(message_body_details).addOnCompleteListener(task1 ->
                    {
                        if (task1.isSuccessful())
                        {
                            Log.v(TAG, "sendClicked: Message Pushed");
                        } else
                        {
                            Log.v(TAG, "sendClicked: Error");
                        }
                        mMessageEditText.setText("");
                    });
                });
            }
            else if(mFileType.equals("image"))
            {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Images");
                String messageSenderRef = "Messages/" + mCurrentUser.getUid() + "/" + mContact.getUid();
                String messageReceiverRef = "Messages/" + mContact.getUid() + "/" + mCurrentUser.getUid();

                DatabaseReference databaseReference = mDatabaseReference.child("Messages").child(mContact.getUid()).child(mCurrentUser.getUid()).push();

                String message_key = databaseReference.getKey();
                StorageReference filePath = storageReference.child(message_key+".jpg");
                mUploadTask = filePath.putFile(mFileUri);
                mUploadTask.continueWithTask((Continuation) task ->
                {
                    if(!task.isSuccessful())
                    {
                        Log.v(TAG,"then: "+task.getException().getMessage());
                    }
                    return filePath.getDownloadUrl();
                }).addOnCompleteListener((OnCompleteListener<Uri>) task ->
                {
                    if(task.isSuccessful())
                    {
                        Uri downloadUrl = task.getResult();
                        mURL = downloadUrl.toString();

                        HashMap<String, String> message_data = new HashMap<>();
                        message_data.put("text", "");
                        message_data.put("type", mFileType);
                        message_data.put("from", mCurrentUser.getUid());
                        message_data.put("lang", Utils.getIntLanguageCode());
                        message_data.put("link", mURL);
                        message_data.put("fileName", mFileUri.getLastPathSegment());
                        message_data.put("to", mContact.getUid());
                        message_data.put("id",message_key);

                        Map message_body_details = new HashMap();
                        message_body_details.put(messageSenderRef + "/" + message_key, message_data);
                        message_body_details.put(messageReceiverRef + "/" + message_key, message_data);

                        mDatabaseReference.updateChildren(message_body_details).addOnCompleteListener(task1 ->
                        {
                            if (task1.isSuccessful())
                            {
                                Log.v(TAG, "sendClicked: Message Pushed");
                            } else
                            {
                                Log.v(TAG, "sendClicked: Error");
                            }
                            mMessageEditText.setText("");
                        });
                    }
                    else
                    {
                        Log.v(TAG,"onActivityResult addOnCompleteListener: "+task.getException().getMessage());
                    }
                });
            }
            else
            {
                Log.v(TAG,"onActivityResult: problem");
            }
        }
        else if (requestCode == MAPS_REQUEST_CODE && resultCode == RESULT_OK)
        {
            UserLocation location = (UserLocation) data.getSerializableExtra("location");
            sendLocation(location);
        }
    }

    private void sendLocation(UserLocation location)
    {
        String messageSenderRef = "Messages/" + mCurrentUser.getUid() + "/" + mContact.getUid();
        String messageReceiverRef = "Messages/" + mContact.getUid() + "/" + mCurrentUser.getUid();

        DatabaseReference databaseReference = mDatabaseReference.child("Messages").child(mContact.getUid()).child(mCurrentUser.getUid()).push();

        String message_key = databaseReference.getKey();

        HashMap<String, String> message_data = new HashMap<>();
        //        message_data.put("text", message);
        message_data.put("type", "location");
        message_data.put("from", mCurrentUser.getUid());
        message_data.put("lang", Utils.getLanguageCode());
        //        message_data.put("link", "");
        message_data.put("to", mContact.getUid());
        message_data.put("id",message_key);
        message_data.put("lat", String.valueOf(location.getLat()));
        message_data.put("lng", String.valueOf(location.getLng()));
        message_data.put("locationtitle", location.getTitle());

        Map message_body_details = new HashMap();
        message_body_details.put(messageSenderRef + "/" + message_key, message_data);
        message_body_details.put(messageReceiverRef + "/" + message_key, message_data);

        mDatabaseReference.updateChildren(message_body_details).addOnCompleteListener(task ->
        {
            if (task.isSuccessful())
            {
                Log.v(TAG, "sendClicked: Message Pushed");
            } else
            {
                Log.v(TAG, "sendClicked: Error");
            }
            mMessageEditText.setText("");
        });
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