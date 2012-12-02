/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.surfing.httpconnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.surfing.R;
import com.surfing.channel.PhotoProviderData;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;

 /*** This helper class download images from the Internet and binds those with the
 * provided ImageView.
 * 
 * <p>
 * It requires the INTERNET permission, which should be added to your
 * application's manifest file.
 * </p>
 * 
 * A local cache of downloaded images is maintained internally to improve
 * performance.
 */
public class ImageDownloader
{
	private static final String LOG_TAG = "ImageDownloader";
	private int mDefaultRes_id = 0;
	private int mWidth = 0;
	private int mHeight = 0;
	private Context mConext = null;
	private boolean mBlArticle_title = false;


	/**
	 * Download the specified image from the Internet and binds it to the
	 * provided ImageView. The binding is immediate if the image is found in the
	 * cache and will be done asynchronously otherwise. A null bitmap will be
	 * associated to the ImageView if an error occurs.
	 * 
	 * @param url
	 *            The URL of the image to download.
	 * @param imageView
	 *            The ImageView to bind the downloaded image to.
	 */
	public void download(String url, ImageView imageView, int default_res_id, Context context)
	{
		mConext = context;
		mDefaultRes_id = default_res_id;
		resetPurgeTimer();
		if ((url == null) || "".equals(url))
		{
			imageView.setImageResource(default_res_id);
			return;
		}

		Bitmap bitmap = null;
		ContentResolver Resolver = mConext.getContentResolver();
		Cursor cursor = Resolver.query(PhotoProviderData.PhotoData.CONTENT_URI, new String[] { PhotoProviderData.PHOTO_PATH }, PhotoProviderData.PHOTO_URL + "='" + url + "'", null, null);
		if ((cursor != null) && (cursor.getCount() > 0))
		{
			cursor.moveToFirst();
			String path = cursor.getString(0);
			File img = new File(path);
			if (!img.exists())
			{
				forceDownload(url, imageView);
				return ;
			}
			Log.i(LOG_TAG, "the path = " + path);
			try
			{
				bitmap = BitmapFactory.decodeFile(path);
			}
			catch (OutOfMemoryError e)
			{
				bitmap = null;
				e.printStackTrace();
			}
		}
		if(cursor != null)
		{
		    cursor.close();
		}
		if (bitmap == null)
		{
			// imageView.setVisibility(View.INVISIBLE);
			forceDownload(url, imageView);
		}
		else
		{
			cancelPotentialDownload(url, imageView);
			imageView.setImageBitmap(bitmap);
		}
	}

	public void download(String url, ImageView imageView, int default_res_id, Context context, boolean artical_title)
	{
		mBlArticle_title = true;
		download(url, imageView, default_res_id, context);
	}

	public void download(String url, ImageView imageView, int default_res_id, int width, int height)
	{
		mWidth = width;
		mHeight = height;
		resetPurgeTimer();
		Bitmap bitmap = getBitmapFromCache(url);
		if (bitmap == null)
		{
			// imageView.setVisibility(View.INVISIBLE);
			forceDownload(url, imageView);
		}
		else
		{
			cancelPotentialDownload(url, imageView);
			imageView.setImageBitmap(bitmap);
		}
	}

	/*
	 * Same as download but the image is always downloaded and the cache is not
	 * used. Kept private at the moment as its interest is not clear. private
	 * void forceDownload(String url, ImageView view) { forceDownload(url, view,
	 * null); }
	 */

	/**
	 * Same as download but the image is always downloaded and the cache is not
	 * used. Kept private at the moment as its interest is not clear.
	 */
	private void forceDownload(String url, ImageView imageView)
	{
		// State sanity: url is guaranteed to never be null in
		// DownloadedDrawable and cache keys.
		if (url == null)
		{
			return;
		}
		try
		{
			if (cancelPotentialDownload(url, imageView))
			{
				BitmapDownloaderTask task = new BitmapDownloaderTask(imageView);
				DownloadedDrawable downloadedDrawable = new DownloadedDrawable(task);
				imageView.setImageDrawable(downloadedDrawable);
				imageView.setMinimumHeight(156);
				task.execute(url);

			}
		}
		catch (Exception e)
		{
			Log.i(LOG_TAG, "forceDownload Error");
		}

	}

	/**
	 * Returns true if the current download has been canceled or if there was no
	 * download in progress on this image view. Returns false if the download in
	 * progress deals with the same url. The download is not stopped in that
	 * case.
	 */
	private static boolean cancelPotentialDownload(String url, ImageView imageView)
	{
		BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);

