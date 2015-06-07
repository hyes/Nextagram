package com.example.viz.nextagram;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.ArrayList;

/**
 * Created by hyes on 2015. 6. 7..
 */
public class HomeController {

    Context context;
    private SharedPreferences pref;
    private Proxy proxy;
    private ProviderDao dao;

    public HomeController(Context context){
        this.context = context;
        this.proxy = new Proxy(context);
        this.dao = new ProviderDao(context);
    }

        public void initSharedPreferences() {
            pref = context.getSharedPreferences(context.getResources().
                    getString(R.string.pref_name), context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString(context.getResources().getString(R.string.server_ip),
                    context.getResources().getString(R.string.server_url_value));
            editor.putString(context.getResources().getString(R.string.pref_article_number), 0 + "");
            editor.commit();

//        pref = context.getSharedPreferences(context.getResources().getString(R.string.pref_name), Context.MODE_PRIVATE);
//
//        SharedPreferences.Editor editor = pref.edit();
//
//        editor.putString(context.getResources().getString(R.string.server_url), context.getResources().getString(R.string.server_url_value));
//        editor.commit();
    }

    public void refreshData(){
        new Thread() {
            public void run() {
                ArrayList<ArticleDTO> articleList = proxy.getArticleDTO();
                dao.insertData(articleList);
            }
        }.start();
    }

    public void startSyncDataService() {
        Intent intent = new Intent("com.example.viz.nextagram.service.SyncDataService");
        context.startService(intent);
       // implicitIntent.setAction("org.nhnnext.android.android.basic.SyncDataService");
        //context.startService(implicitIntent);
    }
}
