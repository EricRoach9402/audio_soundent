package com.example.user.audio_soundnet;

import android.content.Context;
import android.util.Log;
import android.os.Environment;
import java.io.*;
import java.util.ArrayList;

public class AudioHandler {
    private double sample_rate;
    private double duration;
    private long n_frames;
    private WavFile wavfile;
    private Double[] src;
    private double[] data;
    private String filename;
    private ArrayList<Double> modulated;
    private ArrayList<Byte> ByteToArrayList;
    private static Context context;
    private long n;

    public AudioHandler(Context context,String filename){//刪除
        this.context=context;
        this.filename=filename;

        try {

            String root=Environment.getExternalStorageDirectory().toString();
            this.wavfile=WavFile.openWavFile(new File(root,"My_Audio_FSK/"+filename));
            wavfile.display();

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public AudioHandler(Double[] src, Context context, String filename){
        this.src=src;
        this.sample_rate=48000;
        this.duration=src.length/sample_rate;
        this.n_frames=(long)(duration*sample_rate);
        this.filename=filename;
        this.context=context;
        data=new double[src.length];
        for(int i=0;i<src.length;i++){
            data[i]=(double)src[i];
        }
        initWrite();
    }

    private void initWrite() {
        long framCounter=0;
        double[] buffer=new double[100];
        int index=0;
        while (framCounter<n_frames){
            long remaining=wavfile.getFramesRemaining();
            int toWrite=(remaining>100 )? 100 : (int) remaining;
            for(int s=0;s<toWrite;s++,framCounter++,index++){
                buffer[s]=data[index];
            }
            try {
                wavfile.writeFrames(buffer,toWrite);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void writeFile() {


        long frameCounter = 0;
        double[] buffer = new double[100];
        int index = 0;

        while (frameCounter < n_frames) {
            // Determine how many frames to write, up to a maximum of the buffer size
            long remaining = wavfile.getFramesRemaining();
            int toWrite = (remaining > 100) ? 100 : (int) remaining;

            for (int s = 0; s < toWrite; s++, frameCounter++, index++) {
                buffer[s] = data[index];
            }
            try {

                // Write the buffer
                wavfile.writeFrames(buffer, toWrite);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    public ArrayList<Double> read(){
        modulated=new ArrayList<Double>();
        n=0;
        double[] buffer=new double[100];
        int framesRead;
        try{
            do{
                framesRead=wavfile.readFrames(buffer,100);//讀取並返回以bytes對象表示的最多100幀音頻。
                Log.e("Dolphintest","framesRead:"+framesRead);
                for(int s=0;s<framesRead;s++){
                    Double temp =(Double)buffer[s];
                    modulated.add(temp);
                    //Log.e("Dolphintest","temp:"+temp);
                }

            }while (framesRead != 0);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            return modulated;
        }
    }
    public ArrayList<Byte> conversion(byte[] byteArray){
        ByteToArrayList = new ArrayList<Byte>();
        try{
            do{
                for(int s=0;s<byteArray.length;s++){
                    //Double temp =(Double)buffer[s];
                    Byte test = byteArray[s];
                    ByteToArrayList.add(test);
                }

            }while (byteArray.length != 0);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            return ByteToArrayList;
        }
    }

    public void close(){
        try{
            wavfile.close();
        }catch (Exception e){
            e.printStackTrace();
            Log.d("Audio_FSK", e.toString());
        }
    }

    public static boolean canWriteOnExternalStorage() {
        // get the state of your external storage
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // if storage is mounted return true
            Log.d("Audio_FSK", "Yes, can write to external storage.");
            return true;
        }
        return false;
    }
}
