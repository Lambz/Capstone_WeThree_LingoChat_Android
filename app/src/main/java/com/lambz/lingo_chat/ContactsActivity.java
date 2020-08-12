package com.lambz.lingo_chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ContactsActivity extends AppCompatActivity
{

    private SearchView mSearchView;
    private MenuItem mMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        getSupportActionBar().setTitle(R.string.select_contact);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp()
    {
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        finish();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search,menu);
        mMenuItem = menu.findItem(R.id.menu_search);
        mSearchView = (SearchView) mMenuItem.getActionView();
        mSearchView.setOnQueryTextListener(mQueryTextListener);
        mSearchView.setMaxWidth(Integer.MAX_VALUE);
        mSearchView.setIconifiedByDefault(true);
        mSearchView.setFocusable(true);
        mSearchView.setIconified(false);
        mSearchView.requestFocusFromTouch();
        mSearchView.setOnCloseListener(mOnCloseListener);
        return super.onCreateOptionsMenu(menu);
    }

    SearchView.OnQueryTextListener mQueryTextListener = new SearchView.OnQueryTextListener()
    {
        @Override
        public boolean onQueryTextSubmit(String s)
        {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String s)
        {
            System.out.println(s);
            return false;
        }
    };

    SearchView.OnCloseListener mOnCloseListener = new SearchView.OnCloseListener()
    {
        @Override
        public boolean onClose()
        {
            mMenuItem.collapseActionView();
            return true;
        }
    };

    ValueEventListener mUsersValueEventListener = new ValueEventListener()
    {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot)
        {
            List<Contact> contacts = new ArrayList<>();
            for (DataSnapshot ds : snapshot.getChildren())
            {
                String image = null;
                if(ds.hasChild("image"))
                {
                    image = String.valueOf(ds.child("image").getValue());
                }
                contacts.add(new Contact(String.valueOf(ds.child("name").getValue()),image,ds.getKey()));
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error)
        {

        }
    };

    @Override
    protected void onResume()
    {
        super.onResume();
        FirebaseDatabase.getInstance().getReference().child("Users").addValueEventListener(mUsersValueEventListener);
    }
}