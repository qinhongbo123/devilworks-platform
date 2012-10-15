package com.surfing.channel;

import java.util.ArrayList;

import android.os.Handler;

public class CommonUpdate
{
    public static final int EVENT_UPDATE_WEATHER = 0;
    private static final ArrayList<registerItem> mHandleListWeather = new ArrayList<registerItem>();
    private static CommonUpdate mInstance = new CommonUpdate();

    private CommonUpdate()
    {

    }

    public void registerForUpdateWeather(Handler handler, int what, Object object)
    {
        mHandleListWeather.add(new registerItem(handler, what, object));
    }

    public void unregisterForUpdateWeather(Handler handler)
    {
        registerItem item = null;
        for (int i = 0; i < mHandleListWeather.size(); i++)
        {
            item = mHandleListWeather.get(i);
            if (item.handler == handler)
            {
                mHandleListWeather.remove(i);
                break;
            }
        }
    }

    public void NotifyForUpdateWeather()
    {
        registerItem item = null;
        for (int i = 0; i < mHandleListWeather.size(); i++)
        {
            item = mHandleListWeather.get(i);
            item.handler.sendMessage(item.handler.obtainMessage(item.what, item.object));
        }
    }

    public static CommonUpdate getInstance()
    {
        return mInstance;
    }

    private class registerItem
    {
        public Handler handler;
        public int what;
        public Object object;

        public registerItem(Handler handler, int what, Object object)
        {
            this.handler = handler;
            this.what = what;
            this.object = object;
        }

    }
}
