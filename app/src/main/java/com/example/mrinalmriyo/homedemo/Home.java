package com.example.mrinalmriyo.homedemo;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
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
    String email_regex;
    long mob_num = 0;

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
                        .setMessage("Name: "+name+"\n"+"UID: "+UID+"\n"+"Email: "+email_raw+"\n"+"Mobile: "+mob_num)
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                                dialog.dismiss();
                            }
                        })
                        .show();
                break;
            case R.id.whatsapp:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.whatsapp_chat_url)));
                startActivity(browserIntent);


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

        UID = in.getStringExtra(getString(R.string.user_uid_intentkey));
        email_raw = in.getStringExtra(getString(R.string.user_email_intentkey));
        pic_url = in.getStringExtra(getString(R.string.pic_url_intentkey));
        name = in.getStringExtra(getString(R.string.name_intentkey));
        email_regex = in.getStringExtra(getString(R.string.regex_email_intentkey));

        Log.d(TAG,"User email: "+email_raw+" User UID : "+UID);

        DatabaseReference loc= FirebaseDatabase.getInstance().getReference();
        loc.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                mob_num = dataSnapshot.child(getString(R.string.Member_List_Firebase_NodeKey)).child(email_regex).child(getString(R.string.Mobile_Firebase_NodeKey)).getValue(Long.class);
                attendance.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(dataSnapshot.child(getString(R.string.Admin_List_Firebase_NodeKey)).hasChild(UID)) {

                            Log.d(TAG,"Launching Admin Attendance");

                            Intent intent = new Intent(Home.this, AttendanceFirstPage.class);
                            intent.putExtra(getString(R.string.user_uid_intentkey),UID);
                            startActivity(intent);
                        }else{
                            Intent intent = new Intent(Home.this,MarkAttendance.class);
                            intent.putExtra(getString(R.string.user_uid_intentkey),UID);
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
