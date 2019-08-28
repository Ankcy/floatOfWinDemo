package com.example.mario.floatingwindowtest.activitys;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mario.floatingwindowtest.R;
import com.example.mario.floatingwindowtest.services.MusicService;
import com.example.mario.floatingwindowtest.utils.ApplicationUtils;

public class MainActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener {

    private ServiceConnection serviceConnection;
    private Intent serviceIntent;
    private TextView versionName;
    private CheckBox checkBox;

    private MusicService musicService;

    private int REQUEST_DIALOG_PERMISSION = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        int sdkInt = Build.VERSION.SDK_INT;
        if (sdkInt >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(MainActivity.this)) {
                if (sdkInt >= Build.VERSION_CODES.O) {//8.0以上
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                    startActivityForResult(intent, REQUEST_DIALOG_PERMISSION);
                } else if (sdkInt >= Build.VERSION_CODES.M) {//6.0-8.0
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, REQUEST_DIALOG_PERMISSION);
                }
            }
        }
        initAllViews();
        bindService();
        startService(serviceIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_DIALOG_PERMISSION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    Toast.makeText(MainActivity.this, "not granted", Toast.LENGTH_SHORT);
                }
            }
        }
    }

    private void initAllViews() {
        checkBox = (CheckBox) findViewById(R.id.main_check_box);
        checkBox.setOnCheckedChangeListener(this);

        versionName = (TextView) findViewById(R.id.main_version_name);
        versionName.setText(ApplicationUtils.getAppVersionName(this));
    }

    private void bindService() {
        serviceIntent = new Intent(MainActivity.this, MusicService.class);
        if (serviceConnection == null) {
            serviceConnection = new ServiceConnection() {

                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    musicService = ((MusicService.MusicBinder) service).getService();

                    SharedPreferences preferences = getSharedPreferences("FloatMusicPlayer", Context.MODE_PRIVATE);
                    boolean isCheck = preferences.getBoolean("isCheck", false);
                    checkBox.setChecked(isCheck);
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {

                }
            };
            bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);
        }
    }

    private void unbindService() {
        if (null != serviceConnection) {
            unbindService(serviceConnection);
            serviceConnection = null;
        }
    }

    @Override
    protected void onDestroy() {
        unbindService();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        unbindService();
        super.onPause();
    }

    @Override
    protected void onResume() {
        bindService();
        super.onResume();
    }

    @Override
    protected void onRestart() {
        bindService();
        super.onRestart();
    }

    @Override
    protected void onStop() {
        unbindService();
        super.onStop();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            musicService.show();
        } else {
            musicService.dismiss();
        }
        SharedPreferences preferences = getSharedPreferences("FloatMusicPlayer", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isCheck", isChecked);
        editor.commit();
    }
}
