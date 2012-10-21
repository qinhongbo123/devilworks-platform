package com.surfing.Notification;

import com.surfing.R;
import com.surfing.Notification.NotificationProviderMetaData.UserTableMetaData;
import com.surfing.channel.ActivityBase;
import com.surfing.channel.CloseReceiver;
import com.surfing.channel.CommonUpdate;
import com.surfing.channel.MenuTabActivity;
import com.surfing.channel.NewsInformationActivity;
import com.surfing.httpconnection.ImageDownloader;
import com.surfing.util.DisplayWeather;
import com.surfing.util.TitleBarDisplay;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

public class NotificationViewActivity extends ActivityBase {
	private final static String TAG = "NotificationViewActivity";
	private TextView mNotificationView = null;
	public final static String NOTIFY_ID = "notify_id";
	private ImageView mTitleIcon = null;
	private TextView mTitleText = null;
	private Context mContext = null;
	private Handler    myHandler = new Handler(){

        @Override
        public void handleMessage(Message msg)
        {
            switch(msg.what){
                case CommonUpdate.EVENT_UPDATE_WEATHER:{
                    DisplayWeather.updateWeatherDisplay(getApplicationContext(),NotificationViewActivity.this);
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
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		mContext = getApplicationContext();
		setContentView(R.layout.notification_view_layout);
		setupView();
		TitleBarDisplay.TitleBarInit(mTitleText, mTitleIcon,this,getApplicationContext()); 	
		ContentResolver resolver = getContentResolver();
		int notify_id = getIntent().getIntExtra(NOTIFY_ID, 0);
		Cursor cursor = resolver.query(UserTableMetaData.CONTENT_URI,
				new String[]{NotificationProviderMetaData.NOTIFICATION_CONTENT},
				NotificationProviderMetaData.NOTIFICATION_ID+"="+notify_id, 
				null,
				null);
		if(cursor != null){
			cursor.moveToFirst();
			if(cursor.getCount()>0){
				mNotificationView.setText(cursor.getString(0));
			}
		}
		//CloseReceiver.registerCloseActivity(this);
		CommonUpdate.getInstance().registerForUpdateWeather(myHandler,CommonUpdate.EVENT_UPDATE_WEATHER,null);
	}
	@Override
    protected void onDestroy()
    {
        CommonUpdate.getInstance().unregisterForUpdateWeather(myHandler);
        //CloseReceiver.unRegisterActivity(this);
        super.onDestroy();
    }
	@Override
    protected void onResume() {
        super.onResume();
        DisplayWeather.updateWeatherDisplay(getApplicationContext(),this);
    }
	private void setupView(){
		mNotificationView = (TextView)findViewById(R.id.notification_text_id);
		mTitleIcon = (ImageView)findViewById(R.id.titlebar_icon_id);
		mTitleText = (TextView)findViewById(R.id.titlebar_text_id);
	}
}
