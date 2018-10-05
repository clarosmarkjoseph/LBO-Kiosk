package com.example.itdevjr1.kiosk;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.example.itdevjr1.kiosk.BroadcastReceiver.AlarmBroadcastReceiver;
import com.example.itdevjr1.kiosk.WebSocket.SocketApplication;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.engineio.client.Transport;
import com.github.nkzawa.socketio.client.Manager;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {

    String status;
    FrameLayout frameLayout;
    AlarmBroadcastReceiver alarmReceiver;
    Utilities utilities;
    String SERVER_URL = "";
    DataHandler handler;
    TextView lblModuleTitle, lblModuleCaption;
    RelativeLayout relativeToolbar;
    ActivitySingleton activitySingleton;
    ConstraintLayout constrainMainActivity;
    public Socket mSocket;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupMainWindowDisplayMode();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(this.getResources().getColor(R.color.laybareGreen));
            getWindow().setStatusBarColor(this.getResources().getColor(R.color.laybareGreen));
            getWindow().setSoftInputMode(WindowManager.LayoutParams.TYPE_APPLICATION_SUB_PANEL);
        }

        activitySingleton       = new ActivitySingleton();
        utilities               = new Utilities(this);
        frameLayout             = (FrameLayout) findViewById(R.id.frameLayout);
        handler                 = new DataHandler(getApplicationContext());
        SERVER_URL              = utilities.returnIpAddress();
        constrainMainActivity   = (ConstraintLayout) findViewById(R.id.constrainMainActivity);
        constrainMainActivity.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                hideKeyBoard(view);
                return false;
            }
        });
        activitySingleton.Instance().setUtilityClass(utilities);
        setAlarmReceiver();
    }


    private void loadFragment() {

        SocketApplication socketApplication = new SocketApplication();
        mSocket                             = socketApplication.getSocket();
        activitySingleton.Instance().setSocketApplication(mSocket);
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.connect();
        mSocket.open();

        FragmentSplashScreen fragmentSplashScreen = new FragmentSplashScreen();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragmentSplashScreen,"FragmentSplashScreen");
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

    }


    public Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.e("WEW", "Socket Connected!");
        }
    };

    public Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String data = String.valueOf(args[0]);
                    Log.e("onConnectError", "Socket error!" + data);
                }
            });
        }
    };

    public Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String data = String.valueOf(args[0]);
                    Log.e("WEW", "Socket Disconnected!" + data);
                }
            });
        }
    };

    @Override
    public void onStart() {
        super.onStart();
    }


    @Override
    public void onStop() {
        super.onStop();

    }



    private void hideKeyBoard(View view) {
        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(view.getWindowToken(),0);
    }


    private void setAlarmReceiver() {

        status = "on";
        handler.open();

        Cursor c = handler.getAlarmStatus();
        if (c.getCount() > 0) {
            handler.setAlarm(status);
        }
        else {
            handler.insertAlarm(status);
        }

        MediaPlayer alarm = MediaPlayer.create(getApplicationContext(), R.raw.siren_noise);
        AlarmSingleton alarmSingleton = new AlarmSingleton();
        alarmReceiver = new AlarmBroadcastReceiver(alarm);
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        alarmSingleton.Instance().setAlarm(alarm);
        registerReceiver(alarmReceiver, filter);

        loadFragment();

    }


    @Override
    protected void onPostResume() {
        super.onPostResume();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onResume() {
        setupMainWindowDisplayMode();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
        if (alarmReceiver != null){
            unregisterReceiver(alarmReceiver);
        }
    }



    private View setSystemUiVisilityMode() {

        View decorView = getWindow().getDecorView();
        int options;
        options =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE
                        |View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;

        decorView.setSystemUiVisibility(options);
        getWindow().setBackgroundDrawable(null);
        return decorView;
    }

    private void setupMainWindowDisplayMode() {
        View decorView = setSystemUiVisilityMode();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    setSystemUiVisilityMode();
                } else {
                    setSystemUiVisilityMode();
                }

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public void onBackPressed() {
        return;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }



}
