package com.example.mrinalmriyo.homedemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

/**
 * built by Irfan
 *
 * Temporary data entry page for existing onCreate members. To be cleaned up once over.
 *
 *
 */

public class Temp_OCID_input extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private final String TAG = "OC_New_User";
    EditText name_edit;
    EditText email_edit;
    EditText semester_edit;
    TextView UID_disp;
    Button enter;
    Spinner spinner;
    DatabaseReference mdbr;

    final String[] paths = {"CSE","MECH","ECE","EE","CIVIL","BIOTECH"};

    String randomUID;
    String email_head;
    String raw_emailID;
    String name;
    String sem="0";
    boolean registrations_open =false;
    String stream="";

    GoogleApiClient mGoogleApiClient;

    //TODO clean-up once done.

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enter_creds);
        Intent in = getIntent();
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
        email_head = in.getStringExtra("regex_email");
        raw_emailID = in.getStringExtra("user_email");
        name = in.getStringExtra("name");
        mdbr = FirebaseDatabase.getInstance().getReference();

        name_edit = findViewById(R.id.name_edit);
        name_edit.setText(name);
        email_edit = findViewById(R.id.email_id_txt);
        email_edit.setText(raw_emailID);
        semester_edit = findViewById(R.id.sem_edit);
        enter = findViewById(R.id.submit);
        UID_disp = findViewById(R.id.UID_disp);
        spinner = findViewById(R.id.spinner);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(Temp_OCID_input.this,
                android.R.layout.simple_spinner_item,paths);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(Temp_OCID_input.this);


        generateRandom();

        DatabaseReference loc = FirebaseDatabase.getInstance().getReference();
        loc.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                registrations_open = dataSnapshot.child("Admin_List").child("membership_open").getValue(Boolean.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(registrations_open) {
                    mdbr.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            sem = semester_edit.getText().toString();
                            Log.d(TAG, "Semester: " + sem);
                            if (Integer.parseInt(sem) <= 8 && Integer.parseInt(sem) > 0 && stream != "") {

                                //uid = uidenter.getText().toString().toUpperCase();
                                mdbr.child("Member_List").child(email_head).child("UID").setValue(randomUID);
                                mdbr.child("Member_List").child(email_head).child("Name").setValue(name);
                                mdbr.child("Member_List").child(email_head).child("Email").setValue(raw_emailID);
                                mdbr.child("Member_List").child(email_head).child("Semester").setValue(sem);
                                mdbr.child("Member_List").child(email_head).child("Stream").setValue(stream);
                                Toast.makeText(Temp_OCID_input.this, "Registration successful, please sign out and login to use the app", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(Temp_OCID_input.this, "Invalid details,please enter proper values", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }else{
                    Toast.makeText(Temp_OCID_input.this,"Registrations have stopped, Thank you for your interest",Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.user_activity_menu, menu);
        getSupportActionBar().setSubtitle("Your email : "+raw_emailID);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.signout:
                FirebaseAuth.getInstance().signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                startActivity(new Intent(this, Login.class));
                finish();

        }
        return true;
    }

    private void generateRandom(){
        Date date = new Date();
       // Date date; // your date
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int year = cal.get(Calendar.YEAR)-2000;
        int month = cal.get(Calendar.MONTH)+1;
        Random r = new Random();
        int i1 = r.nextInt(999);
        String gen_UID = String.format("%03d", i1);
        randomUID = "OC"+year+""+month+gen_UID;
        mdbr.child("Member_List").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    if(ds.child("UID").getValue(String.class)!=randomUID){
                        //Do nothing
                    }else{
                        generateRandom();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        UID_disp.setText("Your UID is: "+randomUID);

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        stream = paths[i];
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

        stream = "";
    }
}
