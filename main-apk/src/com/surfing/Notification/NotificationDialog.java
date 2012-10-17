package com.surfing.Notification;

import com.surfing.R;
import com.surfing.Notification.NotificationProviderMetaData.UserTableMetaData;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class NotificationDialog extends Activity {
	private TextView mNotifyText = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		requestWindowFeature(Window.FEATURE_NO_TITLE);
	    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.notify_dialog_layout);
		setupView();
		ContentResolver resolver = getContentResolver();
		Cursor cursor = resolver.query(UserTableMetaData.CONTENT_URI,new String[]{NotificationProviderMetaData.NOTIFICATION_ID,NotificationProviderMetaData.NOTIFICATION_TITLE,NotificationProviderMetaData.NOTIFICATION_CONTENT},NotificationProviderMetaData.NOTIFICATION_READFLAG+"=0",null,null);
		if(cursor.getCount() > 0){
			cursor.moveToFirst();
			mNotifyText.setText(cursor.getString(2));
			ContentValues values = new ContentValues();
            values.put(NotificationProviderMetaData.NOTIFICATION_TITLE,cursor.getString(1));
            values.put(NotificationProviderMetaData.NOTIFICATION_CONTENT,cursor.getString(2));
            values.put(NotificationProviderMetaData.NOTIFICATION_READFLAG,1);
            resolver.update(UserTableMetaData.CONTENT_URI,values,NotificationProviderMetaData.NOTIFICATION_ID+"="+cursor.getInt(0),null);
		}
		//getUnreadNotifiction();
	}
	private void setupView(){
		mNotifyText = (TextView)findViewById(R.id.notify_content_id);
	}
	private void getUnreadNotifiction(){
		ContentResolver resolver = getContentResolver();
		Cursor cursor = resolver.query(UserTableMetaData.CONTENT_URI,new String[]{NotificationProviderMetaData.NOTIFICATION_ID},NotificationProviderMetaData.NOTIFICATION_READFLAG+"=0",null,null);
		if((cursor != null) && (cursor.getCount() > 0)){
			AlertDialog.Builder builder = new AlertDialog.Builder(NotificationDialog.this)
			.setTitle(R.string.app_name)
			.setMessage(getString(R.string.message_notify_start)+cursor.getCount()+getString(R.string.message_notify_end))
			.setPositiveButton(R.string.btn_view,new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					Intent myIntent = new Intent();
					myIntent.setClass(getApplicationContext(),NotificationListActivity.class);
					NotificationDialog.this.startActivity(myIntent);
					finish();
				}
			})
			.setNegativeButton(R.string.btn_cancel,new OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					finish();
				}
			});
			builder.create();
			builder.show();
			cursor.close();
		}
	}
}
