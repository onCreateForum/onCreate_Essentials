package com.example.mrinalmriyo.homedemo;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * To be expanded
 *
 * Built by Mrinal and Priti
 */

public class Sessions extends AppCompatActivity {

    Intent intentEventInfo;

    public void nextSession(View view)
    {
        intentEventInfo.putExtra("token",1);
        startActivity(intentEventInfo);
    }

    public void monthlyAppathon(View view)
    {
        intentEventInfo.putExtra("token",3);
        startActivity(intentEventInfo);
    }

    public void weeklyChallenges(View view)
    {
        intentEventInfo.putExtra("token",2);
        startActivity(intentEventInfo);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sessions);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        getIntent();

        intentEventInfo=new Intent(this,EventInfo.class);
    }
}
