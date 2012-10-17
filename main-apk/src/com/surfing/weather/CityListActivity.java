package com.surfing.weather;

import java.util.ArrayList;
import java.util.HashMap;

import com.surfing.R;
import com.surfing.channel.ActivityBase;
import com.surfing.channel.CommonUpdate;
import com.surfing.channel.MenuTabActivity;
import com.surfing.channel.NewsInformationActivity;
import com.surfing.util.DisplayWeather;
import com.surfing.util.ReadConfigFile;
import com.surfing.util.ThemeUpdateUitl;
import com.surfing.util.TitleBarDisplay;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class CityListActivity extends ActivityBase
{
    private static String TAG = "CityListActivity";
    private AutoCompleteTextView mSearchText = null;
    private ListView mList = null;
    private ArrayList<HashMap<String,String>> mArrayList = null;
    private SimpleAdapter mListAdapter = null;
    private Context mContext = null;
    private Window mWindow;
    private int mTheme = 0;
    private Context    mCurrentContext;
    private Handler    myHandler = new Handler(){

        @Override
        public void handleMessage(Message msg)
        {
            switch(msg.what){
                case CommonUpdate.EVENT_UPDATE_WEATHER:{
                    DisplayWeather.updateWeatherDisplay(getApplicationContext(),CityListActivity.this);
                }
                break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
        
    };
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
    	super.onCreate(savedInstanceState,TITLE_TYPE_COMPANY,null,0,true);
        mContext = getApplicationContext();
        setContentView(R.layout.city_select_layout);
        mTheme = ReadConfigFile.getTheme(getApplicationContext());
        setupView();
        mCurrentContext = ReadConfigFile.getCurrentThemeContext(mTheme,getApplicationContext()); 
        mArrayList = getCity();
        mListAdapter = new SimpleAdapter(mContext,
                        mArrayList,
                        R.layout.city_list_item_layout,
                        new String[]{"city"},
                        new int[]{R.id.city_list_text_id});
        mList.setAdapter(mListAdapter);
        mList.setOnItemClickListener(new listItemOnclick());
        mSearchText.setAdapter(mListAdapter);
        mSearchText.setOnItemClickListener(new OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position,
                    long id)
            {
                HashMap<String,String> map = null;
                map = (HashMap<String,String>)mListAdapter.getItem(position);
                mSearchText.setText(map.get("city").toString());
                mSearchText.setSelection(map.get("city").toString().length());
            }
        });
        CommonUpdate.getInstance().registerForUpdateWeather(myHandler,CommonUpdate.EVENT_UPDATE_WEATHER,null);
    }
    @Override
    protected void onDestroy()
    {
        CommonUpdate.getInstance().unregisterForUpdateWeather(myHandler);
        super.onDestroy();
    }
    private void setupView(){
        mSearchText = (AutoCompleteTextView)findViewById(R.id.search_text_id);
        mList = (ListView)findViewById(R.id.city_list_id);
    }
    
    private ArrayList<HashMap<String,String>> getCity(){
        ArrayList<HashMap<String,String>> array = new ArrayList<HashMap<String,String>>();
        String city[] = getResources().getStringArray(R.array.city_name);
        HashMap<String,String> map = null;
        for(int i = 0;i<city.length;i++){
            map = new HashMap<String,String>();
            map.put("city",city[i]);
            array.add(map);
        }
        return array;
    }
    private class listItemOnclick implements OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> adapter, View v, int position,
                long id)
        {
            HashMap<String,String> map = null;
            map = (HashMap<String,String>)mListAdapter.getItem(position);
            Intent myIntent = new Intent();
            myIntent.putExtra("city", map.get("city").toString());
            setResult(RESULT_OK,myIntent);
            finish();
        }
        
    }
}
