package com.surfing.setting;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import com.surfing.R;
import com.surfing.Notification.NotificationDialog;
import com.surfing.channel.ActivityBase;
import com.surfing.channel.CloseReceiver;
import com.surfing.channel.CommonUpdate;
import com.surfing.channel.MenuGridActivity;
import com.surfing.channel.MenuTabActivity;
import com.surfing.channel.NewsInformationActivity;
import com.surfing.httpconnection.ImageDownloader;
import com.surfing.login.LoginActivity;
import com.surfing.update.UpdateVersionActivity;
import com.surfing.util.CommonOperate;
import com.surfing.util.DisplayWeather;
import com.surfing.util.ReadConfigFile;
import com.surfing.util.TitleBarDisplay;  

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.gsm.SmsManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class SettingActivity extends ActivityBase implements OnItemClickListener{
	private static final String TAG = "SettingActivity";
	private SimpleAdapter mSettingListAdapter;
	private ArrayList<HashMap<String, Object>> mSettingItemList;
	private ListView mSettingList;
	private ArrayList<HashMap<String, Object>> mSettingData;
	private Context mContext = null;
	private int mMode = 0;
	public final static String BUILD_VERSION = "68";
	private Handler    myHandler = new Handler(){

        @Override
        public void handleMessage(Message msg)
        {
            switch(msg.what){
                case CommonUpdate.EVENT_UPDATE_WEATHER:{
                    if(ReadConfigFile.getTheme(mContext) == 1){
                        DisplayWeather.updateWeatherDisplay(getApplicationContext(),SettingActivity.this);
                    }
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
		mContext = this.getApplicationContext();
		SharedPreferences prefMode = mContext.getSharedPreferences("mode",MODE_PRIVATE);
		int mMode = prefMode.getInt("mode",0);
		if(mMode == 1){
			super.onCreate(savedInstanceState,TITLE_TYPE_COMPANY,null,0,true);
		}else{
			super.onCreate(savedInstanceState);
		}
		setContentView(R.layout.setting_list_layout);
		HashMap<String,Object> map = null;
		mSettingItemList = new ArrayList<HashMap<String, Object>>();
		mSettingData = getListData();
		for(int i = 0;i<mSettingData.size();i++){
			map = new HashMap<String,Object>();
			map.put("itemtitle",mSettingData.get(i).get("title").toString());
			mSettingItemList.add(map);
		}
		mSettingListAdapter = new SimpleAdapter(getApplicationContext(), 
												mSettingItemList, 
												R.layout.otherchannel_listitem_layout, 
												new String[]{"itemtitle"},
												new int[]{R.id.channel_listitem_id});
		mSettingList = (ListView)findViewById(R.id.setting_list_id);
		mSettingList.setAdapter(mSettingListAdapter);
		mSettingList.setOnItemClickListener(this);
		CloseReceiver.registerCloseActivity(this);
		CommonUpdate.getInstance().registerForUpdateWeather(myHandler,CommonUpdate.EVENT_UPDATE_WEATHER,null);
	}
	@Override
    protected void onStart()
    {
        mMode = ReadConfigFile.getMode(getApplicationContext());
        super.onStart();
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
	@Override
    protected void onDestroy()
    {
        CommonUpdate.getInstance().unregisterForUpdateWeather(myHandler);
        CloseReceiver.unRegisterActivity(this);
        super.onDestroy();
    }
	@Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefMode = mContext.getSharedPreferences("mode",MODE_PRIVATE);
        int mMode = prefMode.getInt("mode",0);
        if(mMode == 1){
            DisplayWeather.updateWeatherDisplay(getApplicationContext(),this);
        }
    }
	private void setupView(){
	}
	private ArrayList<HashMap<String, Object>> getListData(){
		ArrayList<HashMap<String, Object>> listData = new ArrayList<HashMap<String, Object>>();
		//logout
		HashMap<String, Object> map;
		if(ReadConfigFile.mLoginType == ReadConfigFile.LOGIN_TYPE_NORMAL){
			map = new HashMap<String, Object>();
			map.put("title",getString(R.string.setting_logout));
			listData.add(map);
		}
		
		//update
		map = new HashMap<String, Object>();
		map.put("title",getString(R.string.setting_update));
		listData.add(map);
		
		if(ReadConfigFile.mLoginType == ReadConfigFile.LOGIN_TYPE_NORMAL){
			//change password
			map = new HashMap<String,Object>();
			map.put("title", getString(R.string.settign_passward));
			listData.add(map);
		}
		
		//mode
		map = new HashMap<String,Object>();
		map.put("title", getString(R.string.view_mode_text));
		listData.add(map);
		
		//version
		map = new HashMap<String,Object>();
		map.put("title", getString(R.string.version_info));
		listData.add(map);
		
		//recommand
		map = new HashMap<String, Object>();
		map.put("title", getString(R.string.send_to_friend));
		listData.add(map);
		//set theme
//		map = new HashMap<String, Object>();
//		map.put("title",getString(R.string.setting_theme));
//		listData.add(map);
		
		//set about
//      map = new HashMap<String, Object>();
//      map.put("title",getString(R.string.setting_about));
//      listData.add(map);
	  return listData;
	}
	@Override
	public void onItemClick(AdapterView<?> viewAdapter, View v, int position, long id) {
	    String itemname = (String)mSettingItemList.get(position).get("itemtitle");
		if(getString(R.string.setting_logout).equals(itemname)){
			SharedPreferences pref = mContext.getSharedPreferences("user",MODE_PRIVATE);
			Editor editor = pref.edit();
			editor.putString("user_name","");
			editor.putString("user_passwad","");
			editor.commit();
			
			new AlertDialog.Builder(SettingActivity.this) 
			.setTitle(getString(R.string.setting_logout)) 
			.setMessage("确定注销？") 
			.setNegativeButton(R.string.ok,new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					LoginActivity.changeLogState(mContext,LoginActivity.LOGIN_STATE_LOGOUT,true);
				    //android.os.Process.killProcess(android.os.Process.myPid());
					//sendBroadcast(new Intent(CloseReceiver.mCloseAction));
					NotificationManager mNotifyMgr = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
					mNotifyMgr.cancel(LoginActivity.NOTIFICATION_ID);
					//mNotifyMgr.notify(LoginActivity.NOTIFICATION_ID,Notification.);
					CloseReceiver.CloseAllActivity();
					CloseReceiver.CloseAllService();
					android.os.Process.killProcess(android.os.Process.myPid());
					return;
				}
			})
			.show();   
		}else if(getString(R.string.setting_update).equals(itemname)){//update
			Intent myIntent = new Intent();
			myIntent.setClass(mContext,UpdateVersionActivity.class);
			startActivity(myIntent);
			
		}else if(getString(R.string.settign_passward).equals(itemname)){ //setting passward
			Intent myIntent = new Intent();
			myIntent.setClass(mContext,ChanagePasswardActivity.class);
			startActivity(myIntent);  
		}else if(getString(R.string.view_mode_text).equals(itemname)){//mode
			SharedPreferences pref = mContext.getSharedPreferences("mode",MODE_PRIVATE);
			int mode = pref.getInt("mode",0);
			String items[]={getString(R.string.view_mode_list),getString(R.string.view_mode_grid)};
			AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
			builder.setTitle(R.string.view_mode_text);
			builder.setSingleChoiceItems(items, mode,new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int position) {
					// TODO Auto-generated method stub
					SharedPreferences pref = mContext.getSharedPreferences("mode",MODE_PRIVATE);
					Editor editor = pref.edit();
					editor.putInt("mode",position);
					editor.commit();
					dialog.dismiss();
					CloseReceiver.CloseAllActivity();
					CloseReceiver.CloseAllService();
					Intent myIntent = new Intent();
					if(position == 0){
						myIntent.setClass(mContext,MenuTabActivity.class);
					}else{
						myIntent.setClass(mContext,MenuGridActivity.class);
					}
					
					startActivity(myIntent);
					finish();
				}
			});
			builder.show();
		}else if(getString(R.string.version_info).equals(itemname)){ //version
		    AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
            builder.setTitle(R.string.version_info);
            builder.setMessage("SHENTANG_VERSION_"+BUILD_VERSION);
            builder.setPositiveButton(R.string.ok, null);
            builder.create().show();
		}else if(getString(R.string.send_to_friend).equals(itemname)){//recommand
			CommonOperate.sendMsg2Friend(this.getApplicationContext());
		}else if(getString(R.string.setting_theme).equals(itemname)){//theme
		    String items[]={getString(R.string.theme_blue),getString(R.string.theme_Red)};
		    int mTheme = ReadConfigFile.getTheme(mContext)-1;
            AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
            builder.setTitle(R.string.view_mode_text);
            builder.setSingleChoiceItems(items, mTheme,new DialogInterface.OnClickListener() {
                
                @Override
                public void onClick(DialogInterface dialog, int position) {
                    SharedPreferences pref = mContext.getSharedPreferences("theme",MODE_PRIVATE);
                    Editor editor = pref.edit();
                    if(position == 0){//blue
                        editor.putString("theme", "blue");
                    }else if(position == 1){//red
                        editor.putString("theme", "red");
                    }
                    editor.commit();
                    dialog.dismiss();
                    CloseReceiver.CloseAllActivity();
                    CloseReceiver.CloseAllService();
                    Intent myIntent = new Intent();
                    myIntent.setClass(mContext,LoginActivity.class);
                    startActivity(myIntent);
                    finish();
                }
            });
            builder.show();
		}else if(getString(R.string.setting_about).equals(itemname)){ //about
		   
			Intent myIntent = new Intent();
			myIntent.setClass(mContext,AboutActivity.class);
			startActivity(myIntent); 
		}
	}
}
