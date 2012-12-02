package com.surfing.channel;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class PhotoProviders extends ContentProvider
{
    public static final int PHOTO_INFO = 1;
    protected SQLiteDatabase mDb = null;
    private PhotoDatabaseHelper mDbHelper = null;
    public static final UriMatcher uriMatcher; 
    private Context mContext = null;
    static { 
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH); 
        uriMatcher.addURI(PhotoProviderData.AUTHORIY, "photo", PHOTO_INFO); 
    } 
    @Override
    public int delete(Uri arg0, String where, String[] whereArgs)
    {
       return mDb.delete(PhotoProviderData.PHOTO_TABLE, where, whereArgs);
    }

    @Override
    public String getType(Uri arg0)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values)
    {
        switch (uriMatcher.match(uri)){
            case PHOTO_INFO:{
                mDb.insert(PhotoProviderData.PHOTO_TABLE,null,values);
            }
            break;
        }
        return uri;
    }

    @Override
    public boolean onCreate()
    {
        mContext = this.getContext();
        mDbHelper = (PhotoDatabaseHelper)PhotoDatabaseHelper.getInstance(mContext);
        mDb = mDbHelper.getWritableDatabase();
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String orderBy)
    {
        switch (uriMatcher.match(uri)){
            case PHOTO_INFO:{
                return mDb.query(PhotoProviderData.PHOTO_TABLE, projection, selection, selectionArgs, null, null, orderBy);
            }
            default:
                break;
        }
        return null;
    }

    @Override
    public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3)
    {
        // TODO Auto-generated method stub
        return 0;
    }

}
