package com.surfing.Notification;

import com.surfing.Notification.NotificationProviderMetaData.UserTableMetaData;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public class NotificationProvider extends ContentProvider {
	private final static String TAG = "NotificationProvider";
	private NotificationDatabaseHelper mDbHelper = null;
	protected SQLiteDatabase mDb = null;
	private Context mContext = null;
	public static final UriMatcher uriMatcher; 
	public static final int NOTIFICATION_INFO = 1;
	static { 
	        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH); 
	        uriMatcher.addURI(NotificationProviderMetaData.AUTHORIY, "Notifications", NOTIFICATION_INFO); 
	    } 
	@Override
	public int delete(Uri uri, String selection, String[] args) {
		switch (uriMatcher.match(uri)){
		case NOTIFICATION_INFO:{
			return mDb.delete(NotificationProviderMetaData.mNotifictionTab,selection,args);
		}
		}
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)){
		case NOTIFICATION_INFO:{
			return UserTableMetaData.CONTENT_TYPE;
		}
		}
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		switch (uriMatcher.match(uri)){
			case NOTIFICATION_INFO:{
				mDb.insert(NotificationProviderMetaData.mNotifictionTab,null,values);
			}
		}
		return uri;
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		mContext = this.getContext();
		mDbHelper = (NotificationDatabaseHelper)NotificationDatabaseHelper.getInstance(mContext);
		mDb = mDbHelper.getWritableDatabase();
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] args,
			String orders) {
		switch (uriMatcher.match(uri)){
		case NOTIFICATION_INFO:{
			return mDb.query(NotificationProviderMetaData.mNotifictionTab, projection, selection, args, null, null, orders);
		}
		}
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] args) {
		switch (uriMatcher.match(uri)){
		case NOTIFICATION_INFO:{
			return mDb.update(NotificationProviderMetaData.mNotifictionTab, values, selection, args);
		}
		}
		return 0;
	}

}
