package com.example.user.audio_soundnet;

import android.content.Intent;
import android.graphics.Path;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class Login extends AppCompatActivity {


    ImageView Dolphin,DolphinMaster,Soundent;
    EditText username,password;
    Button button,google,facebook;
    ImageView frame1,frame2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Dolphin = (ImageView)findViewById(R.id.Dolphin);
        DolphinMaster = (ImageView)findViewById(R.id.DolphinMaster);
        Soundent = (ImageView)findViewById(R.id.Soundent);
        username = (EditText)findViewById(R.id.username);
        password = (EditText)findViewById(R.id.password);
        button = (Button)findViewById(R.id.button);
        google = (Button)findViewById(R.id.google);
        facebook = (Button)findViewById(R.id.facebook);
        frame1 = (ImageView)findViewById(R.id.frame1);
        frame2 = (ImageView)findViewById(R.id.frame2);

        Opening();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(Login.this,MainActivity.class);
                startActivity(intent);
            }
        });
        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this,checkin.class);
                startActivity(intent);
            }
        });
        facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }
    private void Opening(){
        Animation am = new TranslateAnimation(500,700,600,100);
        Animation bm = new TranslateAnimation(500,350,1500,350);
        Animation cm = new TranslateAnimation(700,550,1600,450);
        am.setDuration(2500);
        bm.setDuration(2500);
        cm.setDuration(2500);
        am.setRepeatCount(0);
        bm.setRepeatCount(0);
        cm.setRepeatCount(0);
        Dolphin.setAnimation(am);
        DolphinMaster.setAnimation(bm);
        Soundent.setAnimation(cm);
        am.startNow();
        bm.startNow();
        cm.startNow();
        am.setFillAfter(true);
        bm.setFillAfter(true);
        cm.setFillAfter(true);
        username.setVisibility(View.INVISIBLE);
        password.setVisibility(View.INVISIBLE);
        button.setVisibility(View.INVISIBLE);
        google.setVisibility(View.INVISIBLE);
        facebook.setVisibility(View.INVISIBLE);
        frame1.setVisibility(View.INVISIBLE);
        frame2.setVisibility(View.INVISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                username.setVisibility(View.VISIBLE);
                password.setVisibility(View.VISIBLE);
                button.setVisibility(View.VISIBLE);
                google.setVisibility(View.VISIBLE);
                facebook.setVisibility(View.VISIBLE);
                frame1.setVisibility(View.VISIBLE);
                frame2.setVisibility(View.VISIBLE);
            }
        },2700);
    }
}