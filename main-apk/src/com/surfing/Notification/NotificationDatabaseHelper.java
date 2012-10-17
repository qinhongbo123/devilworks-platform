package com.surfing.Notification;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

 class NotificationDatabaseHelper extends SQLiteOpenHelper {
	private final static String TAG = "NotificationDatabaseHelper";
	private static NotificationDatabaseHelper  sSingleton = null;
	
	
	
	private final static String mCreateNotificationTab = "CREATE TABLE IF NOT EXISTS "+ NotificationProviderMetaData.mNotifictionTab+" (" +
			NotificationProviderMetaData.NOTIFICATION_ID+" INTEGER PRIMARY KEY AUTOINCREMENT," +
			NotificationProviderMetaData.NOTIFICATION_USER +" TEXT," + 
			NotificationProviderMetaData.NOTIFICATION_TITLE+" TEXT," +
			NotificationProviderMetaData.NOTIFICATION_CONTENT+" TEXT," +
			NotificationProviderMetaData.NOTIFICATION_READFLAG+ " INTEGER NOT NULL DEFAULT 0)";
	
	public NotificationDatabaseHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
		
	}
	public synchronized static NotificationDatabaseHelper getInstance(Context context){
		if(sSingleton == null){
			sSingleton = new NotificationDatabaseHelper(context,
											NotificationProviderMetaData.DATABASE_NAME,
											null,
											NotificationProviderMetaData.DATABASE_VERSION);
		}
		return sSingleton;
	}
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void onOpen(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		super.onOpen(db);
		db.execSQL(mCreateNotificationTab);
	}
	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}

}
