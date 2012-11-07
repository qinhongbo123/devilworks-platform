package com.surfing.channel;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.surfing.httpconnection.ImageDownloader.FlushedInputStream;


public class NetImitate
{
    private static NetImitate netImitate;
    private static final int THREAD_POOL_SIZE = 2;
    private ExecutorService mTheadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    private HashMap<String,Object> mImageUrlCache = new HashMap<String, Object>();
    Context context;

    private NetImitate(Context context)
    {
        this.context = context;
    }

    public static NetImitate getInstance(Context context)
    {
        if (netImitate == null)
        {
            netImitate = new NetImitate(context);
        }
        return netImitate;
    }
    public void downloadAndBindImage(final String url,final ImageCallback callback)
    {
    	
    	final Handler handler = new Handler() {  
            public void handleMessage(Message message) {  
            	callback.imageLoaded((Bitmap) message.obj, url);  
            }  
        };  
        if(mImageUrlCache.containsKey(url)){
            ContentResolver Resolver = context.getContentResolver();
            Cursor cursor = Resolver
                    .query(PhotoProviderData.PhotoData.CONTENT_URI,
                            new String[] { PhotoProviderData.PHOTO_PATH },
                            PhotoProviderData.PHOTO_URL + "='" + url + "'",
                            null, null);
            if (cursor != null)
            {
                if(cursor.getCount() > 0){
                    cursor.moveToFirst();
                    String path = cursor.getString(0);
                    File img = new File(path);
                    if (img.exists())
                    {
                        return;
                    } 
                }
                cursor.close();
                cursor = null;
            }
        }
         mImageUrlCache.put(url,"");
         mTheadPool.execute(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Thread.sleep(500);
                } catch (InterruptedException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                Bitmap bitmap = downloadBitmap(url);
               // callback.imageLoaded(bitmap,url);
                Message message = handler.obtainMessage(0, bitmap);  
                handler.sendMessage(message); 
               
            }
        });

    }

    /**
     * 本方法模拟网络下载数据,sleep是用来模拟数据正在下载所需要的时间
     * 
     * @see SuperListView#listLock
     * @param begin
     * @param lock
     *            {@link SuperListView#listLock}
     * @return
     */
   
    Bitmap downloadBitmap(String url)
    {

        // AndroidHttpClient is not allowed to be used from the main thread
        final HttpClient client = new DefaultHttpClient();
        
        final HttpGet getRequest = new HttpGet(url);

        try
        {
            HttpResponse response = client.execute(getRequest);
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK)
            {
                return null;
            }
            final HttpEntity entity = response.getEntity();
            if (entity != null)
            {
                InputStream inputStream = null;
                try
                {
                	Bitmap bitmap = null;
                    inputStream = entity.getContent();
                    String status = Environment.getExternalStorageState();
                    if (status.equals(Environment.MEDIA_MOUNTED))
                    {
                        BitmapFactory.Options opts = null;
                        String path = null;
                        bitmap = BitmapFactory.decodeStream(new FlushedInputStream(inputStream));
                        path = SavePhoto(url, bitmap);

                    }
                    else
                    {
                        bitmap = BitmapFactory.decodeStream(new FlushedInputStream(inputStream));
                    }
                    // return BitmapFactory.decodeStream(new
                    // FlushedInputStream(inputStream));
                    inputStream.close();
                    return bitmap;
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                catch (OutOfMemoryError e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    if (inputStream != null)
                    {
                        inputStream.close();
                    }
                    entity.consumeContent();
                }
            }
        }
        catch (IOException e)
        {
            getRequest.abort();
        }
        catch (IllegalStateException e)
        {
            getRequest.abort();
        }
        catch (Exception e)
        {
            getRequest.abort();
        }
        finally
        {

        }
        return null;
    }

    private String SavePhoto(String url, Bitmap bitmap) throws IOException
    {
        if(bitmap == null){
            return null;
        }
        String savePath = Environment.getExternalStorageDirectory() + "/DownFile/photo/";
        File dirFile = new File(savePath);
        if (!dirFile.exists())
        {
            dirFile.mkdirs();
        }
        Log.i("ImageDownloader", "savepath = " + savePath);
        long time = Calendar.getInstance().getTimeInMillis();
        String timeString = Long.toString(time);
        savePath += timeString + ".png";
        File myCaptureFile = new File(savePath);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, bos);
        bos.flush();
        bos.close();
        // update database
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(PhotoProviderData.PHOTO_PATH, savePath);
        values.put(PhotoProviderData.PHOTO_URL, url);
        resolver.insert(PhotoProviderData.PhotoData.CONTENT_URI, values);
        return savePath;
    }
    public static Bitmap getImagefromDatabases(Context context,String url){
        ContentResolver Resolver = context.getContentResolver();
        Bitmap bitmap = null;
        Cursor cursor = Resolver
                .query(PhotoProviderData.PhotoData.CONTENT_URI,
                        new String[] { PhotoProviderData.PHOTO_PATH },
                        PhotoProviderData.PHOTO_URL + "='" + url + "'",
                        null, null);
        if ((cursor != null) && (cursor.getCount() > 0))
        {
            cursor.moveToFirst();
            String path = cursor.getString(0);
            File img = new File(path);
            if (img.exists())
            {
                try
                {
                    bitmap = BitmapFactory.decodeFile(path);
                } catch (OutOfMemoryError e)
                {
                    bitmap = null;
                }
            }

        }
        if (cursor != null)
        {
            cursor.close();
        }
        return bitmap;
    }
    public interface ImageCallback {  
        public void imageLoaded(Bitmap bitmap, String imageUrl);  
    }  
}
