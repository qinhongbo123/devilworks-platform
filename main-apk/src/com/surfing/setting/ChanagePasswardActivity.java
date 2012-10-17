package com.surfing.setting;

import com.surfing.R;
import com.surfing.channel.ActivityBase;
import com.surfing.channel.ChannelTabActivity;
import com.surfing.channel.CloseReceiver;
import com.surfing.channel.CommonUpdate;
import com.surfing.channel.MenuTabActivity;
import com.surfing.channel.NewsInformationActivity;
import com.surfing.httpconnection.HttpConnectionCallback;
import com.surfing.httpconnection.HttpConnectionUtil;
import com.surfing.httpconnection.ImageDownloader;
import com.surfing.httpconnection.HttpConnectionUtil.HttpMethod;
import com.surfing.login.LoginActivity;
import com.surfing.util.DisplayWeather;
import com.surfing.util.ReadConfigFile;
import com.surfing.util.TitleBarDisplay;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
 
public class ChanagePasswardActivity extends ActivityBase implements OnClickListener{
	private static final String TAG = "PasswardActivity";
	private Context mContext = null;
	private EditText moldPasswardText = null;
	private EditText mnewPasswardText = null;
	private EditText mconfirmPasswardText = null;
	private Button mOkBtn = null;
	private Button mCancelBtn = null;
	private ProgressDialog mwaittingBar = null;
	private Handler    myHandler = new Handler(){

        @Override
        public void handleMessage(Message msg)
        {
            switch(msg.what){
                case CommonUpdate.EVENT_UPDATE_WEATHER:{
                    DisplayWeather.updateWeatherDisplay(getApplicationContext(),ChanagePasswardActivity.this);
                }
                break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
        
    };
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState,TITLE_TYPE_COMPANY,null,0,true);
		setContentView(R.layout.setting_passward_layout);
		mContext = getApplicationContext();
		setupView();
		CloseReceiver.registerCloseActivity(this);
		CommonUpdate.getInstance().registerForUpdateWeather(myHandler,CommonUpdate.EVENT_UPDATE_WEATHER,null);
	}
	@Override
    protected void onDestroy()
    {
        CommonUpdate.getInstance().unregisterForUpdateWeather(myHandler);
        CloseReceiver.unRegisterActivity(this);
        super.onDestroy();
    }
	@Override
    protected void onResume() {
        super.onResume();
        DisplayWeather.updateWeatherDisplay(getApplicationContext(),this);
    }
	private void setupView(){
		moldPasswardText = (EditText)findViewById(R.id.set_oldpassword_id);
		mnewPasswardText = (EditText)findViewById(R.id.set_newpassword_id);
		mconfirmPasswardText = (EditText)findViewById(R.id.set_confirmpassword_id);
		mOkBtn = (Button)findViewById(R.id.btn_setpassword_id);
		mCancelBtn = (Button)findViewById(R.id.btn_cancelset_id);
		
		mOkBtn.setOnClickListener(this);
		mCancelBtn.setOnClickListener(this);
	}
	@Override
	public void onClick(View v) {
		if(v == mOkBtn){
			String oldPassword = moldPasswardText.getEditableText().toString();
			String newPassword = mnewPasswardText.getEditableText().toString();
			String retypeNewPassword = mconfirmPasswardText.getEditableText().toString();
			if(oldPassword.equals(""))
			{
				Toast.makeText(mContext,getString(R.string.msg_oldpassward_not_null),Toast.LENGTH_LONG).show();
				return;
			}
			if(newPassword.equals("") || retypeNewPassword.equals(""))
			{
				Toast.makeText(mContext,getString(R.string.msg_newpassward_not_null),Toast.LENGTH_LONG).show();
				return;
			}
			if(!newPassword.equals(retypeNewPassword))
			{
				Toast.makeText(mContext,getString(R.string.msg_passward_not_equel),Toast.LENGTH_LONG).show();
				return;
			}
			if(newPassword.equals(oldPassword))
			{
				Toast.makeText(mContext,getString(R.string.msg_passward_equel),Toast.LENGTH_LONG).show();
				return;
			}
			SharedPreferences userpref = this.getApplicationContext().getSharedPreferences("user", MODE_PRIVATE);
			String user_name = userpref.getString("user_name", "");
			HttpConnectionUtil connect = new HttpConnectionUtil(getApplicationContext());
			String geturl = ReadConfigFile.getServerAddress(this
					.getApplicationContext())
					+ "index.php"
					+ "?"
					+ "controller=user&action=changepass&user_name="
					+ user_name
					+ "&oldpass=" + oldPassword
					+ "&newpass=" + newPassword;
			Log.i(TAG,"url = "+geturl);
			mwaittingBar = ProgressDialog.show(ChanagePasswardActivity.this, "",
					getString(R.string.loginning), true);
			mwaittingBar.show();
			connect.asyncConnect(geturl, HttpMethod.GET,new LoginHttpCallBack());
		}else if(v == mCancelBtn){
			finish();
		}
	}
	
	private class LoginHttpCallBack implements HttpConnectionCallback {

		@Override
		public void execute(String response) {
			mwaittingBar.dismiss();
			Log.i(TAG,"response string = "+response);
			if("success".equalsIgnoreCase(response)){
				Log.i(TAG,"change success");
				AlertDialog.Builder builder = new AlertDialog.Builder(ChanagePasswardActivity.this);
				builder.setTitle(R.string.message_title_indicate);
				builder.setMessage(R.string.msg_changed_pwd_success);
				builder.setPositiveButton(R.string.ok, null);
				builder.create().show();
//				SharedPreferences userpref = mContext.getSharedPreferences("user", MODE_PRIVATE);
//				Editor editor = userpref.edit();
//				editor.putString("user_passwad", mnewPasswardText.getEditableText().toString());
//				editor.commit();
				ChanagePasswardActivity.this.finish();
			}else if ("failed:0".equalsIgnoreCase(response) 
					  || "failed:1".equalsIgnoreCase(response)
					  || "failed:2".equalsIgnoreCase(response)
					  || "failed:3".equalsIgnoreCase(response)
					  || "failed:4".equalsIgnoreCase(response)){
				Log.i(TAG,"change failed");
				AlertDialog.Builder builder = new AlertDialog.Builder(ChanagePasswardActivity.this);
				builder.setTitle(R.string.message_title_indicate);
				builder.setMessage(R.string.msg_changed_pwd_failed);
				builder.setPositiveButton(R.string.btn_cancel, null);
				builder.create().show();
			}
		}
		
	}
}
