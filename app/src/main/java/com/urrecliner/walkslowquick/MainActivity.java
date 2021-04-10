package com.urrecliner.walkslowquick;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;

import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    int quickTime, slowTime;
    List<String> timeList;
    int duration, sec, loopTIme;
    TextView tVDuration;
    boolean isQuick, isWalking;
    SharedPreferences sharePref;
    SharedPreferences.Editor editor;
    String quickText, slowText;
    Timer walkTimer, speakTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        TextView tv;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        timeList = new ArrayList<>();
        for (int sec = 10; sec <= 8*60; sec += 10) {
            timeList.add(time2Text(sec));
        }
        sharePref = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharePref.edit();
        editor.apply();
        quickTime = sharePref.getInt("quickTime", 3*60);
        slowTime = sharePref.getInt("slowTime", 2*60);
        quickText = sharePref.getString("quickText","빠르게 걷기");
        slowText = sharePref.getString("slowText", "천천히 걷기");
        tVDuration = findViewById(R.id.duration);
        tv = findViewById(R.id.text_quick); tv.setText(quickText);
        tv = findViewById(R.id.text_slow); tv.setText(slowText);
        buildQuickWheel();
        buildSlowWheel();
        isWalking = false;
        ImageView startStop = findViewById(R.id.startStop);
        startStop.setOnClickListener(new View.OnClickListener() {
            TextView tv;
            @Override
            public void onClick(View v) {
                if (isWalking) {
                    isWalking = false;
                    startStop.setImageResource(R.mipmap.start);
                    walkTimer.cancel(); walkTimer.purge();
                    loopTIme = -1;
                } else {
                    isWalking = true;
                    startStop.setImageResource(R.mipmap.stop);
                    tv = findViewById(R.id.text_quick);
                    quickText = tv.getText().toString();
                    tv = findViewById(R.id.text_slow);
                    slowText = tv.getText().toString();
                    editor.putString("quickText", quickText).apply();
                    editor.putString("slowText", slowText).apply();
                    running();
                }
            }
        });
    }

    private void buildQuickWheel() {

        final WheelView<String> wheelView = findViewById(R.id.wheel_quick);
        wheelView.setOnItemSelectedListener(new WheelView.OnItemSelectedListener<String>() {
            @Override
            public void onItemSelected(WheelView<String> wheelView, String data, int position) {
//                Log.w(TAG, "onItemSelected: data=" + data + ",position=" + position);
            }
        });
        wheelView.setOnWheelChangedListener(new WheelView.OnWheelChangedListener() {
            @Override
            public void onWheelScroll(int scrollOffsetY) { }
            @Override
            public void onWheelItemChanged(int oldPosition, int newPosition) { }
            @Override
            public void onWheelScrollStateChanged(int state) { }
            @Override
            public void onWheelSelected(int position) {
                quickTime = time2Int(timeList.get(position));
                editor.putInt("quickTime", quickTime).apply();
            }
        });

        wheelView.setData(timeList);
        wheelView.setSelectedItemPosition(quickTime / 10 - 1, true);
        wheelView.setSoundEffect(true);
        wheelView.setSoundEffectResource(R.raw.wheel_up_down);
        wheelView.setPlayVolume(0.2f);
    }


    private void buildSlowWheel() {

        final WheelView<String> wheelView = findViewById(R.id.wheel_slow);
        wheelView.setOnItemSelectedListener(new WheelView.OnItemSelectedListener<String>() {
            @Override
            public void onItemSelected(WheelView<String> wheelView, String data, int position) {
//                Log.w(TAG, "onItemSelected: data=" + data + ",position=" + position);
            }
        });
        wheelView.setOnWheelChangedListener(new WheelView.OnWheelChangedListener() {
            @Override
            public void onWheelScroll(int scrollOffsetY) { }
            @Override
            public void onWheelItemChanged(int oldPosition, int newPosition) { }
            @Override
            public void onWheelScrollStateChanged(int state) { }
            @Override
            public void onWheelSelected(int position) {
                slowTime = time2Int(timeList.get(position));
                editor.putInt("slowTime", slowTime).apply();
            }
        });

        wheelView.setData(timeList);
        wheelView.setSelectedItemPosition(slowTime / 10 - 1, true);
        wheelView.setSoundEffect(true);
        wheelView.setSoundEffectResource(R.raw.wheel_up_down);
        wheelView.setPlayVolume(0.2f);
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
                    runOnUiThread(new Runnable() {
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
        long[] patternQuick = {0, 50, 30, 100, 30, 100, 30, 100, 30, 100, 30, 100, 30, 100, 30, 100};
        long[] patternSlow =  {0, 50, 500, 30, 500, 30, 500, 30, 500, 30, 500, 30, 500, 30, 500, 30,};
        Vibrator v = (Vibrator) getApplicationContext().getSystemService(VIBRATOR_SERVICE);
        assert v != null;
        v.vibrate(VibrationEffect.createWaveform((isQuick) ? patternQuick: patternSlow, -1));
    }

    private String time2Text(int timeInt) {
        final DecimalFormat df = new DecimalFormat("00");
        return df.format((int) (timeInt/60)) + ":" + df.format(timeInt % 60);
    }

    int time2Int(String s) {
        String a = s.substring(0,2);
        int i = Integer.parseInt(a);
        a = s.substring(3);
        return Integer.parseInt(s.substring(0,2)) * 60 + Integer.parseInt(s.substring(3));
    }

    private static int loopCount;
    TextToSpeech textToSpeech;

    void speakText() {
        loopCount = 3;
        ready_TTS();
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
        }, 2000, 2000);
    }

    void ready_TTS() {
        textToSpeech = new TextToSpeech(getApplicationContext(), status -> textToSpeech.setLanguage(Locale.getDefault()));
        textToSpeech.setPitch(1.4f);
        textToSpeech.setSpeechRate(1.3f);
    }

}