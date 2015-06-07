package com.example.viz.nextagram;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by viz on 2015. 3. 25..
 */
public class Proxy {

    private String serverUrl;
    private SharedPreferences pref;
    private Context context;


    public Proxy(Context context) {
        this.context = context;
        String prefName = context.getResources().getString(R.string.pref_name);
        pref = context.getSharedPreferences(prefName, context.MODE_PRIVATE);
        serverUrl = pref.getString(
                context.getResources().getString(R.string.server_url), "");
    }

    public String getJSON() {

        try {


            String prefArticleNumberKey = context.getResources().getString(R.string.pref_article_number);

            String articleNumber = pref.getString(prefArticleNumberKey,"0");

            String serverUrl = this.serverUrl + "loadData/?ArticleNumber="+articleNumber;
         //   String serverUrl = this.serverUrl + "loadData/";
            URL url = new URL(serverUrl);
            //URL url = new URL("http://127.0.0.1:5009/loadData");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setConnectTimeout(10 * 1000);
            conn.setReadTimeout(10 * 1000);

            conn.setRequestMethod("GET");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Accept-Charset", "UTF-8");
            conn.setRequestProperty("Cache-Control", "no-cache");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoInput(true);

            conn.connect();

            int status = conn.getResponseCode();
            Log.i("test","ProxyResponseCode: "+status);

            switch(status){
                case 200:
                case 201:
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while((line = br.readLine()) != null){
                        sb.append(line + "\n");
                    }
                    br.close();
                    return sb.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("test", "NETWORK ERROR: " + e);
        }

        return null;
        //TODO: responsecode가 200대 아닐 경우, 200대지만 빈값이 온 경우 어떻게 처리할까?
    }
    public ArrayList<ArticleDTO> getArticleDTO() {
        ArrayList<ArticleDTO> articleList = new ArrayList<ArticleDTO>();

        JSONArray jArr;
        int articleNumber;
        String title;
        String writer;
        String id;
        String content;
        String writeDate;
        String imgName;

        String jsonData = getJSON();
        ArticleDTO articleDTO;

        try {
            jArr = new JSONArray(jsonData);
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jObj = jArr.getJSONObject(i);
                articleNumber = jObj.getInt("ArticleNumber");
                title = jObj.getString("Title");
                writer = jObj.getString("Writer");
                id = jObj.getString("Id");
                content = jObj.getString("Content");
                writeDate = jObj.getString("WriteDate");
                imgName = jObj.getString("ImgName");

                articleDTO = new ArticleDTO(articleNumber, title, writer, id, content, writeDate, imgName);
                articleList.add(articleDTO);
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return articleList;
    }
}