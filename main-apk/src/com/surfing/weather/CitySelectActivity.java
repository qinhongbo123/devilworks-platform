package com.surfing.weather;

import java.util.ArrayList;
import java.util.HashMap;

import com.surfing.R;
import com.surfing.channel.ActivityBase;
import com.surfing.channel.CommonUpdate;
import com.surfing.channel.OtherChannelActivity;
import com.surfing.util.DisplayWeather;
import com.surfing.util.TitleBarDisplay;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class CitySelectActivity extends ActivityBase {
	private static final String TAG = "CitySelectActivity";
	private SimpleAdapter mListAdapter = null;
	private ListView mList = null;
	private AutoCompleteTextView mSearchText = null;
	private Context mContext = null;
	private ArrayList<HashMap<String,String>> mArrayList = null;
	private ImageView mTitleIcon = null;
    private TextView mTitleText = null;
    private Window mWindow;
    private Handler    myHandler = new Handler(){

        @Override
        public void handleMessage(Message msg)
        {
            switch(msg.what){
                case CommonUpdate.EVENT_UPDATE_WEATHER:{
                    DisplayWeather.updateWeatherDisplay(getApplicationContext(),CitySelectActivity.this);
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
		mContext = getApplicationContext();
		setContentView(R.layout.city_select_layout);
		mWindow = getWindow();
        mWindow.setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.titlebar);
        setupView();
        TitleBarDisplay.TitleBarInit(mTitleText, mTitleIcon,CitySelectActivity.this,getApplicationContext()); 
		mArrayList = getCityInfo();
		mListAdapter = new SimpleAdapter(mContext,mArrayList,R.layout.city_list_item_layout,new String[]{"icon","city"},new int[]{R.id.city_list_img_id,R.id.city_list_text_id});
		CommonUpdate.getInstance().registerForUpdateWeather(myHandler,CommonUpdate.EVENT_UPDATE_WEATHER,null);
	}
	@Override
    protected void onDestroy()
    {
        CommonUpdate.getInstance().unregisterForUpdateWeather(myHandler);
        super.onDestroy();
    }
	@Override
    protected void onResume()
    {
        // TODO Auto-generated method stub
        super.onResume();
        DisplayWeather.updateWeatherDisplay(getApplicationContext(),this);
    }
	private void setupView(){
		mList = (ListView)findViewById(R.id.city_list_id);
		mSearchText = (AutoCompleteTextView)findViewById(R.id.search_text_id);
		mTitleIcon = (ImageView)findViewById(R.id.titlebar_icon_id);
        mTitleText = (TextView)findViewById(R.id.titlebar_text_id);
	}
	private ArrayList<HashMap<String,String>> getCityInfo(){
		ArrayList<HashMap<String,String>> array = new ArrayList<HashMap<String,String>>();
		return array;
	}
}
