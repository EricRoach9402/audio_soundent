 package com.example.user.audio_soundnet;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.AutomaticGainControl;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;


 public class Recorder {
    private AudioRecord recorder;
    private GoertzelDetector goertzelDetector;
    private Receiver receiver;
    private int lenght,old_lenght=0;
    private boolean timeout = false;

    static final String TAG = "Recorder";
    private static final int RECORDER_BPP = 16;
    private static final String AUDIO_RECORDER_FILE_EXT_WAV = "recorded.wav";
    private static final String AUDIO_RECORDER_FOLDER = "My_Audio_FSK";
    private static final String AUDIO_RECORDER_TEMP_FILE = "recorded.raw";
    private static final int RECORDER_SAMPLERATE = 48000; //取樣率
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;//單聲道
    private static final int RECORDER_CHANNELS_INT = 1; //
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    static final String dataFile = "goertzelData.txt";

    //private int bufferSize = 200000;
    short[] buffer;//-215 ~ 215
    private Thread recordingThread = null;
    private boolean isRecording = false;
    public boolean recStart,getEND;
    private double symbol_size;
    private double Bw,sym_end;

    private double maxIndex;
    public ArrayList<Double> abc;

    private MainActivity may;
    private boolean stop_anime;
    public static byte [] ArrSteaming = new byte[300000];
    public static byte [] zeroArray = new byte[4044],mixArray = new byte[400000];


    public Recorder(double symbol_size, double Bw, double sym_END, final MainActivity act) {
        this.symbol_size = symbol_size;
        this.Bw = Bw;
        this.sym_end = sym_END;
        this.may = act;
        recStart = false;
        getEND = false;
        stop_anime=true;
        // int bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
       // int buffercount = 4800 / bufferSize;

        recorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, RECORDER_SAMPLERATE, RECORDER_CHANNELS
                , RECORDER_AUDIO_ENCODING, RECORDER_SAMPLERATE);

        if (AutomaticGainControl.isAvailable()) {
            AutomaticGainControl agc = AutomaticGainControl.create(recorder.getAudioSessionId());
            agc.setEnabled(false);
        } else {
            System.out.println("AGC NOT AVAILIABLE");
        }
    }

    public void start() throws IllegalStateException, IOException {
        buffer = new short[4800];
        recorder.startRecording();

        isRecording = true;

        recordingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                writeAudioData();
            }
        });
        recordingThread.start();
    }

    public void stop() {
        System.out.println("Told to stop");
        stopRecording();
    }

    private void stopRecording() {
        if (null != recorder) {
            isRecording = false;
            Log.d("recorder_stop","stop");
            if (recorder.getState() == AudioRecord.STATE_INITIALIZED){
                recorder.stop();
                recorder.release();
            }
        }
    }

    private int findMax(double[] arr, int len) {
        int index = 0;
        double max = 0;
        for (int i = 0; i < len; i++) {
            if (arr[i] > max) {
                max = arr[i];
                index = i;
            }
        }
        Log.d(TAG, "goertzel data for head = "+max);

        return index;
    }

    private void writeAudioData() {

        int read = 0;
        boolean log1 = true;
        // int dataLen = 0;
        //double symbol_size = 0.125;
        int shift_factor = 25;//總共移動25次
        int win_factor = 25;//每240點為一個音框(symbol_N/25)
        int symbol_N = (int) (symbol_size * RECORDER_SAMPLERATE);
        goertzelDetector = new GoertzelDetector(symbol_size, RECORDER_SAMPLERATE);
        int s_len = (buffer.length * 2 - symbol_N / win_factor) / (symbol_N / shift_factor) + 1;

        double[] s = new double[s_len];

        long recLen = 0;
        boolean stopAdd = false;

        int cont=0;//計算if(stop_anime)進去次數
        abc = new ArrayList<Double>();
        while (isRecording) {
            recorder.read(buffer, 0, buffer.length);
            for (int t = 0;t < buffer.length; t ++) {
                //Log.d("Goertzel", "I16Array = " + buffer[t]);
            }
            if (log1) {
                Log.d(TAG, "audio buffer len = " + buffer.length);
                log1 = false;
            }
            if (AudioRecord.ERROR_INVALID_OPERATION != read) {

                double tmpData[] = new double[buffer.length];
                for (int i = 0; i < buffer.length; i++) {
                    tmpData[i] = (double) buffer[i] / 32768.0;//將數值歸一化
                    //Log.d("Goertzel","tmpData: " + tmpData[i]);
                }
                Double max = tmpData[0];
                for (int i = 0;i < tmpData.length;i++){
                    if (max < tmpData[i]){
                        max = tmpData[i];
                    }
                }
                s = goertzelDetector.findCarrier_array(tmpData, Bw, win_factor, shift_factor, s_len);
                for (int i = 0;i < s.length;i++){
                }
                int mInd = findMax(s, s_len);

                if(recStart) {

                    double[] s_end=new double[s_len];
                    s_end=goertzelDetector.findCarrier_array(tmpData, sym_end, win_factor, shift_factor, s_len);

                    int mInd_end = findMax(s_end, s_len);

                    if (s_end[mInd_end] > may.threshold || timeout) {
                        Log.d(TAG, "find Max_END, recLen = " + recLen);
                        Log.d(TAG, "find Max_END, s_end[mInd_end] = " + s_end[mInd_end]);
                        System.arraycopy(zeroArray,0,mixArray,0,zeroArray.length);
                        System.arraycopy(ArrSteaming,0,mixArray,4045, ArrSteaming.length);
                        getEND = true;
                    }

                }
                if (s[mInd] > may.threshold) {//19_1/27,19_3_5 取得Sync
                    Log.d(TAG, "find Max, recLen = " + recLen);
                    maxIndex = s[mInd]; //振幅
                    stopAdd = true;
                    recStart = true;
                    if (stop_anime){
                        stop_anime=false;
                        may.anime=true;

                        Log.d("star_anime_OWL", "stop_anime_cont: " + cont);
                    }
                    Log.d(TAG, "find Max_sync, s[mInd] = " + s[mInd]);
                }
                if (!stopAdd)
                    recLen += buffer.length;
                if (recStart) {

                    byte[] bytes2 = new byte[buffer.length * 2];
                    ByteBuffer.wrap(bytes2).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(buffer);

                    for (int x = 0;x < buffer.length;x++){

                        abc.add((double)buffer[x]);
                        //receiver.modulated.add((double) buffer[x]);
                        //ArrSteaming[x + old_lenght] = bytes2[x];

                    }
                    old_lenght += 9600;
                    if (old_lenght >= 192000)timeout = true;
                }
            }
        }
    }
}
