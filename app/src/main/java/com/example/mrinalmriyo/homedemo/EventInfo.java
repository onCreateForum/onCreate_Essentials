package com.example.mrinalmriyo.homedemo;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * To be implemented in future
 *
 * Built by Mrinal and Priti.
 */
public class EventInfo extends AppCompatActivity {

    Intent intent;
    int tokenReceived;
    LinearLayout linearLayoutEvents;
    ScrollView scrollViewEvents;
    TextView detailsTextView,messageTextViewEvents;
    String eventInfoSession,eventInfoWeekly,eventInfoMonthly;

    private boolean isNetworkConnected() {

        ConnectivityManager cm=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo()!=null;
    }


    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {

            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {

                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }
                return result;

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            linearLayoutEvents.setVisibility(View.VISIBLE);
            scrollViewEvents.setVisibility(View.GONE);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {

                JSONObject jsonObject = new JSONObject(result);
                String membersInfo = jsonObject.getString("Sheet1");

                JSONArray jsonArray = new JSONArray(membersInfo);
                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject member = jsonArray.getJSONObject(i);
                    eventInfoSession = member.getString("Session");
                    eventInfoWeekly = member.getString("Weekly");
                    eventInfoMonthly = member.getString("Monthly");

                }

                switch (tokenReceived)
                {
                    case 1:
                        setTitle(R.string.hands_on);
                        detailsTextView.setText(eventInfoSession);
                        linearLayoutEvents.setVisibility(View.GONE);
                        scrollViewEvents.setVisibility(View.VISIBLE);

                        break;
                    case 2:
                        setTitle(R.string.weekly_challenge);
                        detailsTextView.setText(eventInfoWeekly);
                        linearLayoutEvents.setVisibility(View.GONE);
                        scrollViewEvents.setVisibility(View.VISIBLE);

                        break;
                    case 3:
                        setTitle(R.string.mothly_appathon);
                        detailsTextView.setText(eventInfoMonthly);
                        linearLayoutEvents.setVisibility(View.GONE);
                        scrollViewEvents.setVisibility(View.VISIBLE);

                        break;
                }


            }catch (Exception e)
            {
                e.printStackTrace();
            }


        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_info);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        intent=getIntent();
        tokenReceived=intent.getIntExtra("token",0);
        detailsTextView=findViewById(R.id.detailsTextView);
        messageTextViewEvents=findViewById(R.id.messageTextViewEvents);
        linearLayoutEvents=findViewById(R.id.linearLayoutEvents);
        scrollViewEvents=findViewById(R.id.scrollViewEvents);

        if(isNetworkConnected()==false) {

            linearLayoutEvents.setVisibility(View.VISIBLE);
            scrollViewEvents.setVisibility(View.GONE);
            messageTextViewEvents.setText("Please turn on Internet\n" +
                    "and Try again!");

        }

        else {

            DownloadTask task = new DownloadTask();
            task.execute("https://script.google.com/macros/s" +
                    "/AKfycbxOLElujQcy1-ZUer1KgEvK16gkTLUqYftApjNCM_IRTL3HSuDk" +
                    "/exec?id=1SnxgMwh9fpS0xfYf4wp3LghWXVunJC_aEr75q8DaY2s" +
                    "&sheet=Sheet1");
        }
    }
}
