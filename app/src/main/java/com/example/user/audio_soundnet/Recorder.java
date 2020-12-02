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
import java.util.Timer;

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
    private static final int RECORDER_SAMPLERATE = 48000;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_CHANNELS_INT = 1;

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

            //recorder = null;
            //recordingThread = null;

            copyWaveFile(getTempFilename(AUDIO_RECORDER_TEMP_FILE), getFilename());
        }
    }

    private String getFilename() {
        System.out.println("---3---");
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath, AUDIO_RECORDER_FOLDER);

        if (!file.exists()) {
            file.mkdirs();
        }

        return (file.getAbsolutePath() + "/" + AUDIO_RECORDER_FILE_EXT_WAV);
    }

    private void copyWaveFile(String inFilename, String outFilename) {
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = RECORDER_SAMPLERATE;
        int channels = RECORDER_CHANNELS_INT;
        long byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * channels / 8;

        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;

            WriteWaveFileHeader(out, totalAudioLen, totalDataLen, longSampleRate, channels, byteRate);
            byte[] bytes2 = new byte[buffer.length * 2];
            ByteBuffer.wrap(bytes2).order(ByteOrder.LITTLE_ENDIAN)
                    .asShortBuffer().put(buffer);

            while (in.read(bytes2) != -1){
                out.write(bytes2);
            }

            in.close();
            out.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getFileName(String name) {
        return getTempFilename(name);
    }


    private String getTempFilename(String audioRecorderTempFile) {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath, AUDIO_RECORDER_FOLDER);
        if (!file.exists()) {
            file.mkdirs();
        }
        File tempFile = new File(filepath, audioRecorderTempFile);
        if (tempFile.exists())
            tempFile.delete();
        return (file.getAbsolutePath() + "/" + audioRecorderTempFile);
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
        //double data[]=new double[buffer.length*2];
        String filename = getTempFilename(AUDIO_RECORDER_TEMP_FILE);
        FileOutputStream os = null;
        FileOutputStream fos = null;
        OutputStreamWriter dos = null;

        try {

            os = new FileOutputStream(filename);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {

            fos = new FileOutputStream(getTempFilename(dataFile));
            dos = new OutputStreamWriter(fos);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

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

        while (isRecording) {
            recorder.read(buffer, 0, buffer.length);
            if (log1) {
                Log.d(TAG, "audio buffer len = " + buffer.length);
                log1 = false;
            }
            if (AudioRecord.ERROR_INVALID_OPERATION != read) {

                try {

                    double tmpData[] = new double[buffer.length];
                    for (int i = 0; i < buffer.length; i++) {
                        tmpData[i] = (double) buffer[i] / 32768.0;//將數值歸一化
                    }
                    //18_11_26
                    //s = goertzelDetector.findCarrier_array(tmpData, 20048.0, win_factor, shift_factor, s_len);
                    s = goertzelDetector.findCarrier_array(tmpData, Bw, win_factor, shift_factor, s_len);
                    Log.v("sync","sync:" + s);

                    for (int i = 0; i < s_len; i++) {
                        dos.append("" + s[i] + " ");
                        Log.v("dos","dos:"+dos);
                        //dos.append("" + s[i]);
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
                        os.write(bytes2);
                        lenght += bytes2.length;
                        //ArrSteaming = new byte[65535];
                        for (int x = 0;x < 9600;x++){
                            ArrSteaming[x + old_lenght] = bytes2[x];
                        }
                        //System.arraycopy(bytes2,0,ArrSteaming,lenght,bytes2.length);
                        old_lenght += 9600;
                        if (old_lenght >= 192000)timeout = true;

                        Log.d("Arr","Arr="+lenght);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }

        try {
            os.close();
            dos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen,
                                     long totalDataLen, long longSampleRate, int channels, long byteRate)
            throws IOException {
        System.out.println("---9---");
        byte[] header = new byte[4088];

        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) RECORDER_CHANNELS_INT;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (RECORDER_CHANNELS_INT * RECORDER_BPP / 8); // block align
        header[33] = 0;
        header[34] = RECORDER_BPP; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 4088);
    }
    public byte[] Steaming(byte [] bt){
        byte [] ArrSteaming = bt;
        System.arraycopy(bt,0,ArrSteaming,0,bt.length);
        return ArrSteaming;
    }
}
