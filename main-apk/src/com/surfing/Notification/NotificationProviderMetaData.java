package com.surfing.Notification;

import android.net.Uri;
import android.provider.BaseColumns;

public class NotificationProviderMetaData {
	public static final String AUTHORIY = "com.surfing.Notification.NotificationProvider"; 
    public static final String DATABASE_NAME = "NotificationDataBase.db"; 
    public final static String NOTIFICATION_ID = "_id";
	public final static String NOTIFICATION_TITLE = "title";
	public final static String NOTIFICATION_CONTENT = "content";
	public final static String NOTIFICATION_USER = "user";
	public final static String NOTIFICATION_READFLAG = "is_read";
	public static final int DATABASE_VERSION = 309;
    public final static String mNotifictionTab = "Notifications";
     
    public static final class UserTableMetaData implements BaseColumns { 
        public static final String TABLE_NAME = "Notifications"; 
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORIY + "/Notifications"); 
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.mycontentprovider.Notifications"; 
        public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/vnd.mycontentprovider.Notifications"; 
    } 
}
