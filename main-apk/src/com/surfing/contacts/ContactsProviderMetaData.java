package com.surfing.contacts;

import android.net.Uri;
import android.provider.BaseColumns;

public class ContactsProviderMetaData {
	public static final String AUTHORIY = "com.surfing.contacts.ContactsProvider"; 
    public static final String DATABASE_NAME = "ContactsDataBase_v02.db"; 
    public final static String CONTACTS_ID = "_id";
	public final static String CONTACTS_NAME = "name";
	public final static String CONTACTS_PHONE1 = "phoneNum1";
	public final static String CONTACTS_PHONE2 = "phoneNum2";
	public final static String CONTACTS_PHONE3 = "phoneNum3";
	public final static String CONTACTS_USER = "user";
	public final static String CONTACTS_DEPART = "department";
	public final static String CONTACTS_TITLE = "user_title";
	public final static String CONTACTS_COMPANY = "company";
	public static final int DATABASE_VERSION = 309;
    public final static String mContactsTab = "Contacts_v02";
    public final static String mPhoneTab = "Phones";
    public final static String PHONE_ID = "_id";
    public final static String CONTACTS_PHONE_ID = "contacts_id";
    
    public static final class ContactsData implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORIY + "/Contacts"); 
        public static final Uri CONTENT_PHONE_URI = Uri.parse("content://" + AUTHORIY + "/Phones"); 
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.mycontentprovider.Contacts"; 
        public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/vnd.mycontentprovider.Contacts"; 
        public static final String CONTENT_PHONE_TYPE = "vnd.android.cursor.dir/vnd.mycontentprovider.Phone"; 
    } 
}
