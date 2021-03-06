package com.example.user.audio_soundnet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

//import android.support.annotation.UiThread;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.content.ContextCompat;
//import android.support.v7.app.AppCompatActivity;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.audio_soundnet.OWLoadingAniment.OWLoading;
import com.example.user.audio_soundnet.faceID_Register.Login;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Random;
//import com.sackcentury.shinebuttonlib.ShineButton;


public class  MainActivity extends AppCompatActivity {

    public static double sample_rate;
    public static double duration;
    public static double symbol_size;
    public static double sample_period;
    public static String recovered_string;

    TextView text;
    Button bs;
    OWLoading owLoading;

    private Context context;
    private Receiver receiver;
    private com.example.user.audio_soundnet.WebSocketPackage.WebSocket WebSocket;
    private Login mLogin;

    private static String TAG = "MainActivity";      // Permissions to write to files
    private static final int REQUEST_WRITE_STORAGE = 112;
    private int currentApiVersion;
    private double Bw, sym_end;
    private String hast;
    public double threshold;
    public boolean anime = false;
    public int fstart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //*??????????????????*/
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //sendPOST();
        //*??????????????????*/
        sample_rate = 48000.0;//???????????????
        sample_period = 1 / sample_rate;
        symbol_size = 0.125;//??????symbol??????????????????
        fstart = 18000;//????????????symbol??????

        //2021/08/09:????????????20048END??????20400???????????????????????????

        Bw = 20048.0;//??????sync?????????????????????
        sym_end = 20400.0;//??????END??????
        threshold = 1;//???sync?????????END?????????ESD???


        text = (TextView) findViewById(R.id.dm_text);
        bs = (Button) findViewById(R.id.reStart);
        //initSocketClient();

