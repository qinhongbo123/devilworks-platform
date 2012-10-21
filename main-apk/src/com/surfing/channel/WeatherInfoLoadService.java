package com.surfing.channel;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.surfing.httpconnection.HttpConnectionCallback;
import com.surfing.httpconnection.HttpConnectionUtil;
import com.surfing.httpconnection.HttpConnectionUtil.HttpMethod;
import com.surfing.util.DisplayWeather;
import com.surfing.util.ReadConfigFile;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class WeatherInfoLoadService extends Service
{
    private static final String TAG = "WeatherInfoLoad";
    private Context mContext;
    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public void onCreate()
    {
        mContext = getApplicationContext();
        // TODO Auto-generated method stub
        CloseReceiver.registerCloseService(this);
        super.onCreate();
    }
    
    @Override
    public void onDestroy()
    {
        CloseReceiver.unRegisterService(this);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        HttpConnectionUtil connect = new HttpConnectionUtil(getApplicationContext());
        //String url = "http://www.google.com/ig/api?weather=西安&hl=zh-cn";
        String city = DisplayWeather.getCurrentCity(mContext);
        String url = ReadConfigFile.getServerAddress(getApplicationContext())
                + "index.php?controller=user&action=requestwether&city="+city;

        connect.asyncConnect(url, HttpMethod.GET, new HttpConnectionCallback()
        {
            @Override
            public void execute(String response)
            {
                if((response == null) 
                        || (response.length() == 0) 
                        || response.equals(HttpConnectionUtil.CONNECT_FAILED)
                        || HttpConnectionUtil.RETURN_FAILED.equalsIgnoreCase(response))
                {
                    Log.i(TAG,"request failed");
                    return;
                }
                InputStream stream = null;
                Log.i(TAG, "weather info is:" + response);
                stream = new ByteArrayInputStream(response.getBytes());
                DisplayWeather.ParseWeather(mContext, stream);
                CommonUpdate.getInstance().NotifyForUpdateWeather();
            }
        });
        return super.onStartCommand(intent, flags, startId);
    }
    

}
