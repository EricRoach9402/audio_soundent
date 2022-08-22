package com.example.user.audio_soundnet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class UserLogin extends AppCompatActivity {

    EditText UserName,LoginPassword;
    Button UserLoginButton;
    //String getRequest;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);

        UserName = (EditText)findViewById(R.id.username);
        LoginPassword = (EditText)findViewById(R.id.loginpassword);
        UserLoginButton = (Button)findViewById(R.id.userlogin);

        UserLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String getUserName,getLoginPassword;
                getUserName = UserName.getText().toString();
                getLoginPassword = LoginPassword.getText().toString();
                //選擇隨機音樂
                long  t = System.currentTimeMillis();
                int MusicNumber = RandomMusic(t);
                sendPOST(getUserName,getLoginPassword,MusicNumber);
            }
        });
    }
    private int RandomMusic(long t){
        Random r = new Random(t);
        int musicnumber = r.nextInt(5);
        Log.v("Implement","RandomMusic : " + musicnumber);
        return musicnumber;
    }
    //**數據傳送*/
    public void sendPOST(String getStudenNumber,String getPassword,int MusicNumber) {
        /**建立連線*/
        OkHttpClient client = new OkHttpClient().newBuilder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                .connectTimeout(5, TimeUnit.MINUTES) // connect timeout
                .writeTimeout(5, TimeUnit.MINUTES) // write timeout
                .readTimeout(5, TimeUnit.MINUTES) // read timeout
                .build();
        /**設置傳送所需夾帶的內容*/
        FormBody formBody = new FormBody.Builder()
                .add("StudenNumber", getStudenNumber)
                .add("password", getPassword)
                .add("OpenDoor", "N")
                .add("MusicNumber",String.valueOf(MusicNumber))
                .build();
        final Request request = new Request.Builder()
                //.url("http://192.168.0.106/AudioLogintest.php")//http://192.168.50.172:3000/api/login
                //.url("http://192.168.50.194/AudioLogintest.php")// 2022/0712前使用
                //.url("http://192.168.50.192/AudioLogintest.php") //2022/0712後IP更改
                .url("http://192.168.50.192/sound_networking/PHP/AudioLogin.php") //2022/0712後IP更改
                //.url("http://192.168.50.192/AudioLogintest.php")
                //.url("http://192.168.0.106/client.php")
                .post(formBody)
                .build();
        /**設置回傳*/
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                /**如果傳送過程有發生錯誤*/
                //text.setText(e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                /**取得回傳*/
                String reponseString = response.body().string(),state;//response.body().string()僅可呼叫一次
                String[] cmds = reponseString.split(":");
                Log.e("Dolphinweb", "收到的錯誤消息：" + cmds[1]);
                /*if (cmds[1] == " 錯誤}"){
                    Log.e("Dolphinweb", "收到的消息：" + reponseString);
                }*/
                if (cmds[1].equals("成功}")){
                    Looper.prepare();
                    Toast.makeText(UserLogin.this,reponseString,Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(UserLogin.this, MainActivity.class);
                    startActivity(intent);
                    Looper.loop();
                }
                else{
                    Looper.prepare();
                    Toast.makeText(UserLogin.this,reponseString,Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }

                //第一次版本做法;已棄用
/*                int startUID = reponseString.indexOf("\"isAuth\"");
                int endUID = reponseString.indexOf(",",startUID);
                state = reponseString.substring(startUID+9,endUID);
                Log.e("Dolphinweb","state:" + state);
                if (state.equals("true")) {
                    Intent intent = new Intent(UserLogin.this, MainActivity.class);
                    startActivity(intent);
                }*/
                Log.v("Response",reponseString);
            }
        });
    }
}