package com.example.viz.nextagram;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class WritingArticleView extends Activity implements View.OnClickListener {

    private static final int REQUEST_PHOTO_ALBUM = 1;
    private EditText etWriter;
    private EditText etTitle;
    private EditText etContent;
    private ImageButton ibPhoto;
    private ProgressDialog progressDialog;
    private String filePath;
    private String fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);

        etWriter = (EditText) findViewById(R.id.write_writer);
        etTitle = (EditText) findViewById(R.id.write_title);
        etContent = (EditText) findViewById(R.id.write_content);
        ibPhoto = (ImageButton) findViewById(R.id.write_image_button);
        Button buUpload = (Button) findViewById(R.id.write_upload_button);

        ibPhoto.setOnClickListener(this);
        buUpload.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.write_image_button:
                Log.e("button clicked", "ok");

                Intent intent = new Intent(Intent.ACTION_PICK);

                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_PHOTO_ALBUM); // 두번째 인자 - Request Code

                break;

            case R.id.write_upload_button:

                progressDialog = ProgressDialog.show(WritingArticleView.this, "", "업로드중입니다...");

                String ID = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
                String DATE = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA).format(new Date());

                ArticleDTO articleDTO = new ArticleDTO(
                        0, // 게시글 번호, 서버에서 붙여주는 번호이므로 숫자는 중요하지 않음
                        etTitle.getText().toString(),
                        etWriter.getText().toString(),
                        ID,
                        etContent.getText().toString(),
                        DATE,
                        fileName);

                ArticleWritingProxy.uploadArticle(articleDTO, filePath,
                        new AsyncHttpResponseHandler() {
                            @Override
                            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                                Log.e("uploadArticle", "success: " + i);
                                progressDialog.cancel();
                                Toast.makeText(getApplicationContext(), "onSuccess", Toast.LENGTH_SHORT).show();
                                finish(); // Activity 종료됨
                            }

                            @Override
                            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                                Log.e("uploadArticle", "fail: " + i);
                                progressDialog.cancel();
                                Toast.makeText(getApplicationContext(), "onFailure", Toast.LENGTH_SHORT).show();
                            }
                        });

                // TODO: 파일명이 그대로 서버에 올라가고 있다. 같은 이름의 파일 올라가면 어떻게 될까? ID를 어떻게 암호화한 다음 파일 앞에 붙여서 유니크하게 만들어주자

                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            if (requestCode == REQUEST_PHOTO_ALBUM) {
                Uri uri = getRealPathUri(data.getData());
                filePath = uri.toString();
                fileName = uri.getLastPathSegment();

                Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                Log.e("image original path: ", filePath);
                ibPhoto.setImageBitmap(bitmap);
            }
        } catch (Exception e) {
            Log.e("getImageFromLocal", "fail: " + e);
            // 사용자가 사진을 선택하다가 취소하면 null값 리턴될 수 있기 때문에 try catch 해줌
            // 너무 큰 사진파일의 경우 OutOfMemory 발생할 수 있으므로 작은 사진 파일을 업로드해주자
        }
    }

    // 다른 application에서 이미지를 선택하면 intent를 넘겨주고
    // intent.getData()하면 Content URI를 받아온다
    // 실제 주소가 아닌 Content Provider를 통하는 주소이기 때문에
    // 파일 업로드를 위해 실제 주소로 바꿔줘야 한다
    // ContentResolver를 사용해 이 일을 처리!
    private Uri getRealPathUri(Uri uri) {
        Log.e("content provider uri", uri.toString());
        Uri filePathUri = uri;
        if (uri.getScheme().compareTo("content") == 0) {
            Cursor cursor = getApplicationContext().getContentResolver().query(uri, null, null, null, null);
            if (cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                filePathUri = Uri.parse(cursor.getString(column_index));
            }
            cursor.close();
        }
        return filePathUri;
    }
}
