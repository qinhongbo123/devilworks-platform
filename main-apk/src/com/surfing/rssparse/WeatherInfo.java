package com.surfing.rssparse;

import java.util.ArrayList;

public class WeatherInfo
{
    private WeatherCurrent mWeatherCurrent;
    private ArrayList<WeatherDayInfo>  mWeatherDayList = null;
    public WeatherInfo(){
        mWeatherDayList = new ArrayList<WeatherDayInfo>();
    }
    public WeatherCurrent getmWeatherCurrent()
    {
        return mWeatherCurrent;
    }
    public void setmWeatherCurrent(WeatherCurrent mWeatherCurrent)
    {
        this.mWeatherCurrent = mWeatherCurrent;
    }
    public ArrayList<WeatherDayInfo> getmWeatherDayList()
    {
        return mWeatherDayList;
    }
    public void addmWeatherDayList(WeatherDayInfo weatherdayInfo)
    {
        this.mWeatherDayList.add(weatherdayInfo);
    }
    
}
