package com.wprin.easycallrecorder;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.File;

public class CallRecorderService extends Service {

    private MediaRecorder mediaRecorder = null;
    private boolean isRecord = false;

    public CallRecorderService() {
    }

    private PhoneStateListener phoneStateListener = new PhoneStateListener() {

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);

            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    Log.i("TempLog", "CALL_STATE_IDLE");
                    StopRecord();
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    Log.i("TempLog", "CALL_STATE_OFFHOOK");
                    RecordCalling(incomingNumber);
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    Log.i("TempLog", "CALL_STATE_RINGING");
                    break;
                default:
                    break;
            }

        }
    };

    @Override
    public void onCreate() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

        super.onCreate();
    }

    private void StopRecord() {
        if (isRecord) {
            isRecord = false;
            mediaRecorder.stop();
            mediaRecorder.release();
        }
    }

    private void RecordCalling(String number) {
        try {
            String savePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            savePath += "/wprin/CallRecorder";
            File file = new File(savePath);
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    Log.i("TempLog", "Failed to create directory");
                    throw new Exception("Failed to create directory");
                }
            }

            savePath += "/[" + number + "]_" + System.currentTimeMillis() + ".amr";

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile(savePath);
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecord = true;
        } catch (Exception ex) {
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
