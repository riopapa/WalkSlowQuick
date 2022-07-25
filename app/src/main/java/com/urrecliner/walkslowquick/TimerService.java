package com.urrecliner.walkslowquick;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static com.urrecliner.walkslowquick.MainActivity.isWalking;
import static com.urrecliner.walkslowquick.MainActivity.quickText;
import static com.urrecliner.walkslowquick.MainActivity.quickTime;
import static com.urrecliner.walkslowquick.MainActivity.slowText;
import static com.urrecliner.walkslowquick.MainActivity.slowTime;
import static com.urrecliner.walkslowquick.MainActivity.tVDuration;
import static com.urrecliner.walkslowquick.MainActivity.time2Text;

public class TimerService extends Service {
    Timer walkTimer = null, speakTimer;
    int loopCount, duration, sec, loopTIme;
    boolean isQuick;
    TextToSpeech textToSpeech;

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationCompat.Builder mBuilder = null;
        NotificationChannel mNotificationChannel = null;
        NotificationManager mNotificationManager;
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationChannel = new NotificationChannel("default","default", NotificationManager.IMPORTANCE_DEFAULT);
        mNotificationManager.createNotificationChannel(mNotificationChannel);

        mBuilder = new NotificationCompat.Builder(getApplicationContext(),"default")
                .setSmallIcon(R.mipmap.launcher)
                .setOnlyAlertOnce(true)
                .setAutoCancel(true)
                .setOngoing(false);
        startForeground(111, mBuilder.build());
        ready_TTS();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        running();
        return START_STICKY;
    }

    @Override

    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    void running() {
        duration = 0;
        sec = 0;
        isQuick = false;
        walkTimer = new Timer();
        walkTimer.schedule(new TimerTask() {
            public void run() {
                if (isWalking) {
                    if (sec++ == 0) {
                        isQuick = !isQuick;
                        loopTIme = (isQuick) ? quickTime : slowTime;
                        speakText();
                        vibratePhone();
                    } else if (sec == loopTIme) {
                        sec = 0;
                    }
                    duration++;

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        public void run() {
                            tVDuration.setText(time2Text(duration));
                        }
                    });

                } else {
                    walkTimer.cancel();
                    walkTimer.purge();
                }
            }
        }, 1000, 1000);

    }

    void vibratePhone() {
        long[] intervalQuick = { 93, 449,  93, 449,  93, 449,  93, 449,  93, 449,  93, 449,  93, 449,  93, 449,
                 93, 449,  93, 449,  93, 449,  93, 449,  93, 449,  93, 449 };
        int[] ampQuick       = {250,   0, 150,   0, 250,   0, 150,   0, 250,   0, 150,   0, 250,   0, 150,   0,
                250,   0, 150,   0, 250,   0,  50,   0,  250,   0,  50,   0};
        long[] intervalSlow  = {   0,999,2372, 999, 372, 999,2372, 999, 372, 999,2372, 999};
        int[] ampSlow        = { 250,  0, 250,   0, 250,   0, 250,   0, 250,   0, 250,   0};
        Vibrator v = (Vibrator) getApplicationContext().getSystemService(VIBRATOR_SERVICE);
        assert v != null;
        v.vibrate(VibrationEffect.createWaveform((isQuick) ? intervalQuick: intervalSlow,
                (isQuick) ? ampQuick: ampSlow,  -1));
    }

    void speakText() {
        loopCount = 3;
        speakTimer = new Timer();
        speakTimer.schedule(new TimerTask() {
            public void run() {
                if (loopCount-- > 0) {
                    textToSpeech.speak((isQuick)? quickText:slowText, TextToSpeech.QUEUE_ADD, null, null);
                } else {
                    speakTimer.cancel();
                    speakTimer.purge();
                    textToSpeech.stop();
                }
            }
        }, 1000, 2000);
    }

    void ready_TTS() {
        textToSpeech = new TextToSpeech(getApplicationContext(), status -> textToSpeech.setLanguage(Locale.getDefault()));
        textToSpeech.setPitch(1.4f);
        textToSpeech.setSpeechRate(1.3f);
    }
}
