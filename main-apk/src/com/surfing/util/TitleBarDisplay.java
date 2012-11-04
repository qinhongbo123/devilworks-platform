package com.surfing.util;

import com.surfing.R;
import com.surfing.channel.NetImitate;
import com.surfing.channel.NetImitate.ImageCallback;
import com.surfing.disscusgroup.DisscusActivity;
import com.surfing.httpconnection.ImageDownloader;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

public class TitleBarDisplay
{
    public static void TitleBarInit(TextView mTitleText, final ImageView mTitleIcon, Activity activity, Context context)
    {
        int mTheme = ReadConfigFile.getTheme(context);
        Context mCurrentContext = ReadConfigFile.getCurrentThemeContext(mTheme, context);
        SharedPreferences pref = context.getSharedPreferences("enterprise", Context.MODE_PRIVATE);
        
        String enterprise_name = pref.getString("enterprise_name", "");
        Log.i("TitleBarDisplay", "enterprise_name == " + enterprise_name);
        mTitleText.setText(enterprise_name);
        
        String enterprise_iconAddress = pref.getString("enterprise_icon_url", null);
        updateTitleIcon(enterprise_iconAddress, context, mTitleIcon);
        //skip weather update request
        //DisplayWeather.updateWeatherDisplay(context, activity);
        View view = activity.findViewById(R.id.title_layout_id);
        
        ThemeUpdateUitl.updateTitlebarBg(view, mCurrentContext, R.drawable.title_bg);
    }
    private static void updateTitleIcon(String url, Context context, final ImageView mTitleIcon)
    {
        Log.i("TitleBarDisplay","enterprise_icon_url = "+url);
        if(url != null)
        {
            NetImitate.getInstance(context).downloadAndBindImage(url, new ImageCallback()
            {
                @Override
                public void imageLoaded(Bitmap bitmap, String imageUrl)
                {
                   
                    if (mTitleIcon != null)
                    {
                        mTitleIcon.setImageBitmap(bitmap);
                    }

                }

            });
        }
        
    }
    
    public static String getEnterpriseName(Context context)
    {
        SharedPreferences pref = context.getSharedPreferences("enterprise", Context.MODE_PRIVATE);
        return pref.getString("enterprise_name", "enterprise");

    }

    public static String getUserName(Context context)
    {
        SharedPreferences userpref = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        return userpref.getString("user_name", "");

    }
}
