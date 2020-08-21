package com.lambz.lingo_chat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Looper;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lambz.lingo_chat.adapters.ContactListAdapter;
import com.lambz.lingo_chat.interfaces.ContactClickedInterface;
import com.lambz.lingo_chat.models.Contact;
import com.lambz.lingo_chat.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ContactsActivity extends AppCompatActivity
{

    private static final String TAG = "ContactsActivity";
    private static final int CONTACTS_PERM_CODE = 1890;
    private SearchView mSearchView;
    private MenuItem mMenuItem;
    private ArrayList<String> mEmailList;
    private ArrayList<Contact> mContactList;
    private ContactListAdapter mContactListAdapter;
    private RecyclerView mRecyclerView;
    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        getSupportActionBar().setTitle(R.string.select_contact);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setMemberVariables();
        setupRecyclerView();
    }

    private void setMemberVariables()
    {
        mRecyclerView = findViewById(R.id.recyclerview);
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    private void setupRecyclerView()
    {
        mContactListAdapter = new ContactListAdapter(mContactList, this, mContactClickedInterface);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mContactListAdapter);
    }

    @Override
    public boolean onSupportNavigateUp()
    {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);
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
            ArrayList<Contact> contacts = new ArrayList<>();
            for(Contact contact: mContactList)
            {
                if(contact.getName().contains(s))
                {
                    contacts.add(contact);
                }
            }
            mContactListAdapter.setContactList(contacts);
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
            System.out.println(Looper.getMainLooper().getThread() == Thread.currentThread());
            mContactList = new ArrayList<>();
            if (mEmailList != null)
            {
                for (DataSnapshot ds : snapshot.getChildren())
                {
                    if (!ds.getKey().equals(mCurrentUser.getUid()) && ds.hasChild("email") && mEmailList.contains(ds.child("email").getValue()))
                    {
                        String image = null;
                        if (ds.hasChild("image"))
                        {
                            image = String.valueOf(ds.child("image").getValue());
                        }
                        String name = String.valueOf(ds.child("first_name").getValue());
                        if (ds.hasChild("last_name"))
                        {
                            name += " " + ds.child("last_name").getValue();
                        }
                        mContactList.add(new Contact(name, image, ds.getKey()));
                    }
                }
            }
            mContactListAdapter.setContactList(mContactList);
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
        askContactsPermission();
    }


    public ArrayList<String> getNameEmailDetails()
    {
        ArrayList<String> emails = new ArrayList<>();
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if (cur.getCount() > 0)
        {
            while (cur.moveToNext())
            {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                Cursor cur1 = cr.query(
                        ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                        new String[]{id}, null);
                while (cur1.moveToNext())
                {
                    //to get the contact names
                    String name = cur1.getString(cur1.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    Log.e("Name :", name);
                    String email = cur1.getString(cur1.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                    Log.e("Email", email);
                    if (email != null)
                    {
                        emails.add(email);
                    }
                }
                cur1.close();
            }
        }
        return emails;
    }

    private void askContactsPermission()
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, CONTACTS_PERM_CODE);
        } else
        {
            crossReferenceContacts();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if (requestCode == CONTACTS_PERM_CODE)
        {
            if (grantResults.length < 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                crossReferenceContacts();
                //dispatchTakePictureIntent();
            } else
            {
                Toast.makeText(this, R.string.contact_permission_toast, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void crossReferenceContacts()
    {
        mEmailList = getNameEmailDetails();
        FirebaseDatabase.getInstance().getReference().child("Users").addValueEventListener(mUsersValueEventListener);
    }

    @Override
    public void onBackPressed()
    {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    ContactClickedInterface mContactClickedInterface = contact ->
    {
        Intent intent = new Intent();
        intent.putExtra("contact",contact);
        setResult(RESULT_OK,intent);
        finish();
    };
}