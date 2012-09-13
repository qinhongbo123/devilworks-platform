package com.surfing.disscusgroup;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.surfing.R;
import com.surfing.channel.ActivityBase;
import com.surfing.channel.ChannelActivityOne;
import com.surfing.channel.CloseReceiver;
import com.surfing.channel.CommonUpdate;
import com.surfing.channel.MenuTabActivity;
import com.surfing.channel.NewsInformationActivity;
import com.surfing.httpconnection.HttpConnectionCallback;
import com.surfing.httpconnection.HttpConnectionUtil;
import com.surfing.httpconnection.ImageDownloader;
import com.surfing.httpconnection.HttpConnectionUtil.HttpMethod;
import com.surfing.rssparse.ChannelInformation;
import com.surfing.rssparse.ChannelItem;
import com.surfing.rssparse.DomXMLReader;
import com.surfing.util.DisplayWeather;
import com.surfing.util.ReadConfigFile;
import com.surfing.util.SaveRssFile;
import com.surfing.util.TitleBarDisplay;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DisscusViewActivity extends ActivityBase implements OnClickListener,OnScrollListener{
	private static final String TAG = "DisscusViewActivity";
	private TextView mTopicTitle = null;
	private View mThreadLayout = null;
	private TextView mThreadText = null;
	private Button mNextPageBtn = null;
	private EditText mInputEdit = null;
	private Button mSubmitBtn = null;
	private String mConnectUrl = null;
	private ProgressBar mwaittingBar = null;
	private String mTopic_id = null;
	private String mTopicText = null;
	private Context mContext = null;
	private int mpageIndex ;
	private int mThreadCount = 0;
	private ImageView mTitleIcon = null;
	private TextView mTitleText = null;
	private static final int PAGE_MAX_COUNT = 10;
	private Handler    myHandler = new Handler(){

        @Override
        public void handleMessage(Message msg)
        {
            switch(msg.what){
                case CommonUpdate.EVENT_UPDATE_WEATHER:{
                    DisplayWeather.updateWeatherDisplay(getApplicationContext(),DisscusViewActivity.this);
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
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		setContentView(R.layout.disscuss_view_layout);
		mTopic_id = getIntent().getStringExtra("topic_id");
		mTopicText = getIntent().getStringExtra("topic");
		mContext = getApplicationContext();
		mpageIndex = 1;
		setupView();
		mTopicTitle.setText(mTopicText);
		Log.i(TAG,"the topic is : "+mTopicText);
		Log.i(TAG,"the topic_id is : "+mTopic_id);
		setupData();
		TitleBarDisplay.TitleBarInit(mTitleText, mTitleIcon,DisscusViewActivity.this,getApplicationContext()); 
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
    protected void onResume()
    {
        // TODO Auto-generated method stub
        super.onResume();
        DisplayWeather.updateWeatherDisplay(getApplicationContext(),this);
    }

    private void setupView(){
		mTopicTitle = (TextView)findViewById(R.id.topic_title_id);
		mThreadLayout = (View)findViewById(R.id.disscus_text_list_id);
		mNextPageBtn = (Button)findViewById(R.id.btn_topic_next_id);
		mInputEdit = (EditText)findViewById(R.id.disscus_edit_id);
		mSubmitBtn = (Button)findViewById(R.id.btn_submit_id);
		mwaittingBar = (ProgressBar)findViewById(R.id.loading_progressBar);
		mTitleIcon = (ImageView)findViewById(R.id.titlebar_icon_id);
		mTitleText = (TextView)findViewById(R.id.titlebar_text_id);
		
		mSubmitBtn.setOnClickListener(this);
		mNextPageBtn.setOnClickListener(this);
	}
	private void setupData(){ 
		mConnectUrl = ReadConfigFile.getServerAddress(mContext)+"index.php?controller=Discusstopic&action=PhoneRequieTopicComments&topic_id="+mTopic_id+"&page="+mpageIndex;
		
		mwaittingBar.setVisibility(View.VISIBLE);
		//String geturl = url;
		Log.i(TAG,"the url is : "+mConnectUrl);
		HttpConnectionUtil connect = new HttpConnectionUtil(getApplicationContext());
		ConnectWeb(connect,mConnectUrl);
		
	}
	public void ConnectWeb(HttpConnectionUtil connect,String geturl){
		Log.i(TAG,"the url is : "+geturl);
		connect.asyncConnect(geturl, HttpMethod.POST, new HttpConnectionCallback(){
			
			@Override
			public void execute(String response) {
				mwaittingBar.setVisibility(View.GONE);
				if((response == null) 
                        || (response.length() == 0) 
                        || response.equals(HttpConnectionUtil.CONNECT_FAILED)
                        || HttpConnectionUtil.RETURN_FAILED.equalsIgnoreCase(response))
                {
                    Log.i(TAG,"request failed");
                    return;
                }
				try{
					//InputStream stream = new StringBufferInputStream(response.substring(nindex));
					InputStream stream = new ByteArrayInputStream(response.getBytes());
					Log.i(TAG,"string = "+response);
					//SaveRssFile savefile = new SaveRssFile(mContext);
					//savefile.SaveFile(response.substring(nindex),"disscus.xml");
					ChannelInformation channelInfo = DomXMLReader.readXML(mContext,stream);
					if(channelInfo != null){
						HashMap<String,Object> map = null;
						ArrayList<ChannelItem> channlelist = (ArrayList<ChannelItem>) channelInfo.getmChannelItemList();
						LayoutInflater inflater = getLayoutInflater();
						View imteLayout = null;
						TextView text = null;
						LayoutParams Parames  = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
						for(int i = 0;i<channlelist.size();i++){
						    imteLayout = inflater.inflate(R.layout.disscus_view_item_layout, null);
						    text = (TextView)imteLayout.findViewById(R.id.disscus_item_text_id);
						    text.setText(channlelist.get(i).getmTitle());
						    Log.i(TAG,"the topic title is : "+channlelist.get(i).getmTitle());
						    text = (TextView)imteLayout.findViewById(R.id.disscus_item_date_id);
						    text.setText(channlelist.get(i).getmDate());
						    
						    text = (TextView)imteLayout.findViewById(R.id.disscus_item_user_id);
						    text.setText(channlelist.get(i).getmDescription());
						    
						    ((LinearLayout)mThreadLayout).addView(imteLayout,Parames);
						    mThreadCount++;
						    
						}
						mNextPageBtn.setEnabled(true);
						mNextPageBtn.setVisibility(View.VISIBLE);
						if(channlelist.size() < PAGE_MAX_COUNT){
							mNextPageBtn.setText(R.string.btn_lastpage);
							mNextPageBtn.setOnClickListener(null);
						}else{
							mNextPageBtn.setText(R.string.btn_display_next_text);
							mNextPageBtn.setOnClickListener(DisscusViewActivity.this);
						}
						if(mThreadCount == 0){
							mNextPageBtn.setVisibility(View.GONE);
						}else{
							mNextPageBtn.setVisibility(View.VISIBLE);
						}
						
					}
					
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		});
	}
	@Override
	public void onClick(View v) {
		if(mNextPageBtn == v){
			mpageIndex++;
			setupData();
			mNextPageBtn.setText(getString(R.string.btn_load));
			mNextPageBtn.setEnabled(false);
			Log.i(TAG,"the page is : "+mpageIndex);
		}else if(mSubmitBtn == v){
			if(mInputEdit.getEditableText().toString().length() != 0){
				SharedPreferences userpref = mContext.getSharedPreferences("user",MODE_PRIVATE);
				final String user_name = userpref.getString("user_name", "");
				
				Map<String, String> params = new HashMap<String, String>();
				params.put("user_name",user_name);
				String text = mInputEdit.getEditableText().toString();
				params.put("comment_content",text);
				params.put("topic_id",mTopic_id);
				HttpConnectionUtil connect = new HttpConnectionUtil(getApplicationContext());
				String url =  ReadConfigFile.getServerAddress(mContext)+"index.php?controller=Discusstopic&action=PhoneTopicComment";
				mwaittingBar.setVisibility(View.VISIBLE);
				connect.asyncConnect(url,params ,HttpMethod.POST, new HttpConnectionCallback(){

					@Override
					public void execute(String response) {
						mwaittingBar.setVisibility(View.GONE);
						if("SUCCESS".equalsIgnoreCase(response)){
							mwaittingBar.setVisibility(View.VISIBLE);
							mpageIndex = 1;
							((LinearLayout)mThreadLayout).removeAllViews();
							setupData();
						}else{
							Log.i(TAG,"add new disscus error : "+response);
						}
						
					}});
			}
			mInputEdit.setText("");
		}
	}
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		 if ((firstVisibleItem + visibleItemCount == totalItemCount)  
	                && (totalItemCount != 0)) {  
			 	if(mThreadCount<5){
			 		mSubmitBtn.setVisibility(View.GONE);
				}else{
					mSubmitBtn.setVisibility(View.VISIBLE);
				}
	        }else{
	        	mSubmitBtn.setVisibility(View.GONE);
	        }
	}
	@Override
	public void onScrollStateChanged(AbsListView arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}
}
