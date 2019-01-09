package com.example.mrinalmriyo.homedemo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Built by Irfan S
 *
 * Class to load onCreate member list from Firebase, and display as a listView ,along with sessions attended by each user.
 * Uses the default listview class to display details.
 *
 */

public class Database extends AppCompatActivity implements AdapterView.OnItemClickListener{

    ArrayList<String> memberIDs;
    ListView membersListView;
    final String TAG = "Database_OC";
    ArrayAdapter<String> arrayAdapter;
    TextView messageTextView;
    ProgressDialog mProgressDialog;
    DatabaseReference loc ;

    private boolean isNetworkConnected() {

        ConnectivityManager cm=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo()!=null;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        getIntent();

        memberIDs=new ArrayList<String>();
        membersListView=findViewById(R.id.member_listview);
        messageTextView=findViewById(R.id.message_txtview);
        //listViewLL=findViewById(R.id.linearLayoutMembersAttended);
       // waitLL=findViewById(R.id.linearLayoutDatabase);

        if(isNetworkConnected()==false) {

            messageTextView.setVisibility(View.VISIBLE);
            messageTextView.setText("Please turn on Internet\n" +
                    "and Try again!");

        }
        else {
            showProgressDialog();
            loc  = FirebaseDatabase.getInstance().getReference(getString(R.string.Member_List_Firebase_NodeKey));
            loc.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Log.d(TAG,"Adding "+ ds.getKey()+" to adapter");
                        memberIDs.add(ds.child(getString(R.string.uid_Firebase_NodeKey)).getValue(String.class));
                        Log.d(TAG,"UID: "+ds.child(getString(R.string.uid_Firebase_NodeKey)).getValue(String.class));
                    }
                    arrayAdapter = new ArrayAdapter(Database.this, android.R.layout.simple_list_item_1, memberIDs);
                    membersListView.setAdapter(arrayAdapter);
                    //arrayAdapter.addAll(dates);
                    messageTextView.setVisibility(View.INVISIBLE);
                    arrayAdapter.notifyDataSetChanged();

                    hideProgressDialog();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            membersListView.setOnItemClickListener(this);

        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        if(arrayAdapter!=null) {
            arrayAdapter.clear();
        }
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Fetching details...");
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        final String UID = memberIDs.get(i);
        //final FragmentManager fm=getSupportFragmentManager();

        Log.d(TAG,"User UID selected: "+UID);
        DatabaseReference calc = FirebaseDatabase.getInstance().getReference(getString(R.string.Attendance_Register_Firebase_NodeKey));
        calc.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long totalDates = dataSnapshot.getChildrenCount();
                long attendedDates=0;
                for (DataSnapshot ds:dataSnapshot.getChildren()) {
                    if(ds.hasChild(UID)){
                        attendedDates++;
                    }
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(Database.this,R.style.CustomDialog);
                builder.setMessage("Attended sessions: "+attendedDates+"/"+totalDates)
                        .setTitle(UID)
                .show();



            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
