/*
 * Copyright (C) 2008 Google Inc.
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

package com.surfing.download;

import com.surfing.R;
import com.surfing.channel.ActivityBase;
import com.surfing.channel.CloseReceiver;
import com.surfing.update.UpdateVersionActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;

import org.apache.http.impl.client.DefaultHttpClient;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import java.security.MessageDigest;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class DownloaderActivity extends ActivityBase {
	/** Called when the activity is first created. */
	private static final String TAG = "DownloaderActivity";
	private String Path = null;
	private String mFilePath = null;
	private ProgressBar progressBar;
	private TextView textView;
	private Button button;
	private int FileLength;
	private int DownedFileLength = 0;
	private InputStream inputStream;
	private URLConnection connection;
	private OutputStream outputStream;
	private Intent mResultIntent;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mResultIntent = getIntent();
		setContentView(R.layout.downloader_layout);
		progressBar = (ProgressBar) findViewById(R.id.loading_progressBar);
		textView = (TextView) findViewById(R.id.process_text);
		Path = getIntent().getStringExtra("path");
		Log.i(TAG,"the path is : "+Path);
		Thread thread=new Thread(){  
            public void run(){  
                try {  
                   DownFile(Path);  
               } catch (Exception e) {  
                   // TODO: handle exception  
               }  
            }  
          };  
         thread.start();  
         CloseReceiver.registerCloseActivity(this);
	}
	
	@Override
    protected void onDestroy()
    {
	    CloseReceiver.unRegisterActivity(this);
        super.onDestroy();
    }

    private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (!Thread.currentThread().isInterrupted()) {
				switch (msg.what) {
				case 0:
					progressBar.setMax(FileLength);
					break;
				case 1:
					progressBar.setProgress(DownedFileLength);
					int x = DownedFileLength * 100 / FileLength;
					textView.setText(x + "%");
					break;
				case 2:
					if(mFilePath.contains(".apk")){
						AlertDialog.Builder builder = new AlertDialog.Builder(DownloaderActivity.this)
						.setTitle(R.string.setting_update)
						.setMessage(R.string.message_ask_installapk)
						.setPositiveButton(R.string.btn_install,new OnClickListener() {
							
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								Log.i(TAG,"the path is : "+mFilePath);
								Uri uri = Uri.fromFile(new File(mFilePath));  
								Intent intent = new Intent(Intent.ACTION_VIEW);   
								intent.setDataAndType(uri,"application/vnd.android.package-archive");
								startActivity(intent); 
								
							}
						})
						.setNegativeButton(R.string.btn_cancel,new OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								
							}  
						});
						builder.create();
						builder.show();
					}
					else{
						mResultIntent = new Intent();
						mResultIntent.putExtra("path",mFilePath);
						setResult(RESULT_OK,mResultIntent);
						finish();
					}
					
				break;

				default:
					break;
				}
			}
		}

	};

	private void DownFile(String urlString) {

		/*
		 * ���ӵ�������
		 */

		try {
			URL url = new URL(urlString);
			connection = url.openConnection();
			if (connection.getReadTimeout() == 5) {
				Log.i("---------->", "��ǰ����������");
			    return;
			}
			inputStream = connection.getInputStream();

		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String savePAth = Environment.getExternalStorageDirectory()
				+ "/DownFile";
		File file1 = new File(savePAth);
		if (!file1.exists()) {
			file1.mkdir();
		}
		String filetype[] = urlString.split("/");
		//String filename = filetype[filetype.length-1].replace("zip", "apk");
		String filename = filetype[filetype.length-1].toString();
		mFilePath = Environment.getExternalStorageDirectory()
				+ "/DownFile/" + filename;
		Log.i(TAG,"the file path is : "+filename);
		File file = new File(mFilePath);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Message message = new Message();
		try {
			int readCont = 0;
			outputStream = new FileOutputStream(file);
			byte[] buffer = new byte[1024 * 4];
			FileLength = connection.getContentLength();
			DownedFileLength = 0;
			Log.i(TAG,"FileLength = "+FileLength);
			message.what = 0;
			handler.sendMessage(message);
			while (DownedFileLength < FileLength) {
				readCont = inputStream.read(buffer);
				DownedFileLength += readCont;
				outputStream.write(buffer, 0, readCont);
				Message message1 = new Message();
				message1.what = 1;
				handler.sendMessage(message1);
			}
			outputStream.close();
			inputStream.close();
			Message message2 = new Message();
			message2.what = 2;
			handler.sendMessage(message2);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
