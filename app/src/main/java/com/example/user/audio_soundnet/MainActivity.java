package com.example.user.audio_soundnet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
//import android.support.annotation.UiThread;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.content.ContextCompat;
//import android.support.v7.app.AppCompatActivity;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import com.example.user.audio_soundnet.OWLoadingAniment.OWLoading;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
//import com.sackcentury.shinebuttonlib.ShineButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class  MainActivity extends AppCompatActivity {

    public static double sample_rate;
    public static double duration;
    public static double symbol_size;
    public static double sample_period;
    public static String recovered_string;
    TextView text;
    private Receiver receiver;

    private static String TAG = "MainActivity";      // Permissions to write to files
    private static final int REQUEST_WRITE_STORAGE = 112;
    private Recorder r;
    private Context context;
    public int fstart, anime2 = 0;
    public double threshold;

    private double Bw, sym_end;
    boolean b1 = false;
    public boolean anime = false;

    Button bs;

    OWLoading owLoading;

    private int currentApiVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        sample_rate = 48000.0;//設定取樣率
        sample_period = 1 / sample_rate;
        symbol_size = 0.125;//設定symbol頻率時間長度
        fstart = 18000;//設定起始symbol頻率
        Bw = 20048.0;//設定sync頻率和最大頻率
        sym_end = 20400.0;//設定END頻率
        threshold = 1;//找sync頻率和END頻率的ESD值

        text = (TextView) findViewById(R.id.dm_text);
        bs = (Button) findViewById(R.id.reStart);
        //text.setVisibility(View.INVISIBLE);
        owLoading = (OWLoading) findViewById(R.id.owloading);//5/1
        bs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (text.getText().equals("StartReceiver") || text.getText().equals("Loading...")) {
                    Toast.makeText(context, "正在收音或錄音中", Toast.LENGTH_SHORT).show();
                } else {
                    text.setText("StartReceiver");
                    recorder(context);
                    Toast.makeText(context, "開始收音", Toast.LENGTH_SHORT).show();
                }
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            String channelId  = "default_notification_channel_id";
            String channelName = "default_notification_channel_name";
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW));
        }

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (!task.isSuccessful()) {
                    Log.i("MainActivity", "getInstanceId failed");
                    return;
                }
                // Get new Instance ID token
                String token = task.getResult().getToken();
                Log.i("MainActivity","token "+token);
            }
        });
        context = getApplicationContext();
        recorder(context);//19_1_29_讀音檔

        /*----4/22--------------------*/

        currentApiVersion = Build.VERSION.SDK_INT;

        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        // This work only for android 4.4+
        if (currentApiVersion >= Build.VERSION_CODES.KITKAT) {

            getWindow().getDecorView().setSystemUiVisibility(flags);

            // Code below is to handle presses of Volume up or Volume down.
            // Without this, after pressing volume buttons, the navigation bar will
            // show up and won't hide
            final View decorView = getWindow().getDecorView();
            decorView
                    .setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {

                        @Override
                        public void onSystemUiVisibilityChange(int visibility) {
                            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                                decorView.setSystemUiVisibility(flags);
                            }
                        }
                    });
        }
        //----------------------------
        requestRecordPermissions();
        requestWritePermissions();

    }

    private void recorder(final Context context) {

        new Thread() {
            public void run() {
                try {

                    receiver = new Receiver("recorded.wav", fstart, Bw, sym_end, sample_rate, symbol_size, duration, context, MainActivity.this);
                    receiver.record();//錄音
                    receiver.demodulate();//解調
                    recovered_string = receiver.getRecoverd_string();

                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            //181217----偵測否是網址
                            if (recovered_string.equals("CbST")) {
                                Intent intent = new Intent(MainActivity.this,Animation.class);
                                startActivity(intent);
                                //interNet(recovered_string);//4/23//5/1//19/9/17
                                text.setText("解調完成:\n" + recovered_string);

                            } else if (recovered_string.equals("NLGK")) {

                                interNet(recovered_string);
                                text.setText("解調完成:\n" + recovered_string);

                            } else if (recovered_string.equals("gB2h")) {
                                interNet(recovered_string);
                                text.setText("解調完成:\n" + recovered_string);
                            } else {
                                text.setText("持續收音:\n" + recovered_string);//更改為繼續收音
                                recorder(context);
                            }
                        }
                    });

                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }


    private void interNet(String str) {

        Uri uri = Uri.parse("https://t.ly/" + str);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);

    }

    //請求權限
    public void requestRecordPermissions() {
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission to Record Audio denied");

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Permission to Record Audio")
                        .setTitle("Permission required");

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        Log.i(TAG, "Clicked");
                        makeRecordRequest();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

            } else {
                makeRecordRequest();
            }
        }
    }

    protected void makeRecordRequest() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                REQUEST_WRITE_STORAGE);
    }

    public void requestWritePermissions() {
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission to record denied");

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Permission to access the SD-CARD is required for this app to Download PDF.")
                        .setTitle("Permission required");

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        Log.i(TAG, "Clicked");
                        makeWriteRequest();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

            } else {
                makeWriteRequest();
            }
        }

    }

    protected void makeWriteRequest() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_WRITE_STORAGE);
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_WRITE_STORAGE: {

                if (grantResults.length == 0
                        || grantResults[0] !=
                        PackageManager.PERMISSION_GRANTED) {

                    Log.i(TAG, "Permission has been denied by user");

                } else {

                    Log.i(TAG, "Permission has been granted by user");

                }
                return;
            }
        }
    }

    //4/22-------------------------------------------------------------
    @SuppressLint("NewApi")
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (currentApiVersion >= Build.VERSION_CODES.KITKAT && hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    public Handler mowLoading = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {

                case 1:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            owLoading.startAnim();

                        }
                    });
                    break;
                case 0:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            owLoading.stopAnim();
                        }
                    });
                    break;
            }

        }
    };

    /*private void Button_effects() {
        ShineButton shineButtonJava = new ShineButton(this);
        shineButtonJava.setBtnColor(Color.GRAY);
        shineButtonJava.setBtnFillColor(Color.RED);
        shineButtonJava.setShapeResource(R.raw.heart);
        shineButtonJava.setAllowRandomColor(true);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(100, 100);
        shineButtonJava.setLayoutParams(layoutParams);
        ViewGroup linearLayout = null;
        if (linearLayout != null) {
            //linearLayout.addView(shineButtonJava);
        }
    }*/
}