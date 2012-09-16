package com.surfing.channel;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import com.surfing.R;
import com.surfing.Notification.NotificationListActivity;
import com.surfing.rssparse.ChannelInformation;
import com.surfing.rssparse.ChannelItem;
import com.surfing.rssparse.DomXMLReader;
import com.surfing.util.ReadConfigFile;
import com.surfing.util.ThemeUpdateUitl;
import com.surfing.weather.CityListActivity;
import com.surfing.weather.WeatherActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class MoreChannleInfoActivity extends ActivityBase implements OnItemClickListener{
	private static final String TAG = "OtherChannelActivity";
	private SimpleAdapter mChannelListAdapter;
	private ArrayList<HashMap<String, Object>> mChannelItemList;
	private ListView mChannelList;
	private ArrayList<HashMap<String, Object>> mChannelData;
	private int mMode = 0;
	private Context mContext = null;
	private TextView mTitleText = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mContext = getApplicationContext();
		int mMode = ReadConfigFile.getMode(mContext);
		if(mMode == 1){
			requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		}else{
			requestWindowFeature(Window.FEATURE_NO_TITLE);
		}
		setContentView(R.layout.otherchannel_layout);
		if(mMode == 1){
            Window mWindow = getWindow();
            mWindow.setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.title_style2);
    		mTitleText = (TextView)findViewById(R.id.titlebar_text_id);
    		mTitleText.setText(getString(R.string.channel_more));
    		int mTheme = ReadConfigFile.getTheme(mContext);
            Context mCurrentContext = ReadConfigFile.getCurrentThemeContext(mTheme,mContext); 
            View view = findViewById(R.id.title_layout_id);
            ThemeUpdateUitl.updateTitlebarBg(view, mCurrentContext,R.drawable.title_bg);

		}
		HashMap<String,Object> map = null;
		mChannelItemList = new ArrayList<HashMap<String, Object>>();
		mChannelData = getChannel();
		for(int i = 0;i<mChannelData.size();i++){
			map = new HashMap<String,Object>();
			map.put("ChannelName",mChannelData.get(i).get("title").toString());
			mChannelItemList.add(map);
		}
		mChannelListAdapter = new SimpleAdapter(getApplicationContext(), 
												mChannelItemList, 
												R.layout.otherchannel_listitem_layout, 
												new String[]{"ChannelName"},
												new int[]{R.id.channel_listitem_id});
		mChannelList = (ListView)findViewById(R.id.channel_list_id);
		mChannelList.setAdapter(mChannelListAdapter);
		mChannelList.setOnItemClickListener(this);
		LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
		mChannelList.setLayoutParams(params);
		CloseReceiver.registerCloseActivity(this);
	}
	
	@Override
    protected void onStart()
    {
        mMode = ReadConfigFile.getMode(getApplicationContext());
        super.onStart();
    }

    @Override
    protected void onDestroy()
    {
	    CloseReceiver.unRegisterActivity(this);
        super.onDestroy();
       
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
	private ArrayList<HashMap<String,Object>> getChannel(){
		ArrayList<HashMap<String,Object>> channelArray = new ArrayList<HashMap<String,Object>>();
		HashMap<String,Object> map = null;
//		map = new HashMap<String,Object>();
//		map.put("title",getString(R.string.inner_message));
//		map.put("link","");
		//channelArray.add(map);
		/* delete weather
		map = new HashMap<String,Object>();
		map.put("title",getString(R.string.weather_setting));
		map.put("link","");
		channelArray.add(map);
		*/
		String ColumnInfor = this.getIntent().getStringExtra(ChannelTabActivity.COLUMN_INFO_TAG);
		if(ColumnInfor == null){
			File file = new File(getApplicationContext().getFilesDir().getPath()+"/"+"channel.xml");
	        if(file.exists()){
	            Log.i(TAG,"the file is exist");
	            InputStream inputStream = null;
	            try
	            {
	                inputStream = new FileInputStream(file);
		            int length = inputStream.available();
		            byte[] buffer = new byte[length];
		            inputStream.read(buffer);
		            ColumnInfor = new String(buffer);
		            
	            } 
	            catch (FileNotFoundException e)
	            {
	                Log.i(TAG,"Error :"+e.getMessage());
	                e.printStackTrace();
	            } catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            
	        }
		}
		if(ColumnInfor != null){
			int nindex = 0;
		
			while(ColumnInfor.getBytes()[nindex] != '<'){
				nindex++;
			}
			InputStream stream = new ByteArrayInputStream(ColumnInfor.substring(nindex).getBytes());
			ChannelInformation channelInfo = DomXMLReader.readXML(MoreChannleInfoActivity.this,stream);
			//Log.i(TAG,ColumnInfor.substring(nindex));
			if(channelInfo != null){
				ArrayList<ChannelItem> channlelist = (ArrayList<ChannelItem>) channelInfo.getmChannelItemList();
				for(int i = ChannelTabActivity.mDisplayCont;i<channlelist.size();i++){
					Log.i(TAG,"the title is : "+channlelist.get(i).getmTitle());
					map = new HashMap<String,Object>();
					map.put("title",channlelist.get(i).getmTitle());
					map.put("link",channlelist.get(i).getmLink());
					channelArray.add(map);
				}
			}
			
		}
		/* hidden bianmin
		map = new HashMap<String,Object>();
        map.put("title",getString(R.string.title_bianming));
        map.put("link","");
        channelArray.add(map);
        */
		return channelArray;
	}
	@Override
	public void onItemClick(AdapterView<?> AdapterView, View v, int position, long id) {
		HashMap<String, Object> map = mChannelData.get(position);
		String link = (String)map.get("link");
		String itemname = (String)map.get("title");
		/*if(link.length() != 0)*/{
			Intent myIntent = new Intent();
			if(getString(R.string.inner_message).equals(itemname)){
				myIntent.setClass(getApplicationContext(), NotificationListActivity.class);
			}else if(getString(R.string.weather_setting).equals(itemname)){
			    myIntent.setClass(getApplicationContext(), WeatherActivity.class); 
			}else if(getString(R.string.title_bianming).equals(itemname)){
                myIntent.setClass(getApplicationContext(), MoreModuleActivity.class); 
            }else{
				myIntent.setClass(getApplicationContext(), OtherChannelActivity.class);
			}
			myIntent.putExtra(ChannelTabActivity.CHANNLE_LINK,link);
			startActivity(myIntent);
		}
	}
}
