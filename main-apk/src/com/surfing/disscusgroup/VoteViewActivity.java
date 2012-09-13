package com.surfing.disscusgroup;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import com.surfing.R;
import com.surfing.channel.ActivityBase;
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
import com.surfing.rssparse.VoteItem;
import com.surfing.rssparse.VoteOptions;
import com.surfing.util.DisplayWeather;
import com.surfing.util.ReadConfigFile;
import com.surfing.util.TitleBarDisplay;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class VoteViewActivity extends ActivityBase implements OnClickListener{
	private final String TAG = "VoteViewActivity";
	TextView mVoteTitleView = null;
	TextView mVoteSumView = null;
	Button mVoteBtnView = null;
	ImageView mVoteImageView = null;
	Context mContext = null;
	VoteItem mVoteitem = null;
	ImageDownloader mImageDownLoad = null;
	private String mConnectUrl = null;
	private ProgressDialog mwaittingBar = null;
	private Handler    myHandler = new Handler(){

        @Override
        public void handleMessage(Message msg)
        {
            switch(msg.what){
                case CommonUpdate.EVENT_UPDATE_WEATHER:{
                    DisplayWeather.updateWeatherDisplay(getApplicationContext(),VoteViewActivity.this);
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
		super.onCreate(savedInstanceState,TITLE_TYPE_COMPANY,null,0,true);
		
		mContext = getApplicationContext();	
		setContentView(R.layout.voteview_layout);
		setupView();
		mConnectUrl = getIntent().getStringExtra("link");
		setupData();
		mImageDownLoad = new ImageDownloader();
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
		mVoteTitleView = (TextView)findViewById(R.id.vote_title_id);
		mVoteSumView = (TextView)findViewById(R.id.vote_summery_id);
		mVoteBtnView = (Button)findViewById(R.id.btn_vote_id);
		mVoteImageView = (ImageView)findViewById(R.id.vote_image_id);
		mVoteBtnView.setOnClickListener(this);
	}
	private void setupData(){
		mwaittingBar = ProgressDialog.show(VoteViewActivity.this,null,null);
		SharedPreferences userpref = this.getApplicationContext().getSharedPreferences("user",MODE_PRIVATE);
		String user_name = userpref.getString("user_name", "");
		mConnectUrl = ReadConfigFile.getServerAddress(mContext)+"index.php?controller=vote&action=requireid&user_name="+user_name+"&vote_id="+mConnectUrl;
		HttpConnectionUtil connect = new HttpConnectionUtil(getApplicationContext());
		ConnectWeb(connect,mConnectUrl);
	}
	private void ConnectWeb(HttpConnectionUtil connect,String geturl){
		Log.i(TAG,"connection url is : "+geturl);
		connect.asyncConnect(geturl, HttpMethod.GET, new HttpConnectionCallback(){
			@Override
			public void execute(String response) {
				mwaittingBar.dismiss();
				if((response == null) 
                        || (response.length() == 0) 
                        || response.equals(HttpConnectionUtil.CONNECT_FAILED)
                        || HttpConnectionUtil.RETURN_FAILED.equalsIgnoreCase(response))
                {
                    Log.i(TAG,"request failed");
                    return;
                }
				
				InputStream stream = new ByteArrayInputStream(response.getBytes());
				Log.i(TAG,"string"+response);
				mVoteitem = DomXMLReader.readXMLVote(mContext,stream);
				if(mVoteitem == null){
					return;
				}
				if(mVoteitem.getmIsVoted()){
					mVoteBtnView.setText(R.string.btn_result_text);
				}else{
					mVoteBtnView.setText(R.string.btn_vote_text);
				}
				mVoteTitleView.setText(mVoteitem.getmTitle());
				if(mVoteitem.getmSummary().length() == 0){
				    mVoteSumView.setVisibility(View.GONE);
				}else{
				    mVoteSumView.getBackground().setAlpha(100);
				    mVoteSumView.setText(mVoteitem.getmSummary());
				}
				
				mImageDownLoad.download(mVoteitem.getmIconLink(),mVoteImageView,R.drawable.vote_default_bg,mContext);
			}
			
		});
	}
	private void ConnectWebResult(HttpConnectionUtil connect,String geturl,HashMap<String, String> map){
		Log.i(TAG,"connection url is : "+geturl);
		mwaittingBar = ProgressDialog.show(VoteViewActivity.this,null,null);
		connect.asyncConnect(geturl, map,HttpMethod.POST,new HttpConnectionCallback(){

			@Override
			public void execute(String response) {
				Log.i(TAG,"response === "+response);
				mwaittingBar.dismiss();
				if("success".equalsIgnoreCase(response)){
					AlertDialog.Builder builder = new AlertDialog.Builder(VoteViewActivity.this);
					builder.setTitle(R.string.message_title_indicate);
					builder.setMessage(R.string.msg_vote_success);
					builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int arg1) {
							VoteViewActivity.this.finish();
						}
					});
					builder.create().show();
				}else{
					AlertDialog.Builder builder = new AlertDialog.Builder(VoteViewActivity.this);
					builder.setTitle(R.string.message_title_indicate);
					builder.setMessage(R.string.msg_vote_failed);
					builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int arg1) {
							VoteViewActivity.this.finish();
						}
					});
					builder.create().show();
				}
			}
			
		});
	}
	private void CaculatorRate(){
		int sum = 0;
		int count = 0;
		if(mVoteitem.getmVoteOptions() == null){
			return;
		}
		for(int i = 0;i<mVoteitem.getmVoteOptions().size();i++){
			sum = sum + mVoteitem.getmVoteOptions().get(i).getCount();
			Log.i(TAG,"getCount() == "+mVoteitem.getmVoteOptions().get(i).getCount());
			Log.i(TAG,"sum == "+sum);
		}
		for(int j = 0;j<mVoteitem.getmVoteOptions().size();j++){
			count = mVoteitem.getmVoteOptions().get(j).getCount();
			if(sum != 0){
				mVoteitem.getmVoteOptions().get(j).setRate((count*100)/sum);
			}else{
				mVoteitem.getmVoteOptions().get(j).setRate(0);
			}
			Log.i(TAG,"the rate is "+mVoteitem.getmVoteOptions().get(j).getRate());
		}
	}
	@Override
	public void onClick(View v) {
		if(v == mVoteBtnView){
			final HashMap<String,String> map = new HashMap<String,String>();
			AlertDialog.Builder builder = new AlertDialog.Builder(VoteViewActivity.this);
			builder.setTitle(mVoteitem.getmTitle());
			if(mVoteitem.getmIsVoted()){
				CaculatorRate();
				ListAdapter list = new ListAdapter(mContext,mVoteitem.getmVoteOptions());
				builder.setAdapter(list,null);
				builder.setPositiveButton(R.string.ok,null);
			}else{
				String items[] = new String[mVoteitem.getmVoteOptions().size()];
				final boolean chectitem[] = new boolean[mVoteitem.getmVoteOptions().size()];
				for(int i = 0;i<mVoteitem.getmVoteOptions().size();i++){
					items[i] = mVoteitem.getmVoteOptions().get(i).getDesc();
					Log.i(TAG,"items[i] === "+items[i]);
				}
				if(mVoteitem.getmType().equals("0")){
					builder.setSingleChoiceItems(items,-1,new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface dialog, int which) {
							map.put("optionid",mVoteitem.getmVoteOptions().get(which).getId());
						}
						 
					});
					builder.setPositiveButton(R.string.ok,new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							String url =  ReadConfigFile.getServerAddress(mContext)+"index.php?controller=vote&action=count";
							map.put("voteid", mVoteitem.getmId());
							SharedPreferences userpref = mContext.getSharedPreferences("user",MODE_PRIVATE);
							String user_name = userpref.getString("user_name", "");
							map.put("username", user_name);
							Log.i(TAG,"map =="+map.toString());
							HttpConnectionUtil connect = new HttpConnectionUtil(getApplicationContext());
							ConnectWebResult(connect,url,map);
						}
					});
				}else{
					if((items == null) || (items.length == 0)){
						return;
					}
					builder.setMultiChoiceItems(items,chectitem,new DialogInterface.OnMultiChoiceClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which, boolean isChecked) {
							chectitem[which] = isChecked;
						}
					});
					builder.setPositiveButton(R.string.ok,new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							String idstr = null;
							
							for(int i = 0;i<chectitem.length;i++){
								if(chectitem[i]){
									if(idstr == null){
										idstr = mVoteitem.getmVoteOptions().get(i).getId();
									}else{
										idstr += ","+ mVoteitem.getmVoteOptions().get(i).getId();
									}
								}
								
							}
							String url =  ReadConfigFile.getServerAddress(mContext)+"index.php?controller=vote&action=count";
							map.put("optionid", idstr);
							Log.i(TAG,"select idstr == "+idstr);
							map.put("voteid", mVoteitem.getmId());
							SharedPreferences userpref = mContext.getSharedPreferences("user",MODE_PRIVATE);
							String user_name = userpref.getString("user_name", "");
							map.put("username", user_name);
							HttpConnectionUtil connect = new HttpConnectionUtil(getApplicationContext());
							ConnectWebResult(connect,url,map);
						} 
					});
				}
			}
			
			builder.create().show();
			
		}
	}
	private class ListAdapter extends BaseAdapter {
		  
		   private final LayoutInflater mInflater;
		   private final ArrayList<VoteOptions> mItems ;
		  
		   public ListAdapter(Context context,ArrayList<VoteOptions> list) {
		    super();
		    // ��ȡLayoutInflater
		    mInflater = (LayoutInflater)
		    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		    mItems = list;
		   }
		   @Override
		   public int getCount() {
		    return mItems.size();
		   }
		   @Override
		   public Object getItem(int position) {
		    return mItems.get(position);
		   }
		   @Override
		   public long getItemId(int position) {
		    return position;
		   }
		   @Override
		   public View getView(int position, View convertView, ViewGroup parent) {
			   VoteOptions item = (VoteOptions) getItem(position);

		    if (convertView == null) {
		     // add_list_item.xmlΪ���List������
		     convertView = mInflater.inflate(R.layout.voteview_item_layout, parent,
		       false);
		    }
		    TextView optionText = (TextView)convertView.findViewById(R.id.vote_option_text_id);
		    ProgressBar progessBar = (ProgressBar)convertView.findViewById(R.id.vote_count_pro_id);
		    TextView countText = (TextView)convertView.findViewById(R.id.vote_count_text_id);
		    optionText.setText(item.getDesc());
		    progessBar.setProgress(item.getRate());
		    countText.setText(item.getRate()+"%");
		    Log.i(TAG,"optionText is "+item.getDesc());
		    return convertView;
		   }
		}
}
