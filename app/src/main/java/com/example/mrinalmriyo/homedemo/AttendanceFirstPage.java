package com.example.mrinalmriyo.homedemo;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AttendanceFirstPage extends AppCompatActivity {

    /**
     * Attendance Page for admin accounts. Allows users to mark their attendance when this activity is loaded
     *
     * Built by Irfan S
     */

    TextView attendance;
    TextView registration_status;
    final String TAG = "A1P_tag";
    String UID;

    boolean isOpen;
    boolean isAttendanceLive = false;


    DatabaseReference loc;
    GoogleApiClient mGoogleApiClient;

    public void viewList(View view)
    {
        Intent intent=new Intent(this, AttendanceSecondPage.class);
        intent.putExtra(getString(R.string.user_uid_intentkey),UID);
        startActivity(intent);
    }

    public void manageRegistrations(View view){
        if (!isOpen){
            loc.child(getString(R.string.Admin_List_Firebase_NodeKey)).child(getString(R.string.Membership_open_Firebase_NodeKey)).setValue(true);
        }else{
            loc.child(getString(R.string.Admin_List_Firebase_NodeKey)).child(getString(R.string.Membership_open_Firebase_NodeKey)).setValue(false);
        }
       // updateUI();
    }

    private void updateUI(){
        registration_status.setText("Registrations open: "+isOpen);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_first_page);

        Log.d(TAG,"Admin Attendance started");

        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);



        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        loc = FirebaseDatabase.getInstance().getReference();
        loc.child(getString(R.string.Admin_List_Firebase_NodeKey)).child(getString(R.string.online_Firebase_NodeKey)).setValue(true);

        registration_status = findViewById(R.id.reg_status_disp);
        attendance = findViewById(R.id.attendance_disp);

        Intent in = getIntent();
        UID = in.getStringExtra(getString(R.string.user_uid_intentkey));
        Log.d(TAG,"UID : "+UID);

        loc.child(getString(R.string.Admin_List_Firebase_NodeKey)).child(getString(R.string.online_Firebase_NodeKey)).onDisconnect().setValue(false);

        loc.child(getString(R.string.Admin_List_Firebase_NodeKey)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                isOpen = dataSnapshot.child(getString(R.string.Membership_open_Firebase_NodeKey)).getValue(Boolean.class);
                isAttendanceLive = dataSnapshot.child(getString(R.string.online_Firebase_NodeKey)).getValue(Boolean.class);


                if(isAttendanceLive){
                    attendance.setText("Mark your attendance now,\nset your hotspot to OC101");
                }else{
                    attendance.setText("Please wait!");
                }
                updateUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }

    @Override
    protected void onPause(){
        super.onPause();
        loc.child(getString(R.string.Admin_List_Firebase_NodeKey)).child(getString(R.string.online_Firebase_NodeKey)).setValue(false);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        loc.child(getString(R.string.Admin_List_Firebase_NodeKey)).child(getString(R.string.online_Firebase_NodeKey)).setValue(false);
    }
}
