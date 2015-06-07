package com.example.viz.nextagram;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.lang.ref.WeakReference;


public class ArticleView extends Activity {

    private ImageView ivImage;
    private WeakReference<ImageView> imageViewReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);

        TextView tvTitle = (TextView) findViewById(R.id.viewer_title);
        TextView tvWriter = (TextView) findViewById(R.id.viewer_writer);
        TextView tvContent = (TextView) findViewById(R.id.viewer_content);
        TextView tvWriteTime = (TextView) findViewById(R.id.viewer_write_time);

        ivImage = (ImageView) findViewById(R.id.viewer_image_view);
        imageViewReference = new WeakReference<ImageView>(ivImage);

        String articleNumber = getIntent().getExtras().getString("ArticleNumber");

        ProviderDao dao = new ProviderDao(getApplicationContext());
        ArticleDTO articleDTO = dao.getArticleByArticleNumber(Integer.parseInt(articleNumber));

        tvTitle.setText(articleDTO.getTitle());
        tvWriter.setText(articleDTO.getWriter());
        tvContent.setText(articleDTO.getContent());
        tvWriteTime.setText(articleDTO.getWriteDate());

        try {
            // 이미지 파일 inputstream -> drawable 로 하는 방법
            // InputStream is = getApplicationContext().getAssets().open(article.getImgName());
            // Drawable d = Drawable.createFromStream(is, null);
            // ivImage.setImageDrawable(d);

            String imgPath = getFilesDir().getPath() + "/" + articleDTO.getImgName();

            Bitmap bitmap = ImageLoader.getInstance().get(imgPath);
            if (bitmap != null) {
                Log.e("result", "getCache");
                imageViewReference.get().setImageBitmap(bitmap);
            } else {
                Log.e("result", "putCache");
                File imgLoadPath = new File(imgPath);

                if (imgLoadPath.exists()) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 2; // n개의 픽셀을 1개의 픽셀로 decode한다는 뜻
                    // 2의 지수값을 넣는 것이 decode 속도가 빠르다
                    options.inPurgeable = true; // 롤리팝부터 메모리구조 바뀌면서 deprecated됨
                    // 메모리 부족할 때 시스템이 가져다 쓸 수 있다고 체크해주는 것

                    bitmap = BitmapFactory.decodeFile(imgPath, options);
                    ImageLoader.getInstance().put(imgPath, bitmap);
                    // imageLoader에 저장하는 건 inSampleSize로 줄인 것, resized된 걸 넣어줄 필요는 없다

                    // Bitmap resized = Bitmap.createScaledBitmap(bitmap, 1000, 1000, true);
                    // inSampleSize를 조정함으로써 파일 크기가 줄어들긴 했지만, 동시에 사이즈도 줄어들었다
                    // 따라서 사용할 크기에 맞춰 resize 해줘야 한다 (bitmap, width, height, filter)
                    // width와 height는 픽셀 값임
                    // TODO: filter가 뭐지? 무슨 역할?

                    imageViewReference.get().setImageBitmap(bitmap);
                } else {
                    Log.e("viewImage", "fail: file not found");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_viewer, menu);
        return true;
    }

    // bitmap recycle을 활용
//    @Override
//    protected void onDestroy() {
//        Drawable d = ivImage.getDrawable();
//        if (d instanceof BitmapDrawable) {
//            Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
//            bitmap.recycle();
//            bitmap = null;
//        }
//        super.onDestroy();
//    }
}
