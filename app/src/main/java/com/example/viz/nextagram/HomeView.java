package com.example.viz.nextagram;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import java.io.IOException;
import java.util.ArrayList;

public class HomeView extends Activity implements AdapterView.OnItemClickListener, OnClickListener {

//    private static AsyncHttpClient client = new AsyncHttpClient();
    private Button button1;
    private Button button2;
    private ArrayList<ArticleDTO> articleDTOList;
    private HomeController homeController;
    private final static String TAG = HomeView.class.getSimpleName();
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    public static final String PROPERTY_REG_ID = "registration_id";
    private SharedPreferences pref;
    private String SENDER_ID = "644474322682";
    private GoogleCloudMessaging gcm;
    private String regid;
    Context context;

    private static SyncHttpClient client = new SyncHttpClient();

    private void sendRegistrationToServer(String regid) {
        RequestParams params = new RequestParams();
        params.put("reg_id", regid);
        client.post("http://192.168.56.1:5009/gcm", params, new JsonHttpResponseHandler() {

        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        homeController = new HomeController(getApplicationContext());
        homeController.initSharedPreferences();
        homeController.startSyncDataService();



        pref = getSharedPreferences(getString(R.string.pref_name), MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(getString(R.string.server_ip), getString(R.string.server_ip_value));
        editor.apply();

        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button1.setOnClickListener(this);
        button2.setOnClickListener(this);

        Intent intentSync = new Intent(this, SyncDataService.class);
        startService(intentSync);

        listView();

        context = getApplicationContext();

        if (checkGooglePlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);
            registerInBackground();
        }
    }

    private boolean checkGooglePlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "이 장비는 Google Play Service를 지원하지 않습니다.");
                finish();
            }
            return false;
        }
        return true;
    }

    private String getRegistrationId(Context context) {
        String registrationId = pref.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        Log.i(TAG, "Registration ID = " + registrationId);
        return registrationId;
    }

    private void registerInBackground() {
        new AsyncTask<Object, Object, Object>() {
            @Override
            protected Object doInBackground(Object... params) {
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    Log.i(TAG, "Registration ID=" + regid);

                    sendRegistrationToServer(regid);
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    Log.e(TAG, "Error :" + ex.getMessage());
                }
                return null;
            }
        }.execute(null, null, null);
    }

    private void storeRegistrationId(Context context, String regid) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(PROPERTY_REG_ID, regid);
        editor.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();

        homeController.refreshData();
//        // 서버에서 데이터를 가져와서 db에 넣는 부분
//        refreshData();
//        // db로부터 게시판 글을 가져와서 리스트에 넣는 부분
//        listView(); // refreshData()에서 서버에서 json과 이미지 파일 가져오는 작업이 비동기기 때문에 이게 먼저 실행되기 때문에 처음 화면에 화면에 아무것도 안 보임
    }



//    public void refreshData() {
//
//
//        client.get(getString(R.string.server_url_value) + "/loadData", new AsyncHttpResponseHandler() {
//            @Override
//            public void onSuccess(int i, Header[] headers, byte[] bytes) {
//                String jsonData = new String(bytes);
//                Log.i("getJSonData", "success: " + jsonData);
//                Dao dao = new Dao(getApplicationContext());
//                dao.insertJsonData(jsonData);
//                listView();
//            }
//
//            @Override
//            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
//                Log.i("getJSonData: ", "fail: " + throwable.getMessage());
//            }
//        });

//    }
    public void listView() {
        ListView listView = (ListView) findViewById(R.id.customlist_listview);

        Cursor cursor = getContentResolver().query(
                NextagramContract.Article.CONTENT_URI,
                NextagramContract.Article.PROJECTION_ALL, null, null,
                NextagramContract.Article.SORT_ORDER_DEFAULT
        );

        HomeViewAdapter homeViewAdapter = new HomeViewAdapter(this, cursor, R.layout.custom_list_row);

        listView.setAdapter(homeViewAdapter);
        listView.setOnItemClickListener(this);
    }
//    private void listView() {
//        Dao dao = new Dao(getApplicationContext());
//        articleDTOList = dao.getArticleList();
//       // HomeViewAdapter homeViewAdapter = new HomeViewAdapter(this, R.layout.custom_list_row, articleDTOList);
//        ListView listView = (ListView) findViewById(R.id.customlist_listview);
//
//        Cursor mCursor = getContentResolver().query(
//                NextagramContract.Article.CONTENT_URI,
//                NextagramContract.Article.PROJECTION_ALL, null, null,
//                NextagramContract.Article._ID + "asc"
//        );
//        HomeViewAdapter customAdapter = new HomeViewAdapter(this, mCursor, R.layout.custom_list_row);
//
//
//        listView.setAdapter(customAdapter);
//        listView.setOnItemClickListener(this);
//    }

    @Override
    public void onClick(View arg0) {
        switch (arg0.getId()) {
            case R.id.button1:
                Intent intentWrite = new Intent(".WritingArticle");
                startActivity(intentWrite);
                break;
            case R.id.button2:
               // refreshData();
                listView();
                break;

        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(".ArticleView");
        String articleNumber = articleDTOList.get(position).getArticleNumber() + "";
        intent.putExtra("ArticleNumber", articleNumber);
        startActivity(intent);
    }



}