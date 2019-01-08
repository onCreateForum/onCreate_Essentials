package com.example.mrinalmriyo.homedemo;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * To be expanded
 *
 * Built by Mrinal and Priti
 */
public class Feedback extends AppCompatActivity {

    TextView aboutUsTextView;

    public void sendMail(View view)
    {
        Intent intent=new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"cse.mobilecomputingforum@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT,"Query/Suggestion");
        intent.putExtra(Intent.EXTRA_TEXT,"<Sent via onCreate() Essential>\n\n\n");

        try {
            startActivity(Intent.createChooser(intent,"Sending mail via"));
        }
        catch (android.content.ActivityNotFoundException e)
        {
            Toast.makeText(this, "No E-mail service available!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        getIntent();

    }
}
