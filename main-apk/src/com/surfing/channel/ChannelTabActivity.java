package com.surfing.channel;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.surfing.R;
import com.surfing.rssparse.ChannelInformation;
import com.surfing.rssparse.ChannelItem;
import com.surfing.rssparse.DomXMLReader;
import com.surfing.util.DisplayWeather;
import com.surfing.util.ReadConfigFile;
import com.surfing.util.TitleBarDisplay;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.ImageView;
import android.widget.TabWidget;
import android.widget.TextView;

public class ChannelTabActivity extends TabActivity
{
    private static final String TAG = "ChannelTabActivity";
    public static final String COLUMN_INFO_TAG = "collum_info";
    public static final String CHANNLE_LINK = "channel_link_id";
    public static final String CHANNEL_TITLE = "title";
    private TabHost mTabHost;
    private Context mContext = null;
    public static final int mDisplayCont = 2;
    private ImageView mTitleIcon = null;
    private TextView mTitleText = null;
    private int mTheme = 0;
    private Context mCurrentContext;
    /*
    private Handler myHandler = new Handler()
    {

        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
            case CommonUpdate.EVENT_UPDATE_WEATHER:
            {
                if (ReadConfigFile.getTheme(mContext) == 1)
                {
                    DisplayWeather.updateWeatherDisplay(getApplicationContext(), ChannelTabActivity.this);
                }
            }
                break;
            default:
                break;
            }
            super.handleMessage(msg);
        }

    };
*/
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        mTheme = ReadConfigFile.getTheme(mContext);
        SharedPreferences prefMode = mContext.getSharedPreferences("mode", MODE_PRIVATE);
        int mMode = prefMode.getInt("mode", 0);
        if (mMode == 1)
        {
            requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        }
        else
        {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
        mCurrentContext = ReadConfigFile.getCurrentThemeContext(mTheme, getApplicationContext());
        mTabHost = this.getTabHost();
        if (mMode == 1)
        {
            Window mWindow = getWindow();
            mWindow.setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);
            setupView();
            TitleBarDisplay.TitleBarInit(mTitleText, mTitleIcon, ChannelTabActivity.this, mContext);

        }

        String ColumnInfor = this.getIntent().getStringExtra(COLUMN_INFO_TAG);
        if (ColumnInfor == null)
        {
            File file = new File(mContext.getFilesDir().getPath() + "/channel.xml");
            if (file.exists())
            {
                Log.i(TAG, file.getPath() + "the file exists");
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
                    Log.i(TAG, "Error :" + e.getMessage());
                    e.printStackTrace();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

            }
        }
        ArrayList<ChannelItem> channlelist = null;
        int index = 0;
        if ((ColumnInfor != null) && (ColumnInfor.length() > 0) && !"failed".equals(ColumnInfor))
        {
            int nindex = 0;
            Log.i(TAG, "the ColunmInfo == " + ColumnInfor);
            byte[] ColumnByte = ColumnInfor.getBytes();
            for (int i = 0; i < ColumnByte.length; i++)
            {
                if (ColumnByte[i] == '<')
                {
                    break;
                }
                else
                {
                    nindex++;
                }
            }

            InputStream stream = new ByteArrayInputStream(ColumnInfor.substring(nindex).getBytes());
            ChannelInformation channelInfo = DomXMLReader.readXML(ChannelTabActivity.this, stream);
            if (channelInfo != null)
            {
                Log.i(TAG, ColumnInfor.substring(nindex));
                channlelist = (ArrayList<ChannelItem>) channelInfo.getmChannelItemList();
                Intent myIntent = null;

                int num = channlelist.size() > mDisplayCont ? mDisplayCont : channlelist.size();
                for (int i = 0; i < num; i++)
                {
                    myIntent = new Intent();
                    myIntent.setClass(getApplicationContext(), ChannelActivityOne.class);
                    Log.i(TAG, "the title is : " + channlelist.get(i).getmTitle());
                    Log.i(TAG, "the getmLink is : " + channlelist.get(i).getmLink());
                    myIntent.putExtra(CHANNLE_LINK, channlelist.get(i).getmLink());
                    mTabHost.addTab(mTabHost.newTabSpec(channlelist.get(i).getmTitle()).setContent(myIntent).setIndicator(getTabItemView(index, channlelist.get(i).getmTitle())));
                    index++;
                }
            }
        }
        Intent myIntent2 = new Intent();
        myIntent2.setClass(getApplicationContext(), ChannelActivityOne.class);
        myIntent2.putExtra(CHANNLE_LINK, "public");
        mTabHost.addTab(mTabHost.newTabSpec(getString(com.surfing.R.string.channel_public)).setContent(myIntent2).setIndicator(getTabItemView(index, getString(com.surfing.R.string.channel_public))));
        index++;

        Intent myIntentMore = new Intent();

