package com.surfing.channel;


import com.surfing.R;
import com.surfing.Notification.NotificationListActivity;
import com.surfing.Notification.NotificationProviderMetaData;
import com.surfing.Notification.NotificationProviderMetaData.UserTableMetaData;
import com.surfing.contacts.EnterpriseContactsActivity;
import com.surfing.disscusgroup.DisscusActivity;
import com.surfing.disscusgroup.VoteListActivity;
import com.surfing.httpconnection.HttpConnectionCallback;
import com.surfing.httpconnection.HttpConnectionUtil;
import com.surfing.httpconnection.ImageDownloader;
import com.surfing.httpconnection.HttpConnectionUtil.HttpMethod;
import com.surfing.setting.SettingActivity;
import com.surfing.util.DisplayWeather;
import com.surfing.util.ReadConfigFile;
import com.surfing.util.SaveRssFile;
import com.surfing.util.ThemeUpdateUitl;
import com.surfing.util.TitleBarDisplay;

import android.app.Activity;
import android.app.ActivityGroup;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.TabActivity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.webkit.WebSettings.TextSize;
import android.webkit.WebSettings.ZoomDensity;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.Toast;

public class MenuTabActivity extends ActivityGroup{
	private static final String TAG = "MenuTabActivity";
	private TabHost mTabHost;
	private Window mWindow;
	private ImageView mTitleIcon = null;
	private TextView mTitleText = null;
	private static String mtabSpec[]={"tab1","tab2","tab3","tab4","tab6"};
	private static int mIndicateId[]={R.string.channel_title,
									  /*R.string.discuss_title,*/
									  R.string.inner_message,
									  R.string.contact_title,
									  /*R.string.vote_title,*/
									  R.string.setting_title};
	private int mTheme = 0;
	private Context    mCurrentContext;
	
	private Handler    myHandler = new Handler(){

        @Override
        public void handleMessage(Message msg)
        {
            switch(msg.what){
                case CommonUpdate.EVENT_UPDATE_WEATHER:{
                    DisplayWeather.updateWeatherDisplay(getApplicationContext(),MenuTabActivity.this);
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
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); 
		setContentView(R.layout.menutabhost_layout);
		mWindow = getWindow();
		mWindow.setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.titlebar);
		mTheme = ReadConfigFile.getTheme(getApplicationContext());
		setupView();
		
		Intent myChannelIntent = new Intent();
		myChannelIntent.setClass(getApplicationContext(), ChannelTabActivity.class);
		mTabHost = (TabHost)findViewById(R.id.tab_host);
		mTabHost.setup(this.getLocalActivityManager());
		mCurrentContext = ReadConfigFile.getCurrentThemeContext(mTheme,getApplicationContext()); 
		for(int i = 0;i<mIndicateId.length;i++){
			TabSpec tabSpec = mTabHost.newTabSpec(mtabSpec[i]);
			tabSpec.setIndicator(getTabItemView(i));
			tabSpec.setContent(getItemIntent(mIndicateId[i]));
			mTabHost.addTab(tabSpec);
			if(mCurrentContext != null){
			    mTabHost.getTabWidget().getChildAt(i).setBackgroundDrawable(mCurrentContext.getResources().getDrawable(R.drawable.tab_bg));
			}else{
			    mTabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.tab_bg);
			}
		}
		
		mTabHost.setCurrentTab(0);
		
		View view = findViewById(R.id.title_layout_id);
        ThemeUpdateUitl.updateTitlebarBg(view, mCurrentContext,R.drawable.title_bg);
        
		layoutTab();
		SharedPreferences userpref = this.getApplicationContext().getSharedPreferences("user",MODE_PRIVATE);
		String user_name = userpref.getString("user_name", "");
		String mConnectUrl = ReadConfigFile.getServerAddress(getApplicationContext())+"index.php?controller=enterprise&action=RequireEnterLogoEx&user_name="+user_name;
		//getUnreadNotifiction();
		Log.i(TAG,"company url is : "+mConnectUrl);
		HttpConnectionUtil connect = new HttpConnectionUtil(getApplicationContext());
		ConnectWeb(connect,mConnectUrl);
		CloseReceiver.registerCloseActivity(this);
		CommonUpdate.getInstance().registerForUpdateWeather(myHandler,CommonUpdate.EVENT_UPDATE_WEATHER,null);
		String status = Environment.getExternalStorageState();
		  if (status.equals(Environment.MEDIA_MOUNTED)) {
		   //do something
		  } else {
		   Toast.makeText(getApplicationContext(), getString(R.string.msg_sdcard),Toast.LENGTH_SHORT).show();
		  }

	}
	@Override
    public void onBackPressed()
    {
        Log.i(TAG,"MenuTabActivity onBackPressed");
        // TODO Auto-generated method stub
        super.onBackPressed();
    }
	@Override
    protected void onDestroy()
    {
	    CommonUpdate.getInstance().unregisterForUpdateWeather(myHandler);
	    CloseReceiver.unRegisterActivity(this);
        super.onDestroy();
    }

