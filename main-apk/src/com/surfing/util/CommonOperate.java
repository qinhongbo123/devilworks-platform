package com.surfing.util;

import com.surfing.R;
import com.surfing.setting.SettingActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

public class CommonOperate {
	
	public static void sendMsg2Friend(Context context){
		if(context == null){
			return;
		}
		Intent myIntent = new Intent();
		String enterprise_id = null; 
		SharedPreferences pref = context.getSharedPreferences("enterprise",Context.MODE_PRIVATE);
		enterprise_id=pref.getString("enterprise_id","1");
		String path = ReadConfigFile.getServerAddress(context) + "apk/"+enterprise_id+"/surf-platform-"+SettingActivity.BUILD_VERSION+".apk";
	    //Intent sendIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("sms:")); 
	    myIntent.setAction(Intent.ACTION_SENDTO);
	    myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    myIntent.setData( Uri.parse("sms:"));
	    myIntent.putExtra("sms_body", context.getString(R.string.msg_recommand)+path); 
	    context.startActivity(myIntent);
	   // myInent.setClass(getApplicationContext(),WeatherActivity.class);
	}
}
