package com.surfing.disscusgroup;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class VoteListActivity extends ActivityBase implements OnItemClickListener, OnClickListener{

	private static final String TAG = "VoteListActivity";
	private Context mContext = null;
	private static ArrayList<ChannelItem> mVotelist  = null;
	private String mConnectUrl = null;
	private static float mStartX = 0;
	private SimpleAdapter mListAdapter = null;
	private ArrayList<HashMap<String,Object>> mListArrayList;
	private ListView mList = null;
	private Button mButton = null;
	private static final int REQUEST_TYPE_REFRASH = 0;
	private static final int REQUEST_TYPE_NEXT = 1;
	private int mRequestType = REQUEST_TYPE_REFRASH;
	private static final int PAGE_MAX_COUNT = 10;
	private int mpageIndex = 1;
	//private ProgressBar mwaittingBar = null;
	private int mMode = 0;
	private Handler    myHandler = new Handler(){

        @Override
        public void handleMessage(Message msg)
        {
            switch(msg.what){
                case CommonUpdate.EVENT_UPDATE_WEATHER:{
                    if(ReadConfigFile.getTheme(mContext) == 1){
                        DisplayWeather.updateWeatherDisplay(getApplicationContext(),VoteListActivity.this);
                    }
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
		mContext = getApplicationContext();
		SharedPreferences prefMode = mContext.getSharedPreferences("mode",MODE_PRIVATE);
		int mMode = prefMode.getInt("mode",0);
		if(mMode == 1){
			super.onCreate(savedInstanceState,TITLE_TYPE_COMPANY,null,0,true);
		}else{
			super.onCreate(savedInstanceState);
		}
		  
		setContentView(R.layout.votelist_layout);
		setupView();
		mListArrayList = new ArrayList<HashMap<String,Object>>();
		mListAdapter = new SimpleAdapter(getApplicationContext(),
				mListArrayList,
				R.layout.vote_list_item_layout,
				new String[]{"title","desc","date","icon"}, 
				new int[]{R.id.vote_item_text_id,R.id.vote_item_user_id,R.id.vote_item_date_id,R.id.vote_item_img_id});
		mList.setAdapter(mListAdapter);
		mList.setOnItemClickListener(this);
		mpageIndex = 1;
		setupData();
		mButton.setOnClickListener(this);
		CloseReceiver.registerCloseActivity(this);
		CommonUpdate.getInstance().registerForUpdateWeather(myHandler,CommonUpdate.EVENT_UPDATE_WEATHER,null);
	}
	@Override
    protected void onStart()
    {
        mMode = ReadConfigFile.getMode(getApplicationContext());
        super.onStart();
    }
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
	    if(mMode == 0){
	        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
	            super.dialog();
	            return false;
	        } 
	    }
        
        return super.onKeyDown(keyCode, event);
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
		SharedPreferences prefMode = mContext.getSharedPreferences("mode",MODE_PRIVATE);
        int mMode = prefMode.getInt("mode",0);
        if(mMode == 1){
            DisplayWeather.updateWeatherDisplay(getApplicationContext(),this);
        }
	}
	private void setupData(){
		//mwaittingBar.setVisibility(View.VISIBLE);
		SharedPreferences userpref = this.getApplicationContext().getSharedPreferences("user",MODE_PRIVATE);
		String user_name = userpref.getString("user_name", "");
		mConnectUrl = ReadConfigFile.getServerAddress(mContext)+"index.php?controller=vote&action=require&user_name="+user_name+"&page="+mpageIndex;
		HttpConnectionUtil connect = new HttpConnectionUtil(getApplicationContext());
		ConnectWeb(connect,mConnectUrl);
	}
	private void ConnectWeb(HttpConnectionUtil connect,String geturl){
		Log.i(TAG,"connection url is : "+geturl);
		connect.asyncConnect(geturl, HttpMethod.GET, new HttpConnectionCallback(){
			@Override
			public void execute(String response) {
				//mwaittingBar.setVisibility(View.GONE);
				if((response == null) 
                        || (response.length() == 0) 
                        || response.equals(HttpConnectionUtil.CONNECT_FAILED)
                        || HttpConnectionUtil.RETURN_FAILED.equalsIgnoreCase(response))
                {
                    Log.i(TAG,"request failed");
                    return;
                }
				
				InputStream stream = new ByteArrayInputStream(response.getBytes());
				Log.i(TAG,"string="+response);
				
				ChannelInformation channelInfo = DomXMLReader.readXML(mContext,stream);
				if(channelInfo != null){
					HashMap<String,Object> map = null;
					ArrayList<ChannelItem> channlelist = (ArrayList<ChannelItem>) channelInfo.getmChannelItemList();
					//String ServerUrl = ReadConfigFile.getServerAddress(mContext);
					for(int i = 0;i<channlelist.size();i++){
						map = new HashMap<String,Object>();
						Log.i(TAG,"the title is : "+channlelist.get(i).getmTitle());
						map.put("title",channlelist.get(i).getmTitle());
						map.put("desc",channlelist.get(i).getmDescription());
						map.put("link",channlelist.get(i).getmLink());
						map.put("icon",R.drawable.vote_item);
						mListArrayList.add(map);
					}
					mListAdapter.notifyDataSetChanged();
					mButton.setEnabled(true);
					if(channlelist.size() == 0){
						if(mRequestType == REQUEST_TYPE_NEXT)
						{
							mpageIndex--;
							mButton.setText(getString(R.string.btn_lastpage));
							mButton.setVisibility(View.VISIBLE);
							mButton.setOnClickListener(null);
						}else{
							mButton.setVisibility(View.GONE);
						}
					}
					if(channlelist.size() < PAGE_MAX_COUNT){
						mButton.setText(getString(R.string.btn_lastpage));
						mButton.setVisibility(View.VISIBLE);
						mButton.setOnClickListener(null);
					}else{
						mButton.setText(getString(R.string.btn_display_next_text));
						mButton.setVisibility(View.VISIBLE);
						mButton.setOnClickListener(VoteListActivity.this);
					}
				}
				
			}
			
		});
	}
	private void setupView(){
		mList = (ListView)findViewById(R.id.vote_list_id);
		//mwaittingBar = (ProgressBar)findViewById(R.id.loading_progressBar);
		mButton = (Button)findViewById(R.id.Vote_display_btn_id);
		mButton.setVisibility(View.GONE);
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
	    if(HttpConnectionUtil.goodNet(mContext)){
	        HashMap<String,Object> map =  mListArrayList.get(position);
	        Intent myIntent = new Intent();
	        myIntent.putExtra("link",map.get("link").toString());
	        myIntent.setClass(mContext,VoteViewActivity.class);
	        startActivity(myIntent);
	    }else{
	        Toast.makeText(mContext,getString(R.string.network_error),Toast.LENGTH_SHORT).show();
	    }
		
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		menu.add(0, menu.FIRST, Menu.NONE,R.string.refrash);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()){
		case Menu.FIRST:{ //refash
			mListArrayList.clear();
			mRequestType = REQUEST_TYPE_REFRASH;
			HttpConnectionUtil connect = new HttpConnectionUtil(getApplicationContext());
			ConnectWeb(connect,mConnectUrl);
		}
		break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	@Override
	public void onClick(View v)
	{
		if(v == mButton){
			mRequestType = REQUEST_TYPE_NEXT;
			mpageIndex++;
			setupData();
			mButton.setEnabled(false);
			mButton.setText(R.string.btn_load);
			Log.i(TAG,"the page is : "+mpageIndex);
		}
	}
}