    @Override
    protected void onResume()
    {
        // TODO Auto-generated method stub
        super.onResume();
        DisplayWeather.updateWeatherDisplay(getApplicationContext(),this);
    }

    public void ConnectWeb(HttpConnectionUtil connect,String geturl){
		Log.i(TAG,"url = "+geturl);
		connect.asyncConnect(geturl, HttpMethod.GET, new HttpConnectionCallback(){
			
			@Override
			public void execute(String response) {
			    if((response == null) 
	                    || (response.length() == 0) 
	                    || response.equals(HttpConnectionUtil.CONNECT_FAILED)
	                    || HttpConnectionUtil.RETURN_FAILED.equalsIgnoreCase(response))
			    {
			        Log.i(TAG,"request failed");
			        return;
			    }
				Log.i(TAG,"company  string = "+response);
				
				int nIndex = response.indexOf("\\r\\n");
				if(nIndex < 0){
					return;
				}
				String enterprise_name = response.substring(0,nIndex);
				String subString = response.substring(nIndex+"\\r\\n".length());
				nIndex = subString.indexOf("\\r\\n");
				String enterprise_iconAddress = subString.substring(0,nIndex);
				String enterprise_id = subString.substring(nIndex+"\\r\\n".length());
				enterprise_name = enterprise_name.substring("enterprise_name=".length()+1);
				enterprise_iconAddress = enterprise_iconAddress.substring("enterprise_logo=".length());
				enterprise_id = enterprise_id.substring("enterprise_id=".length());
				Log.i(TAG,"enterprise name is : "+enterprise_name);
				Log.i(TAG,"icon address is : "+enterprise_iconAddress);
				Log.i(TAG,"enterprise id = "+enterprise_id);
				mTitleText.setText(enterprise_name);
				ImageDownloader imageDownloader = new ImageDownloader();
				imageDownloader.download(enterprise_iconAddress,mTitleIcon,R.drawable.notify_online,getApplicationContext());
				SharedPreferences pref = MenuTabActivity.this.getApplicationContext().getSharedPreferences("enterprise",MODE_PRIVATE);
			    Editor editor = pref.edit();
			    editor.putString("enterprise_name",enterprise_name);
			    editor.putString("enterprise_icon_url",enterprise_iconAddress);
			    editor.putString("enterprise_id",enterprise_id);
			    editor.commit();
			    TitleBarDisplay.TitleBarInit(mTitleText, mTitleIcon,MenuTabActivity.this,getApplication());
			
			}
			
		});
	}
	private void layoutTab(){
		TabWidget tabWidget = mTabHost.getTabWidget();
		int count = tabWidget.getChildCount(); 
		int nCurrentIndex = 0;
		nCurrentIndex = mTabHost.getCurrentTab();
		DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int mDensity = metrics.densityDpi;
		for (int i = 0; i < count; i++) { 
			View view = tabWidget.getChildTabViewAt(i);   
			if (mDensity == 240) { 
                view.getLayoutParams().height = 65;
            } else if (mDensity == 160) {
                view.getLayoutParams().height = 35;
            } else if(mDensity == 120) {
                view.getLayoutParams().height = 35;
            }
			final TextView tv = (TextView) view.findViewById(R.id.text_view_id); 
			if(i == nCurrentIndex){
			    if(mCurrentContext != null){
			        if(mTheme == ReadConfigFile.THEME_BLUE){
			            tv.setBackgroundDrawable(mCurrentContext.getResources().getDrawable(com.surfing.R.drawable.table_sel));
			        }
			        
			    }else{
			        tv.setBackgroundResource(com.surfing.R.drawable.menu_tab_bg);
			    }
			    if(mTheme == ReadConfigFile.THEME_RED){
				    tv.setTextColor(Color.rgb(255,255,255));
				}
			}else{
			    if(mCurrentContext != null){
                    if(mTheme == ReadConfigFile.THEME_BLUE){
                        tv.setBackgroundDrawable(mCurrentContext.getResources().getDrawable(com.surfing.R.drawable.table_unsel));
                    }
                    
                }else{
                    tv.setBackgroundResource(com.surfing.R.drawable.menu_tab_bg);
                }
			    if(mTheme == ReadConfigFile.THEME_RED){
			        tv.setTextColor(Color.rgb(0,0,0));
                }
			}
		}
			mTabHost.setOnTabChangedListener(new OnTabChangeListener() {
			@Override
			public void onTabChanged(String arg0) {
				TabWidget tabWidget = mTabHost.getTabWidget();
				int count = tabWidget.getChildCount(); 
				int nCurrentIndex = 0;
				nCurrentIndex = mTabHost.getCurrentTab();
				for (int i = 0; i < count; i++) { 
					View view = tabWidget.getChildTabViewAt(i);
					final TextView tv = (TextView) view.findViewById(R.id.text_view_id); 
					if(i == nCurrentIndex){
		                if(mCurrentContext != null){
		                    if(mTheme == ReadConfigFile.THEME_BLUE){
		                        tv.setBackgroundDrawable(mCurrentContext.getResources().getDrawable(com.surfing.R.drawable.table_sel));
		                    }
		                    
		                }else{
		                    tv.setBackgroundResource(com.surfing.R.drawable.menu_tab_bg);
		                }
		                if(mTheme == ReadConfigFile.THEME_RED){
		                    tv.setTextColor(Color.rgb(255,255,255));
		                }
		            }else{
		                if(mCurrentContext != null){
		                    if(mTheme == ReadConfigFile.THEME_BLUE){
		                        tv.setBackgroundDrawable(mCurrentContext.getResources().getDrawable(com.surfing.R.drawable.table_unsel));
		                    }
		                    
		                }else{
		                    tv.setBackgroundResource(com.surfing.R.drawable.menu_tab_bg);
		                }
		                if(mTheme == ReadConfigFile.THEME_RED){
		                    tv.setTextColor(Color.rgb(0,0,0));
		                }
		            }
					tv.setGravity(Gravity.CENTER);
				} 
			}
		});
	}
	private void setupView(){
		mTitleIcon = (ImageView)findViewById(R.id.titlebar_icon_id);
		mTitleText = (TextView)findViewById(R.id.titlebar_text_id);
	}
	private void getUnreadNotifiction(){
		ContentResolver resolver = getContentResolver();
		Cursor cursor = resolver.query(UserTableMetaData.CONTENT_URI,new String[]{NotificationProviderMetaData.NOTIFICATION_ID},NotificationProviderMetaData.NOTIFICATION_READFLAG+"=0",null,null);
		if((cursor != null) && (cursor.getCount() > 0)){
			AlertDialog.Builder builder = new AlertDialog.Builder(MenuTabActivity.this)
			.setTitle(R.string.message_title_indicate)
			.setMessage(getString(R.string.message_notify_start)+cursor.getCount()+getString(R.string.message_notify_end))
			.setPositiveButton(R.string.btn_view,new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					Intent myIntent = new Intent();
					myIntent.setClass(getApplicationContext(),NotificationListActivity.class);
					MenuTabActivity.this.startActivity(myIntent);
				}
			})
			.setNegativeButton(R.string.btn_cancel,new OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					// TODO Auto-generated method stub
					
				}
			});
			builder.create();
			builder.show();
			cursor.close();
		}
	}
	private View getTabItemView(int index)
	{
		LayoutInflater mLayoutInflater  = (LayoutInflater)MenuTabActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
		View view = mLayoutInflater.inflate(R.layout.tab_item_view, null);

		TextView textView = (TextView) view.findViewById(R.id.text_view_id);
		textView.setText(mIndicateId[index]);
		return view; 
	}
	private Intent getItemIntent(int index){
		Intent myInent = new Intent();
		switch(index){
		case R.string.channel_title:
			myInent.setClass(getApplicationContext(), ChannelTabActivity.class);
			String response = getIntent().getStringExtra(ChannelTabActivity.COLUMN_INFO_TAG);
			myInent.putExtra(ChannelTabActivity.COLUMN_INFO_TAG,response);
		break;
		case R.string.discuss_title:
			myInent.setClass(getApplicationContext(), DisscusActivity.class);
		break;
		case R.string.contact_title:
			myInent.setClass(getApplicationContext(),EnterpriseContactsActivity.class);
		break;	
		case R.string.vote_title:
			myInent.setClass(getApplicationContext(),VoteListActivity.class);
		break;
		case R.string.setting_title:
			myInent.setClass(getApplicationContext(), SettingActivity.class);
		break;
		case R.string.inner_message:
			myInent.setClass(getApplicationContext(), NotificationListActivity.class);
		break;
		default:
			break;
		}
		return myInent;
	}
}
