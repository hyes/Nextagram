package com.example.viz.nextagram;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;

import java.util.Locale;

/**
 * Created by hyes on 2015. 6. 7..
 */
public class NextagramProvider extends ContentProvider {

    private Context context;
    private SQLiteDatabase database;
    private final String TABLE_NAME = "Articles";

    private static final int ARTICLE_LIST = 1;
    private static final int ARTICLE_ID = 2;
    private static final UriMatcher URI_MATCHER;

    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(NextagramContract.AUTHORITY, "Article", ARTICLE_LIST);
        URI_MATCHER.addURI(NextagramContract.AUTHORITY, "Article/#", ARTICLE_ID);
    }

    private void SQLiteInitialize() {
        database = context.openOrCreateDatabase("nextagram.db", SQLiteDatabase.CREATE_IF_NECESSARY, null);
        database.setLocale(Locale.getDefault());
        database.setVersion(1);
    }

    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS " +
                TABLE_NAME +
                "(ID integer primary key autoincrement," +
                "_id integer UNIQUE not null," +
                "Title text not null," +
                "WriterName text not null," +
                "WriterID text not null," +
                "Content text not null," +
                "WriteDate text not null," +
                "ImgName text not null);";
        database.execSQL(sql);
    }

    private boolean isTableExist() {
        String searchTable = "select DISTINCT tbl_name from " +
                "sqlite_master where tbl_name = '" + TABLE_NAME + "';";
        Cursor cursor = database.rawQuery(searchTable, null);

        if (cursor.getCount() == 0) {
            cursor.close();
            return false;
        }

        cursor.close();
        return true;
    }

    @Override
    public boolean onCreate() {
        this.context = getContext();
        SQLiteInitialize();
        if (!isTableExist()) {
            createTable();
        }

        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        switch (URI_MATCHER.match(uri)) {
            case ARTICLE_LIST:
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = NextagramContract.Article.SORT_ORDER_DEFAULT;
                }
                break;
            case ARTICLE_ID:
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = NextagramContract.Article.SORT_ORDER_DEFAULT;
                }
                if (selection == null) {
                    selection = "_id = ?";
                    selectionArgs = new String[]{uri.getLastPathSegment()};
                }
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        Cursor cursor = database.query(TABLE_NAME,
                NextagramContract.Article.PROJECTION_ALL, selection,
                selectionArgs, null, null, sortOrder);

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        if (URI_MATCHER.match(uri) != ARTICLE_LIST) {
            throw new IllegalArgumentException("Insertion을 지원하지 않는 URI입니다 : " + uri);
        }

        if (URI_MATCHER.match(uri) == ARTICLE_LIST) {
            long id = database.insert("Article", null, values);

            Uri itemUri = ContentUris.withAppendedId(uri, id);
            getContext().getContentResolver().notifyChange(itemUri, null);

            return itemUri;
        }

        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }
}
