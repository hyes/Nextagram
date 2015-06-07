package com.example.viz.nextagram;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by viz on 2015. 3. 30..
 */
public class ArticleWritingProxy {

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void uploadArticle(ArticleDTO articleDTO, String filePath, AsyncHttpResponseHandler responseHandler) {
        RequestParams params = new RequestParams();
        params.put("title", articleDTO.getTitle());
        params.put("writer", articleDTO.getWriter());
        params.put("id", articleDTO.getId());
        params.put("content", articleDTO.getContent());
        params.put("writeDate", articleDTO.getWriteDate());
        params.put("imgName", articleDTO.getImgName());

        try {
            params.put("uploadedfile", new File(filePath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        client.post("http://192.168.56.1:5009/upload", params, responseHandler); // TODO: 싱글톤으로 주소 빼기
    }
}