		if (bitmapDownloaderTask != null)
		{
			String bitmapUrl = bitmapDownloaderTask.url;
			if ((bitmapUrl == null) || (!bitmapUrl.equals(url)))
			{
				bitmapDownloaderTask.cancel(true);
			}
			else
			{
				// The same URL is already being downloaded.
				return false;
			}
		}
		return true;
	}

	/**
	 * @param imageView
	 *            Any imageView
	 * @return Retrieve the currently active download task (if any) associated
	 *         with this imageView. null if there is no such task.
	 */
	private static BitmapDownloaderTask getBitmapDownloaderTask(ImageView imageView)
	{
		if (imageView != null)
		{
			Drawable drawable = imageView.getDrawable();
			if (drawable instanceof DownloadedDrawable)
			{
				DownloadedDrawable downloadedDrawable = (DownloadedDrawable) drawable;
				return downloadedDrawable.getBitmapDownloaderTask();
			}
		}
		return null;
	}

	Bitmap downloadBitmap(String url)
	{

		// AndroidHttpClient is not allowed to be used from the main thread
		final HttpClient client = new DefaultHttpClient();
		;
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
					inputStream = entity.getContent();
					String status = Environment.getExternalStorageState();
					if (status.equals(Environment.MEDIA_MOUNTED))
					{
						BitmapFactory.Options opts = null;
						Bitmap bitmap = null;
						String path = null;
						bitmap = BitmapFactory.decodeStream(new FlushedInputStream(inputStream));
						path = SavePhoto(url, bitmap);
						inputStream.close();
						if (mBlArticle_title)
						{
							opts = new BitmapFactory.Options();
							opts.inJustDecodeBounds = true;
							BitmapFactory.decodeFile(path, opts);
							opts.inSampleSize = computeSampleSize(opts, -1, 70*60);  
							opts.inJustDecodeBounds = false;
							bitmap = BitmapFactory.decodeStream(inputStream,null,opts);
							path = SavePhoto(url, bitmap);
							
						}else{
							bitmap = BitmapFactory.decodeStream(new FlushedInputStream(inputStream));
							path = SavePhoto(url, bitmap);
							
						}

					}
					else
					{
						Bitmap bitmap = BitmapFactory.decodeStream(new FlushedInputStream(inputStream));
						return bitmap;
					}
					// return BitmapFactory.decodeStream(new
					// FlushedInputStream(inputStream));

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
		ContentResolver resolver = mConext.getContentResolver();
		ContentValues values = new ContentValues();
		values.put(PhotoProviderData.PHOTO_PATH, savePath);
		values.put(PhotoProviderData.PHOTO_URL, url);
		resolver.insert(PhotoProviderData.PhotoData.CONTENT_URI, values);
		return savePath;
	}

	/*
	 * An InputStream that skips the exact number of bytes provided, unless it
	 * reaches EOF.
	 */
	public static class FlushedInputStream extends FilterInputStream
	{
		public FlushedInputStream(InputStream inputStream)
		{
			super(inputStream);
		}

		@Override
		public long skip(long n) throws IOException
		{
			long totalBytesSkipped = 0L;
			while (totalBytesSkipped < n)
			{
				long bytesSkipped = in.skip(n - totalBytesSkipped);
				if (bytesSkipped == 0L)
				{
					int b = read();
					if (b < 0)
					{
						break; // we reached EOF
					}
					else
					{
						bytesSkipped = 1; // we read one byte
					}
				}
				totalBytesSkipped += bytesSkipped;
			}
			return totalBytesSkipped;
		}
	}

	/**
	 * The actual AsyncTask that will asynchronously download the image.
	 */
	class BitmapDownloaderTask extends AsyncTask<String, Void, Bitmap>
	{
		private String url;
		private final WeakReference<ImageView> imageViewReference;

		public BitmapDownloaderTask(ImageView imageView)
		{
			imageViewReference = new WeakReference<ImageView>(imageView);
		}

		/**
		 * Actual download method.
		 */
		@Override
		protected Bitmap doInBackground(String... params)
		{
			url = params[0];
			return downloadBitmap(url);
		}

		/**
		 * Once the image is downloaded, associates it to the imageView
		 */
		@Override
		protected void onPostExecute(Bitmap bitmap)
		{
			if (isCancelled())
			{
				bitmap = null;
			}

			// addBitmapToCache(url, bitmap);

			if (imageViewReference != null)
			{
				ImageView imageView = imageViewReference.get();
				BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);
				// Change bitmap only if this process is still associated with
				// it
				// Or if we don't use any bitmap to task association
				// (NO_DOWNLOADED_DRAWABLE mode)
				if (this == bitmapDownloaderTask)
				{
					if (bitmap != null)
					{
						imageView.setImageBitmap(bitmap);
					}
					else
					{
						if (mDefaultRes_id != 0)
						{
							imageView.setImageResource(mDefaultRes_id);
						}
					}
				}
			}
		}
	}

	/**
	 * A fake Drawable that will be attached to the imageView while the download
	 * is in progress.
	 * 
	 * <p>
	 * Contains a reference to the actual download task, so that a download task
	 * can be stopped if a new binding is required, and makes sure that only the
	 * last started download process can bind its result, independently of the
	 * download finish order.
	 * </p>
	 */
	static class DownloadedDrawable extends ColorDrawable
	{
		private final WeakReference<BitmapDownloaderTask> bitmapDownloaderTaskReference;

		public DownloadedDrawable(BitmapDownloaderTask bitmapDownloaderTask)
		{
			super(Color.WHITE);
			bitmapDownloaderTaskReference = new WeakReference<BitmapDownloaderTask>(bitmapDownloaderTask);
		}

		public BitmapDownloaderTask getBitmapDownloaderTask()
		{
			return bitmapDownloaderTaskReference.get();
		}
	}

	/*
	 * Cache-related fields and methods.
	 * 
	 * We use a hard and a soft cache. A soft reference cache is too
	 * aggressively cleared by the Garbage Collector.
	 */

	private static final int HARD_CACHE_CAPACITY = 10;
	private static final int DELAY_BEFORE_PURGE = 10 * 1000; // in milliseconds

	// Hard cache, with a fixed maximum capacity and a life duration
	private final HashMap<String, Bitmap> sHardBitmapCache = new LinkedHashMap<String, Bitmap>(HARD_CACHE_CAPACITY / 2, 0.75f, true)
	{
		@Override
		protected boolean removeEldestEntry(LinkedHashMap.Entry<String, Bitmap> eldest)
		{
			if (size() > HARD_CACHE_CAPACITY)
			{
				// Entries push-out of hard reference cache are transferred to
				// soft reference cache
				sSoftBitmapCache.put(eldest.getKey(), new SoftReference<Bitmap>(eldest.getValue()));
				return true;
			}
			else
				return false;
		}
	};

	// Soft cache for bitmaps kicked out of hard cache
	private final static ConcurrentHashMap<String, SoftReference<Bitmap>> sSoftBitmapCache = new ConcurrentHashMap<String, SoftReference<Bitmap>>(HARD_CACHE_CAPACITY / 2);

	private final Handler purgeHandler = new Handler();

	private final Runnable purger = new Runnable()
	{
		public void run()
		{
			clearCache();
		}
	};

	/**
	 * Adds this bitmap to the cache.
	 * 
	 * @param bitmap
	 *            The newly downloaded bitmap.
	 */
	private void addBitmapToCache(String url, Bitmap bitmap)
	{
		if (bitmap != null)
		{
			synchronized (sHardBitmapCache)
			{
				sHardBitmapCache.put(url, bitmap);
			}
		}
	}

	/**
	 * @param url
	 *            The URL of the image that will be retrieved from the cache.
	 * @return The cached bitmap or null if it was not found.
	 */
	private Bitmap getBitmapFromCache(String url)
	{
		// First try the hard reference cache
		synchronized (sHardBitmapCache)
		{
			final Bitmap bitmap = sHardBitmapCache.get(url);
			if (bitmap != null)
			{
				// Bitmap found in hard cache
				// Move element to first position, so that it is removed last
				sHardBitmapCache.remove(url);
				sHardBitmapCache.put(url, bitmap);
				return bitmap;
			}
		}

		// Then try the soft reference cache
		SoftReference<Bitmap> bitmapReference = sSoftBitmapCache.get(url);
		if (bitmapReference != null)
		{
			final Bitmap bitmap = bitmapReference.get();
			if (bitmap != null)
			{
				// Bitmap found in soft cache
				return bitmap;
			}
			else
			{
				// Soft reference has been Garbage Collected
				sSoftBitmapCache.remove(url);
			}
		}

		return null;
	}

	/**
	 * Clears the image cache used internally to improve performance. Note that
	 * for memory efficiency reasons, the cache will automatically be cleared
	 * after a certain inactivity delay.
	 */
	public void clearCache()
	{
		sHardBitmapCache.clear();
		sSoftBitmapCache.clear();
	}

	/**
	 * Allow a new delay before the automatic cache clear is done.
	 */
	private void resetPurgeTimer()
	{
		purgeHandler.removeCallbacks(purger);
		purgeHandler.postDelayed(purger, DELAY_BEFORE_PURGE);
	}

	public static int computeSampleSize(BitmapFactory.Options options,

	int minSideLength, int maxNumOfPixels)
	{

		int initialSize = computeInitialSampleSize(options, minSideLength,

		maxNumOfPixels);

		int roundedSize;

		if (initialSize <= 8)
		{

			roundedSize = 1;

			while (roundedSize < initialSize)
			{

				roundedSize <<= 1;

			}

		}
		else
		{
			roundedSize = (initialSize + 7) / 8 * 8;
		}

		return roundedSize;

	}

	private static int computeInitialSampleSize(BitmapFactory.Options options,

	int minSideLength, int maxNumOfPixels)
	{

		double w = options.outWidth;

		double h = options.outHeight;

		int lowerBound = (maxNumOfPixels == -1) ? 1 :

		(int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));

		int upperBound = (minSideLength == -1) ? 128 :

		(int) Math.min(Math.floor(w / minSideLength),

		Math.floor(h / minSideLength));

		if (upperBound < lowerBound)
		{

			// return the larger one when there is no overlapping zone.

			return lowerBound;

		}

		if ((maxNumOfPixels == -1) &&(minSideLength == -1))
		{

			return 1;

		}
		else if (minSideLength == -1)
		{

			return lowerBound;

		}
		else
		{
			return upperBound;
		}

	}
}
