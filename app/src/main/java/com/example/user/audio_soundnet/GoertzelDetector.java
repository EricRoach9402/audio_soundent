package com.example.user.audio_soundnet;

import android.util.Log;

import java.util.ArrayList;

public class GoertzelDetector {

    private static final String TAG = "Goertzel";
    private double symbol_size;
    private double sample_rate;
    private double sample_period;
    public int symbol_N;
    private ArrayList<Double> recovered_signal;

    public GoertzelDetector(double symbol_size, double sample_rate) {
        this.symbol_size = symbol_size;
        this.sample_rate = sample_rate;
        this.sample_period = 1.0 / sample_rate;
        this.symbol_N = (int) (symbol_size * sample_rate);
    }

    public double findCarrier_value(double[] x, double fin, int xlen) {//GoertzelDetector基本運算
        int k = (int) (0.5 + xlen * fin / sample_rate);
        double w = 2 * Math.PI * k / xlen;
        double cosine = Math.cos(w);
        double coe = 2 * cosine;
        double Q0 = 0;
        double Q1 = 0;
        double Q2 = 0;
        for (int i = 0; i < xlen; i++) {
            Q0 = coe * Q1 - Q2 + x[i];
            Q2 = Q1;
            Q1 = Q0;
        }
        double y = Math.sqrt(Q1 * Q1 + Q2 * Q2 - Q1 * Q2 * coe);
        return y;
    }

    public double[] findCarrier_array(double[] signal, double fin, int win_factor, int shift_factor, int len) {//GoertzelDetector的個別音框陣列計算
        double[] res = new double[len];//資料總長度
        int win_N = symbol_N / win_factor;//計算音框的點數
        int shift_N = symbol_N / shift_factor;//移動音框的點數
        double[] x = new double[win_N];

        for (int i = 0; i < len; i++) {

            if(signal.length-i*shift_N>=win_N){
                //arraycopy:來源,起始索引,目的地,起始索引,複製長度
                System.arraycopy(signal, i * shift_N, x, 0, win_N);
                res[i] = findCarrier_value(x, fin, win_N);
            }
        }
        return res;
    }

}
