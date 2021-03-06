package com.example.mrinalmriyo.homedemo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.view.View;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
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
import com.squareup.picasso.Picasso;

/**
 * Built by Irfan S
 *
 * Login and Security page for the app, allows user to sign-in based on existing credentials.
 * Manages user auth and allows new users to sign up. Hub for all security activities.
 *
 * Uses Picasso for efficient image loading.
 */

public class Login extends AppCompatActivity {
    final String TAG = "Login_OCE";
    private static final int RC_SIGN_IN = 9001;
    SignInButton signInButton;
    ProgressDialog mProgressDialog;
    GoogleApiClient mGoogleApiClient;
    ViewFlipper viewFlipper;
    ImageView mainDisp;

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

        mainDisp = findViewById(R.id.main_img);
        Picasso.get().load(R.drawable.icon_launch).resize(600,600).into(mainDisp);
        mainDisp.setVisibility(View.VISIBLE);

        //TODO implement local caching of images for ViewFlipper implementation.

//        int images[]={R.drawable.image1, R.drawable.image2, R.drawable.image3, R.drawable.image4,
//        R.drawable.image5, R.drawable.image6};
//        viewFlipper=findViewById(R.id.viewFlipper);
//        for(int i=0; i<images.length; i++)
//        {
//            flipperImages(images[i]);
//        }
//        viewFlipper.setFlipInterval(4000);
//        viewFlipper.setAutoStart(true);
//        Animation in=AnimationUtils.loadAnimation(this,android.R.anim.slide_in_left);
//        viewFlipper.setInAnimation(in);
//        in=AnimationUtils.loadAnimation(this,android.R.anim.slide_out_right);
//        viewFlipper.setOutAnimation(in);

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

//public void flipperImages(int image)
//    {
//        ImageView imageView=new ImageView(this);
//        imageView.setBackgroundResource(image);
//        viewFlipper.addView(imageView);
//        viewFlipper.setFlipInterval(4000);
//        viewFlipper.setAutoStart(true);
//        Animation in=AnimationUtils.loadAnimation(this,android.R.anim.slide_in_left);
//        viewFlipper.setInAnimation(in);
//        in=AnimationUtils.loadAnimation(this,android.R.anim.slide_out_right);
//        viewFlipper.setOutAnimation(in);
//    }

    private void signIn() {
        Log.d(TAG,"Starting sign-in daemon..");
        showProgressDialog();
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG,"Sign-in result obtained");

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            Log.d(TAG,"Result:"+result.isSuccess());
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);

            } else if (resultCode==12502 || !result.isSuccess()) {
                hideProgressDialog();
                Log.d(TAG,"Login failed due to "+result.getStatus());
                Toast.makeText(getApplicationContext(), "Unable to sign-in ,check your network connection and sign-in again", Toast.LENGTH_SHORT).show();
                showUI();
                // Google Sign In failed, update UI appropriately
                // ...
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG,"Firing onStart()");
        FirebaseUser mUser = mAuth.getCurrentUser();
        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(TAG, "Cached sign-in found, checking validity..");
            GoogleSignInResult result = opr.get();
            GoogleSignInAccount account = result.getSignInAccount();
            firebaseAuthWithGoogle(account);
        } else {
            if (mUser != null) {
                //dialog.show();
                //signInButton.setVisibility(View.VISIBLE);
                signIn();
            } else {
                hideProgressDialog();
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

                            final FirebaseUser user = mAuth.getCurrentUser();
                            //Replacing invalid chars for database addition
                            final String raw_email = user.getEmail();
                            final String name = user.getDisplayName();
                            final String raw_pic_url = user.getPhotoUrl().toString();
                            final String email = user.getEmail().replaceAll("[.,#_$]","!");
                            newUser = task.getResult().getAdditionalUserInfo().isNewUser();
                            hideProgressDialog();
                            if (newUser) {
                                Intent in = new Intent(Login.this,NewMemberSignUp.class);

                                in.putExtra(getString(R.string.user_email_intentkey),raw_email);
                                in.putExtra(getString(R.string.regex_email_intentkey),email);
                                in.putExtra(getString(R.string.name_intentkey),name);

                                //hideProgressDialog();
                                startActivity(in);
                                finish();
                            } else {
                                local_DBR.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.child(getString(R.string.Member_List_Firebase_NodeKey)).hasChild(email)) {
                                            Toast.makeText(Login.this,"Signed in as : "+task.getResult().getUser().getDisplayName(),Toast.LENGTH_SHORT).show();

                                            String OC_UID = dataSnapshot.child(getString(R.string.Member_List_Firebase_NodeKey)).child(email).child(getString(R.string.uid_Firebase_NodeKey)).getValue(String.class);
                                            Log.d(TAG, "User found in DB , UID: " + OC_UID);
                                            Intent in = new Intent(Login.this, Home.class);

                                            in.putExtra(getString(R.string.user_email_intentkey), raw_email);
                                            in.putExtra(getString(R.string.user_uid_intentkey), OC_UID);
                                            in.putExtra(getString(R.string.pic_url_intentkey), raw_pic_url);
                                            in.putExtra(getString(R.string.name_intentkey),name);
                                            in.putExtra(getString(R.string.regex_email_intentkey),email);

                                           // hideProgressDialog();
                                            startActivity(in);
                                            finish();
                                        }else{
                                            Toast.makeText(Login.this,"Details not found, please log in again.",Toast.LENGTH_LONG).show();
                                            user.delete();
                                           // hideProgressDialog();
                                            showUI();
                                            Log.d(TAG,"User deleted from database due to inconsistent details");
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
                            hideProgressDialog();
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
