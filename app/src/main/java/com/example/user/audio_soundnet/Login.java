package com.example.user.audio_soundnet;

import android.content.Intent;
import android.graphics.Path;
import android.os.Handler;
//import android.support.v7.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.royrodriguez.transitionbutton.TransitionButton;
import org.jetbrains.annotations.NotNull;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class Login extends AppCompatActivity {


    ImageView Dolphin,DolphinMaster,Soundent;
    EditText username,password;
    Button button,google,facebook;
    ImageView frame1,frame2;
    boolean isSuccessful = false;
    private TransitionButton transitionButton;

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
        transitionButton = findViewById(R.id.transition_button);

        Opening();
        transitionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the loading animation when the user tap the button
                transitionButton.startAnimation();
                String entername = username.getText().toString();
                String enterpassword = password.getText().toString();
                sendPost(entername,enterpassword);

                // Do your networking task or background work here.
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        // Choose a stop animation if your call was succesful or not
                        if (isSuccessful) {
                            transitionButton.stopAnimation(TransitionButton.StopAnimationStyle.EXPAND, new TransitionButton.OnAnimationStopEndListener() {
                                @Override
                                public void onAnimationStopEnd() {
                                    Intent intent = new Intent(getBaseContext(), MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                    startActivity(intent);
                                }
                            });
                        } else {
                            transitionButton.stopAnimation(TransitionButton.StopAnimationStyle.SHAKE, null);
                        }
                    }
                }, 2000);
            }
        });
        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this,MainActivity.class);
                startActivity(intent);
            }
        });
        facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, com.example.user.audio_soundnet.Animation.class);
                startActivity(intent);
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
        //button.setVisibility(View.INVISIBLE);
        google.setVisibility(View.INVISIBLE);
        facebook.setVisibility(View.INVISIBLE);
        frame1.setVisibility(View.INVISIBLE);
        frame2.setVisibility(View.INVISIBLE);
        transitionButton.setVisibility(View.INVISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                username.setVisibility(View.VISIBLE);
                password.setVisibility(View.VISIBLE);
                //button.setVisibility(View.VISIBLE);
                google.setVisibility(View.VISIBLE);
                facebook.setVisibility(View.VISIBLE);
                frame1.setVisibility(View.VISIBLE);
                frame2.setVisibility(View.VISIBLE);
                transitionButton.setVisibility(View.VISIBLE);
            }
        },2700);
    }
    private  void sendPost(String username,String password){
        String json = "{\"username\":\"" + username + "\",\"password\":\""+password+"\"}";
        /**建立連線*/
        OkHttpClient client = new OkHttpClient().newBuilder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                .build();
        /**設置傳送所需夾帶的內容*/
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"), json);
        /**設置傳送需求*/
        Request request = new Request.Builder()
                .url("http://172.20.10.4:5000/applogin")
                .post(body)
                .build();
        /**設置回傳*/
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                final String errorMMessage = e.getMessage();
                Login.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isSuccessful = false;
                        //tvRes.setText(errorMMessage);
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
                final String Message = response.body().string();
                if (Message.equals("ok")) {
                    Login.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            isSuccessful = true;
                        }
                    });
                }
                else{
                    isSuccessful = false;
                }
            }
        });
    }
}