package com.example.mrinalmriyo.homedemo;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.mrinalmriyo.homedemo.fragments.ListViewFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Displays member list for the sessions conducted so far. Only for admins. Links to the Attendance_Register node in Firebase
 * Uses the custom ListViewFragment to display member details.
 *
 * Built by Irfan S
 *
 */
public class AttendanceSecondPage extends AppCompatActivity implements AdapterView.OnItemClickListener {



    ListView listView;
    private final String TAG = "Attendance2Page_OCE";
    String UID;
    ArrayList<String> dates = new ArrayList<>();
    DatabaseReference loc = FirebaseDatabase.getInstance().getReference(getString(R.string.Attendance_Register_Firebase_NodeKey));
    ArrayAdapter arrayAdapter;
    ArrayList<String> IDs = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_second_page);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Intent t = getIntent();
        UID = t.getStringExtra(getString(R.string.user_uid_intentkey));

        listView = findViewById(R.id.membersAttendedListView);

        loc.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Log.d(TAG,"Adding "+ ds.getKey()+" to adapter");
                    dates.add(ds.getKey());
                    arrayAdapter = new ArrayAdapter(AttendanceSecondPage.this, android.R.layout.simple_list_item_1, dates);
                    listView.setAdapter(arrayAdapter);
                }
                //arrayAdapter.addAll(dates);
                arrayAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        listView.setOnItemClickListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(arrayAdapter!=null) {
            arrayAdapter.clear();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        final String date = dates.get(i);
        final FragmentManager fm=getSupportFragmentManager();

        Log.d(TAG,"Date selected: "+date);
        loc.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.child(date).getChildren()) {
                    IDs.add(ds.getKey());
                }
                Bundle bundle = new Bundle();
                bundle.putStringArrayList("ids",IDs);
                bundle.putString("date",date);
                final ListViewFragment p=new ListViewFragment();
                p.setArguments(bundle);
                p.setStyle(DialogFragment.STYLE_NORMAL,R.style.CustomDialog);
                p.show(fm,"Students for "+date);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
