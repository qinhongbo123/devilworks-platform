package com.surfing.channel;

import com.surfing.contacts.ContactsProviderMetaData;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class PhotoDatabaseHelper extends SQLiteOpenHelper
{
    private final static String TAG = "PhotoDatabaseHelper";
    private static PhotoDatabaseHelper  sSingleton = null;
    
    private String createPhotoSql =  "CREATE TABLE IF NOT EXISTS "+ PhotoProviderData.PHOTO_TABLE+" (" +
            PhotoProviderData.PHOTO_ID+" INTEGER PRIMARY KEY AUTOINCREMENT," +
            PhotoProviderData.PHOTO_URL +" TEXT,"+
            PhotoProviderData.PHOTO_PATH + " TEXT)";
    private PhotoDatabaseHelper(Context context, String name,
            CursorFactory factory, int version)
    {
        super(context, name, factory, version);
        // TODO Auto-generated constructor stub
    }
    public synchronized static PhotoDatabaseHelper getInstance(Context context){
        if(sSingleton == null){
            sSingleton = new PhotoDatabaseHelper(context,
                                            PhotoProviderData.DATABASE_NAME,
                                            null,
                                            PhotoProviderData.DATABASE_VERSION);
        }
        return sSingleton;
    }
    @Override
    public void onCreate(SQLiteDatabase db)
    {
        super.onOpen(db);
        db.execSQL(createPhotoSql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub

    }

}
