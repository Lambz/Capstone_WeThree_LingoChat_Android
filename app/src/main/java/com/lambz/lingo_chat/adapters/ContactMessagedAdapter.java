package com.lambz.lingo_chat.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lambz.lingo_chat.R;
import com.lambz.lingo_chat.Utils;
import com.lambz.lingo_chat.activities.ChatActivity;
import com.lambz.lingo_chat.models.Contact;
import com.lambz.lingo_chat.models.Message;
import com.lambz.lingo_chat.models.Users;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ContactMessagedAdapter extends RecyclerView.Adapter<ContactMessagedAdapter.ViewHolder>
{
    private static final String TAG = "ContactMessagedAdapter";
    private List<Message> mMessageList;
    private Context mContext;
    private FirebaseUser mCurrentUser;
    private Translate mTranslate;

    public ContactMessagedAdapter(List<Message> contactList, Context context, Translate translate)
    {
        this.mMessageList = contactList;
        this.mContext = context;
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        this.mTranslate = translate;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.contact_messaged_layout, parent, false);
        return new ViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        final Users[] users = new Users[1];
        Message message = mMessageList.get(position);
        if (message.getType().equals("text"))
        {
            holder.mLastMessageTextView.setText(message.getText());
            new Thread(() ->
            {
                Translation translation = mTranslate.translate(message.getText(), Translate.TranslateOption.targetLanguage(Utils.getLanguageCode()));
                new Handler(Looper.getMainLooper()).post(() -> holder.mLastMessageTextView.setText(translation.getTranslatedText()));
            }).start();
        } else
        {
            holder.mLastMessageTextView.setText(message.getType());
            new Thread(() ->
            {
                Translation translation = mTranslate.translate(message.getType(), Translate.TranslateOption.targetLanguage(Utils.getLanguageCode()));
                new Handler(Looper.getMainLooper()).post(() -> holder.mLastMessageTextView.setText(translation.getTranslatedText()));
            }).start();
        }
        String uid;
        if (message.getFrom().equals(mCurrentUser.getUid()))
        {
            uid = message.getTo();
        } else
        {
            uid = message.getFrom();
        }
        FirebaseDatabase.getInstance().getReference().child("Users").child(uid).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                users[0] = snapshot.getValue(Users.class);
                if (!users[0].getImage().isEmpty())
                {
                    Picasso.get().load(users[0].getImage()).placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder).into(holder.mProfileImageView);
                }
                holder.mUserNameTextView.setText(users[0].getFirst_name() + " " + users[0].getLast_name());
//                new Thread(() ->
//                {
//                    Translation translation = mTranslate.translate(users[0].getFirst_name() + " " + users[0].getLast_name(), Translate.TranslateOption.targetLanguage(Utils.getLanguageCode()));
//                    new Handler(Looper.getMainLooper()).post(() -> holder.mUserNameTextView.setText(translation.getTranslatedText()));
//                }).start();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });
        holder.mMainLayout.setOnClickListener(view ->
        {
            Contact contact = new Contact(users[0].getFirst_name() + " " + users[0].getLast_name(), users[0].getImage(), uid);
            Intent intent = new Intent(mContext, ChatActivity.class);
            intent.putExtra("contact", contact);
            mContext.startActivity(intent);
        });
    }

    @Override
    public int getItemCount()
    {
        if (mMessageList == null)
        {
            return 0;
        }
        return mMessageList.size();
    }

    public void addMessage(Message message, int position)
    {
        mMessageList.add(position,message);
        this.notifyItemInserted(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        ConstraintLayout mMainLayout;
        ImageView mProfileImageView;
        TextView mUserNameTextView;
        TextView mLastMessageTextView;

        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            mMainLayout = itemView.findViewById(R.id.layout);
            mProfileImageView = itemView.findViewById(R.id.profile_img);
            mUserNameTextView = itemView.findViewById(R.id.name_textview);
            mLastMessageTextView = itemView.findViewById(R.id.last_message_textview);
        }
    }

    public void setMessageList(List<Message> messageList)
    {
        this.mMessageList = messageList;
        notifyDataSetChanged();
    }

    public Message deleteItem(int position)
    {
        Message message = mMessageList.remove(position);
        notifyItemRemoved(position);
        return message;
    }
}