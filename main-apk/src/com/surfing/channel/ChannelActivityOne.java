package com.surfing.channel;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.surfing.R;
import com.surfing.channel.NetImitate.ImageCallback;
import com.surfing.httpconnection.HttpConnectionCallback;
import com.surfing.httpconnection.HttpConnectionUtil;
import com.surfing.httpconnection.HttpConnectionUtil.HttpMethod;
import com.surfing.httpconnection.ImageDownloader;
import com.surfing.rssparse.ChannelInformation;
import com.surfing.rssparse.ChannelItem;
import com.surfing.rssparse.DomXMLReader;
import com.surfing.util.ReadConfigFile;
import com.surfing.util.SaveRssFile;
import com.surfing.util.ThemeUpdateUitl;
import com.surfing.util.TitleBarDisplay;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class ChannelActivityOne extends ActivityBase implements OnClickListener,OnItemClickListener{
	private final String TAG="ChannelActivityOne";
	//private ListView mchannelList = null;
	private SimpleAdapter mListAdapter = null;
	private BaseAdapter mBannerAdapter = null;
	private ArrayList<HashMap<String,Object>> mListArrayList;
	private ArrayList<HashMap<String,Object>> mBannerDataList;;
	private ProgressBar mwaittingBar = null;
	private Context mContext = null;
	private String mConnectUrl = null;
	private Button mButton = null;
	private int mpageIndex = 1;
	private String mChannel_id = null;
	private GestureDetector detector;
	private ViewFlipper mBannerFlipper = null;
	private View mBannerView = null;
	private View mListViewItem = null;
	private ListView mListView = null;
	private ArrayList<View> mBannerItemArray = null;
	private ArrayList<View> mListItemArray = null;
	private int mBannerIndex = 0;
	private int mSumBanner = 0;
	private LayoutInflater mLayoutInflater= null;
	private MyScrollView mScrollView = null;
	private float mOldX = 0;
	private boolean mblaction = false;
	private static final int PAGE_SIZE = 5;
	private int mMode = 0;
	private static final int REQUEST_TYPE_REFRASH = 0;
	private static final int REQUEST_TYPE_NEXT = 1;
	private int mRequestType = REQUEST_TYPE_REFRASH;
	private static final int PAGE_MAX_COUNT = 10;
	private Gallery mBannerGallery = null;
	private static final String BTN_NEXT_FLG_TEXT = "surfing_btn_next";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mContext = this.getApplicationContext();
	    mMode = ReadConfigFile.getMode(mContext);
	    if(mMode == 1){
	    	String title = getIntent().getStringExtra(ChannelTabActivity.CHANNEL_TITLE);
	    	super.onCreate(savedInstanceState,TITLE_TYPE_DEFINE_TEXT,title,0,false);
	    }else{
	    	super.onCreate(savedInstanceState);
			requestWindowFeature(Window.FEATURE_NO_TITLE);
	    }
		
		setContentView(R.layout.channel_layout);
		setupView();
		
		mLayoutInflater  = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
		
		//detector = new GestureDetector(this);
		//mBannerFlipper.setOnTouchListener(this);
		//mScrollView.setGestureDetector(detector);
		mChannel_id = this.getIntent().getStringExtra(ChannelTabActivity.CHANNLE_LINK);
		mListArrayList = new ArrayList<HashMap<String,Object>>();
		mBannerDataList  = new ArrayList<HashMap<String,Object>>();
		mListItemArray = new ArrayList<View>();
		
		setupData(mChannel_id);
		setupBannerData(mChannel_id);
		mListAdapter = new SimpleAdapterList(mContext,mListArrayList,R.layout.channel_listitem_layout,
				new String[]{"icon","title","desc"},
				new int[]{R.id.channel_listitem_img_id,R.id.channel_listitem_title_id,R.id.channel_listitem_desc_id});
		
		
		mListView.addHeaderView(LayoutInflater.from(this).inflate(
				R.layout.banner_gallery, null));
		mBannerGallery = (Gallery)findViewById(R.id.gallery);
		mBannerAdapter = new SimpleAdapterBanner();
		mBannerGallery.setAdapter(mBannerAdapter);
		mBannerGallery.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int position, long id)
			{
				 HashMap<String,Object> map = mBannerDataList.get(position);
				 String link = (String)map.get("link");
				    if(link != null){
				        Log.i(TAG,"connect the news information");
				        Intent myIntent = new Intent();
			            myIntent.setClass(ChannelActivityOne.this,NewsInformationActivity.class);
			            myIntent.putExtra("link",link);
			            startActivity(myIntent);
				    }
			}
		});
		
		//add foot view 
		mListView.addFooterView(LayoutInflater.from(this).inflate(R.layout.list_next_button,null));
		mButton = (Button)findViewById(R.id.channel_list_next_btn_id);
		mButton.setOnClickListener(this);
		mButton.setVisibility(View.INVISIBLE);
		mListView.setAdapter(mListAdapter);
		mListView.setOnItemClickListener(this);
		//mButton.setOnClickListener(this);	
		mpageIndex = 1;
		CloseReceiver.registerCloseActivity(this);
	}
	
	

    @Override
    protected void onStart()
    {
        mMode = ReadConfigFile.getMode(mContext);
        super.onStart();
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
	    if(mMode == 0){
	        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
	            super.dialog();
	            return false;
	        }
	    }
		
		return super.onKeyDown(keyCode, event);
	}
	private void setupView(){
		//mButton = (Button)findViewById(R.id.channel_display_btn_id);
		mwaittingBar = (ProgressBar)findViewById(R.id.loading_progressBar);
		//mBannerFlipper = (ViewFlipper)findViewById(R.id.channel_viewfilpper_id);
		//mBannerView = (View)findViewById(R.id.channel_viewfilpper_id);
		mListView = (ListView)findViewById(R.id.channel_list_id);
		//mScrollView = (MyScrollView)findViewById(R.id.channel_scroll_id);
	}
	private void setupData(String id){ 	
		//setup list data
		
		if(id.equals("public")){
			mConnectUrl = ReadConfigFile.getServerAddress(mContext)+"index.php?controller=channel&action=Require&channel_id=3&page="+mpageIndex;
		}else{
			mConnectUrl = ReadConfigFile.getServerAddress(mContext)+"index.php?controller=column&action=require&column_id="+id+"&page="+mpageIndex;
		}
		mwaittingBar.setVisibility(View.VISIBLE);
		//String geturl = url;
		Log.i(TAG,"the url is : "+mConnectUrl);
		
		HttpConnectionUtil connect = new HttpConnectionUtil(getApplicationContext());
		ConnectWeb(connect,mConnectUrl);
		
	}
	private void setupBannerData(String id){ 	
		//setup list data
		String bannerUril = null;
		SharedPreferences userpref = this.getApplicationContext().getSharedPreferences("user", MODE_PRIVATE);
		String user_name = userpref.getString("user_name", "");
		if(id.equals("public")){
			bannerUril = ReadConfigFile.getServerAddress(mContext)+"index.php?controller=user&action=requirebanner&user_name="+user_name;
		}else{
			bannerUril = ReadConfigFile.getServerAddress(mContext)+"index.php?controller=user&action=requirebanner&user_name="+user_name;
		}
		
		mwaittingBar.setVisibility(View.VISIBLE);
		//String geturl = url;
		Log.i(TAG,"setupBannerData url is : "+bannerUril);
		HttpConnectionUtil connect = new HttpConnectionUtil(getApplicationContext());
		ConnectWebBanner(connect, bannerUril);
		
	}
	public void ConnectWeb(HttpConnectionUtil connect,String geturl){
		Log.i(TAG,"the url is : "+geturl);
		connect.asyncConnect(geturl, HttpMethod.POST, new HttpConnectionCallback(){
			
			@Override
			public void execute(String response) {
				
				//setupBannerData(mChannel_id);
				mButton.setText(getString(R.string.btn_display_next_text));
				mButton.setVisibility(View.VISIBLE);
				if((response == null) 
				    || (response.length() == 0) 
				    || response.equals(HttpConnectionUtil.CONNECT_FAILED)
				    || HttpConnectionUtil.RETURN_FAILED.equalsIgnoreCase(response)){
				    //Toast.makeText(mContext,"connect service error",Toast.LENGTH_LONG).show();
				    return;
				}
				try{
					InputStream stream = new ByteArrayInputStream(response.getBytes());
					Log.i(TAG,response);
					//SaveRssFile savefile = new SaveRssFile(mContext);
					//savefile.SaveFile(response.substring(nindex),"disscus.xml");
					ChannelInformation channelInfo = DomXMLReader.readXML(ChannelActivityOne.this,stream);
					if(channelInfo != null){
						HashMap<String,Object> map = null;
						ArrayList<ChannelItem> channlelist = (ArrayList<ChannelItem>) channelInfo.getmChannelItemList();
						//String ServerUrl = ReadConfigFile.getServerAddress(mContext);
						for(int i = 0;i<channlelist.size();i++){
							map = new HashMap<String,Object>();
							Log.i(TAG,"the title is : "+channlelist.get(i).getmTitle());
							Log.i(TAG,"the desc is : "+channlelist.get(i).getmDescription());
							Log.i(TAG,"the icon is : "+channlelist.get(i).getmIcon());
							map.put("title",channlelist.get(i).getmTitle());
							map.put("desc",channlelist.get(i).getmDescription());
							map.put("link",channlelist.get(i).getmLink());
							map.put("icon",channlelist.get(i).getmIcon());
							mListArrayList.add(map);
						}
						if(channlelist.size() == 0){
							mButton.setText(getString(R.string.btn_lastpage));
						}else{
							mListAdapter.notifyDataSetChanged();
						}
						
					}
					
				}catch (Exception e) {
					if(mRequestType == REQUEST_TYPE_NEXT)
					{
						mpageIndex--;
					}
					e.printStackTrace();
				}
				mwaittingBar.setVisibility(View.GONE);
			}
			
		});
	}
	
	public void ConnectWebBanner(HttpConnectionUtil connect,String geturl){
		Log.i(TAG,"ConnectWebBanner the url is : "+geturl);
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
					Log.i(TAG,"string="+response); 
					//SaveRssFile savefile = new SaveRssFile(mContext);
					//savefile.SaveFile(response.substring(nindex),"disscus.xml");
					ChannelInformation channelInfo = DomXMLReader.readXML(ChannelActivityOne.this,stream);
					if(channelInfo != null){
						HashMap<String,Object> map = null;
						ArrayList<ChannelItem> channlelist = (ArrayList<ChannelItem>) channelInfo.getmChannelItemList();
						//String ServerUrl = ReadConfigFile.getServerAddress(mContext);
						for(int i = 0;i<channlelist.size();i++){
							map = new HashMap<String,Object>();
							Log.i(TAG,"banner the title is : "+channlelist.get(i).getmTitle());
							Log.i(TAG,"banner the desc is : "+channlelist.get(i).getmDescription());
							Log.i(TAG,"banner the link is : "+channlelist.get(i).getmLink());
							Log.i(TAG,"banner the icon is : "+channlelist.get(i).getmIcon());
							map.put("title",channlelist.get(i).getmTitle());
							map.put("desc",channlelist.get(i).getmDescription());
							map.put("link",channlelist.get(i).getmLink());
							map.put("icon",channlelist.get(i).getmIcon());
							mBannerDataList.add(map); 
						} 
						mSumBanner = mBannerDataList.size();
						mBannerAdapter.notifyDataSetChanged();

					}
					
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		});
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
			mBannerDataList.clear();
			mListItemArray.clear();
			mBannerAdapter.notifyDataSetChanged();
			mListAdapter.notifyDataSetChanged();
			mpageIndex = 1;
			//HttpConnectionUtil connect = new HttpConnectionUtil(getApplicationContext());
			//ConnectWeb(connect,mConnectUrl);
			setupData(mChannel_id);
			setupBannerData(mChannel_id);
			mButton.setVisibility(View.INVISIBLE);
			mButton.setText(getString(R.string.btn_display_next_text));
			mRequestType = REQUEST_TYPE_REFRASH;
		}
		break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		if((v == mButton) && !(getString(R.string.btn_lastpage).equals(mButton.getText()))){
			mpageIndex++;
			setupData(mChannel_id); 
			Log.i(TAG,"the page is : "+mpageIndex);
			mButton.setText(getString(R.string.btn_load));
			mRequestType = REQUEST_TYPE_NEXT;
		}
	}
	
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev)
    {   
//        if(detector.onTouchEvent(ev)){
//            return false;
//        }
        return super.dispatchTouchEvent(ev);
    }
    @Override
	public boolean onTouchEvent(MotionEvent event) {
	
	  return super.onTouchEvent(event);
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View v, int position, long id)
	{
		 
		 HashMap<String,Object> map = mListArrayList.get(position-1);
		 String link = (String)map.get("link");
		
		    if(link != null){
		        Log.i(TAG,"connect the news information");
		        Intent myIntent = new Intent();
	            myIntent.setClass(ChannelActivityOne.this,NewsInformationActivity.class);
	            myIntent.putExtra("link",link);
	            startActivity(myIntent);
		    }
		
	}
	private void updateImage(HashMap<String,Object> map, ImageView imageview,final View viewparent){
		Bitmap bitmap = null;
		boolean blDownload = true;
		String imageUrl = map.get("icon").toString();
		ContentResolver Resolver = mContext.getContentResolver();
		Cursor cursor = Resolver.query(PhotoProviderData.PhotoData.CONTENT_URI, new String[] { PhotoProviderData.PHOTO_PATH }, PhotoProviderData.PHOTO_URL + "='" + imageUrl + "'", null, null);
		if ((cursor != null) && (cursor.getCount() > 0))
		{
			cursor.moveToFirst();
			String path = cursor.getString(0);
			File img = new File(path);
			if (img.exists())
			{
				blDownload = false;
				try
				{
					Log.i("chenmei","load from sdcard");
					bitmap = BitmapFactory.decodeFile(path);
					imageview.setImageBitmap(bitmap);
				}
				catch (OutOfMemoryError e)
				{
					bitmap = null;
					e.printStackTrace();
				}
			}
			
		}
		if(cursor != null)
		{
			cursor.close();
		}
		imageview.setTag(imageUrl);
		if(blDownload){
			NetImitate.getInstance(mContext).downloadAndBindImage(map,imageUrl, new ImageCallback(){

				@Override
				public void imageLoaded(Bitmap bitmap, String imageUrl)
				{
					Log.i("chenmei","imageLoaded from url");
					ImageView image = (ImageView)viewparent.findViewWithTag(imageUrl);
					if(image != null){
						image.setImageBitmap(bitmap);
					}
					
				}
				
			});
		}
	}
	class SimpleAdapterList extends SimpleAdapter{

		public SimpleAdapterList(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to)
		{
			super(context, data, resource, from, to);
			// TODO Auto-generated constructor stub
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View view  = convertView;
			HashMap<String,Object> map = mListArrayList.get(position);
			if(view == null){
				view = (View)View.inflate(mContext,R.layout.channel_listitem_layout,null);
			}
			TextView title = (TextView)view.findViewById(R.id.channel_listitem_title_id);
			title.setText((String)map.get("title"));
			TextView desc = (TextView)view.findViewById(R.id.channel_listitem_desc_id);
			desc.setText((String)map.get("desc"));
			
			ImageView image = (ImageView)view.findViewById(R.id.channel_listitem_img_id);
			updateImage(map,image,mListView);
			return view;
		}
		
	}
	class SimpleAdapterBanner extends BaseAdapter{

		

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			HashMap<String,Object> map = mBannerDataList.get(position);
			View view = (View)View.inflate(mContext,R.layout.channel_layout_new,null);
			TextView text = (TextView)view.findViewById(R.id.channel_firstnews_title_id);
			text.setText((String)map.get("title"));
			ImageView image = (ImageView)view.findViewById(R.id.channel_firstnews_img_id);
			updateImage(map,image,mBannerGallery);
			return view;
		}

		@Override
		public int getCount()
		{
			Log.i(TAG,"--- mBannerDataList.size() = "+mBannerDataList.size());
			return mBannerDataList.size();
		}

		@Override
		public Object getItem(int position)
		{
			// TODO Auto-generated method stub
			return mBannerDataList.get(position);
		}

		@Override
		public long getItemId(int positon)
		{
			// TODO Auto-generated method stub
			return positon;
		}
	}
}
