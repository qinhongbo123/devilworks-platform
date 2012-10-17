package com.surfing.contacts;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

 class ContactsDatabaseHelper extends SQLiteOpenHelper {
	private final static String TAG = "ContactsDatabaseHelper";
	private static ContactsDatabaseHelper  sSingleton = null;
	
	
	
	private final static String mContactTabCreate = "CREATE TABLE IF NOT EXISTS "+ ContactsProviderMetaData.mContactsTab+" (" +
			ContactsProviderMetaData.CONTACTS_ID+" INTEGER PRIMARY KEY AUTOINCREMENT," +
			ContactsProviderMetaData.CONTACTS_NAME +" TEXT,"+
			ContactsProviderMetaData.CONTACTS_PHONE1 + " TEXT ,"+
			ContactsProviderMetaData.CONTACTS_PHONE2 + " TEXT ,"+
			ContactsProviderMetaData.CONTACTS_PHONE3 + " TEXT ,"+
			ContactsProviderMetaData.CONTACTS_DEPART + " TEXT ,"+
			ContactsProviderMetaData.CONTACTS_COMPANY + " TEXT ,"+
			ContactsProviderMetaData.CONTACTS_USER+" TEXT,"+
			ContactsProviderMetaData.CONTACTS_TITLE +" TEXT)";
	private final static String mPhoneTabCreate = "CREATE TABLE IF NOT EXISTS " + ContactsProviderMetaData.mPhoneTab+" (" +
	        ContactsProviderMetaData.PHONE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"+
	        ContactsProviderMetaData.CONTACTS_PHONE_ID + " INTEGER NOT NULL, "+
	        ContactsProviderMetaData.CONTACTS_USER+" TEXT)"; 
	
	public ContactsDatabaseHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
		
	}
	public synchronized static ContactsDatabaseHelper getInstance(Context context){
		if(sSingleton == null){
			sSingleton = new ContactsDatabaseHelper(context,
											ContactsProviderMetaData.DATABASE_NAME,
											null,
											ContactsProviderMetaData.DATABASE_VERSION);
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
		db.execSQL(mContactTabCreate);
		db.execSQL(mPhoneTabCreate);
	}
	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}

}
