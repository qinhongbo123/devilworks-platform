package com.surfing.channel;

import android.net.Uri;
import android.provider.BaseColumns;

public class PhotoProviderData
{
    public static final String AUTHORIY = "com.surfing.channel.PhotoProviders"; 
    public static final String DATABASE_NAME = "PhotoInfo.db"; 
    public static final int DATABASE_VERSION = 400;
    public static final String PHOTO_ID = "_id";
    public static final String PHOTO_URL = "photo_url";
    public static final String PHOTO_PATH = "photo_path";
    public static final String PHOTO_TABLE = "PhotoTable";
    
    public static final class PhotoData implements BaseColumns{
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORIY + "/photo"); 
    }
}
