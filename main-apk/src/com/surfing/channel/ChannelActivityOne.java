package com.surfing.channel;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.surfing.R;
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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
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

public class ChannelActivityOne extends ActivityBase implements OnClickListener,OnScrollListener , OnGestureListener,OnTouchListener{
	private final String TAG="ChannelActivityOne";
	//private ListView mchannelList = null;
	private SimpleAdapter mListAdapter = null;
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
	private LinearLayout mListView = null;
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
		initFlipper();
		detector = new GestureDetector(this);
		mBannerFlipper.setOnTouchListener(this);
		mScrollView.setGestureDetector(detector);
		mChannel_id = this.getIntent().getStringExtra(ChannelTabActivity.CHANNLE_LINK);
		mListArrayList = new ArrayList<HashMap<String,Object>>();
		mBannerDataList  = new ArrayList<HashMap<String,Object>>();
		mListItemArray = new ArrayList<View>();
		
		setupData(mChannel_id);
		setupBannerData(mChannel_id);
		mButton.setOnClickListener(this);
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

    private void initFlipper(){
		mBannerItemArray = new ArrayList<View>();
		TextView textview = null;
		
		View itemView = mLayoutInflater.inflate(R.layout.channel_layout_new,mBannerFlipper,false);
		textview = (TextView)itemView.findViewById(R.id.channel_firstnews_title_id);
		textview.getBackground().setAlpha(100);
		mBannerFlipper.addView(itemView);
		mBannerItemArray.add(itemView);
		
		itemView = mLayoutInflater.inflate(R.layout.channel_layout_new,mBannerFlipper,false);
		textview = (TextView)itemView.findViewById(R.id.channel_firstnews_title_id);
		textview.getBackground().setAlpha(100);
		mBannerFlipper.addView(itemView);
		mBannerItemArray.add(itemView);
		
		itemView = mLayoutInflater.inflate(R.layout.channel_layout_new,mBannerFlipper,false);
		textview = (TextView)itemView.findViewById(R.id.channel_firstnews_title_id);
		textview.getBackground().setAlpha(100);
		mBannerFlipper.addView(itemView);
		mBannerItemArray.add(itemView);
		
		itemView = mLayoutInflater.inflate(R.layout.channel_layout_new,mBannerFlipper,false);
		textview = (TextView)itemView.findViewById(R.id.channel_firstnews_title_id);
		textview.getBackground().setAlpha(100);
		mBannerFlipper.addView(itemView);
		mBannerItemArray.add(itemView);
		
		itemView = mLayoutInflater.inflate(R.layout.channel_layout_new,mBannerFlipper,false);
		textview = (TextView)itemView.findViewById(R.id.channel_firstnews_title_id);
		textview.getBackground().setAlpha(100);
		mBannerFlipper.addView(itemView);
		mBannerItemArray.add(itemView);
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
		mButton = (Button)findViewById(R.id.channel_display_btn_id);
		mwaittingBar = (ProgressBar)findViewById(R.id.loading_progressBar);
		mBannerFlipper = (ViewFlipper)findViewById(R.id.channel_viewfilpper_id);
		mBannerView = (View)findViewById(R.id.channel_viewfilpper_id);
		mListView = (LinearLayout)findViewById(R.id.channel_list_id);
		mScrollView = (MyScrollView)findViewById(R.id.channel_scroll_id);
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
		Log.i(TAG,"the url is : "+bannerUril);
		HttpConnectionUtil connect = new HttpConnectionUtil(getApplicationContext());
		ConnectWebBanner(connect, bannerUril);
		
	}
	public void ConnectWeb(HttpConnectionUtil connect,String geturl){
		Log.i(TAG,"the url is : "+geturl);
		connect.asyncConnect(geturl, HttpMethod.POST, new HttpConnectionCallback(){
			
			@Override
			public void execute(String response) {
				
				//setupBannerData(mChannel_id);
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
							addListItem(map);
						}
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
							mButton.setOnClickListener(ChannelActivityOne.this);
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
	public void addListItem(HashMap<String,Object> map){
	    ImageView icon = null;
	    TextView title = null;
	    TextView subtitle = null;
	    View itemView = mLayoutInflater.inflate(R.layout.channel_listitem_layout,mListView,false);
	    icon = (ImageView)itemView.findViewById(R.id.channel_listitem_img_id);
	    title = (TextView)itemView.findViewById(R.id.channel_listitem_title_id);
	    subtitle = (TextView)itemView.findViewById(R.id.channel_listitem_desc_id);
	    ImageDownloader imageDownloader = new ImageDownloader();
	    if(map.get("icon").toString() != null){
	        imageDownloader.download(map.get("icon").toString(),icon,R.drawable.firstnews,getApplicationContext());
	    }else{
	        imageDownloader.download(null,icon,R.drawable.firstnews,getApplicationContext());
	    }
	    if(map.get("title") != null){
	        title.setText(map.get("title").toString());
	    }
        if(map.get("desc") != null){
            subtitle.setText(map.get("desc").toString());
        }
        Log.i(TAG,"link = "+map.get("link"));
        itemView.setTag(map.get("link"));
        itemView.setOnClickListener(this);
        mListView.addView(itemView);
	    mListItemArray.add(itemView);
	    
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
							Log.i(TAG,"the title is : "+channlelist.get(i).getmTitle());
							Log.i(TAG,"the desc is : "+channlelist.get(i).getmDescription());
							Log.i(TAG,"the link is : "+channlelist.get(i).getmLink());
							Log.i(TAG,"the icon is : "+channlelist.get(i).getmIcon());
							map.put("title",channlelist.get(i).getmTitle());
							map.put("desc",channlelist.get(i).getmDescription());
							map.put("link",channlelist.get(i).getmLink());
							map.put("icon",channlelist.get(i).getmIcon());
							mBannerDataList.add(map); 
						}
						mSumBanner = mBannerDataList.size();
						//HashMap<String,Object> map = mBannerDataList.get(mBannerIndex);
						for(int i = 0;i<mBannerDataList.size();i++){
							View itemView = mBannerItemArray.get(i);
							map = mBannerDataList.get(i);
							TextView textview = (TextView)itemView.findViewById(R.id.channel_firstnews_title_id);
							ImageView image = (ImageView)itemView.findViewById(R.id.channel_firstnews_img_id);
							textview.setText(map.get("title").toString());
							ImageDownloader imageDownloader = new ImageDownloader();
							imageDownloader.download(map.get("icon").toString(),image,R.drawable.firstnews,getApplicationContext());
						}
						if(mBannerDataList.size() < 5){
						    for(int i = 4;i>=mBannerDataList.size();i--){
						        Log.i(TAG,"the remove i = "+i);
						        View itemView = mBannerItemArray.get(i);
						        mBannerFlipper.removeView(itemView);
						        mBannerItemArray.remove(i);
						    }
						}
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
			mListView.removeAllViews();
			HttpConnectionUtil connect = new HttpConnectionUtil(getApplicationContext());
			ConnectWeb(connect,mConnectUrl);
			mButton.setVisibility(View.GONE);
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
		if(v == mButton){
			mpageIndex++;
			setupData(mChannel_id);
			Log.i(TAG,"the page is : "+mpageIndex);
			mButton.setVisibility(View.GONE);
			mRequestType = REQUEST_TYPE_NEXT;
		}else{
		    String link = v.getTag().toString();
		    if(link != null){
		        Log.i(TAG,"connect the news information");
		        Intent myIntent = new Intent();
	            myIntent.setClass(ChannelActivityOne.this,NewsInformationActivity.class);
	            myIntent.putExtra("link",link);
	            startActivity(myIntent);
		    }
		    
		}
	}
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
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
	public boolean onDown(MotionEvent event) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
            float velocityY) {
		 Log.i("chenmei","onFling length ===== "+(e1.getX() - e2.getX()));
		 if (e1.getX() - e2.getX() > 120) { 
	            this.mBannerFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_in));
	            this.mBannerFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_out));
	            this.mBannerFlipper.showNext();
	            Log.i(TAG,"mBannerDataList.size() == "+mBannerDataList.size());
	            if(mBannerIndex == (mBannerDataList.size()-1)){
	            	mBannerIndex = 0;
	            }else{
	            	mBannerIndex++;
	            }
	            Log.i(TAG,"left mBannerIndex == "+mBannerIndex);
	            return true;
	        } else if (e1.getX() - e2.getX() < -120) {
	            this.mBannerFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_in));
	            this.mBannerFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_out));
	            this.mBannerFlipper.showPrevious();
	            if(mBannerIndex == 0){
	            	mBannerIndex = mBannerDataList.size()-1;
	            }else{
	            	mBannerIndex--;
	            }
	            Log.i(TAG,"right mBannerIndex == "+mBannerIndex);
	            return true;
	        }
	        
	        return false;
	}
	public boolean onFlingEx(float x1, float x2) {
         Log.i("chenmei","onFling length ===== "+(x1 - x2));
         if (x1 - x2 > 120) { 
                this.mBannerFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_in));
                this.mBannerFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_out));
                this.mBannerFlipper.showNext();
                Log.i(TAG,"mBannerDataList.size() == "+mBannerDataList.size());
                if(mBannerIndex == (mBannerDataList.size()-1)){
                    mBannerIndex = 0;
                }else{
                    mBannerIndex++;
                }
                Log.i(TAG,"left mBannerIndex == "+mBannerIndex);
                return true;
            } else if (x1 - x2 < -120) {
                this.mBannerFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_in));
                this.mBannerFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_out));
                this.mBannerFlipper.showPrevious();
                if(mBannerIndex == 0){
                    mBannerIndex = mBannerDataList.size()-1;
                }else{
                    mBannerIndex--;
                }
                Log.i(TAG,"right mBannerIndex == "+mBannerIndex);
                return true;
            }
            
            return false;
    }
	@Override
	public void onLongPress(MotionEvent event) {
	}
	@Override
	public boolean onScroll(MotionEvent arg0, MotionEvent event, float arg2,
			float arg3) {
		// TODO Auto-generated method stub
		return false;
	} 
	@Override
	public void onShowPress(MotionEvent event) {
	    
	}
	@Override
	public boolean onSingleTapUp(MotionEvent event) {
		Log.i(TAG,"mBannerIndex == === onClick "+mBannerIndex);
		if(mBannerDataList.size() > mBannerIndex){
		    HashMap<String,Object> map = mBannerDataList.get(mBannerIndex);
	        Intent myIntent = new Intent();
	        myIntent.setClass(ChannelActivityOne.this,NewsInformationActivity.class);
	        myIntent.putExtra("link",map.get("link").toString());
	        
	        startActivity(myIntent);
		} 
		return false;
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
    public boolean onTouch(View  v, MotionEvent event)
    {
       if(mBannerView == v){
           detector.onTouchEvent(event);
       }
        return true;
    }
	
}
