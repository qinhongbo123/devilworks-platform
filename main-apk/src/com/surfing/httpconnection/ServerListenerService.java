package com.surfing.httpconnection;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;


import com.surfing.R;
import com.surfing.Notification.NotificationDialog;
import com.surfing.Notification.NotificationProviderMetaData;
import com.surfing.Notification.NotificationProviderMetaData.UserTableMetaData;
import com.surfing.channel.CloseReceiver;
import com.surfing.httpconnection.HttpConnectionUtil.HttpMethod;
import com.surfing.login.LoginActivity;
import com.surfing.rssparse.ChannelInformation;
import com.surfing.rssparse.ChannelItem;
import com.surfing.rssparse.DomXMLReader;
import com.surfing.util.ReadConfigFile;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class ServerListenerService extends Service {
	private static final String TAG = "ServerListenerService";
	private HttpConnectionUtil mClientUtil = null;
	private Context mContext = null;
	private static String user_name ;
	private static String user_passwad ;
	private NotificationManager mNotifyMgr;
	ListenerThread thread = null;
	NetWorkListener networkListener;
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mClientUtil = new HttpConnectionUtil(getApplicationContext());
		mContext = this.getApplicationContext();
		IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
		networkListener = new NetWorkListener();
		this.registerReceiver(networkListener, filter);
		thread = new ListenerThread();
		thread.start();
		mNotifyMgr = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		//just to show the notify on the statebar
		LoginActivity.changeLogState(mContext, LoginActivity.getLogState(mContext),true);
		CloseReceiver.registerCloseService(this);
	}
	
	@Override
    public void onDestroy()
    {
	    this.unregisterReceiver(networkListener);
	    CloseReceiver.unRegisterService(this);
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        // TODO Auto-generated method stub
        return super.onUnbind(intent);
    }

    public boolean  goodNet()  
    {    
        ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);      
        NetworkInfo networkinfo = manager.getActiveNetworkInfo();      
        if (networkinfo == null || !networkinfo.isAvailable()) {      
        	Log.i(TAG,"no NetWork available");
        	
            return false;    
        }  
        return true;    
    }
	public class ConnectCallback implements HttpConnectionCallback{

		@Override
		public void execute(String response) {
			ContentResolver resolver = mContext.getContentResolver();
			Log.i(TAG,"response string is : "+response);
			if((response == null) 
                || (response.length() == 0) 
                || response.equals(HttpConnectionUtil.CONNECT_FAILED)
                || HttpConnectionUtil.RETURN_FAILED.equalsIgnoreCase(response)){
                    
                    return;
                }
			try{
				Log.i(TAG,"connect success");
				int nindex = 0;
    				
				InputStream stream = new ByteArrayInputStream(response.substring(nindex).getBytes());
				Log.i(TAG,response.substring(nindex));
				ChannelInformation channelInfo = DomXMLReader.readXML(mContext,stream);
				if(channelInfo != null){
					ArrayList<ContentProviderOperation> operationList  = new ArrayList<ContentProviderOperation>();
					ContentProviderOperation.Builder builder = null;
					ContentValues values = null;
					ArrayList<ChannelItem> channlelist = (ArrayList<ChannelItem>) channelInfo.getmChannelItemList();
					SharedPreferences userpref = mContext.getSharedPreferences("user",MODE_PRIVATE);
					String user_name = userpref.getString("user_name", "");
					for(int i = 0;i<channlelist.size();i++){
						builder = ContentProviderOperation.newInsert(UserTableMetaData.CONTENT_URI); 
						values = new ContentValues();
						values.put(NotificationProviderMetaData.NOTIFICATION_USER,user_name);
						values.put(NotificationProviderMetaData.NOTIFICATION_TITLE,channlelist.get(i).getmTitle());
						values.put(NotificationProviderMetaData.NOTIFICATION_CONTENT,channlelist.get(i).getmDescription());
						builder.withValues(values);
						operationList.add(builder.build());
					}
					if(operationList.size()>0){
					    resolver.applyBatch(NotificationProviderMetaData.AUTHORIY,operationList);
	                    //notify
	                    Intent notifyIntent = new Intent();
	                    notifyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
	                    notifyIntent.setClass(ServerListenerService.this,NotificationDialog.class);
	                    ServerListenerService.this.startActivity(notifyIntent);
	                    showNotification(R.drawable.notify_online,
	                            getResources().getString(R.string.notify_state_display),
	                            null,
	                            getResources().getString(R.string.notify_state_list_text));
					}
					
				}
				
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}

	public void showNotification(int icon,String tickertext,String title,String content){
    	Notification notification=new Notification(icon,tickertext,System.currentTimeMillis());
    	notification.defaults=Notification.DEFAULT_ALL; 
    	
    	PendingIntent pt = PendingIntent.getActivity(this, 0, new Intent(this,LoginActivity.class), 0);
    	notification.setLatestEventInfo(this,title,content,pt);
    	mNotifyMgr.notify(LoginActivity.NOTIFICATION_ID, notification);
    }
	private class ListenerThread extends Thread {

		@Override
		public void run() {
			while(true){
				if(LoginActivity.getLogState(mContext).equals(LoginActivity.LOGIN_STATE_LOGINED)){
					SharedPreferences userpref = mContext.getSharedPreferences("user",MODE_PRIVATE);
					String user_name = userpref.getString("user_name", "");
					String url = ReadConfigFile.getServerAddress(mContext)+"index.php?controller=Notification&action=PhoneReqNotificationList&user_name="+user_name;
					mClientUtil.syncConnect(url,HttpMethod.GET,new ConnectCallback());
				}
				try {
					Thread.sleep(60000);
				} catch (InterruptedException e) {
					
				}
			}
		}
		 
	}
	private void login(){
	   SharedPreferences pref = mContext.getSharedPreferences("user",MODE_PRIVATE);
	   user_name = pref.getString("user_name", "");
	   user_passwad = pref.getString("user_passwad","");
	   Log.i(TAG,"the user name is = "+user_name+" the passwad is = "+user_passwad);
	   String geturl = ReadConfigFile.getServerAddress(mContext)+"index.php" + "?" + "controller=user&action=PhoneLogin&username="+user_name
				+ "&password="+user_passwad;
		HttpConnectionUtil connect = new HttpConnectionUtil(getApplicationContext());
		connect.asyncConnect(geturl, HttpMethod.POST,new HttpConnectionCallback(){

			@Override
			public void execute(String response) {
			    if((response == null) 
                        || (response.length() == 0) 
                        || response.equals(HttpConnectionUtil.CONNECT_FAILED)
                        || HttpConnectionUtil.RETURN_FAILED.equalsIgnoreCase(response)){
					Log.i(TAG,"Login Error");
		       		SharedPreferences pref = mContext.getSharedPreferences(LoginActivity.LOGIN_STATE,MODE_PRIVATE);
				    Editor editor = pref.edit();
				    editor.putString(LoginActivity.LOGIN_STATE,LoginActivity.LOGIN_STATE_LOGOUT);
				    editor.commit();
				    LoginActivity.changeLogState(mContext,LoginActivity.LOGIN_STATE_LOGOUT,true);
				}else{
					Log.i(TAG,"Login success");
		       		SharedPreferences pref = mContext.getSharedPreferences("user",MODE_PRIVATE);
				    Editor editor = pref.edit();
				    editor.putString("user_name",user_name);
				    editor.putString("user_passwad",user_passwad);
				    editor.commit();
				    LoginActivity.changeLogState(mContext,LoginActivity.LOGIN_STATE_LOGINED,true);
				}
			}
			
		});
	}
	private class NetWorkListener extends BroadcastReceiver{
		
		@Override
		public void onReceive(Context context, Intent intent) {
		   if (goodNet() && LoginActivity.getLogState(mContext).equals(LoginActivity.LOGIN_STATE_LOGOUT)){
			   login();
		   }else{
		       
	       		//LoginActivity.changeLogState(mContext,LoginActivity.LOGIN_STATE_LOGOUT,true);
		   }
		}
	}
}
