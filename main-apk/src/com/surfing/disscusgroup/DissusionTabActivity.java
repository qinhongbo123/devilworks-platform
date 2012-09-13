package com.surfing.disscusgroup;

import com.surfing.channel.CloseReceiver;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

public class DissusionTabActivity extends TabActivity {
	
	private TabHost mTabHost;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		mTabHost = this.getTabHost();
		Intent myIntent = new Intent();
		myIntent.setClass(getApplicationContext(), DisscusActivity.class);
		
		mTabHost.addTab(mTabHost.newTabSpec("热门讨论")
				.setContent(myIntent)
				.setIndicator("热门讨论"));
		
		mTabHost.addTab(mTabHost.newTabSpec("所有讨论")
				.setContent(myIntent)
				.setIndicator("所有讨论"));
		layoutTab();
		CloseReceiver.registerCloseActivity(this);
	}
	private void layoutTab(){
		TabWidget tabWidget = mTabHost.getTabWidget();
		int count = tabWidget.getChildCount(); 
		for (int i = 0; i < count; i++) { 
			View view = tabWidget.getChildTabViewAt(i);   
			view.getLayoutParams().height = 70;
			//view.setBackgroundResource(com.surfing.R.drawable.title);
			
			final TextView tv = (TextView) view.findViewById(android.R.id.title); 
			
			tv.setTextSize(18); 
			tv.setTextColor(this.getResources().getColorStateList( 
					android.R.color.white)); 
			} 
	}
}