        if ((channlelist != null) && (channlelist.size() > mDisplayCont))
        {
            String response = getIntent().getStringExtra(ChannelTabActivity.COLUMN_INFO_TAG);
            myIntentMore.putExtra(ChannelTabActivity.COLUMN_INFO_TAG, response);
        }
        myIntentMore.setClass(getApplicationContext(), MoreChannleInfoActivity.class);
        mTabHost.addTab(mTabHost.newTabSpec(getString(com.surfing.R.string.channel_more)).setContent(myIntentMore).setIndicator(getTabItemView(index, getString(com.surfing.R.string.channel_more))));
        layoutTab();
        CloseReceiver.registerCloseActivity(this);
        // CommonUpdate.getInstance().registerForUpdateWeather(myHandler,CommonUpdate.EVENT_UPDATE_WEATHER,null);
    }

    @Override
    public void onBackPressed()
    {
        Log.i(TAG, "ChannelTabActivity onBackPressed");
        // TODO Auto-generated method stub
        super.onBackPressed();
    }

    @Override
    protected void onDestroy()
    {
        // CommonUpdate.getInstance().unregisterForUpdateWeather(myHandler);
        CloseReceiver.unRegisterActivity(this);
        super.onDestroy();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        SharedPreferences prefMode = mContext.getSharedPreferences("mode", MODE_PRIVATE);
        int mMode = prefMode.getInt("mode", 0);
        if (mMode == 1)
        {
            DisplayWeather.updateWeatherDisplay(mContext, ChannelTabActivity.this);
        }
    }

    private View getTabItemView(int index, String text)
    {
        LayoutInflater mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = mLayoutInflater.inflate(R.layout.tab_item_view, null);

        TextView textView = (TextView) view.findViewById(R.id.text_view_id);
        textView.setText(text);
        return view;
    }

    private void setupView()
    {
        mTitleIcon = (ImageView) findViewById(R.id.titlebar_icon_id);
        mTitleText = (TextView) findViewById(R.id.titlebar_text_id);
    }

    private void layoutTab()
    {
        TabWidget tabWidget = mTabHost.getTabWidget();
        int count = tabWidget.getChildCount();
        int nCurrentIndex = 0;
        nCurrentIndex = mTabHost.getCurrentTab();
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int mDensity = metrics.densityDpi;
        for (int i = 0; i < count; i++)
        {
            View view = tabWidget.getChildTabViewAt(i);
            if (mDensity == 240)
            {
                view.getLayoutParams().height = 65;
            }
            else if (mDensity == 160)
            {
                view.getLayoutParams().height = 35;
            }
            else if (mDensity == 120)
            {
                view.getLayoutParams().height = 35;
            }
            final TextView tv = (TextView) view.findViewById(R.id.text_view_id);
            if (mCurrentContext != null)
            {
                view.setBackgroundDrawable(mCurrentContext.getResources().getDrawable(R.drawable.tab_bg));
            }
            else
            {
                view.setBackgroundResource(R.drawable.tab_bg);
            }

            if (i == nCurrentIndex)
            {
                if (mCurrentContext != null)
                {
                    tv.setBackgroundDrawable(mCurrentContext.getResources().getDrawable(com.surfing.R.drawable.table_sel));
                }
                else
                {
                    tv.setBackgroundResource(com.surfing.R.drawable.table_sel);
                }
                if (mTheme == ReadConfigFile.THEME_RED)
                {
                    tv.setTextColor(Color.WHITE);
                }

            }
            else
            {
                if (mCurrentContext != null)
                {
                    tv.setBackgroundDrawable(mCurrentContext.getResources().getDrawable(com.surfing.R.drawable.table_unsel));
                }
                else
                {
                    tv.setBackgroundResource(com.surfing.R.drawable.table_unsel);
                }
                if (mTheme == ReadConfigFile.THEME_RED)
                {
                    tv.setTextColor(Color.BLACK);
                }

            }
        }
        mTabHost.setOnTabChangedListener(new OnTabChangeListener()
        {

            @Override
            public void onTabChanged(String arg0)
            {
                TabWidget tabWidget = mTabHost.getTabWidget();
                int count = tabWidget.getChildCount();
                int nCurrentIndex = 0;
                nCurrentIndex = mTabHost.getCurrentTab();
                for (int i = 0; i < count; i++)
                {
                    View view = tabWidget.getChildTabViewAt(i);
                    final TextView tv = (TextView) view.findViewById(R.id.text_view_id);
                    if (i == nCurrentIndex)
                    {
                        if (mCurrentContext != null)
                        {
                            tv.setBackgroundDrawable(mCurrentContext.getResources().getDrawable(com.surfing.R.drawable.table_sel));
                        }
                        else
                        {
                            tv.setBackgroundResource(com.surfing.R.drawable.table_sel);
                        }
                        if (mTheme == ReadConfigFile.THEME_RED)
                        {
                            tv.setTextColor(Color.WHITE);
                        }
                    }
                    else
                    {
                        if (mCurrentContext != null)
                        {
                            tv.setBackgroundDrawable(mCurrentContext.getResources().getDrawable(com.surfing.R.drawable.table_unsel));
                        }
                        else
                        {
                            tv.setBackgroundResource(com.surfing.R.drawable.table_unsel);
                        }
                        if (mTheme == ReadConfigFile.THEME_RED)
                        {
                            tv.setTextColor(Color.BLACK);
                        }

                    }
                }
            }
        });
    }
}
