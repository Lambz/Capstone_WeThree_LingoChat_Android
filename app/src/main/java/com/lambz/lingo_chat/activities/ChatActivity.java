package com.lambz.lingo_chat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lambz.lingo_chat.R;
import com.lambz.lingo_chat.Utils;
import com.lambz.lingo_chat.models.Contact;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity
{
    private static final String TAG = "ChatActivity";
    private ImageView mUserImageView;
    private TextView mUserNameTextView;
    private EditText mMessageEditText;
    private Contact mContact;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        getSupportActionBar().hide();
        setMemberVariables();
    }

    private void setMemberVariables()
    {
        mUserImageView = findViewById(R.id.user_imageview);
        mUserNameTextView = findViewById(R.id.user_name_textview);
        mMessageEditText = findViewById(R.id.message_edittext);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mContact = (Contact) getIntent().getSerializableExtra("contact");
        if(mContact.getImage() != null)
        {
            Picasso.get().load(mContact.getImage()).placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder).into(mUserImageView);
        }
        mUserNameTextView.setText(mContact.getName());
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

        Map message_body_details = new HashMap();
        message_body_details.put(messageSenderRef+"/"+message_key,message_data);
        message_body_details.put(messageReceiverRef+"/"+message_key,message_data);

        mDatabaseReference.updateChildren(message_body_details).addOnCompleteListener(new OnCompleteListener()
        {
            @Override
            public void onComplete(@NonNull Task task)
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
            }
        });
    }
}