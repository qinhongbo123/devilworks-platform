package com.surfing.update;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import com.surfing.R;
import com.surfing.Notification.NotificationListActivity;
import com.surfing.channel.ActivityBase;
import com.surfing.channel.CloseReceiver;
import com.surfing.channel.CommonUpdate;
import com.surfing.channel.MenuTabActivity;
import com.surfing.channel.NewsInformationActivity;
import com.surfing.download.DownloaderActivity;
import com.surfing.httpconnection.HttpConnectionCallback;
import com.surfing.httpconnection.HttpConnectionUtil;
import com.surfing.httpconnection.ImageDownloader;
import com.surfing.httpconnection.HttpConnectionUtil.HttpMethod;
import com.surfing.setting.SettingActivity;
import com.surfing.util.DisplayWeather;
import com.surfing.util.ReadConfigFile;
import com.surfing.util.TitleBarDisplay;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class UpdateVersionActivity extends ActivityBase implements
		View.OnClickListener {
	private static final String TAG = "UpdateVersionActivity";
	private Button mUpdateBtn = null;
	private Context mContext = null;
	private int REQUESTCODE = 10; // Download
	private String remoteVersionString = "";
	private String enterpriseID = "";
	private Handler myHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case CommonUpdate.EVENT_UPDATE_WEATHER: {
				DisplayWeather.updateWeatherDisplay(getApplicationContext(),
						UpdateVersionActivity.this);
			}
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState,TITLE_TYPE_COMPANY,null,0,true);
		mContext = getApplicationContext();
		setContentView(R.layout.updateversion_layout);
		setupView();
		mUpdateBtn.setOnClickListener(this);
		CloseReceiver.registerCloseActivity(this);
		CommonUpdate.getInstance().registerForUpdateWeather(myHandler,
				CommonUpdate.EVENT_UPDATE_WEATHER, null);
	}

	@Override
	protected void onDestroy() {
		CommonUpdate.getInstance().unregisterForUpdateWeather(myHandler);
		CloseReceiver.unRegisterActivity(this);
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		DisplayWeather.updateWeatherDisplay(getApplicationContext(), this);
	}

	private void setupView() {
		mUpdateBtn = (Button) findViewById(R.id.update_btn_id);
		
	}

	@Override
	public void onClick(View v) {
		if (v == mUpdateBtn) {
			
			final String serverString = ReadConfigFile.getServerAddress(mContext);

			SharedPreferences userpref = mContext.getSharedPreferences("user",
					MODE_PRIVATE);
			String userName = userpref.getString("user_name", "");

			String requestPath = String
					.format("%sindex.php?controller=user&action=requestnewclient&username=%s",
							serverString, userName);
  
			
			HttpConnectionUtil connect = new HttpConnectionUtil(
					getApplicationContext());
			Log.e(TAG,"request path = "+requestPath);
			connect.syncConnect(requestPath, HttpMethod.GET,
					new HttpConnectionCallback() {

						@Override
						public void execute(String response) {
							Log.e(TAG, "==============" + response);
							// TODO Auto-generated method stub
							if ((response == null)
									|| (response.length() == 0)
									|| response.equals(HttpConnectionUtil.CONNECT_FAILED)
									|| HttpConnectionUtil.RETURN_FAILED.equalsIgnoreCase(response)) {
								Log.i(TAG, "request failed");
								return;
							}
							String [] versionEntpriseID = response.split(",");
							remoteVersionString = versionEntpriseID[0];
							enterpriseID = versionEntpriseID[1];
							Log.e(TAG, "remoteVersionString = " + remoteVersionString);
							int localVersion = Integer.parseInt(SettingActivity.BUILD_VERSION);
							int remoteVersion = Integer.parseInt(remoteVersionString);
							if (remoteVersion <= localVersion) {
								Toast.makeText(mContext,
										getString(R.string.msg_current_is_latest),
										Toast.LENGTH_LONG).show();
							} else {
								Intent myIntent = new Intent();
								myIntent.setClass(mContext, DownloaderActivity.class);
								String clientPath = serverString + "apk/" + enterpriseID + "/surf-platform-"
										+ remoteVersionString + ".apk";
								myIntent.putExtra("path", clientPath);
								startActivityForResult(myIntent, REQUESTCODE);
							}

						}

					});
			
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode == RESULT_OK) {
			final String filePath = data.getStringExtra("path");
			Log.i(TAG, "the path is : " + filePath);
			Toast.makeText(getApplicationContext(), "" + filePath,
					Toast.LENGTH_LONG).show();
			AlertDialog.Builder builder = new AlertDialog.Builder(
					UpdateVersionActivity.this)
					.setTitle(R.string.setting_update)
					.setMessage(R.string.message_ask_installapk)
					.setPositiveButton(R.string.btn_install,
							new OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {
									Uri uri = Uri.fromFile(new File(filePath));
									Intent intent = new Intent(
											Intent.ACTION_VIEW);
									intent.setDataAndType(uri,
											"application/vnd.android.package-archive");
									startActivity(intent);

								}
							})
					.setNegativeButton(R.string.btn_cancel,
							new OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {

								}
							});
			builder.create();
			builder.show();
		}
	}
}
