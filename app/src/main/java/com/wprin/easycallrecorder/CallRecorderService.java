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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CallRecorderService extends Service {

    private Map _records = new HashMap();

    public CallRecorderService() {
    }

    private PhoneStateListener phoneStateListener = new PhoneStateListener() {

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);

            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    Log.d("TempLog", "CALL_STATE_IDLE state:" + state + ",number:" + incomingNumber);
                    StopRecord(incomingNumber);
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    Log.d("TempLog", "CALL_STATE_OFFHOOK state:" + state + ",number:" + incomingNumber);
                    if (_records.size() == 0) {
                        RecordCalling(incomingNumber);
                    } else {
                        Log.d("TempLog", "Due to other recorder is running,so current recorder can't continue.");
                    }
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    Log.d("TempLog", "CALL_STATE_RINGING state:" + state + ",number:" + incomingNumber);
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

    private void StopRecord(String number) {
        Log.d("TempLog", "stop record:" + number);
        if (_records.containsKey(number)) {
            Log.d("TempLog", "remove map key:" + number);
            MediaRecorder recorder = (MediaRecorder) _records.get(number);
            recorder.stop();
            _records.remove(number);
        }
    }

    private void RecordCalling(String number) {
        try {
            String savePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            savePath += "/wprin/CallRecorder";
            File file = new File(savePath);
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    Log.d("TempLog", "Failed to create directory");
                    throw new Exception("Failed to create directory");
                }
            }

            DateFormat dtFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            String date = dtFormat.format(new Date());

            savePath = String.format("%s/[%s]-[%s].amr", savePath, date, number);

            MediaRecorder recorder = new MediaRecorder();
            _records.put(number, recorder);

            Log.d("TempLog", "start record:" + number);

            recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setOutputFile(savePath);
            recorder.prepare();
            recorder.start();
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
