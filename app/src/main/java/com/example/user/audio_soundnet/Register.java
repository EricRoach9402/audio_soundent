package com.example.user.audio_soundnet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.user.audio_soundnet.faceID_Register.Login;
import com.example.user.audio_soundnet.faceID_Register.LoginFaceID;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class Register extends AppCompatActivity {

    EditText FirstName,LastName,StudenNumber,Password,Password2;
    Button Finish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        FirstName = (EditText)findViewById(R.id.firstname);
        LastName = (EditText)findViewById(R.id.lastname);
        StudenNumber = (EditText)findViewById(R.id.studennumber);
        Password = (EditText)findViewById(R.id.password);
        Password2 = (EditText)findViewById(R.id.password2);
        Finish = (Button)findViewById(R.id.finishbutton);

        Finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String getFirstName,getLastName,getStudenNumber,getPassword,getPassword2;
                getFirstName = FirstName.getText().toString();
                getLastName = LastName.getText().toString();
                getStudenNumber = StudenNumber.getText().toString();
                getPassword = Password.getText().toString();
                getPassword2 = Password2.getText().toString();
                sendPOST(getFirstName,getLastName,getStudenNumber,getPassword,getPassword2);
            }
        });
    }
    //**數據傳送*/
    private void sendPOST(String getFirstName,String getLastName,String getStudenNumber,String getPassword,String getPassword2) {
        /**建立連線*/
        OkHttpClient client = new OkHttpClient().newBuilder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                .build();
        /**設置傳送所需夾帶的內容*/
        FormBody formBody = new FormBody.Builder()
                .add("firstname", getFirstName)
                .add("lastname", getLastName)
                .add("StudenNumber", getStudenNumber)
                .add("password", getPassword)
                .add("password2",getPassword2)
                .build();
        Request request = new Request.Builder()
                .url("http://192.168.50.172:3000/api/register")
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
                //text.setText("POST回傳：\n" + response.body().string());
                Log.e("Dolphinweb", "Response:" + response.body().string());
                Intent intent = new Intent(Register.this, Login.class);
                startActivity(intent);
            }
        });
    }
}