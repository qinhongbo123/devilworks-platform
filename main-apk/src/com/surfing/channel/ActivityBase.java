package com.surfing.channel;

import java.util.ArrayList;

import com.surfing.R;
import com.surfing.login.LoginActivity;
import com.surfing.util.ReadConfigFile;
import com.surfing.util.ThemeUpdateUitl;
import com.surfing.util.TitleBarDisplay;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

public class ActivityBase extends Activity
{
    private String LOG_TAG = "ActivityBase";
    public static final int TITLE_TYPE_NO_TITLE = 0;
    public static final int TITLE_TYPE_COMPANY = 1;
    public static final int TITLE_TYPE_DEFINE_TEXT_ICON = 2;
    public static final int TITLE_TYPE_DEFINE_TEXT = 3;
    public static final int TITLE_TYPE_DEFINE_ICON = 4;
    private int mTitleType = TITLE_TYPE_COMPANY;

    protected void onCreate(Bundle savedInstanceState, int titletype, String title, int iconId, boolean bldisplayWeather)
    {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        Log.i(LOG_TAG, "titletype = " + titletype);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(null);
        Window mWindow = getWindow();
        if (titletype == TITLE_TYPE_DEFINE_TEXT)
        {
            mWindow.setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_style2);
            TextView mTitleText = (TextView) findViewById(R.id.titlebar_text_id);
            // String title =
            // getIntent().getStringExtra(ChannelTabActivity.CHANNEL_TITLE);
            mTitleText.setText(title);
            int mTheme = ReadConfigFile.getTheme(getApplicationContext());
            Context mCurrentContext = ReadConfigFile.getCurrentThemeContext(mTheme, getApplicationContext());
            View view = findViewById(R.id.title_layout_id);
            Log.i("ActivityBase", "mCurrentContext = " + mCurrentContext);
            ThemeUpdateUitl.updateTitlebarBg(view, mCurrentContext, R.drawable.title_bg);
        }
        else if (titletype == TITLE_TYPE_COMPANY)
        {
            mWindow.setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);
            ImageView mTitleIcon = (ImageView) findViewById(R.id.titlebar_icon_id);
            TextView mTitleText = (TextView) findViewById(R.id.titlebar_text_id);
            TitleBarDisplay.TitleBarInit(mTitleText, mTitleIcon, this, getApplicationContext());
        }

    }

    public void updateTitle()
    {
        Log.i("ActivityBase","updateTitle mTitleType = "+mTitleType);
        if (mTitleType == TITLE_TYPE_COMPANY)
        {
            ImageView mTitleIcon = (ImageView) findViewById(R.id.titlebar_icon_id);
            TextView mTitleText = (TextView) findViewById(R.id.titlebar_text_id);
            TitleBarDisplay.TitleBarInit(mTitleText, mTitleIcon, this, getApplicationContext());
        }
    }

    @Override
    public void setContentView(View view)
    {
        if (view == null)
        {
            setContentView(R.layout.empty_layout);
        }
        else
        {
            setContentView(view);
        }
    }

    protected void dialog()
    {
        int mMode = ReadConfigFile.getMode(getApplicationContext());
        AlertDialog.Builder builder = (mMode == 0) ? (new Builder(this.getParent())): (new Builder(this));
        
        builder.setMessage(R.string.message_confirm_quit);
        builder.setTitle(R.string.message_title_indicate);
        builder.setPositiveButton(R.string.ok, new android.content.DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int arg1)
            {
                dialog.dismiss();
                // android.os.Process.killProcess(android.os.Process.myPid());
                // LoginActivity.changeLogState(getApplicationContext(),LoginActivity.LOGIN_STATE_LOGOUT,false);
                CloseReceiver.CloseAllActivity();
                CloseReceiver.CloseAllService();
                String state = LoginActivity.getLogState(getApplicationContext());
                LoginActivity.changeLogState(getApplicationContext(), state, false);

            }
        });
        builder.setNegativeButton(R.string.run_back, new android.content.DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
                CloseReceiver.CloseAllActivity();
                String state = LoginActivity.getLogState(getApplicationContext());
                LoginActivity.changeLogState(getApplicationContext(), state, true);
            }
        });
        builder.create().show();
    }
}
