package com.example.mrinalmriyo.homedemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

/**
 * Built by Irfan S
 *
 * Temporary data entry page for existing onCreate members.
 *
 *
 */

public class NewMemberSignUp extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private final String TAG = "OC_New_User";
    EditText name_edit;
    EditText email_edit;
    EditText semester_edit;
    EditText mobile_edit;
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
    long mob_num = 0;
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
        email_head = in.getStringExtra(getString(R.string.regex_email_intentkey));
        raw_emailID = in.getStringExtra(getString(R.string.user_email_intentkey));
        name = in.getStringExtra(getString(R.string.name_intentkey));
        mdbr = FirebaseDatabase.getInstance().getReference();

        name_edit = findViewById(R.id.name_edit);
        name_edit.setText(name);
        email_edit = findViewById(R.id.email_id_txt);
        email_edit.setText(raw_emailID);
        mobile_edit = findViewById(R.id.mobile_number_edit);
        semester_edit = findViewById(R.id.sem_edit);
        enter = findViewById(R.id.submit);
        UID_disp = findViewById(R.id.UID_disp);
        spinner = findViewById(R.id.spinner);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(NewMemberSignUp.this,
                android.R.layout.simple_spinner_item,paths);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(NewMemberSignUp.this);


        generateRandom();

        DatabaseReference loc = FirebaseDatabase.getInstance().getReference();
        loc.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                registrations_open = dataSnapshot.child(getString(R.string.Admin_List_Firebase_NodeKey)).child(getString(R.string.Membership_open_Firebase_NodeKey)).getValue(Boolean.class);
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
                            mob_num = Long.parseLong(mobile_edit.getText().toString());
                            Log.d(TAG, "Semester: " + sem);
                            if (Integer.parseInt(sem) <= 8 && Integer.parseInt(sem) > 0 && stream != "") {

                                //uid = uidenter.getText().toString().toUpperCase();
                                mdbr.child(getString(R.string.Member_List_Firebase_NodeKey)).child(email_head).child(getString(R.string.uid_Firebase_NodeKey)).setValue(randomUID);
                                mdbr.child(getString(R.string.Member_List_Firebase_NodeKey)).child(email_head).child(getString(R.string.Name_Firebase_NodeKey)).setValue(name);
                                mdbr.child(getString(R.string.Member_List_Firebase_NodeKey)).child(email_head).child(getString(R.string.Email_Firebase_NodeKey)).setValue(raw_emailID);
                                mdbr.child(getString(R.string.Member_List_Firebase_NodeKey)).child(email_head).child(getString(R.string.Semester_Firebase_NodeKey)).setValue(sem);
                                mdbr.child(getString(R.string.Member_List_Firebase_NodeKey)).child(email_head).child(getString(R.string.Mobile_Firebase_NodeKey)).setValue(mob_num);
                                mdbr.child(getString(R.string.Member_List_Firebase_NodeKey)).child(email_head).child(getString(R.string.Stream_Firebase_NodeKey)).setValue(stream);
                                Toast.makeText(NewMemberSignUp.this, "Registration successful , please sign-in to access the app", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(NewMemberSignUp.this,Login.class));
                                finish();
                            } else {
                                Toast.makeText(NewMemberSignUp.this, "Invalid details,please enter proper values", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }else{
                    Toast.makeText(NewMemberSignUp.this,"Registrations have stopped, Thank you for your interest",Toast.LENGTH_LONG).show();
                }
            }
        });
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
        mdbr.child(getString(R.string.Member_List_Firebase_NodeKey)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    if(ds.child(getString(R.string.uid_Firebase_NodeKey)).getValue(String.class)!=randomUID){
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