        //????????????
        owLoading = (OWLoading) findViewById(R.id.owloading);
        bs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (text.getText().equals("Loading") || text.getText().equals("Loading...")) {
                    Toast.makeText(context, "Loading", Toast.LENGTH_SHORT).show();
                } else {
                    text.setText("Loading");
                    recorder(context);
                    Toast.makeText(context, "Loading", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //**????????????*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            String channelId  = "default_notification_channel_id";
            String channelName = "default_notification_channel_name";
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW));
        }
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

        //**firebase????????????*/
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener( new OnCompleteListener<InstanceIdResult>() {
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

        //**?????????*/
        context = getApplicationContext();
        recorder(context);//19_1_29_?????????



        //**????????????*/
        requestRecordPermissions();
        requestWritePermissions();

    }

    //**???????????????*/
    private void recorder(final Context context) {

        new Thread() {
            public void run() {
                try {
                    receiver = new Receiver("recorded.wav", fstart, Bw, sym_end, sample_rate, symbol_size, duration, context, MainActivity.this);
                    receiver.record();//??????
                    receiver.demodulate();//??????
                    recovered_string = receiver.getRecoverd_string();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //**????????????????????????*/
                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //181217----??????????????????
                            if (recovered_string.equals("CbST")) {
                                Intent intent = new Intent(MainActivity.this,Animation.class);
                                startActivity(intent);
                                //interNet(recovered_string);//4/23//5/1//19/9/17
                                //text.setText("????????????");
                                text.setText("????????????:\n" + recovered_string);

                            }
                            /*else if (recovered_string.equals("a")) {

                                //interNet(recovered_string);
                                //text.setText("????????????");
                                text.setText("????????????:\n" + recovered_string);

                            }*/
                            else if (recovered_string.equals("A")) {

                                //interNet(recovered_string);
                                //text.setText("????????????");
                                sendPOST();
                                text.setText("????????????:\n" + recovered_string);

                            }else if (recovered_string.equals("NLGK")) {

                                //interNet(recovered_string);
                                //text.setText("????????????");
                                text.setText("????????????:\n" + recovered_string);

                            } else if (recovered_string.equals("gB2h")) {
                                //interNet(recovered_string);
                                //text.setText("????????????");
                                text.setText("????????????:\n" + recovered_string);
                            } else if (recovered_string.equals("meet")) {
                                hast = recovered_string;
                                sendPOST();
                                //text.setText("????????????");
                                text.setText("????????????:\n" + recovered_string);
                            }else if (recovered_string.equals("6js7")) {
                                hast = recovered_string;
                                sendPOST();
                                //interNet(recovered_string);
                                //WebSocket.send("6js7");
                                //Log.d("Dolphinweb","????????????" + "6js7");
                                //text.setText("????????????");
                                text.setText("????????????:\n" + recovered_string);

                            }
                            else if (recovered_string.equals("iqje")) {
                                hast = recovered_string;
                                //sendPOST();
                                //text.setText("????????????");
                                text.setText("????????????:\n" + recovered_string);
                            }else if (recovered_string.equals("wppu")) {
                                hast = recovered_string;
                                sendPOST();
                                //text.setText("????????????");
                                text.setText("????????????:\n" + recovered_string);
                            }else if (recovered_string.equals("o7tg")) {
                                hast = recovered_string;
                                sendPOST();
                                //Log.e("Dolphinweb", "mUID???" +mLogin.UID + "msoundID:" + mLogin.SOUNDID);
                                //text.setText("????????????");
                                text.setText("????????????:\n" + recovered_string);
                            }else if (recovered_string.equals("2X")) {
                                hast = recovered_string;
                                //sendPOST();
                                //text.setText("????????????");
                                text.setText("????????????:\n" + recovered_string);
                            } else if (recovered_string.equals("QC")) {
                                hast = recovered_string;
                                sendPOST();
                                //text.setText("????????????");
                                text.setText("????????????:\n" + recovered_string);
                            }
                            else if (recovered_string.equals("AD")) {
                                hast = recovered_string;
                                sendPOST();
                                //text.setText("????????????");
                                text.setText("????????????:\n" + recovered_string);
                            }else if (recovered_string.equals("ZH")) {
                                hast = recovered_string;
                                sendPOST();
                                //text.setText("????????????");
                                text.setText("????????????:\n" + recovered_string);
                            }else if (recovered_string.equals("L6")) {
                                hast = recovered_string;
                                sendPOST();
                                //text.setText("????????????");
                                text.setText("????????????:\n" + recovered_string);
                            }
                            else if (recovered_string.equals("9W")) {
                                hast = recovered_string;
                                sendPOST();
                                //text.setText("????????????");
                                text.setText("????????????:\n" + recovered_string);
                            }
                            else if (recovered_string.equals("V3")) {
                                hast = recovered_string;
                                sendPOST();
                                //text.setText("????????????");
                                text.setText("????????????:\n" + recovered_string);
                            }else if (recovered_string.equals("FP")) {
                                hast = recovered_string;
                                sendPOST();
                                //text.setText("????????????");
                                text.setText("????????????:\n" + recovered_string);
                            }else if (recovered_string.equals("JR")) {
                                hast = recovered_string;
                                sendPOST();
                                //text.setText("????????????");
                                text.setText("????????????:\n" + recovered_string);
                            }else if (recovered_string.equals("T4")) {
                                hast = recovered_string;
                                sendPOST();
                                //text.setText("????????????");
                                text.setText("????????????:\n" + recovered_string);
                            }else if (recovered_string.equals("XZ")) {
                                hast = recovered_string;
                                sendPOST();
                                //text.setText("????????????");
                                text.setText("????????????:\n" + recovered_string);
                            }else if (recovered_string.equals("PL")) {
                                hast = recovered_string;
                                sendPOST();
                                //text.setText("????????????");
                                text.setText("????????????:\n" + recovered_string);
                            }else if (recovered_string.equals("C4")) {
                                hast = recovered_string;
                                sendPOST();
                                //text.setText("????????????");
                                text.setText("????????????:\n" + recovered_string);
                            }else if (recovered_string.equals("5A")) {
                                hast = recovered_string;
                                sendPOST();
                                //text.setText("????????????");
                                text.setText("????????????:\n" + recovered_string);
                            }else if (recovered_string.equals("ED")) {
                                hast = recovered_string;
                                sendPOST();
                                //text.setText("????????????");
                                text.setText("????????????:\n" + recovered_string);
                            }else if (recovered_string.equals("UD")) {
                                hast = recovered_string;
                                sendPOST();
                                //text.setText("????????????");
                                text.setText("????????????:\n" + recovered_string);
                            }else if (recovered_string.equals("47")) {
                                hast = recovered_string;
                                sendPOST();
                                //text.setText("????????????");
                                text.setText("????????????:\n" + recovered_string);
                            }else if (recovered_string.equals("MG")) {
                                hast = recovered_string;
                                sendPOST();
                                //text.setText("????????????");
                                text.setText("????????????:\n" + recovered_string);
                            }else if (recovered_string.equals("7Z")) {
                                hast = recovered_string;
                                sendPOST();
                                text.setText("????????????");
                                //text.setText("????????????:\n" + recovered_string);
                            }else if (recovered_string.equals("G7")) {
                                hast = recovered_string;
                                sendPOST();
                                //text.setText("????????????");
                                text.setText("????????????:\n" + recovered_string);
                            }
                            else {
                                text.setText("????????????:\n" + recovered_string);//?????????????????????
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
    //**????????????*/
    private void sendPOST() {
        /**????????????*/
        OkHttpClient client = new OkHttpClient().newBuilder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                .build();
        /**?????????????????????????????????*/
        FormBody formBody = new FormBody.Builder()
                //.add("firstname", "???")
                //.add("lastname", "??????")
                .add("OpenDoor","Y")
                //.add("password","a12345678")
                //.add("password2","a12345678")
                .build();
        /**?????????????????????????????????
        FormBody formBody = new FormBody.Builder()
                .add("soundId", mLogin.SOUNDID)
                .add("hash", hast)
                .add("door",mLogin.doorID)
                .build();*/
        /**??????????????????*/
        Request request = new Request.Builder()
                .url("http://192.168.50.192/sound_networking/PHP/AudioLogin.php")//https://soundnet-server.herokuapp.com/api/server/verify;http://192.168.50.172:3000/api/login
                .post(formBody)
                .build();
        /**????????????*/
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                /**?????????????????????????????????*/
                //text.setText(e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                /**????????????*/
                //text.setText("POST?????????\n" + response.body().string());
                Log.e("Dolphinweb", "Response:" + response.body().string());
            }
        });
    }
    //*??????*/
    private void interNet(String str) {

        Uri uri = Uri.parse("https://t.ly/" + str);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    //????????????
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
}