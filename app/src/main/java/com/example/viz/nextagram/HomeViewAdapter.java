package com.example.viz.nextagram;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.lang.ref.WeakReference;

public class HomeViewAdapter extends CursorAdapter {

    private Context mContext;
    private Cursor mCursor;
    private int mLayoutResourceId;
    private LayoutInflater mLayoutInflater;
    private SharedPreferences pref;

    static class ViewHolderItem {
        ImageView imageView;
        TextView tvWriter;
        TextView tvTitle;
        int articleNumber;
    }

    public HomeViewAdapter(Context context, Cursor cursor, int layoutResourceId) {
        super(context, cursor, layoutResourceId);

        this.mContext = context;
        this.mCursor = cursor;
        this.mLayoutResourceId = layoutResourceId;
        pref = context.getSharedPreferences(context.getString(R.string.pref_name), context.MODE_PRIVATE);

        mLayoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View row = mLayoutInflater.inflate(mLayoutResourceId, parent, false);

        ViewHolderItem viewHolder = new ViewHolderItem();

        viewHolder.imageView = (ImageView) row.findViewById(R.id.customlist_imageview);
        viewHolder.tvTitle = (TextView) row.findViewById(R.id.customlist_textview1);
        viewHolder.tvWriter = (TextView) row.findViewById(R.id.customlist_textview2);

        row.setTag(viewHolder);

        return row;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        String writerName = cursor.getString(
                cursor.getColumnIndex(NextagramContract.Article.WRITER_NAME));
        String title = cursor.getString(
                cursor.getColumnIndex(NextagramContract.Article.TITLE));
        String imgName = cursor.getString(
                cursor.getColumnIndex(NextagramContract.Article.IMG_NAME));
        String articleNumber = cursor.getString(
                cursor.getColumnIndex(NextagramContract.Article.ARTICLE_NUMBER));

        ViewHolderItem viewHolder = (ViewHolderItem) view.getTag();
        viewHolder.articleNumber = Integer.parseInt(articleNumber);
        viewHolder.tvWriter.setText(writerName);
        viewHolder.tvTitle.setText(title);

        String imgPath = context.getFilesDir().getPath() + "/" + imgName;

        WeakReference<ImageView> imageViewReference = new WeakReference<ImageView>(viewHolder.imageView);

        Bitmap bitmap = ImageLoader.getInstance().get(imgPath);
        if (bitmap != null) {
            Log.i("ImageLoader", "getCache");
            imageViewReference.get().setImageBitmap(bitmap);
        } else {
            Log.i("ImageLoader", "putCache");
            File imgLoadPath = new File(imgPath);

            if (imgLoadPath.exists()) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 4;
                options.inPurgeable = true;

                bitmap = BitmapFactory.decodeFile(imgPath, options);
                ImageLoader.getInstance().put(imgPath, bitmap);

                imageViewReference.get().setImageBitmap(bitmap);
            } else {
                Log.e(HomeViewAdapter.class.getSimpleName(), "file does not exist!");
            }
        }
    }
}
// extends ArrayAdapter<ArticleDTO> {
//    private Context context;
//    private int layoutResourceId;
//    private ArrayList<ArticleDTO> articleDTOData;
//
//    public HomeViewAdapter(Context context, int layoutResourceId, ArrayList<ArticleDTO> articleDTOData) {
//        super(context, layoutResourceId, articleDTOData);
//        this.context = context;
//        this.layoutResourceId = layoutResourceId;
//        this.articleDTOData = articleDTOData;
//    }
//
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        View row = convertView;
//
//        // 해당 row의 레이아웃 그려주기
//        if (row == null) {
//            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
//            row = inflater.inflate(layoutResourceId, parent, false);
//        }
//
//        // 해당 row의 텍스트들 넣어주기
//        TextView tvTitle = (TextView) row.findViewById(R.id.customlist_textview1);
//        TextView tvContent = (TextView) row.findViewById(R.id.customlist_textview2);
//        tvTitle.setText(articleDTOData.get(position).getTitle());
//        tvContent.setText(articleDTOData.get(position).getContent());
//
//        // 해당 row의 이미지 넣어주기
//        ImageView imageView = (ImageView) row.findViewById(R.id.customlist_imageview);
//        WeakReference<ImageView> imageViewReference = new WeakReference<ImageView>(imageView);
//
//        String imgPath = context.getFilesDir().getPath() + "/" + articleDTOData.get(position).getImgName();
//
//        Bitmap bitmap = ImageLoader.getInstance().get(imgPath);
//        if (bitmap != null) {
//            Log.e("result", "getCache");
//            imageViewReference.get().setImageBitmap(bitmap);
//        } else {
//            Log.e("result", "putCache");
//            File imgLoadPath = new File(imgPath);
//
//            if (imgLoadPath.exists()) {
//                BitmapFactory.Options options = new BitmapFactory.Options();
//                options.inSampleSize = 2;
//                options.inPurgeable = true;
//
//                bitmap = BitmapFactory.decodeFile(imgPath, options);
//                ImageLoader.getInstance().put(imgPath, bitmap);
//
//                // Bitmap resized = Bitmap.createScaledBitmap(bitmap, 1000, 1000, true);
//                imageViewReference.get().setImageBitmap(bitmap);
//            } else {
//                Log.e("viewImage", "fail: file not found");
//            }
//        }
//
////        try {
////            InputStream is = context.getAssets().open(articleData.get(position).getImgName());
////            Drawable d = Drawable.createFromStream(is, null);
////            imageView.setImageDrawable(d);
////        } catch (IOException e) {
////            e.printStackTrace();
////        }
//
//        return row;
//    }
//}
