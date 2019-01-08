package com.example.mrinalmriyo.homedemo;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;

/**
 * Splash screen
 *
 * Designed and built by Priti and Mrinal
 */

public class Launch extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        CountDownTimer countDownTimer=  new CountDownTimer(2000,1000){

            public void onTick(long millisecondsUntilDone){

                //countdown is counting(every second)
            }

            public void onFinish(){

                Intent intent=new Intent(getApplicationContext(),Login.class);
                startActivity(intent);
                finish();
            }

        };
        countDownTimer.start();

    }
}
