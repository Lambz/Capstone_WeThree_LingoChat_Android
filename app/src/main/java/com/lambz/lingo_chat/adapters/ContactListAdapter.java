package com.lambz.lingo_chat.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.lambz.lingo_chat.interfaces.ContactClickedInterface;
import com.lambz.lingo_chat.models.Contact;
import com.lambz.lingo_chat.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ContactListAdapter extends RecyclerView.Adapter<ContactListAdapter.ViewHolder>
{
    private List<Contact> mContactList;
    private Context mContext;
    private ContactClickedInterface mContactClickedInterface;

    public ContactListAdapter(List<Contact> contactList, Context context, ContactClickedInterface contactClickedInterface)
    {
        this.mContactList = contactList;
        this.mContext = context;
        this.mContactClickedInterface = contactClickedInterface;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.user_display_layout, parent, false);
        return new ViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        Contact contact = mContactList.get(position);
        if (!contact.getImage().isEmpty())
        {
            Picasso.get().load(contact.getImage()).placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder).into(holder.mUserImageView);
        }
        holder.mUserNameTextView.setText(contact.getName());
        holder.mMainLayout.setOnClickListener(view -> mContactClickedInterface.contactClicked(contact));
    }

    @Override
    public int getItemCount()
    {
        if (mContactList == null)
        {
            return 0;
        }
        return mContactList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        ConstraintLayout mMainLayout;
        ImageView mUserImageView;
        TextView mUserNameTextView;

        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            mMainLayout = itemView.findViewById(R.id.layout);
            mUserImageView = itemView.findViewById(R.id.user_imageview);
            mUserNameTextView = itemView.findViewById(R.id.user_name_textview);
        }
    }

    public void setContactList(List<Contact> mContactList)
    {
        this.mContactList = mContactList;
        notifyDataSetChanged();
    }
}
