package com.example.user.audio_soundnet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.user.audio_soundnet.faceID_Register.Login;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

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
    String getRequest;

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
                sendPOST(getUserName,getLoginPassword);
            }
        });
    }
    //**數據傳送*/
    private void sendPOST(String getStudenNumber,String getPassword) {
        /**建立連線*/
        OkHttpClient client = new OkHttpClient().newBuilder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                .build();
        /**設置傳送所需夾帶的內容*/
        FormBody formBody = new FormBody.Builder()
                .add("StudenNumber", getStudenNumber)
                .add("password", getPassword)
                .build();
        Request request = new Request.Builder()
                .url("http://192.168.50.172:3000/api/login")
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
                int startUID = reponseString.indexOf("\"isAuth\"");
                int endUID = reponseString.indexOf(",",startUID);
                state = reponseString.substring(startUID+9,endUID);
                Log.e("Dolphinweb","state:" + state);
                if (state.equals("true")) {
                    Intent intent = new Intent(UserLogin.this, MainActivity.class);
                    startActivity(intent);
                }
            }
        });
    }
}