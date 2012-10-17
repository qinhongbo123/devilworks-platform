package com.surfing.weather;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.surfing.channel.ActivityBase;
import com.surfing.channel.ChannelTabActivity;
import com.surfing.channel.MenuGridActivity;
import com.surfing.channel.MenuTabActivity;
import com.surfing.httpconnection.HttpConnectionCallback;
import com.surfing.httpconnection.HttpConnectionUtil;
import com.surfing.httpconnection.ImageDownloader;
import com.surfing.httpconnection.HttpConnectionUtil.HttpMethod;
import com.surfing.rssparse.DomXMLReader;
import com.surfing.rssparse.WeatherCurrent;
import com.surfing.rssparse.WeatherDayInfo;
import com.surfing.rssparse.WeatherInfo;
import com.surfing.util.DisplayWeather;
import com.surfing.util.ReadConfigFile;
import com.surfing.util.SaveRssFile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.surfing.R;

public class WeatherActivity extends ActivityBase implements OnClickListener
{
    private final static String TAG = "WeatherActivity";
    private Context mContext = null;
    private TextView mCurrentTempView = null;
    private TextView mCurrentTempAllView = null;
    private TextView mCurrentTimeView = null;
    private TextView mCurrentDateview = null;
    private TextView mCurrentWeekView = null;
    private TextView mCurrentCondition = null;
    private TextView mCityView = null;
    private Button	 mRefashBtn = null;
    private Button 	 mReplaceBtn = null;
    private ProgressBar mwaittingBar = null;
    private WeatherInfo mWeatherInfo = null;
    private View mOtherDayView = null;
    public static final int REQUEST_GET_CITY = 0;
    private String mCurrentCity = null;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mContext = getApplicationContext();
        setContentView(R.layout.weather_layout);
        mCurrentCity = DisplayWeather.getCurrentCity(mContext);
        setupView();
        updateView();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(resultCode == RESULT_CANCELED){
            return ;
        }
        switch(requestCode){
            case REQUEST_GET_CITY:{
                if((data != null) && (data.hasExtra("city"))){
                    mCurrentCity = data.getStringExtra("city");
                    Log.i(TAG,"the current city = "+mCurrentCity);
                    getWeatherFromWeb(mCurrentCity);
                    DisplayWeather.setCurrentCity(mContext, mCurrentCity);
                }
                
            }
            break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public  String getWeekOfDate(Date dt) {
        String[] weekDays = {getString(R.string.week_seven),
                getString(R.string.week_one), 
                getString(R.string.week_two),
                getString(R.string.week_three),
                getString(R.string.week_four), 
                getString(R.string.week_five), 
                getString(R.string.week_six)};
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0)
            w = 0;
        return weekDays[w];
    }
    private void setupView(){
        mCurrentTempView = (TextView)findViewById(R.id.current_temp_text_id);
        mCurrentTempAllView = (TextView)findViewById(R.id.current_temp_all_id);
        mCurrentTimeView = (TextView)findViewById(R.id.current_time_text_id);
        mCurrentDateview = (TextView)findViewById(R.id.current_date_text_id);
        mCurrentWeekView = (TextView)findViewById(R.id.current_week_day_id);
        mCityView = (TextView)findViewById(R.id.current_city_text_id);
        mOtherDayView = findViewById(R.id.day_temp_info_id);
        mwaittingBar = (ProgressBar)findViewById(R.id.loading_progressBar);
        mCurrentCondition = (TextView)findViewById(R.id.current_condition_id);
        mRefashBtn = (Button)findViewById(R.id.btn_refash_id);
        mReplaceBtn = (Button)findViewById(R.id.btn_replace_id);
        mRefashBtn.setOnClickListener(this);
        mReplaceBtn.setOnClickListener(this);
    }
    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        menu.add(0, menu.FIRST, Menu.NONE,R.string.refrash);
        menu.add(0, menu.FIRST+1, Menu.NONE,R.string.replace_city_text);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId()){
            case Menu.FIRST:{ //refash
                getWeatherFromWeb(mCurrentCity);
            }
            break;
            case Menu.FIRST+1:{
                Intent myItent = new Intent();
                myItent = new Intent();
                myItent.setClass(WeatherActivity.this,CityListActivity.class);
                startActivityForResult(myItent, REQUEST_GET_CITY);
            }
            break;
            default:
                break;
            }
        return super.onOptionsItemSelected(item);
    }
    */
    private void getWeatherFromWeb(String city)
    {
        HttpConnectionUtil connect = new HttpConnectionUtil(getApplicationContext());
        //String url = "http://www.google.com/ig/api?weather=瑗垮畨&hl=zh-cn";
        mwaittingBar.setVisibility(View.VISIBLE);
        String url = ReadConfigFile.getServerAddress(this
                .getApplicationContext())
                + "index.php?controller=user&action=requestwether&city="+city;

        connect.asyncConnect(url, HttpMethod.GET, new HttpConnectionCallback()
        {
            @Override
            public void execute(String response)
            {
                mwaittingBar.setVisibility(View.INVISIBLE);
                if((response == null) 
                        || (response.length() == 0) 
                        || response.equals(HttpConnectionUtil.CONNECT_FAILED)
                        || HttpConnectionUtil.RETURN_FAILED.equalsIgnoreCase(response))
                {
                	mCurrentCondition.setText(getString(R.string.msg_getdate_failed));
                    mRefashBtn.setVisibility(View.INVISIBLE);
                    mReplaceBtn.setVisibility(View.INVISIBLE);
                    Log.i(TAG,"request failed");
                    return;
                }

                // Log.i(TAG, "weather info is:" + response);
                SaveRssFile savefile = new SaveRssFile(WeatherActivity.this.getApplicationContext());
                try
                {
                    savefile.SaveFile(response, "weather.xml");
                }
                catch (Exception e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } 
                InputStream stream = null;
                Log.i(TAG, "weather info is:" + response);
                stream = new ByteArrayInputStream(response.getBytes());
                DisplayWeather.ParseWeather(mContext, stream);
                updateView();
            }
        });
    }
    private void updateView(){
        Log.i(TAG,"the path = "+mContext.getFilesDir().getPath());
        File file = new File(mContext.getFilesDir().getPath()+"/"+"weather.xml");
        if(file.exists()){
            Log.i(TAG,"the file is exist");
            InputStream inputStream = null;
            try
            {
                inputStream = new FileInputStream(file);
            } 
            catch (FileNotFoundException e)
            {
                Log.i(TAG,"Error :"+e.getMessage());
                e.printStackTrace();
            }
            mWeatherInfo = DomXMLReader.readXMLWeather(mContext,inputStream);
            if(mWeatherInfo != null){
                if(mWeatherInfo.getmWeatherCurrent() != null){
                    WeatherCurrent current = mWeatherInfo.getmWeatherCurrent();
                    mCurrentTempView.setText(current.getmTemp_c()+getString(R.string.degree_text));
                    mCurrentTempAllView.setText(current.getmHumidity());
                    mCurrentCondition.setText(current.getmCondition());
                }
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat formatter =   new SimpleDateFormat("HH:mm");  
                Date curDate = calendar.getTime();    
                String strTime =    formatter.format(curDate);   
                formatter =   new SimpleDateFormat("MM月dd日"); 
                String strDate =  formatter.format(curDate);
                mCurrentTimeView.setText(strTime);
                mCurrentDateview.setText(strDate);
                mCurrentWeekView.setText(getWeekOfDate(calendar.getTime()));
                mCityView.setText(mCurrentCity);
                if(mWeatherInfo.getmWeatherDayList() != null){
                    LayoutInflater inflater = getLayoutInflater();
                    View imteLayout = null;
                    TextView weekText = null; 
                    TextView tempAll = null; 
                    ImageView condition = null;
                    WeatherDayInfo weatherDayInfo = null;
                    ImageDownloader imageload = null;
                    ((LinearLayout)mOtherDayView).removeAllViews();
                    for(int i = 0;i<mWeatherInfo.getmWeatherDayList().size();i++){
                        imteLayout = inflater.inflate(R.layout.weather_day_item, null);
                        weekText = (TextView)imteLayout.findViewById(R.id.day_week_text_id);
                        tempAll = (TextView)imteLayout.findViewById(R.id.day_temp_all_id);
                        condition = (ImageView)imteLayout.findViewById(R.id.day_weather_img_id);
                        weatherDayInfo = mWeatherInfo.getmWeatherDayList().get(i);
                        if(weatherDayInfo != null){
                            weekText.setText(weatherDayInfo.getmWeek());
                            tempAll.setText(weatherDayInfo.getmTemp_low()+"/"+weatherDayInfo.getmTemp_High()+getString(R.string.degree_text));
                            Log.i(TAG,"the image url = "+weatherDayInfo.getmIconUrl());
                            condition.getBackground().setAlpha(200);
                            if(weatherDayInfo.getmIconUrl() != null){
                                imageload = new ImageDownloader();
                                imageload.download("http://g0.gstatic.com" +weatherDayInfo.getmIconUrl(),condition,R.drawable.button_bg,getApplicationContext());
                            }
                            ((LinearLayout)mOtherDayView).addView(imteLayout);
                            imteLayout.getBackground().setAlpha(150);
                        }
                        
                    }
                }
                
            }
        }else{
        	Log.i(TAG,"reload the data from web");
        	getWeatherFromWeb(mCurrentCity);
        }
   }

	@Override
	public void onClick(View v) {
		if(v == mRefashBtn){
			getWeatherFromWeb(mCurrentCity);
			
		}else if(v == mReplaceBtn){
			 Intent myItent = new Intent();
		        myItent = new Intent();
		        myItent.setClass(WeatherActivity.this,CityListActivity.class);
		        startActivityForResult(myItent, REQUEST_GET_CITY);
		}
	}
        
}
