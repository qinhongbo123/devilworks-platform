package com.surfing.util;

import java.io.InputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.surfing.R;
import com.surfing.httpconnection.ImageDownloader;
import com.surfing.rssparse.DomXMLReader;
import com.surfing.rssparse.WeatherCurrent;
import com.surfing.rssparse.WeatherDayInfo;
import com.surfing.rssparse.WeatherInfo;
import com.surfing.weather.WeatherActivity;

public class DisplayWeather
{
    private static WeatherInfo mWeatherInfo;

    public static void ParseWeather(Context context, InputStream instream)
    {
        mWeatherInfo = DomXMLReader.readXMLWeather(context, instream);
    }

    public static WeatherInfo getmWeatherInfo()
    {
        return mWeatherInfo;
    }

    public static void setmWeatherInfo(WeatherInfo mWeatherInfo)
    {
        DisplayWeather.mWeatherInfo = mWeatherInfo;
    }

    public static WeatherCurrent getCurrentWeather()
    {
        return mWeatherInfo.getmWeatherCurrent();
    }

    public static ArrayList<WeatherDayInfo> getDayWeatherList()
    {
        return mWeatherInfo.getmWeatherDayList();
    }

    public static void updateWeatherDisplay(final Context context, final Activity activity)
    {
        // don't display weather
        return;
        /*
         * if ((activity == null) || (context == null)) { return; } View view =
         * activity.findViewById(R.id.weather_display_id);
         * Log.i("DisplayWeather","view = "+view); if(view == null){ return; }
         * TextView mCityText = (TextView)
         * activity.findViewById(R.id.city_text_id); TextView mTempCurrent =
         * (TextView) activity.findViewById(R.id.temp_current_id); TextView
         * mConditionText = (TextView)
         * activity.findViewById(R.id.condition_current_id); ImageView
         * mConditionImg = (ImageView)
         * activity.findViewById(R.id.condition_icon_id);
         * mCityText.setText(DisplayWeather.getCurrentCity(context)); if
         * (mWeatherInfo != null) { //Log.i("weather",
         * "getmWeatherCurrent().getmTemp_c() == " +
         * mWeatherInfo.getmWeatherCurrent().getmTemp_c()); //Log.i("weather",
         * "getmWeatherCurrent().getmCondition() == " +
         * mWeatherInfo.getmWeatherCurrent().getmCondition());
         * if(mWeatherInfo.getmWeatherCurrent() != null){
         * mTempCurrent.setText("" +
         * mWeatherInfo.getmWeatherCurrent().getmTemp_c()
         * +context.getString(R.string.degree_text)); Log.i("weather",
         * "getmWeatherCurrent().getmCondition() == " +
         * mWeatherInfo.getmWeatherCurrent().getmCondition());
         * mConditionText.setText
         * (mWeatherInfo.getmWeatherCurrent().getmCondition()); ImageDownloader
         * imgDown = new ImageDownloader(); String url = "http://g0.gstatic.com"
         * + mWeatherInfo.getmWeatherCurrent().getmIconUrl();
         * imgDown.download(url, mConditionImg,
         * R.drawable.notify_online,context); } } view.setOnClickListener(new
         * OnClickListener() {
         * 
         * @Override public void onClick(View arg0) {
         * Log.i("DisplayWeather","onClick"); Intent myIntent = new Intent();
         * myIntent.setClass(activity,WeatherActivity.class);
         * myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         * context.startActivity(myIntent); } });
         */
    }

    public static String getCurrentCity(Context context)
    {
        String city = null;
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences("city", Context.MODE_PRIVATE);
        city = pref.getString("cityname", context.getString(R.string.city_text));
        return city;
    }

    public static void setCurrentCity(Context context, String city)
    {
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences("city", Context.MODE_PRIVATE);
        Editor editor = pref.edit();
        editor.putString("cityname", city);
        editor.commit();
    }
}
