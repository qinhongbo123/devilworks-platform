package com.surfing.httpconnection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
	private static final String TAG = "PhoneStartBroadCast";
	@Override
	public void onReceive(Context context, Intent intent) {
		if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())){
			Log.i(TAG,"boot completed");
			Intent myIntent = new Intent();
			myIntent.setClass(context, ServerListenerService.class);
			context.startService(myIntent);
		}
	}

}
