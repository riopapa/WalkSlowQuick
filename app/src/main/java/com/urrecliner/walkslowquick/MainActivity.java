package com.urrecliner.walkslowquick;

import android.Manifest;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    static int quickTime, slowTime;
    List<String> timeList;
    static TextView tVDuration;
    static boolean isWalking;
    static String quickText, slowText;
    SharedPreferences sharePref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        TextView tv;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        askPermission();

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
                    stopTimer();
                } else {
                    isWalking = true;
                    startStop.setImageResource(R.mipmap.stop);
                    tv = findViewById(R.id.text_quick);
                    quickText = tv.getText().toString();
                    tv = findViewById(R.id.text_slow);
                    slowText = tv.getText().toString();
                    editor.putString("quickText", quickText).apply();
                    editor.putString("slowText", slowText).apply();
//                    bindService(new Intent(MainActivity.this, TimerService.class), sconn , BIND_AUTO_CREATE);
                    startTimer();
                }
            }
        });
    }

    void startTimer() {
        Intent intent = new Intent(this, TimerService.class);
        startService(intent);
    }

    void stopTimer() {
        Intent intent = new Intent(this, TimerService.class);
        stopService(intent);
    }

    private void buildQuickWheel() {

        final WheelView<String> wheelView = findViewById(R.id.wheel_quick);
        wheelView.setOnItemSelectedListener((wheelView1, data, position) -> { });
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
        wheelView.setOnItemSelectedListener((wheelView1, data, position) -> { });
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

    static String time2Text(int timeInt) {
        final DecimalFormat df = new DecimalFormat("00");
        return df.format(timeInt/60) + ":" + df.format(timeInt % 60);
    }

    static int time2Int(String s) {
        return Integer.parseInt(s.substring(0,2)) * 60 + Integer.parseInt(s.substring(3));
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, TimerService.class);
        stopService(intent);
        Toast.makeText(getApplicationContext(),"Exit Application",Toast.LENGTH_SHORT).show();
        finish();
        new Timer().schedule(new TimerTask() {
            public void run() {
                finishAffinity();
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(0);
            }
        }, 2000);

    }


    // ↓ ↓ ↓ P E R M I S S I O N    RELATED /////// ↓ ↓ ↓ ↓
    ArrayList<String> permissions = new ArrayList<>();
    private final static int ALL_PERMISSIONS_RESULT = 101;
    ArrayList permissionsToRequest;
    ArrayList<String> permissionsRejected = new ArrayList<>();

    private void askPermission() {
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        permissions.add(Manifest.permission.ACCESS_NOTIFICATION_POLICY);
        permissions.add(Manifest.permission.RECEIVE_BOOT_COMPLETED);
        permissionsToRequest = findUnAskedPermissions(permissions);
        if (permissionsToRequest.size() != 0) {
            requestPermissions((String[]) permissionsToRequest.toArray(new String[0]),
                    ALL_PERMISSIONS_RESULT);
        }
    }

    private ArrayList findUnAskedPermissions(@NonNull ArrayList<String> wanted) {
        ArrayList <String> result = new ArrayList<>();
        for (String perm : wanted) if (hasPermission(perm)) result.add(perm);
        return result;
    }
    private boolean hasPermission(@NonNull String permission) {
        return (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == ALL_PERMISSIONS_RESULT) {
            for (Object perms : permissionsToRequest) {
                if (hasPermission((String) perms)) {
                    permissionsRejected.add((String) perms);
                }
            }
            if (permissionsRejected.size() > 0) {
                if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                    String msg = "These permissions are mandatory for the application. Please allow access.";
                    showDialog(msg);
                }
            }
            else
                Toast.makeText(getApplicationContext(), "Permissions not granted.", Toast.LENGTH_LONG).show();
        }
    }
    private void showDialog(String msg) {
        showMessageOKCancel(msg,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermissions(permissionsRejected.toArray(
                                new String[0]), ALL_PERMISSIONS_RESULT);
                    }
                });
    }
    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new android.app.AlertDialog.Builder(getApplicationContext())
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
// ↑ ↑ ↑ ↑ P E R M I S S I O N    RELATED /////// ↑ ↑ ↑

}