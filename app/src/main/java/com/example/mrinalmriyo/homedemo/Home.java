package com.example.mrinalmriyo.homedemo;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Built by Mrinal and Priti
 * Firebase integration and sign-in by Irfan S
 *
 * Main page of onCreate Essentials, launches Database,Feedback , Sessions ,AttendanceFirstPage and Mark_Attendance.
 *
 *
 */

public class Home extends AppCompatActivity {

    GoogleApiClient mGoogleApiClient;

    String UID;
    String email_raw;
    String pic_url;
    String name;
    final String TAG = "Home_OCE";


    CardView attendance;

    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.user_activity_menu, menu);
        getSupportActionBar().setSubtitle("Your UID : "+UID);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.signout:
                FirebaseAuth.getInstance().signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                startActivity(new Intent(this, Login.class));
                finish();
                break;

            case R.id.myaccount:
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    //TODO change this display, can be put into another activity if you want to.
                    builder = new AlertDialog.Builder(Home.this, android.R.style.Theme_Material_Dialog);
                } else {
                    builder = new AlertDialog.Builder(Home.this);
                }
                builder.setTitle("Details")
                        .setMessage("Name: "+name+"\n"+"UID: "+UID+"\n"+"Email: "+email_raw)
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                                dialog.dismiss();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

        }
        return true;
    }
    public void members(View view)
    {
            Intent intent= new Intent(this,Database.class);
            startActivity(intent);
            //intent.putExtra("name",)
    }

    public void feedback(View view)
    {
       Intent intent=new Intent(this, Feedback.class);
       startActivity(intent);
    }

   public void sessions(View view)
   {
       Intent intent=new Intent(this,Sessions.class);
       startActivity(intent);
   }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        attendance = findViewById(R.id.attendance);

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
        Intent in = getIntent();

        //TODO clean-up unused data.

        UID = in.getStringExtra("user_uid");
        email_raw = in.getStringExtra("user_email");
        pic_url = in.getStringExtra("pic_url");
        name = in.getStringExtra("name");

        Log.d(TAG,"User email: "+email_raw+" User UID : "+UID);

        DatabaseReference loc= FirebaseDatabase.getInstance().getReference();
        loc.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                attendance.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(dataSnapshot.child("Admin_List").hasChild(UID)) {

                            Intent intent = new Intent(Home.this, AttendanceFirstPage.class);
                            intent.putExtra("uid",UID);
                            startActivity(intent);
                        }else{
                            Intent intent = new Intent(Home.this,Mark_attendance.class);
                            intent.putExtra("uid",UID);
                            startActivity(intent);
                            //Toast.makeText(Home.this,"Admin only feature",Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    public static class MyFirebaseMessagingService {
    }
}
