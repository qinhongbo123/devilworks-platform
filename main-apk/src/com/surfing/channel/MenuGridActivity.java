package com.surfing.channel;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.surfing.R;
import com.surfing.Notification.NotificationListActivity;
import com.surfing.Notification.NotificationProviderMetaData;
import com.surfing.Notification.NotificationProviderMetaData.UserTableMetaData;
import com.surfing.contacts.EnterpriseContactsActivity;
import com.surfing.disscusgroup.DisscusActivity;
import com.surfing.disscusgroup.VoteListActivity;
import com.surfing.httpconnection.HttpConnectionCallback;
import com.surfing.httpconnection.HttpConnectionUtil;
import com.surfing.httpconnection.ImageDownloader;
import com.surfing.httpconnection.HttpConnectionUtil.HttpMethod;
import com.surfing.rssparse.ChannelInformation;
import com.surfing.rssparse.ChannelItem;
import com.surfing.rssparse.DomXMLReader;
import com.surfing.setting.SettingActivity;
import com.surfing.update.UpdateVersionActivity;
import com.surfing.util.CommonOperate;
import com.surfing.util.DisplayWeather;
import com.surfing.util.ReadConfigFile;
import com.surfing.util.ThemeUpdateUitl;
import com.surfing.util.TitleBarDisplay;
import com.surfing.weather.WeatherActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class MenuGridActivity extends ActivityBase
{
    public static final String TAG = "MenuGridActivity";
    GridView maingv;
    private Context mCurrentContext;
    private int mTheme = 0;
    ArrayList<ChannelItem> mChannlelist = null;
    public String[] names = null;
    private int[] icons = null;
    private Handler myHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case CommonUpdate.EVENT_UPDATE_WEATHER:
                {
                    DisplayWeather.updateWeatherDisplay(getApplicationContext(), MenuGridActivity.this);
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
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState, TITLE_TYPE_COMPANY, null, 0, true);
        setContentView(R.layout.menugridview_layout);

        setupView();
        // get channel info
        String response = getIntent().getStringExtra(ChannelTabActivity.COLUMN_INFO_TAG);
        if (response == null)
        {
            File file = new File(getApplicationContext().getFilesDir().getPath() + "/" + "channel.xml");
            if (file.exists())
            {
                Log.i(TAG, "the file is exist");
                InputStream inputStream = null;
                try
                {
                    inputStream = new FileInputStream(file);
                    int length = inputStream.available();
                    byte[] buffer = new byte[length];
                    inputStream.read(buffer);
                    response = new String(buffer);

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
        
        Log.i(TAG, "channle string = " + response);
        InputStream stream = new ByteArrayInputStream(response.getBytes());
        ChannelInformation channelInfo = DomXMLReader.readXML(MenuGridActivity.this, stream);
        mChannlelist = (ArrayList<ChannelItem>) channelInfo.getmChannelItemList();
        
        maingv.setAdapter(new MainGridViewAdapter(this));
        maingv.setOnItemClickListener(new MainItemClickListener());
        
        Context context = this.getApplicationContext();

        getUnreadNotifiction();
        
        mTheme = ReadConfigFile.getTheme(context);
        mCurrentContext = ReadConfigFile.getCurrentThemeContext(mTheme, context);
        View view = findViewById(R.id.title_layout_id);
        ThemeUpdateUitl.updateTitlebarBg(view, mCurrentContext, R.drawable.title_bg);
        ThemeSet();

        HttpConnectionUtil connect = new HttpConnectionUtil(context);
        String url = this.getEntLogoUrl(context);
        ConnectWeb(connect, url);
        Log.i(TAG, "the url is : " + url);
        CloseReceiver.registerCloseActivity(this);

        // CommonUpdate.getInstance().registerForUpdateWeather(myHandler,
        // CommonUpdate.EVENT_UPDATE_WEATHER, null);

    }
    
    private String getEntLogoUrl(Context context)
    {
        String username = context.getSharedPreferences("user", MODE_PRIVATE).getString("user_name", "");
        // http://125.76.228.165/tianyi/index.php?controller=enterprise&action=RequireEnterLogoEx&user_name=460030919293952
        return ReadConfigFile.getServerAddress(context) + "index.php?controller=enterprise&action=RequireEnterLogoEx&user_name=" + username;
    }

    private void ThemeSet()
    {
        if (mTheme == ReadConfigFile.THEME_BLUE)
        {
            if (mCurrentContext != null)
            {
                View view = findViewById(R.id.menuGrid_Layout_id);
                view.setBackgroundDrawable(mCurrentContext.getResources().getDrawable(R.drawable.content_bg));
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
        {
            super.dialog();
            return false;
        }
        return super.onKeyDown(keyCode, event);
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
        // TODO Auto-generated method stub
        super.onResume();
        DisplayWeather.updateWeatherDisplay(getApplicationContext(), this);
    }

    public void ConnectWeb(HttpConnectionUtil connect, String geturl)
    {
        Log.i(TAG, "url = " + geturl);
        connect.asyncConnect(geturl, HttpMethod.GET, new HttpConnectionCallback()
        {

            @Override
            public void execute(String response)
            {
                if ((response != null) && !HttpConnectionUtil.RETURN_FAILED.equalsIgnoreCase(response) && !HttpConnectionUtil.CONNECT_FAILED.equalsIgnoreCase(response))
                {
                    Log.i(TAG, "enterprise info = " + response);
                    int nIndex = response.indexOf("\\r\\n");
                    String enterprise_name = response.substring(0, nIndex);
                    String subString = response.substring(nIndex + "\\r\\n".length());
                    nIndex = subString.indexOf("\\r\\n");
                    String enterprise_iconAddress = subString.substring(0, nIndex);
                    String enterprise_id = subString.substring(nIndex + "\\r\\n".length());
                    enterprise_name = enterprise_name.substring("enterprise_name=".length() + 1);
                    enterprise_iconAddress = enterprise_iconAddress.substring("enterprise_logo=".length());
                    enterprise_id = enterprise_id.substring("enterprise_id=".length());
                    Log.i(TAG, "enterprise name is : " + enterprise_name);
                    Log.i(TAG, "icon address is : " + enterprise_iconAddress);
                    Log.i(TAG, "enterprise id = " + enterprise_id);

                    SharedPreferences pref = MenuGridActivity.this.getApplicationContext().getSharedPreferences("enterprise", MODE_PRIVATE);
                    Editor editor = pref.edit();
                    editor.putString("enterprise_name", enterprise_name);
                    editor.putString("enterprise_icon_url", enterprise_iconAddress);
                    editor.putString("enterprise_id", enterprise_id);
                    editor.commit();
                    MenuGridActivity.this.updateTitle();
                }
            }
        });
    }

    private void getUnreadNotifiction()
    {
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(UserTableMetaData.CONTENT_URI, new String[] { NotificationProviderMetaData.NOTIFICATION_ID }, NotificationProviderMetaData.NOTIFICATION_READFLAG + "=0", null,
                null);
        if ((cursor != null) && (cursor.getCount() > 0))
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(MenuGridActivity.this).setTitle(R.string.message_title_indicate)
                    .setMessage(getString(R.string.message_notify_start) + cursor.getCount() + getString(R.string.message_notify_end)).setPositiveButton(R.string.btn_view, new OnClickListener()
                    {

                        @Override
                        public void onClick(DialogInterface arg0, int arg1)
                        {
                            Intent myIntent = new Intent();
                            myIntent.setClass(getApplicationContext(), NotificationListActivity.class);
                            MenuGridActivity.this.startActivity(myIntent);
                        }
                    }).setNegativeButton(R.string.btn_cancel, new OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1)
                        {
                            // TODO Auto-generated method stub

                        }
                    });
            builder.create();
            builder.show();
        }
        if(cursor != null)
        {
            cursor.close();
        }
    }

    private void setupView()
    {
        maingv = (GridView) this.findViewById(R.id.gv_all);
    }

    private class MainItemClickListener implements OnItemClickListener
    {

        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            Intent myIntent = new Intent();
            Boolean blStart = true;
            String Itemtext = names[position];
            for (int i = 0; i < mChannlelist.size(); i++)
            {
                if (Itemtext.equals(mChannlelist.get(i).getmTitle()))
                {
                    myIntent.setClass(getApplicationContext(), ChannelActivityOne.class);
                    myIntent.putExtra(ChannelTabActivity.CHANNLE_LINK, mChannlelist.get(i).getmLink());
                    myIntent.putExtra(ChannelTabActivity.CHANNEL_TITLE, mChannlelist.get(i).getmTitle());
                    startActivity(myIntent);
                    return;
                }
            }
            if (Itemtext.equals(getString(R.string.channel_public)))
            {
                myIntent.setClass(getApplicationContext(), ChannelActivityOne.class);
                myIntent.putExtra(ChannelTabActivity.CHANNLE_LINK, "public");
                myIntent.putExtra(ChannelTabActivity.CHANNEL_TITLE, getString(R.string.channel_public));
                startActivity(myIntent);
                return;
            }
            if (Itemtext.equals(getString(R.string.contact_title)))
            {
                myIntent.setClass(getApplicationContext(), EnterpriseContactsActivity.class);
                startActivity(myIntent);
                return;
            }
            if (Itemtext.equals(getString(R.string.setting_title)))
            {
                myIntent.setClass(getApplicationContext(), SettingActivity.class);
                startActivity(myIntent);
                return;
            }
            if (Itemtext.equals(getString(R.string.inner_message)))
            {
                myIntent.setClass(getApplicationContext(), NotificationListActivity.class);
                startActivity(myIntent);
                return;
            }
            if (Itemtext.equals(getString(R.string.send_to_friend)))
            {
                CommonOperate.sendMsg2Friend(MenuGridActivity.this.getApplicationContext());
                return;
            }

        }
    }

    private class MainGridViewAdapter extends BaseAdapter
    {
        private static final String TAG = "MainGridViewAdapter";

        LayoutInflater infalter;

        public MainGridViewAdapter(Context context)
        {
            infalter = LayoutInflater.from(context);
            names = new String[mChannlelist.size() + 4];
            icons = new int[mChannlelist.size() + 4];
            
            
            HashMap<String, Integer> customColumn = new HashMap<String, Integer>()
            {
                {
                    put("工作动态", R.drawable.channel);
                    put("突发事件", R.drawable.channel);
                    put("应急演练", R.drawable.channel);
                    put("宣传培训", R.drawable.channel);
                }
            };

            int i = 0;
            for (ChannelItem channelItem : mChannlelist)
            {
                try
                {
                    names[i] = channelItem.getmTitle();
                    icons[i] = customColumn.get(channelItem.getmTitle());
                    ++i;
                } catch (Exception e)
                {
                    // TODO: handle exception
                    Log.e("MENUGRID", e.getMessage());
                }
            }
            
            HashMap<String, Integer> commonColumn = new HashMap<String, Integer>()
            {
                {
                    put(getString(R.string.channel_public), R.drawable.channel);
                    put(getString(R.string.contact_title), R.drawable.contacts);
                    put(getString(R.string.setting_title), R.drawable.settings);
                    put(getString(R.string.inner_message), R.drawable.notification);
                }
            };
            Iterator iter = commonColumn.entrySet().iterator();
            while (iter.hasNext())
            {
                Map.Entry entry = (Map.Entry)iter.next();
                names[i] = (String) entry.getKey();
                icons[i] = (Integer) entry.getValue();
                ++i;
            }
        }

        public int getCount()
        {
            return names.length;
        }

        public Object getItem(int position)
        {
            return position;
        }

        public long getItemId(int position)
        {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent)
        {
            Log.i(TAG, "GETVIEW " + position);
            View view = infalter.inflate(R.layout.menugridview_item_layout, null);
            ImageView iv = (ImageView) view.findViewById(R.id.main_gv_iv);
            TextView tv = (TextView) view.findViewById(R.id.main_gv_tv);
            iv.setImageResource(icons[position]);
            tv.setText(names[position]);
            return view;
        }
    }

}
