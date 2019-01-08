package com.example.mrinalmriyo.homedemo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Built by Irfan
 *
 * Login and Security page for the app, allows user to sign-in based on existing credentials.
 *
 *
 */

public class Login extends AppCompatActivity {
    final String TAG = "Login_OCE";
    private static final int RC_SIGN_IN = 9001;
    SignInButton signInButton;
    ProgressDialog mProgressDialog;
    GoogleApiClient mGoogleApiClient;

    boolean newUser;
    private DatabaseReference local_DBR;
    private FirebaseAuth mAuth;

    // This method configures Google SignIn
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        local_DBR = FirebaseDatabase.getInstance().getReference();

        setContentView(R.layout.userauth_screen);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        signInButton = findViewById(R.id.sign_in_button);
        signInButton.setVisibility(View.INVISIBLE);
//        dialog = new ProgressDialog(this);
//        dialog.setMessage("Loading...");
//        dialog.setCancelable(false);
//        dialog.setInverseBackgroundForced(false);
        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                } /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        //mAuthListener has been removed, does'nt seem to be required (for now).

    }


    private void signIn() {
        showProgressDialog();
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            // Log.d(TAG,"Result:"+result.isSuccess());
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);

            } else if (resultCode==12502) {
                Log.d(TAG,"Login failed due to "+result.getStatus());
                Toast.makeText(getApplicationContext(), "Retrying login", Toast.LENGTH_SHORT).show();
                // Google Sign In failed, update UI appropriately
                // ...
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser mUser = mAuth.getCurrentUser();
        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            GoogleSignInAccount account = result.getSignInAccount();
            firebaseAuthWithGoogle(account);
        } else {
            if (mUser != null) {
                //dialog.show();
                //signInButton.setVisibility(View.VISIBLE);
                signIn();
            } else {
                signInButton.setVisibility(View.VISIBLE);
                signInButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        signIn();
                    }
                });
            }
        }
        //mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
//        if (mAuthListener != null) {
//            mAuth.removeAuthStateListener(mAuthListener);
//        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle launched with : " + acct.getId());

        //TODO add checking mechanism to see if the member exists in the database before letting em sign in.

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull final Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
                        //dialog.dismiss();
                        if(task.isSuccessful()) {

                            hideProgressDialog();
                            Toast.makeText(Login.this,"Signed in as : "+task.getResult().getUser().getDisplayName(),Toast.LENGTH_SHORT).show();
                            final FirebaseUser user = mAuth.getCurrentUser();
                            //Replacing invalid chars for database addition
                            final String raw_email = user.getEmail();
                            final String name = user.getDisplayName();
                            final String raw_pic_url = user.getPhotoUrl().toString();
                            final String email = user.getEmail().replaceAll("[.,#_$]","!");
                            newUser = task.getResult().getAdditionalUserInfo().isNewUser();
                            if (newUser) {
                                Intent in = new Intent(Login.this,Temp_OCID_input.class);
                                in.putExtra("user_email",raw_email);
                                in.putExtra("regex_email",email);
                                in.putExtra("name",name);
                                startActivity(in);
                                finish();
                            } else {
                                local_DBR.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.child("Member_List").hasChild(email)) {

                                            String OC_UID = dataSnapshot.child("Member_List").child(email).child("UID").getValue(String.class);
                                            Log.d(TAG, "User found in DB , UID: " + OC_UID);
                                            Intent in = new Intent(Login.this, Home.class);
                                            in.putExtra("user_email", raw_email);
                                            in.putExtra("user_uid", OC_UID);
                                            in.putExtra("pic_url", raw_pic_url);
                                            startActivity(in);
                                            finish();
                                        }else{
                                            Toast.makeText(Login.this,"Details not found, please check your account details",Toast.LENGTH_LONG).show();
                                            user.delete();
                                            showUI();
                                            Log.d(TAG,"User deleted from database due to incosistent details");
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                            }
                            Log.d(TAG, "New User:" + newUser);

                            // }
                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.
                        }
                        else if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(Login.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Logging in...");
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    private void showUI(){
        signInButton.setVisibility(View.VISIBLE);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }




}
