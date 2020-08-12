package com.lambz.lingo_chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ContactListAdapter extends RecyclerView.Adapter<ContactListAdapter.ViewHolder>
{
    private List<Contact> mContactList;
    private Context mContext;

    public ContactListAdapter(List<Contact> mContactList, Context mContext)
    {
        this.mContactList = mContactList;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.user_display_layout, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {

    }

    @Override
    public int getItemCount()
    {
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
