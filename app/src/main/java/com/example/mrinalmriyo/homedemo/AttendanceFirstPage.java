package com.example.mrinalmriyo.homedemo;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.DataBuffer;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class AttendanceFirstPage extends AppCompatActivity {


    TextView attendace;
    TextView registration_status;
    final String TAG = "A1P_tag";
    String UID;
    boolean isOpen;


    DatabaseReference loc;
    GoogleApiClient mGoogleApiClient;

    public void viewList(View view)
    {
        Intent intent=new Intent(this, AttendanceSecondPage.class);
        intent.putExtra("uid",UID);
        startActivity(intent);
    }

    public void manageRegistrations(View view){
        if (isOpen==false){
            loc.child("Admin_List").child("membership_open").setValue(true);
        }else{
            loc.child("Admin_List").child("membership_open").setValue(false);
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


        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        loc = FirebaseDatabase.getInstance().getReference();
        loc.child("Admin_List").child("online").setValue(1);



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

        registration_status = findViewById(R.id.reg_status_disp);
        attendace = findViewById(R.id.attendance_disp);

        Intent in = getIntent();
        UID = in.getStringExtra("uid");
        Log.d(TAG,"UID : "+UID);

        loc.child("Admin_List").child("online").onDisconnect().setValue(false);

        loc.child("Admin_List").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                isOpen = dataSnapshot.child("membership_open").getValue(Boolean.class);


                if(dataSnapshot.child("online").getValue(Integer.class)==1){
                    attendace.setText("Attendance marking is live, set your hotspot to OC101");
                }else{
                    attendace.setText("Attendance not live");
                }
                updateUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onPause(){
        super.onPause();
        loc.child("Admin_List").child("online").setValue(0);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        loc.child("Admin_List").child("online").setValue(0);
    }
}
