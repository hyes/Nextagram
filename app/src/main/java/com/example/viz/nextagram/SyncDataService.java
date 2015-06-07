package com.example.viz.nextagram;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by hyes on 2015. 4. 27..
 */
public class SyncDataService extends Service {

    private TimerTask mTask;
    private Timer mTimer;

    private Proxy proxy;
    private ProviderDao dao;

    private static final String TAG = SyncDataService.class.getSimpleName();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");

        proxy = new Proxy(getApplicationContext());
        dao = new ProviderDao(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");

        mTask = new TimerTask() {
            @Override
            public void run() {
                ArrayList<ArticleDTO> articleList = proxy.getArticleDTO();
                dao.insertData(articleList);
            }
        };

        // 새로운 Timer를 생성한다.
        try {
            mTimer = new Timer();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Timer에게 위에 작성한 TimerTask, 시작하기 원하는 시점, 주기를 인자로 설정한다.
        try {
            mTimer.schedule(mTask, 1000*5, 1000*5);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return super.onStartCommand(intent, flags, startId);
   }


    private boolean isOnline() {
        try {
            ConnectivityManager conMan = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo.State wifi = conMan.getNetworkInfo(1).getState();
            if (wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING) {
                return true;
            }

            NetworkInfo.State mobile = conMan.getNetworkInfo(0).getState();
            if (mobile == NetworkInfo.State.CONNECTED || mobile == NetworkInfo.State.CONNECTING) {
                return true;
            }

        } catch (NullPointerException e) {
            return false;
        }

        return false;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }
}
