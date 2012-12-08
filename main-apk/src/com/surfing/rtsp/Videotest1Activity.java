package com.surfing.rtsp;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.VideoView;
import com.surfing.R;

public class Videotest1Activity extends Activity {
    /** Called when the activity is first created. */
    VideoView videoView1;
    String rtspUrlZhonglu   = "rtsp://117.35.58.70:2554/service?PuId-ChannelNo=27000000000000000011200027400000-1&PlayMethod=0&StreamingType=2";
    String rtspUrlWenjinglu = "rtsp://117.35.58.70:2554/service?PuId-ChannelNo=27000000000000000011200020100000-1&PlayMethod=0&StreamingType=2";
    String rtspUrlTaihualu  = "rtsp://117.35.58.70:2554/service?PuId-ChannelNo=27000000000000000011200042400000-1&PlayMethod=0&StreamingType=2";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_rtsp_layout);
        
        Button zhongluBt=(Button)findViewById(R.id.button1);
        Button wenjingluBt=(Button)findViewById(R.id.button2);
        Button taihualuBt=(Button)findViewById(R.id.button3);
        videoView1 = (VideoView)findViewById(R.id.videoView1);
        
        zhongluBt.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
			    videoView1.setVideoURI(Uri.parse(rtspUrlZhonglu));
		        videoView1.requestFocus();
		        videoView1.start();
		     			
			}
		});
        
        wenjingluBt.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
			    videoView1.setVideoURI(Uri.parse(rtspUrlWenjinglu));
		        videoView1.requestFocus();
		        videoView1.start();
				
			}
		});
        taihualuBt.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
			    videoView1.setVideoURI(Uri.parse(rtspUrlTaihualu));
		        videoView1.requestFocus();
		        videoView1.start();
				
			}
		});
               
    }
}