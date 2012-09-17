package com.surfing.Notification;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.Inflater;

import com.surfing.R;
import com.surfing.Notification.NotificationProviderMetaData.UserTableMetaData;
import com.surfing.channel.ActivityBase;
import com.surfing.channel.ChannelActivityOne;
import com.surfing.channel.CloseReceiver;
import com.surfing.channel.CommonUpdate;
import com.surfing.channel.MenuTabActivity;
import com.surfing.channel.NewsInformationActivity;
import com.surfing.httpconnection.HttpConnectionCallback;
import com.surfing.httpconnection.HttpConnectionUtil;
import com.surfing.httpconnection.ImageDownloader;
import com.surfing.httpconnection.HttpConnectionUtil.HttpMethod;
import com.surfing.rssparse.ChannelInformation;
import com.surfing.rssparse.ChannelItem;
import com.surfing.rssparse.DomXMLReader;
import com.surfing.util.DisplayWeather;
import com.surfing.util.ReadConfigFile;
import com.surfing.util.SaveRssFile;
import com.surfing.util.TitleBarDisplay;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class NotificationListActivity extends ActivityBase implements OnItemClickListener{
	private final String TAG = "NotificationListActivity";
	private ListView mList = null;
	private NotificationCursorAdapter mNotificationlListAdapter;
	private NotifyHandler mHandler = null;
	private Context mContext = null;
	private int COLUMN_NEWS_ID = 0;
	private int COLUMN_NEWS_TILTE = 1;
	private int COLUMN_NEWS_CONTENT = 2;
	private int COLUMN_NEWS_READ = 3;
	private static final int TOKEN_QUERY = 42;
	private static final int TOKEN_DELETE = 43;
	private ImageView mTitleIcon = null;
	private TextView mTitleText = null;
	private int mMode = 0;
	private Handler    myHandler = new Handler(){

        @Override
        public void handleMessage(Message msg)
        {
            switch(msg.what){
                case CommonUpdate.EVENT_UPDATE_WEATHER:{
                    DisplayWeather.updateWeatherDisplay(getApplicationContext(),NotificationListActivity.this);
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
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.notification_list_layout);
		mContext = getApplicationContext();
		mMode = ReadConfigFile.getMode(mContext);
		setupView();
		TitleBarDisplay.TitleBarInit(mTitleText, mTitleIcon,this,getApplicationContext()); 
		View view = findViewById(R.id.title_layout_id);
		if(mMode == 1){
			view.setVisibility(View.VISIBLE);
		}else{
			view.setVisibility(View.GONE);
		}
		
		mHandler = new NotifyHandler(mContext.getContentResolver());
		mNotificationlListAdapter = new NotificationCursorAdapter(mContext,null);
		mList.setAdapter(mNotificationlListAdapter);
		mList.setOnItemClickListener(this);
		HttpConnectionUtil connect = new HttpConnectionUtil(getApplicationContext());
		ConnectWeb(connect,getUrl());
		CloseReceiver.registerCloseActivity(this);
		CommonUpdate.getInstance().registerForUpdateWeather(myHandler,CommonUpdate.EVENT_UPDATE_WEATHER,null);
	} 
	@Override
    protected void onDestroy()
    {
        CommonUpdate.getInstance().unregisterForUpdateWeather(myHandler);
        CloseReceiver.unRegisterActivity(this);
        super.onDestroy();
    }
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		SharedPreferences userpref = mContext.getSharedPreferences("user",MODE_PRIVATE);
		String user_name = userpref.getString("user_name", "");
		Log.i(TAG,"Notification resume");
		mHandler.startQuery(TOKEN_QUERY,null,UserTableMetaData.CONTENT_URI,
				new String[]{NotificationProviderMetaData.NOTIFICATION_ID,NotificationProviderMetaData.NOTIFICATION_TITLE,NotificationProviderMetaData.NOTIFICATION_CONTENT,NotificationProviderMetaData.NOTIFICATION_READFLAG}, 
				NotificationProviderMetaData.NOTIFICATION_USER+"=?",new String[]{user_name}, null);
	   mHandler.startDelete(TOKEN_DELETE,null,UserTableMetaData.CONTENT_URI,NotificationProviderMetaData.NOTIFICATION_USER+" not in ('"+user_name+"')",null);
	   DisplayWeather.updateWeatherDisplay(getApplicationContext(),this);
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
	    if(mMode == 0){
	        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
	            super.dialog();
	            return false;
	        }
	    }
		
		return super.onKeyDown(keyCode, event);
	}
	public void ConnectWeb(HttpConnectionUtil connect,String geturl){
			Log.i(TAG,"the url is : "+ geturl);
			connect.asyncConnect(geturl, HttpMethod.POST, new HttpConnectionCallback(){

			@Override
			public void execute(String response) {
				ContentResolver resolver = NotificationListActivity.this.getContentResolver();
				if((response == null) 
                        || (response.length() == 0) 
                        || response.equals(HttpConnectionUtil.CONNECT_FAILED)
                        || HttpConnectionUtil.RETURN_FAILED.equalsIgnoreCase(response)){
					Log.i(TAG,"connection failed");
					SharedPreferences userpref = mContext.getSharedPreferences("user",MODE_PRIVATE);
					String user_name = userpref.getString("user_name", "");
					Cursor cursor = resolver.query(UserTableMetaData.CONTENT_URI,
							new String[]{NotificationProviderMetaData.NOTIFICATION_ID,NotificationProviderMetaData.NOTIFICATION_TITLE,NotificationProviderMetaData.NOTIFICATION_CONTENT,NotificationProviderMetaData.NOTIFICATION_READFLAG}, 
							NotificationProviderMetaData.NOTIFICATION_USER+"=?",new String[]{user_name}, null);
					mNotificationlListAdapter.changeCursor(cursor);
					return;
				}
				try{
					Log.i(TAG,"connect success");
					
					InputStream stream = new ByteArrayInputStream(response.getBytes());
					Log.i(TAG,"string"+response);
					//SaveRssFile savefile = new SaveRssFile(mContext);
					//savefile.SaveFile(response.substring(nindex),"news.xml");
					ChannelInformation channelInfo = DomXMLReader.readXML(NotificationListActivity.this,stream);
					if(channelInfo != null){
						ArrayList<ContentProviderOperation> operationList  = new ArrayList<ContentProviderOperation>();
						ContentProviderOperation.Builder builder = null;
						ContentValues values = null;
						
						ArrayList<ChannelItem> channlelist = (ArrayList<ChannelItem>) channelInfo.getmChannelItemList();
						SharedPreferences userpref = mContext.getSharedPreferences("user",MODE_PRIVATE);
						String user_name = userpref.getString("user_name", "");
						for(int i = 0;i<channlelist.size();i++){
							Log.i(TAG,"there have new news channlelist.size() == "+channlelist.size());
							builder = ContentProviderOperation.newInsert(UserTableMetaData.CONTENT_URI); 
							values = new ContentValues();
							values.put(NotificationProviderMetaData.NOTIFICATION_USER,user_name);
							values.put(NotificationProviderMetaData.NOTIFICATION_TITLE,channlelist.get(i).getmTitle());
							values.put(NotificationProviderMetaData.NOTIFICATION_CONTENT,channlelist.get(i).getmDescription());
							builder.withValues(values);
							operationList.add(builder.build());
						}
						resolver.applyBatch(NotificationProviderMetaData.AUTHORIY,operationList);
						mHandler.startQuery(TOKEN_QUERY,null,UserTableMetaData.CONTENT_URI,
								new String[]{NotificationProviderMetaData.NOTIFICATION_ID,NotificationProviderMetaData.NOTIFICATION_TITLE,NotificationProviderMetaData.NOTIFICATION_CONTENT,NotificationProviderMetaData.NOTIFICATION_READFLAG}, 
								NotificationProviderMetaData.NOTIFICATION_USER+"=?",new String[]{user_name}, null);
						mHandler.startDelete(TOKEN_DELETE,null,UserTableMetaData.CONTENT_URI,NotificationProviderMetaData.NOTIFICATION_USER+" not in ('"+user_name+"')",null);
					}
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		});
	}
	private void setupView(){
		mList = (ListView)findViewById(R.id.notification_list_id);
		mTitleIcon = (ImageView)findViewById(R.id.titlebar_icon_id);
		mTitleText = (TextView)findViewById(R.id.titlebar_text_id);
	}
	private class NotificationCursorAdapter extends CursorAdapter{
		
		public NotificationCursorAdapter(Context context, Cursor c) {
			super(context, c);
			
			
		}

		@Override
		public void bindView(View v, Context context, Cursor cursor) {
			TextView textview = (TextView)v.findViewById(R.id.notification_list_text_id);
			ImageView imgview = (ImageView)v.findViewById(R.id.notification_list_img_id);
			if(textview != null){
				textview.setText(cursor.getString(COLUMN_NEWS_TILTE));
			}
			if(imgview != null){
				int flag = cursor.getInt(COLUMN_NEWS_READ);
				if(flag == 0){
					imgview.setImageResource(R.drawable.news_unread);
				}else{
					imgview.setImageResource(R.drawable.news_read);
				}
			}
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup viewgroup) {
			final LayoutInflater inflater = getLayoutInflater();
			View ItemView = inflater.inflate(R.layout.notification_listitem_layout,mList, false);
			return ItemView;
		}
		
	}
	@Override
	public void onItemClick(AdapterView<?> viewAdapter, View view, int position, long id) {
		Cursor cursor = mNotificationlListAdapter.getCursor();
		ContentResolver resolver = NotificationListActivity.this.getContentResolver();
		if(cursor != null){
			cursor.moveToPosition(position);
			int notify_id = cursor.getInt(COLUMN_NEWS_ID);
			Intent myIntent = new Intent();
			myIntent.putExtra(NotificationViewActivity.NOTIFY_ID,notify_id);
			myIntent.setClass(mContext, NotificationViewActivity.class);
			startActivity(myIntent);
			ContentValues values = new ContentValues();
			values.put(NotificationProviderMetaData.NOTIFICATION_TITLE,cursor.getString(COLUMN_NEWS_TILTE));
			values.put(NotificationProviderMetaData.NOTIFICATION_CONTENT,cursor.getString(COLUMN_NEWS_CONTENT));
			values.put(NotificationProviderMetaData.NOTIFICATION_READFLAG,1);
			resolver.update(UserTableMetaData.CONTENT_URI,values,NotificationProviderMetaData.NOTIFICATION_ID+"="+notify_id,null);
		}
	}
	private String getUrl(){
		SharedPreferences userpref = this.getApplicationContext().getSharedPreferences("user",MODE_PRIVATE);
		String user_name = userpref.getString("user_name", "");
		String url = ReadConfigFile.getServerAddress(mContext)+"index.php?controller=Notification&action=PhoneReqNotificationList&user_name="+user_name;
		return url;
	}
	private class NotifyHandler extends AsyncQueryHandler{

		public NotifyHandler(ContentResolver cr) {
			super(cr);
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			
			mNotificationlListAdapter.changeCursor(cursor);
			if(cursor != null){
				Log.i(TAG,"cursor count is : "+cursor.getCount());
			}
			
		}
		
	}
}
