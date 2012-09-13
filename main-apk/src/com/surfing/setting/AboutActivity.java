package com.surfing.setting;

import java.io.InputStream;

import com.surfing.channel.ActivityBase;
import com.surfing.channel.ChannelTabActivity;
import com.surfing.util.ReadConfigFile;
import com.surfing.util.ThemeUpdateUitl;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import com.surfing.R;

public class AboutActivity extends ActivityBase {
	private TextView mAboutText = null;
	private TextView mTitleText = null;
	private Context mContext = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState,TITLE_TYPE_DEFINE_TEXT,getString(R.string.setting_about),0,false);
		mContext = getApplicationContext();
		setContentView(R.layout.about_layout);
		mAboutText = (TextView)findViewById(R.id.about_text_id);
		InputStream isr = null;  
	    String databuffer = null; 
	    byte databyte[] = null;
	    try{
	        isr = mContext.getResources().getAssets().open("updateinfo.txt");
	        int longs = isr.available();
	        databyte = new byte[longs];
	        isr.read(databyte);
	        databuffer = new String(databyte,"UTF-8");
	        mAboutText.setText(databuffer);
	        isr.close();
	    }catch (Exception e) {
        e.printStackTrace();
       }
	}
	
}
