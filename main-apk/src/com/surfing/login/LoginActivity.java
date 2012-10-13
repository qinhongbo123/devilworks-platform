package com.surfing.login;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import com.surfing.channel.ActivityBase;
import com.surfing.channel.ChannelActivityOne;
import com.surfing.channel.ChannelTabActivity;
import com.surfing.channel.CloseReceiver;
import com.surfing.channel.CommonUpdate;
import com.surfing.channel.MenuGridActivity;
import com.surfing.channel.MenuTabActivity;
import com.surfing.channel.WeatherInfoLoadService;
import com.surfing.httpconnection.HttpConnectionCallback;
import com.surfing.httpconnection.HttpConnectionUtil;
import com.surfing.httpconnection.ServerListenerService;
import com.surfing.httpconnection.HttpConnectionUtil.HttpMethod;
import com.surfing.rssparse.DomXMLReader;
import com.surfing.rssparse.WeatherInfo;
import com.surfing.util.DisplayWeather;
import com.surfing.util.ReadConfigFile;
import com.surfing.util.SaveRssFile;
import com.surfing.weather.CityListActivity;
import com.surfing.R;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class LoginActivity extends ActivityBase implements OnClickListener
{
    /** Called when the activity is first created. */
    private static final String TAG                    = "Login";
    public static final String  LOGIN_STATE            = "login_state";
    public static final String  LOGIN_STATE_LOGOUT     = "logout";
    public static final String  LOGIN_STATE_LOGINNING  = "loginning";
    public static final String  LOGIN_STATE_LOGINED    = "logined";
    public static final String  APPLICATION_SATE       = "application_state";
    public static final String  APPLICATION_ACTIVE     = "active";
    public static final String  APPLICATION_BACKGROUND = "background";

    private ImageView           mLogoImage             = null;
    private EditText            mLoginNameText         = null;
    private EditText            mLoginPasswadText      = null;
    private CheckBox			mRemberPwd 			   = null;
    private Button              mLoginInBtn            = null;
    private Button              mLoginOutBtn           = null;
    private ProgressDialog      mwaittingBar           = null;
    private View                mLoginView             = null;
    private ImageView           mLoadingImage          = null;
    public final static int     NOTIFICATION_ID        = 53472439;
    private Context             mContext               = null;
    private String              user_name              = null;
    private String              user_password           = null;
    private String              mResponse              = null;
    private int                 mTheme                 = 0;
    private AnimationDrawable   mLoadingAnimate        = null;
    private static final int    EVENT_LOADING          =1;
    private static final int	EVENT_START			   =2;
    private boolean 			blRemberPwd			   = false;
    private static 	final int 	LOGIN_TYPE_PHONENUM    = 0;
    private static final  int 	LOGIN_TYPE_MEID		   = 1;
    private TelephonyManager    mTelephonyManager 	   = null;
    private static String 		mMeid 				   = null;
    private static int			mUserNmae_type 		   = LOGIN_TYPE_MEID;
    private long				mAnimationStart 	   = 0;
    private boolean				mBlDeday			   = true;
    private Handler    myHandler = new Handler(){

        @Override
        public void handleMessage(Message msg)
        {
            switch(msg.what){
                case EVENT_LOADING:{
                    if(mLoadingAnimate != null){
                        Log.i(TAG,"loading image start");
                        //mAnimationStart = (int) System.currentTimeMillis();
                        mLoadingImage.setVisibility(View.VISIBLE);
                        mLoadingAnimate.start();
                    }
                }
                break;
                case EVENT_START:{
                	String reponse = (String)msg.obj;
                	StartMeau(reponse);
                }
                break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
        
    };
    @Override
    public void onCreate(Bundle savedInstanceState)
    { 
        super.onCreate(savedInstanceState);
        Intent ServiceIntent = new Intent();
        mContext = this.getApplicationContext();
        mTheme = ReadConfigFile.getTheme(mContext); 
        ServiceIntent.setClass(getApplicationContext(),
                ServerListenerService.class);
        startService(ServiceIntent);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //get meid  
        mTelephonyManager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        Log.e(TAG,"onResume  == "+getIntent().getStringExtra("flag"));
        if("background".equals(getIntent().getStringExtra("flag"))){
        	mBlDeday = false;
        }
        mMeid = mTelephonyManager.getSubscriberId();
        //mMeid = "46003"+mMeid.substring(mMeid.length()-10);
        //mMeid = "460030919293952"; 
        Log.i(TAG,"Meid = "+mMeid);
       // Log.i(TAG,"mTelephonyManager.getDeviceId() = "+mTelephonyManager.getDeviceId());
       // Log.i(TAG,"mTelephonyManager.getSimSerialNumber() = "+mTelephonyManager.getSimSerialNumber());
        Log.i(TAG,"mTelephonyManager.getSubscriberId() = "+mTelephonyManager.getSubscriberId());
        Log.i(TAG,"mTelephonyManager.getSubscriberId() = "+mTelephonyManager.getSimOperator());
        HashMap<String,String> map = ReadConfigFile.getUserInfo(mContext);
        setContentView(R.layout.relogin_layout);
        mLoadingImage = (ImageView)findViewById(R.id.loading_animate_id);
        mLoadingImage.setBackgroundDrawable(getResources().getDrawable(R.anim.loading));
        mLoadingAnimate = (AnimationDrawable)mLoadingImage.getBackground();
        myHandler.sendMessageDelayed(myHandler.obtainMessage(EVENT_LOADING),0);
        HttpConnectionUtil connect = new HttpConnectionUtil(getApplicationContext());
        String geturl = getLoginURL(mUserNmae_type,user_name,user_password,mMeid);
        ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);      
        NetworkInfo networkinfo = manager.getActiveNetworkInfo();      
        if (networkinfo == null || !networkinfo.isAvailable()) {      
        	Log.i(TAG,"no NetWork available");
        	Toast.makeText(mContext,mContext.getString(R.string.network_error),Toast.LENGTH_SHORT).show();
           this.finish();
           return;
        }  
        mAnimationStart = System.currentTimeMillis()/1000;
        connect.asyncConnect(geturl, HttpMethod.GET,
                new LoginHttpCallBack());
//        if(ReadConfigFile.mLoginType == ReadConfigFile.LOGIN_TYPE_NORMAL){
//        	SharedPreferences pref = this.getSharedPreferences(LOGIN_STATE,
//                    MODE_PRIVATE);
//            String loginstate = pref.getString(LOGIN_STATE, LOGIN_STATE_LOGOUT);
//            Log.i(TAG, "Loggin state is onCreate: " + loginstate);
//            SharedPreferences userpref = this.getApplicationContext()
//                    .getSharedPreferences("user", MODE_PRIVATE);
//            user_name = userpref.getString("user_name", "");
//            user_password = userpref.getString("user_passwad", "");
//            if (((user_name.length()== 0) || (user_password.length() == 0)) 
//            		&& (mUserNmae_type == LOGIN_TYPE_PHONENUM))
//            {
//
//                setContentView(R.layout.loginlayout);
//                setupView();
//                Log.i("chenmei","loadTheme(mContext)"); 
//                loadTheme(mContext);
//                mLoginNameText.setText(user_name);
//                mLoginPasswadText.setText(user_password);
//                Log.i(TAG, "the user name is = " + user_name + " the passwad is = "
//                        + user_password);
//                changeLogState(LoginActivity.this.getApplicationContext(),
//                        LOGIN_STATE_LOGINNING,true);
//            }
//            else
//            {
//                requestAgain();
//            }
//        }else{
//        	setContentView(R.layout.relogin_layout);
//        	mLoadingImage = (ImageView)findViewById(R.id.loading_animate_id);
//            mLoadingImage.setBackgroundDrawable(getResources().getDrawable(R.anim.loading));
//            mLoadingAnimate = (AnimationDrawable)mLoadingImage.getBackground();
//            myHandler.sendMessageDelayed(myHandler.obtainMessage(EVENT_LOADING),500);
//        	user_name = map.get("user_name");
//        	user_password = map.get("user_passwad");
//        	String geturl = getLoginURL(mUserNmae_type,null,null, mMeid);
//            Log.i(TAG, ReadConfigFile.getServerAddress(this.getApplicationContext()));
//            HttpConnectionUtil connect = new HttpConnectionUtil(getApplicationContext());
//            connect.asyncConnect(geturl, HttpMethod.GET,
//                    new LoginHttpCallBack());
//            if(mLoadingAnimate != null){
//                mLoadingImage.setVisibility(View.VISIBLE);
//                mLoadingAnimate.start();
//            }
//
//        }
        
        CloseReceiver.registerCloseActivity(this);
    }

	@Override
    protected void onDestroy()
    {
        CloseReceiver.unRegisterActivity(this);
        super.onDestroy();
    }
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
    	if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
    		NotificationManager mNotifyMgr = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            mNotifyMgr.cancel(LoginActivity.NOTIFICATION_ID);
    	   	LoginActivity.changeLogState(getApplicationContext(),LoginActivity.LOGIN_STATE_LOGOUT,false);
    	   	CloseReceiver.CloseAllActivity();
    	   	CloseReceiver.CloseAllService();
            return false;
        }
    	return super.onKeyDown(keyCode, event);
	
	}
    private String getLoginURL(int LoginUserType,String UserName,String UserPwd,String meid){
    	String geturl = null;
    	if(LOGIN_TYPE_PHONENUM == LoginUserType){
    		geturl = ReadConfigFile.getServerAddress(this
                    .getApplicationContext())
                    + "index.php"
                    + "?"
                    + "controller=user&action=PhoneLogin&username="
                    + UserName
                    + "&password=" + UserPwd;
    	}else{//meid
    		geturl = ReadConfigFile.getServerAddress(this
                    .getApplicationContext())
                    + "index.php"
                    + "?"
                    + "controller=user&action=PhoneMEIDLogin&meid="
                    + meid;
    	}
    	return geturl;
    }
    @Override
    public void onClick(View v)
    {
        if (v == mLoginInBtn)
        {
            //hide the inputmethod 
           InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
           imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
          
            ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

            NetworkInfo networkinfo = manager.getActiveNetworkInfo();
            if (networkinfo == null || !networkinfo.isAvailable())
            {
                new AlertDialog.Builder(this)
                        .setMessage(getString(R.string.msg_nonetwork))
                        .setPositiveButton(R.string.ok, null).show();
                return;
            }
            user_name = mLoginNameText.getText().toString();
            user_password = mLoginPasswadText.getText().toString();
            mRemberPwd.setVisibility(View.INVISIBLE);
            blRemberPwd = true;//mRemberPwd.isChecked();
            ReadConfigFile.setSharedPreference(mContext,"user","user_rember", blRemberPwd);
            if ((user_name.length() == 0) || (user_password.length() == 0))
            {
                new AlertDialog.Builder(this)
                        .setMessage(getString(R.string.msg_user_password_null))
                        .setPositiveButton(R.string.ok, null).show();
                return;
            }
            
            String geturl = getLoginURL(mUserNmae_type,null,null, mMeid);
            
            Log.i(TAG, ReadConfigFile.getServerAddress(this.getApplicationContext()));
            HttpConnectionUtil connect = new HttpConnectionUtil(getApplicationContext());
            connect.asyncConnect(geturl, HttpMethod.GET,
                    new LoginHttpCallBack());
            if(mLoadingAnimate != null){
                mLoadingImage.setVisibility(View.VISIBLE);
                mLoadingAnimate.start();
            }
        }
        else if (v == mLoginOutBtn)
        {
        	 NotificationManager mNotifyMgr = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
             mNotifyMgr.cancel(LoginActivity.NOTIFICATION_ID);
        	LoginActivity.changeLogState(getApplicationContext(),LoginActivity.LOGIN_STATE_LOGOUT,false);
        	CloseReceiver.CloseAllActivity();
        	CloseReceiver.CloseAllService();
            //LoginActivity.this.finish();
        }
    }

    private void setupView()
    {
        mLogoImage = (ImageView) findViewById(R.id.LoginTitle_id);
        mLoginNameText = (EditText) findViewById(R.id.LoginNameEdit_id);
        mLoginPasswadText = (EditText) findViewById(R.id.LoginPaswdEdit_id);
        mRemberPwd = (CheckBox)findViewById(R.id.Rebm_Passwd_check_id);
        mLoginInBtn = (Button) findViewById(R.id.LoginInBtn_id);
        mLoginOutBtn = (Button) findViewById(R.id.LoginOutBtn_id);
        mLoginInBtn.setOnClickListener(this);
        mLoginOutBtn.setOnClickListener(this);
        mLoginNameText.setInputType(InputType.TYPE_CLASS_NUMBER);
        mLoginView = (View)findViewById(R.id.login_layout_id);
        mLoadingImage = (ImageView)findViewById(R.id.loading_animate_id);
        mLoadingImage.setBackgroundDrawable(getResources().getDrawable(R.anim.loading));
        mLoadingAnimate = (AnimationDrawable)mLoadingImage.getBackground();
        
        mRemberPwd.setChecked(ReadConfigFile.getSharedPreferenceBool(mContext,"user","user_rember"));
        //mRemberPwd.setChecked(checked);
        // View titleView = (View) findViewById(R.id.application_title_id);
        // titleView.setVisibility(View.VISIBLE);
    }
    private void loadTheme(Context context){
        Context mCurrentThemeContext = ReadConfigFile.getCurrentThemeContext(mTheme, mContext); 
        if(mCurrentThemeContext != null){
            Log.i(TAG,"loading theme...");
            mLoginView.setBackgroundDrawable(mCurrentThemeContext.getResources().getDrawable(R.drawable.login_bg));
        }
    }
    private void requestAgain()
    {
        Log.i(TAG, "requestAgain");
        SharedPreferences pref = mContext.getSharedPreferences("user",
                MODE_PRIVATE);
        user_name = pref.getString("user_name", "");
        user_password = pref.getString("user_passwad", "");
        Log.i(TAG, "the user name is = " + user_name + " the passwad is = "
                + user_password);
        if (((user_name.length() != 0) && (user_password.length() != 0))
        	|| (mMeid != null))
        {
            setContentView(R.layout.relogin_layout);
            mLoadingImage = (ImageView)findViewById(R.id.loading_animate_id);
            mLoadingImage.setBackgroundDrawable(getResources().getDrawable(R.anim.loading));
            mLoadingAnimate = (AnimationDrawable)mLoadingImage.getBackground();
            myHandler.sendMessageDelayed(myHandler.obtainMessage(EVENT_LOADING),0);
            HttpConnectionUtil connect = new HttpConnectionUtil(getApplicationContext());
            String geturl = getLoginURL(mUserNmae_type,user_name,user_password,mMeid);;
           
            connect.asyncConnect(geturl, HttpMethod.GET,
                    new LoginHttpCallBack());
            
        }
        else
        {
            setContentView(R.layout.loginlayout);
            setupView();
            mLoginNameText.setText(user_name);
            mLoginPasswadText.setText(user_password);
            changeLogState(LoginActivity.this.getApplicationContext(),
                    LOGIN_STATE_LOGINNING,true);
        }

    }

    private class LoginHttpCallBack implements HttpConnectionCallback
    {

        @Override
        public void execute(String response)
        {
            //mwaittingBar.dismiss();
            Log.i(TAG, "return string : " + response);
            if((response == null) 
                    || (response.length() == 0) 
                    || response.equals(HttpConnectionUtil.CONNECT_FAILED)
                    || HttpConnectionUtil.RETURN_FAILED.equalsIgnoreCase(response))
            {
               
             // clear the passward
            	if(ReadConfigFile.mLoginType == ReadConfigFile.LOGIN_TYPE_NORMAL){
            		ReadConfigFile.setSharedPreference(mContext,"user","user_passwad","");
                    new AlertDialog.Builder(LoginActivity.this)
                            .setTitle(R.string.message_error)
                            .setMessage(R.string.message_error_text)
                            .setNegativeButton(R.string.ok,
                                    new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface arg0,
                                                int arg1)
                                        {
                                            changeLogState(mContext, LOGIN_STATE_LOGOUT,true);
                                            if(mUserNmae_type == LOGIN_TYPE_PHONENUM){
                                            	requestAgain();
                                            }else{
                                            	LoginActivity.this.finish();
                                            }
                                            
                                            // LoginActivity.this.finish();
                                        }
                                    }).show();
            	}else{
            		 Toast.makeText(mContext,mContext.getString(R.string.network_error),Toast.LENGTH_SHORT).show();
            		 LoginActivity.this.finish();
            		 return;
            	}
               
            }else
            {
                SharedPreferences prefsate = mContext.getSharedPreferences(
                        LOGIN_STATE, MODE_PRIVATE);
                String loginstate = prefsate.getString(LOGIN_STATE,
                        LOGIN_STATE_LOGOUT);
              //  if (!LOGIN_STATE_LOGINED.equals(loginstate))
                {
                    SharedPreferences pref = LoginActivity.this
                            .getApplicationContext().getSharedPreferences(
                                    "user", MODE_PRIVATE);
                    Editor editor = pref.edit();
                    if(mUserNmae_type == LOGIN_TYPE_MEID){
                    	editor.putString("user_name",mMeid);
                    	editor.putString("user_passwad", "");
                    }else{ 
                    	 editor.putString("user_name", user_name);
                         if(blRemberPwd){
                         	editor.putString("user_passwad", user_password);
                         }else{
                         	editor.putString("user_passwad", "");
                         }
                    }
                   
                    
                    editor.commit();
                }
 
                changeLogState(LoginActivity.this.getApplicationContext(),
                        LOGIN_STATE_LOGINED,true); 
                mResponse = response;
                long duration = Math.abs(System.currentTimeMillis()/1000 - mAnimationStart);
                
                if((duration < 2) && mBlDeday){
                	 myHandler.sendMessageDelayed(myHandler.obtainMessage(EVENT_START,mResponse),(2-duration)*1000);
                }else{
                	 //myHandler.sendMessageDelayed(myHandler.obtainMessage(EVENT_START,mResponse),0);
                	myHandler.sendMessage(myHandler.obtainMessage(EVENT_START,mResponse));
                }
               
                
            }
        }
    }
    private void StartMeau(String response){
    	 Intent myIntent = new Intent();
         SharedPreferences pref =
         mContext.getSharedPreferences("mode",MODE_PRIVATE);
         //make the display style as grid
		 Editor editor = pref.edit();
		 editor.putInt("mode",1);
		 editor.commit();
         int mode = pref.getInt("mode",0);
         if(mode == 0){
         myIntent.setClass(getApplicationContext(),MenuTabActivity.class);
         }else{
         myIntent.setClass(getApplicationContext(),MenuGridActivity.class);
         }
         myIntent.putExtra(ChannelTabActivity.COLUMN_INFO_TAG,response);
         SaveRssFile savefile = new SaveRssFile(LoginActivity.this.getApplicationContext());
         
         try {
         savefile.SaveFile(response,"channel.xml");
         } catch (Exception e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
         }
         
        //getWeatherFromWeb();
        startActivity(myIntent);
        if(mLoadingAnimate != null){
            mLoadingAnimate.stop();
            mLoadingAnimate = null;
        }
        LoginActivity.this.finish();
    }
    private void getWeatherFromWeb()
    {
       Intent myIntent = new Intent();
       myIntent.setClass(mContext,WeatherInfoLoadService.class);
       startService(myIntent);
    }

    public static void changeLogState(Context context, String state,boolean blShow)
    {
        Log.i(TAG, "[changeLogState] the login state is : " + state);
        SharedPreferences pref = context.getApplicationContext()
                .getSharedPreferences(LOGIN_STATE, MODE_PRIVATE);
        Editor editor = pref.edit();
        editor.putString(LOGIN_STATE, state);
        editor.commit();
        //RuntimeException r = new RuntimeException();
        //r.printStackTrace();
        if(!blShow){
            return;
        }
        Notification mNotification;
        NotificationManager mNotificationManager;
        if (LoginActivity.LOGIN_STATE_LOGINED.equals(state))
        {
            mNotification = new Notification(R.drawable.notify_online, null,
                    System.currentTimeMillis());
        }
        else if (LoginActivity.LOGIN_STATE_LOGOUT.equals(state))
        {
            mNotification = new Notification(R.drawable.notify_offline, null,
                    System.currentTimeMillis());
        }
        else
        {
            mNotification = new Notification(R.drawable.notify_offline, null,
                    System.currentTimeMillis());
        }
        Intent mIntent = new Intent(context, LoginActivity.class);
        mIntent.putExtra("flag","background");
        mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent mContentIntent = PendingIntent.getActivity(context, 0,
                mIntent, 0);
        //modify the app_name to app_title
        mNotification.setLatestEventInfo(context,
                context.getString(R.string.app_title), null, mContentIntent);
        mNotificationManager = (NotificationManager) context
                .getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, mNotification);

    }

    public static String getLogState(Context context)
    {
        SharedPreferences pref = context.getApplicationContext()
                .getSharedPreferences(LOGIN_STATE, MODE_PRIVATE);
        return pref.getString(LOGIN_STATE, LoginActivity.LOGIN_STATE_LOGOUT);
    }

    protected void dialog()
    {
        AlertDialog.Builder builder = new Builder(LoginActivity.this);
        builder.setMessage(R.string.message_confirm_quit);
        builder.setTitle(R.string.message_title_indicate);
        builder.setPositiveButton(R.string.ok,
                new android.content.DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1)
                    {
                        changeLogState(
                                LoginActivity.this.getApplicationContext(),
                                LOGIN_STATE_LOGOUT,true);
                        dialog.dismiss();
                        LoginActivity.this.finish();

                    }
                });
        builder.setNegativeButton(R.string.btn_cancel,
                new android.content.DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }
}
