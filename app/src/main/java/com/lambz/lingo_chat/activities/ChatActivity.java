package com.lambz.lingo_chat.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lambz.lingo_chat.R;
import com.lambz.lingo_chat.Utils;
import com.lambz.lingo_chat.adapters.MessageAdapter;
import com.lambz.lingo_chat.models.Contact;
import com.lambz.lingo_chat.models.Message;
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
    private ImageView mUserImageView;
    private TextView mUserNameTextView;
    private EditText mMessageEditText;
    private Contact mContact;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mCurrentUser;
    private RecyclerView mRecyclerView;
    private List<Message> mMessageList = new ArrayList<>();
    private MessageAdapter mMessageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        getSupportActionBar().hide();
        setMemberVariables();
        setupRecyclerView();setupBlurView();
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
    }

    private void setupRecyclerView()
    {
        mMessageAdapter = new MessageAdapter(mMessageList, this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
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
        if(!mContact.getImage().isEmpty())
        {
            Picasso.get().load(mContact.getImage()).placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder).into(mUserImageView);
        }
        mUserNameTextView.setText(mContact.getName());
        mRecyclerView = findViewById(R.id.recyclerview);
    }

    public void backClicked(View view)
    {
        finish();
    }

    public void sendClicked(View view)
    {
        String message = mMessageEditText.getText().toString();
        if(message.isEmpty())
        {
            return;
        }

        String messageSenderRef = "Messages/"+mCurrentUser.getUid()+"/"+mContact.getUid();
        String messageReceiverRef = "Messages/"+mContact.getUid()+"/"+mCurrentUser.getUid();

        DatabaseReference databaseReference = mDatabaseReference.child("Messages").child(mContact.getUid()).child(mCurrentUser.getUid()).push();

        String message_key = databaseReference.getKey();

        HashMap<String,String> message_data = new HashMap<>();
        message_data.put("text",message);
        message_data.put("type","text");
        message_data.put("from",mCurrentUser.getUid());
        message_data.put("lang", Utils.getLanguageCode());
        message_data.put("link","");
        message_data.put("to",mContact.getUid());

        Map message_body_details = new HashMap();
        message_body_details.put(messageSenderRef+"/"+message_key,message_data);
        message_body_details.put(messageReceiverRef+"/"+message_key,message_data);

        mDatabaseReference.updateChildren(message_body_details).addOnCompleteListener(task ->
        {
            if(task.isSuccessful())
            {
                Log.v(TAG,"sendClicked: Message Pushed");
            }
            else
            {
                Log.v(TAG,"sendClicked: Error");
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
            mMessageList.add(message);
            mMessageAdapter.setMessageList(mMessageList);
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
}