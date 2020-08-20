package com.lambz.lingo_chat.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lambz.lingo_chat.R;
import com.lambz.lingo_chat.activities.ChatActivity;
import com.lambz.lingo_chat.activities.MainActivity;
import com.lambz.lingo_chat.interfaces.ContactClickedInterface;
import com.lambz.lingo_chat.models.Contact;
import com.lambz.lingo_chat.models.Message;
import com.lambz.lingo_chat.models.Users;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ContactMessagedAdapter extends RecyclerView.Adapter<ContactMessagedAdapter.ViewHolder>
{
    private List<Message> mMessageList;
    private Context mContext;

    public ContactMessagedAdapter(List<Message> contactList, Context context)
    {
        this.mMessageList = contactList;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.contact_messaged_layout, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        final Users[] users = new Users[1];
        Message message = mMessageList.get(position);
        if (message.getType().equals("text"))
        {
            holder.mLastMessageTextView.setText(message.getText());
        } else
        {
            holder.mLastMessageTextView.setText(message.getType());
        }
        FirebaseDatabase.getInstance().getReference().child("Users").child(message.getTo()).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                users[0] = snapshot.getValue(Users.class);
                if(!users[0].getImage().isEmpty())
                {
                    Picasso.get().load(users[0].getImage()).placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder).into(holder.mProfileImageView);
                }
                holder.mUserNameTextView.setText(users[0].getFirst_name() + " " + users[0].getLast_name());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });
        holder.mMainLayout.setOnClickListener(view ->
        {
            Contact contact = new Contact(users[0].getFirst_name()+" "+users[0].getLast_name(), users[0].getImage(), message.getTo());
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
}