package com.example.android.pathfinder.Database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by kenm on 3/15/2016.
 */
public class RouteProvider extends ContentProvider {

    private static String PROVIDER_NAME = "com.example.android.pathfinder.provider";
    private static final String TABLE_NAME = "pathfinderroutes";
    private static String  URL = "content://" + PROVIDER_NAME + "/" + TABLE_NAME;

    public static final Uri CONTENT_URI = Uri.parse(URL);
    private SQLiteDatabase mSqLiteDatabase;
    private SqliteHelper mSqliteHelper;

    private static final String DATABASE_NAME = "pathfinder";
    private static final int DATABASE_VERSION = 1;

    public static final String idField = "_id";
    public static final String startLatField = "startLatField";
    public static final String startLonField = "startLonField";
    public static final String endLatField = "endLatField";
    public static final String endLonField = "endLonField";
    public static final String startAddressField = "startAddressField";
    public static final String endAddressField = "endAddressField";

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mSqliteHelper = new SqliteHelper(context);
        mSqLiteDatabase = mSqliteHelper.getWritableDatabase();

        return (mSqLiteDatabase != null);
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();
        sqLiteQueryBuilder.setTables(TABLE_NAME);

        Cursor cursor = sqLiteQueryBuilder.query(mSqLiteDatabase, projection, selection, selectionArgs,
                null, null, null);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long rowID = mSqLiteDatabase.insert(TABLE_NAME, "", values);
        if (rowID > 0)
        {
            Uri _uri = ContentUris.withAppendedId(uri, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);

            return _uri;
        }

        throw new SQLException("Unable to add a new route record into " + uri);

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = mSqLiteDatabase.delete(TABLE_NAME, selection, selectionArgs);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    // Inner helper class that does the actual chores of dealing with the sqlite database
    private static class SqliteHelper extends SQLiteOpenHelper {
        SqliteHelper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase)
        {
            String sql ="CREATE TABLE " + TABLE_NAME + " ("
                    + idField + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + startAddressField + " TEXT NOT NULL,"
                    + endAddressField + " TEXT NOT NULL,"
                    + startLatField + " DOUBLE NOT NULL,"
                    + startLonField + " DOUBLE NOT NULL,"
                    + endLatField + " DOUBLE NOT NULL,"
                    + endLonField + " DOUBLE NOT NULL"
                    + ")";

            sqLiteDatabase.execSQL(sql);
        }
        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " +  TABLE_NAME);
            onCreate(sqLiteDatabase);
        }
    }
}
