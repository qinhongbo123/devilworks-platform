package com.surfing.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class SaveRssFile {
	private static final String TAG = "SaveRssFile";
	private Context mContext;
	public SaveRssFile(Context context){
		mContext = context;
	}
	public void SaveFile(String filestring,String filename) throws Exception {
		String path = mContext.getFilesDir().getPath()+"/";
		Log.i(TAG,"the file Path is "+path);
		
        byte[] data = filestring.getBytes();
        File file = new File(path + filename);  
        FileOutputStream fos = new FileOutputStream(file);  
        fos.write(data);
        fos.close();
    }
}
