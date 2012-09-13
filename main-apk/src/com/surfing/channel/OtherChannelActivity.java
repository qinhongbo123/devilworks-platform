package com.surfing.channel;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.surfing.R;
import com.surfing.Notification.NotificationListActivity;
import com.surfing.httpconnection.HttpConnectionCallback;
import com.surfing.httpconnection.HttpConnectionUtil;
import com.surfing.httpconnection.HttpConnectionUtil.HttpMethod;
import com.surfing.httpconnection.ImageDownloader;
import com.surfing.rssparse.ChannelInformation;
import com.surfing.rssparse.ChannelItem;
import com.surfing.rssparse.DomXMLReader;
import com.surfing.util.DisplayWeather;
import com.surfing.util.ReadConfigFile;
import com.surfing.util.SaveRssFile;
import com.surfing.util.ThemeUpdateUitl;
import com.surfing.util.TitleBarDisplay;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class OtherChannelActivity extends ActivityBase implements OnItemClickListener,OnClickListener{
	private final String TAG="ChannelActivityOne";
	private ListView mchannelList = null;
	private SimpleAdapter mListAdapter = null;
	private ArrayList<HashMap<String,Object>> mListArrayList;
	private ProgressDialog mwaittingBar = null;
	private Context mContext = null;
	private String mConnectUrl = null;
	private Window mWindow;
	private Context    mCurrentContext;
    private int mTheme = 0;
    private Button mButton = null;
	private static final int REQUEST_TYPE_REFRASH = 0;
	private static final int REQUEST_TYPE_NEXT = 1;
	private int mRequestType = REQUEST_TYPE_REFRASH;
	private static final int PAGE_MAX_COUNT = 10;
	private int mpageIndex = 1;
	private String mChannel_id;
    private Handler    myHandler = new Handler(){

        @Override
        public void handleMessage(Message msg)
        {
            switch(msg.what){
                case CommonUpdate.EVENT_UPDATE_WEATHER:{
                    DisplayWeather.updateWeatherDisplay(getApplicationContext(),OtherChannelActivity.this);
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
		
		mContext = this.getApplicationContext();
		
		setContentView(R.layout.other_channel_layout);
		setupView();
		mChannel_id = this.getIntent().getStringExtra(ChannelTabActivity.CHANNLE_LINK);
		setupData(mChannel_id);
		mchannelList.setOnItemClickListener(this);
		mListArrayList = new ArrayList<HashMap<String,Object>>();
		mListAdapter = new ListSimpleAdapter(getApplicationContext(),
				mListArrayList,
				R.layout.channel_listitem_layout,
				new String[]{"icon","title","desc"}, 
				new int[]{R.id.channel_listitem_img_id,R.id.channel_listitem_title_id,R.id.channel_listitem_desc_id});
		mchannelList.setAdapter(mListAdapter);
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
		mchannelList = (ListView)findViewById(R.id.channel_list_id);
		mButton = (Button)findViewById(R.id.channel_display_btn_id);
		
	}
	private void setupData(String id){
		mConnectUrl = ReadConfigFile.getServerAddress(mContext)+"index.php?controller=column&action=require&column_id="+id+"&page="+mpageIndex;
		
		Log.i(TAG,"the url is : "+mConnectUrl);
		HttpConnectionUtil connect = new HttpConnectionUtil(getApplicationContext());
		ConnectWeb(connect,mConnectUrl);
		
	}
	public void ConnectWeb(HttpConnectionUtil connect,String geturl){
		connect.asyncConnect(geturl, HttpMethod.POST, new HttpConnectionCallback(){

			@Override
			public void execute(String response) {
				//mwaittingBar.dismiss();
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
					Log.i(TAG,"string="+response);
					//SaveRssFile savefile = new SaveRssFile(mContext);
					//savefile.SaveFile(response,"toutiao.xml");
					ChannelInformation channelInfo = DomXMLReader.readXML(OtherChannelActivity.this,stream);
					if(channelInfo != null){
						HashMap<String,Object> map = null;
						ArrayList<ChannelItem> channlelist = (ArrayList<ChannelItem>) channelInfo.getmChannelItemList();
						String ServerUrl = ReadConfigFile.getServerAddress(mContext);
						for(int i = 0;i<channlelist.size();i++){
							map = new HashMap<String,Object>();
							Log.i(TAG,"the title is : "+channlelist.get(i).getmTitle());
							map.put("title",channlelist.get(i).getmTitle());
							map.put("desc",channlelist.get(i).getmDescription());
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
							
							mButton.setOnClickListener(OtherChannelActivity.this);
						}
					}
					
				}catch (Exception e) {
					e.printStackTrace();
				} 
			}
		});
	}
	@Override
	public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
		HashMap<String,Object> map = mListArrayList.get(position);
		Intent myIntent = new Intent();
		myIntent.setClass(OtherChannelActivity.this,NewsInformationActivity.class);
		myIntent.putExtra("link",map.get("link").toString());
		
		startActivity(myIntent);
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		menu.add(0, menu.FIRST, Menu.NONE, R.string.refrash);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()){
		case Menu.FIRST:{ //refash
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

	private class ListSimpleAdapter extends SimpleAdapter {

		private final ImageDownloader imageDownloader = new ImageDownloader();

		private Map<Integer, View> viewMap = new HashMap<Integer, View>();
		private List<? extends Map<String, ?>> mList = null;
		
		public ListSimpleAdapter(Context context,
				List<? extends Map<String, ?>> data, int resource,
				String[] from, int[] to) {
			super(context, data, resource, from, to);
			mList = data;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = null;
			view =  super.getView(position, convertView, parent);
			HashMap<String,Object> map = (HashMap<String, Object>) mList.get(position);
			ImageView iconView = (ImageView) view.findViewById(R.id.channel_listitem_img_id);
			imageDownloader.download((String)map.get("icon"), iconView,0,getApplicationContext());
			return view;
		}
	}

	@Override
	public void onClick(View v) {
		if(v == mButton){
			mpageIndex++;
			setupData(mChannel_id);
			Log.i(TAG,"the page is : "+mpageIndex);
			mButton.setText(getString(R.string.btn_load));
			mButton.setEnabled(false);
			mRequestType = REQUEST_TYPE_NEXT;
		}
	}	
}
