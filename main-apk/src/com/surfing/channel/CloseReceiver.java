package com.surfing.channel;

import java.util.ArrayList;

import com.surfing.R;
import com.surfing.login.LoginActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.Service;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class CloseReceiver {

	public static String mCloseAction = "com.surfing.close";
	static CloseReceiver mCloseReceiver = new CloseReceiver();
	static ArrayList<Activity> mActivityList = new ArrayList<Activity>();
	static ArrayList<Service> mServiceList = new ArrayList<Service>();
	private CloseReceiver(){
		
	}
	public static ArrayList<Activity> getActivityList(){
        return mActivityList;
        
    }
	public static CloseReceiver getInstance(){
		return mCloseReceiver;
	}
	public static void registerCloseActivity(Activity activity){
	    if(activity == null){
	        return;
	    }
		mActivityList.add(activity);
		if(mActivityList.size() > 0){
		    NotificationManager mNotifyMgr = (NotificationManager)activity.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotifyMgr.cancel(LoginActivity.NOTIFICATION_ID);
		}
	}
	public static void unRegisterActivity(Activity activity){
	    if(activity == null){
	        return;
	    }
	    mActivityList.remove(activity);
	}
	public static void registerCloseService(Service service){
	    if(service == null){
	        return;
	    }
	    mServiceList.add(service);
    }
	public static void unRegisterService(Service service){
	    if(service == null){
	        return;
	    }
	    mServiceList.remove(service);
	}
	public static void CloseAllActivity(){
		for(int i = 0;i<mActivityList.size();i++){
			Log.i("chenmei","if(intent.getAction().equals(mCloseAction)) i == "+i);
			mActivityList.get(i).finish();
		}
		mActivityList.clear();
		
	}
	public static void CloseAllService(){
        for(int i = 0;i<mServiceList.size();i++){
            mServiceList.get(i).stopSelf();
        }
        
    }
}
