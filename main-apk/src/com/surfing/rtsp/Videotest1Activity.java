package com.surfing.rtsp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.VideoView;
import com.surfing.R;
import com.surfing.channel.ActivityBase;

public class Videotest1Activity extends ActivityBase {
    /** Called when the activity is first created. */
    VideoView videoView1;
    ListView mVideoList = null;
    SimpleAdapter mVideoAdapter = null;
    ArrayList<HashMap<String, Object>> mVideoArray = new ArrayList<HashMap<String, Object>>();
    Context mContext = null;
    ProgressDialog  mWaitDialog = null;
    String rtspUrlZhonglu   = "rtsp://117.35.58.70:2554/service?PuId-ChannelNo=27000000000000000011200027400000-1&PlayMethod=0&StreamingType=2";
    String rtspUrlWenjinglu = "rtsp://117.35.58.70:2554/service?PuId-ChannelNo=27000000000000000011200020100000-1&PlayMethod=0&StreamingType=2";
    String rtspUrlTaihualu  = "rtsp://117.35.58.70:2554/service?PuId-ChannelNo=27000000000000000011200042400000-1&PlayMethod=0&StreamingType=2";

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState, TITLE_TYPE_COMPANY, null, 0, true);
        setContentView(R.layout.video_rtsp_layout);
        
        mContext = this.getApplicationContext();
        mVideoList = (ListView)findViewById(R.id.video_list);
        videoView1 = (VideoView)findViewById(R.id.videoView1);

        //init data
        getVideoList();

        mVideoAdapter = new SimpleAdapter(mContext, mVideoArray,
                R.layout.videolist_item_layout,
                new String[]{"road"},
                new int[]{R.id.video_item_text});
        mVideoList.setAdapter(mVideoAdapter);
        
        mVideoList.setOnItemClickListener(new OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long id)
            {
                HashMap<Object, String> map = mVideoArray.get(position);
                String url = map.get("link");
                try{
                    videoView1.setVideoURI(Uri.parse(url));
                    boolean blFocus = videoView1.requestFocus();
                    videoView1.start();
                    Log.i("Chenmei----","blFocus = "+blFocus);
                    mWaitDialog = null;
                    mWaitDialog = new ProgressDialog(Videotest1Activity.this);
                    mWaitDialog.show();
                }catch (Exception e) {
                    // TODO: handle exception
                }
                
            }
        });
      
        videoView1.setOnPreparedListener(new OnPreparedListener()
        {
            
            @Override
            public void onPrepared(MediaPlayer arg0)
            {
                mWaitDialog.dismiss();
                
            }
        });
 
    }
    
    private void getVideoList(){
        HashMap<Object, String> map = null;
        
        //wenjing road
        map = new HashMap<Object, String>();
        map.put("road","文景路");
        map.put("link", rtspUrlWenjinglu);
        mVideoArray.add(map);
        
        //zhonglou tower
        map = new HashMap<Object, String>();
        map.put("road","钟楼");
        map.put("link", rtspUrlZhonglu);
        mVideoArray.add(map);
        
        //taihua road
      //zhonglou tower
        map = new HashMap<Object, String>();
        map.put("road","太华路");
        map.put("link", rtspUrlTaihualu);
        mVideoArray.add(map);
    }
}