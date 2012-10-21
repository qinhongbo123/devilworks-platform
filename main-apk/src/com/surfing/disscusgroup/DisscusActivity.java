package com.surfing.disscusgroup;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;

import com.surfing.R;
import com.surfing.channel.ActivityBase;
import com.surfing.channel.ChannelActivityOne;
import com.surfing.channel.CloseReceiver;
import com.surfing.channel.CommonUpdate;
import com.surfing.channel.MenuGridActivity;
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
import com.surfing.util.TitleBarDisplay;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class DisscusActivity extends ActivityBase implements OnItemClickListener,OnClickListener,OnScrollListener{
	private static final String TAG = "DisscusActivity";
	private ListView mList = null;
	//private ImageView mCountView = null;
	//private TextView mTitleView = null;
	private Context mContext = null;
	private String mConnectUrl = null;
	private SimpleAdapter mListAdapter = null;
	private ArrayList<HashMap<String,Object>> mListArrayList;
	private Button mButton = null;
	private int mpageIndex ;
	private String mUser_name;
	//private ProgressBar mwaittingBar = null;
	private int mMode = 0;
	private static final int REQUEST_TYPE_REFRASH = 0;
	private static final int REQUEST_TYPE_NEXT = 1;
	private int mRequestType = REQUEST_TYPE_REFRASH;
	private static final int PAGE_MAX_COUNT = 10;
	private Handler    myHandler = new Handler(){

        @Override
        public void handleMessage(Message msg)
        {
            switch(msg.what){
                case CommonUpdate.EVENT_UPDATE_WEATHER:{
                    if(ReadConfigFile.getTheme(mContext) == 1){
                        DisplayWeather.updateWeatherDisplay(getApplicationContext(),DisscusActivity.this);
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
		mContext = getApplicationContext();
		mMode = ReadConfigFile.getMode(mContext);
		if(mMode == 1){
			super.onCreate(savedInstanceState,TITLE_TYPE_COMPANY,null,0,true);
		}else{
			super.onCreate(savedInstanceState);
		}
		setContentView(R.layout.disscuss_title_layout);
		setupView();	
		mListArrayList = new ArrayList<HashMap<String,Object>>();
		mListAdapter = new SimpleAdapter(getApplicationContext(),
				mListArrayList,
				R.layout.disscus_listitem_layout,
				new String[]{"title","desc"}, 
				new int[]{R.id.channel_listitem_title_id,R.id.channel_listitem_desc_id});
		mList.setAdapter(mListAdapter);
		mpageIndex = 1;
		SharedPreferences userpref = mContext.getSharedPreferences("user",MODE_PRIVATE);
		mUser_name = userpref.getString("user_name", "");
		setupData();
		mList.setOnItemClickListener(this);
		mList.setOnScrollListener(this);
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
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, menu.FIRST, Menu.NONE, getString(R.string.disscus_new_text));
		menu.add(0,menu.FIRST+1,Menu.NONE,getString(R.string.refrash));
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case Menu.FIRST:{ //create new
			final EditText editText = new EditText(this);
			SharedPreferences userpref = mContext.getSharedPreferences("user",MODE_PRIVATE);
			final String user_name = userpref.getString("user_name", "");
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.disscus_add_new_text)
            .setView(editText).setPositiveButton(R.string.ok, null)
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					if(editText.getEditableText().toString().length() == 0){
						return;
					}
					Map<String, String> params = new HashMap<String, String>();
					params.put("user_name",user_name);
					String text = editText.getEditableText().toString();
					params.put("topic_content",text);
					HttpConnectionUtil connect = new HttpConnectionUtil(getApplicationContext());
					String url =  ReadConfigFile.getServerAddress(mContext)+"index.php?controller=Discusstopic&action=PhoneWriteTopic";
					//mwaittingBar.setVisibility(View.VISIBLE);
					connect.asyncConnect(url,params ,HttpMethod.POST, new HttpConnectionCallback(){

						@Override
						public void execute(String response) {
							//mwaittingBar.setVisibility(View.GONE);
							if(response.equalsIgnoreCase("success")){
								mListArrayList.clear();
								setupData();
							}else{
								Log.i(TAG,"add new disscus error : "+response);
							}
							
						}});
				}
           });
            builder.create().show();
		}
		break;
		case Menu.FIRST+1:{//frash
			mRequestType = REQUEST_TYPE_REFRASH;
		    mpageIndex=1;
			mListArrayList.clear();
			setupData();
		}
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void setupView(){
		mList = (ListView)findViewById(R.id.disscuss_list_id);
		//mCountView = (ImageView)findViewById(R.id.Diss_title_count_view_id);
		//mTitleView = (TextView)findViewById(R.id.Diss_title_text_id);
		mButton = (Button)findViewById(R.id.Disscus_display_btn_id);
		mButton.setVisibility(View.GONE);
		
	}
	
	private void setupData(){ 
		//setup list data
		mConnectUrl = ReadConfigFile.getServerAddress(mContext)+"index.php?controller=Discusstopic&action=DisscussRequire&user_name="+mUser_name+"&page="+mpageIndex;
		//.setVisibility(View.VISIBLE);
		Log.i(TAG,"the url is : "+mConnectUrl);
		HttpConnectionUtil connect = new HttpConnectionUtil(getApplicationContext());
		ConnectWeb(connect,mConnectUrl);
		
	}
	public void ConnectWeb(HttpConnectionUtil connect,String geturl){
		Log.i(TAG,"the url is : "+geturl);
		connect.asyncConnect(geturl, HttpMethod.POST, new HttpConnectionCallback(){
			
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
				try{
					InputStream stream = new ByteArrayInputStream(response.getBytes());
					Log.i(TAG,"string = "+response);
					//SaveRssFile savefile = new SaveRssFile(mContext);
					//savefile.SaveFile(response.substring(nindex),"toutiao.xml");
					ChannelInformation channelInfo = DomXMLReader.readXML(mContext,stream);
					if(channelInfo != null){
						HashMap<String,Object> map = null;
						ArrayList<ChannelItem> channlelist = (ArrayList<ChannelItem>) channelInfo.getmChannelItemList();
						//String ServerUrl = ReadConfigFile.getServerAddress(mContext);
						for(int i = 0;i<channlelist.size();i++){
							map = new HashMap<String,Object>();
							Log.i(TAG,"the title is : "+channlelist.get(i).getmTitle());
							Log.i(TAG,"the desc is : "+channlelist.get(i).getmDescription());
							map.put("title",channlelist.get(i).getmTitle());
							map.put("desc",channlelist.get(i).getmDescription()+"    "+channlelist.get(i).getmDate());
							map.put("link",channlelist.get(i).getmLink());
							map.put("icon",channlelist.get(i).getmIcon());
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
							mButton.setOnClickListener(DisscusActivity.this);
						}
						
					}
					
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		});
	}
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount){
//		 if ((firstVisibleItem + visibleItemCount == totalItemCount)  
//	                && (totalItemCount != 0)) {  
//			 	if(mListArrayList.size()<5){
//					mButton.setVisibility(View.GONE);
//				}else{
//					mButton.setVisibility(View.VISIBLE);
//				}
//	        }else{
//	        	mButton.setVisibility(View.GONE);
//	        }
	}
	@Override
	public void onScrollStateChanged(AbsListView arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onClick(View v) {
		if(v == mButton){
			mRequestType = REQUEST_TYPE_NEXT;
			mpageIndex++;
			setupData();
			mButton.setEnabled(false);
			mButton.setText(R.string.btn_load);
			Log.i(TAG,"the page is : "+mpageIndex);
		}
	}
	@Override
	public void onItemClick(AdapterView<?> adapter, View v, int posisition, long id) {
		
		HashMap<String,Object> map = mListArrayList.get(posisition);
		Intent myIntent = new Intent();
		myIntent.setClass(mContext,DisscusViewActivity.class);
		myIntent.putExtra("topic_id",map.get("link").toString());
		myIntent.putExtra("topic",map.get("title").toString());
		startActivity(myIntent);
	}
}
