package com.example.viz.nextagram;

import android.graphics.Bitmap;
import android.util.LruCache;

/**
 * Created by viz on 2015. 4. 8..
 */
public class ImageLoader {
    private static final ImageLoader instance = new ImageLoader();
    private final LruCache<String, Bitmap> mMemoryCache;

    public ImageLoader() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    public static ImageLoader getInstance(){
        return instance;
    }

    public Bitmap get(String imagePath){
        return mMemoryCache.get(imagePath);
    }

    public void put(String imagePath, Bitmap bitmap){
        mMemoryCache.put(imagePath, bitmap);
    }
}
