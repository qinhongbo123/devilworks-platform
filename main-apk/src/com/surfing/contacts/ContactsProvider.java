package com.surfing.contacts;

import com.surfing.contacts.ContactsProviderMetaData.ContactsData;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public class ContactsProvider extends ContentProvider {
	private final static String TAG = "ContactsProvider";
	private ContactsDatabaseHelper mDbHelper = null;
	protected SQLiteDatabase mDb = null;
	private Context mContext = null;
	public static final UriMatcher uriMatcher; 
	public static final int CONTACTS_INFO = 1;
	public static final int PHONE_INFO = 2;
	static { 
	        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH); 
	        uriMatcher.addURI(ContactsProviderMetaData.AUTHORIY, "Contacts", CONTACTS_INFO); 
	        //uriMatcher.addURI(ContactsProviderMetaData.AUTHORIY, "Contacts", PHONE_INFO); 
	    } 
	@Override
	public int delete(Uri uri, String selection, String[] args) {
		switch (uriMatcher.match(uri)){
		case CONTACTS_INFO:{
			return mDb.delete(ContactsProviderMetaData.mContactsTab,selection,args);
		}
		case PHONE_INFO:{
		    return mDb.delete(ContactsProviderMetaData.mPhoneTab,selection,args);
		}
		}
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)){
		case CONTACTS_INFO:{
			return ContactsData.CONTENT_TYPE;
		}
		case PHONE_INFO:{
		    return ContactsData.CONTENT_PHONE_TYPE;
		}
		}
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		switch (uriMatcher.match(uri)){
			case CONTACTS_INFO:{
				mDb.insert(ContactsProviderMetaData.mContactsTab,null,values);
			}
			break;
			case PHONE_INFO:{
			    mDb.insert(ContactsProviderMetaData.mPhoneTab,null,values);
			}
			break;
		}
		return uri;
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		mContext = this.getContext();
		mDbHelper = (ContactsDatabaseHelper)ContactsDatabaseHelper.getInstance(mContext);
		mDb = mDbHelper.getWritableDatabase();
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] args,
			String orders) {
		switch (uriMatcher.match(uri)){
		case CONTACTS_INFO:{
			return mDb.query(ContactsProviderMetaData.mContactsTab, projection, selection, args, null, null, orders);
		}
		case PHONE_INFO:{
		    return mDb.query(ContactsProviderMetaData.mPhoneTab, projection, selection, args, null, null, orders);
		}
		}
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] args) {
		switch (uriMatcher.match(uri)){
		case CONTACTS_INFO:{
			return mDb.update(ContactsProviderMetaData.mContactsTab, values, selection, args);
		}
		}
		return 0;
	}

}
