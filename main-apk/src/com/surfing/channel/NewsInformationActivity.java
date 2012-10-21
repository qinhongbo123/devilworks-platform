package com.surfing.channel;

import java.util.HashMap;

import com.surfing.R;
import com.surfing.download.DownloaderActivity;
import com.surfing.httpconnection.HttpConnectionCallback;
import com.surfing.httpconnection.HttpConnectionUtil;
import com.surfing.httpconnection.HttpConnectionUtil.HttpMethod;
import com.surfing.httpconnection.ImageDownloader;
import com.surfing.util.DisplayWeather;
import com.surfing.util.ReadConfigFile;
import com.surfing.util.ThemeUpdateUitl;
import com.surfing.util.TitleBarDisplay;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.webkit.DownloadListener;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebSettings.TextSize;
import android.webkit.WebSettings.ZoomDensity;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class NewsInformationActivity extends ActivityBase  implements OnTouchListener {
	private static final String TAG = "NewsInformationActivity";
	private WebView mHtmlView = null;
	private ProgressBar mLoadingBar = null;
	private Handler mHandler = new Handler();
	private Window mWindow;
	private ImageView mTitleIcon = null;
	private TextView mTitleText = null;
	private final  String CONFIG_VERSION = "1.0";
	private final static String DATA_PATH = "/sdcard/data/downloadTest";
	private final static String USER_AGENT = "MyApp Downloader";
	private Context mContext = null;
	private ProgressBar mwaittingBar = null;
	private Handler    myHandler = new Handler(){

        @Override
        public void handleMessage(Message msg)
        {
            switch(msg.what){
                case CommonUpdate.EVENT_UPDATE_WEATHER:{
                    DisplayWeather.updateWeatherDisplay(getApplicationContext(),NewsInformationActivity.this);
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
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		mContext = getApplicationContext();
		setContentView(R.layout.newsinformation_layout);
		
		setupView();
        TitleBarDisplay.TitleBarInit(mTitleText, mTitleIcon,NewsInformationActivity.this,mContext); 
		mHtmlView.getSettings().setJavaScriptEnabled(true);
		//load image when text was loaded. 
		mHtmlView.getSettings().setBlockNetworkImage(true);
		mHtmlView.setWebViewClient(new WebViewClient(){       
            public boolean shouldOverrideUrlLoading(WebView view, String url) {    
            	Log.i(TAG,"url is : "+url);
                view.loadUrl(url);       
                return true;       
            }

            @Override
            public void onPageFinished(WebView view, String url)
            {
            	Log.i(TAG,"=======onPageFinished");
                mwaittingBar.setVisibility(View.GONE);
                view.getSettings().setBlockNetworkImage(false);
                super.onPageFinished(view, url);
            }   
            
             
		});   
		mHtmlView.setWebChromeClient(new NewswebView());
		//mHtmlView.setWebViewClient(new WebViewClientListener());
		String url = getIntent().getStringExtra("link");
		mHtmlView.loadUrl(url);
		//String ServerUrl = ReadConfigFile.getServerAddress(this.getApplicationContext());
		//mHtmlView.loadUrl(ServerUrl+"45.html");
		mHtmlView.setOnTouchListener(this);
		
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int mDensity = metrics.densityDpi;
		Log.i(TAG,"mDensity == "+mDensity);
		if (mDensity == 240) { 
			mHtmlView.getSettings().setDefaultZoom(ZoomDensity.FAR);
			mHtmlView.getSettings().setTextSize(TextSize.LARGEST);
		} else if (mDensity == 160) {
	    	mHtmlView.getSettings().setDefaultZoom(ZoomDensity.MEDIUM);
	    	mHtmlView.getSettings().setTextSize(TextSize.LARGER);
	    } else if(mDensity == 120) {
	    	mHtmlView.getSettings().setDefaultZoom(ZoomDensity.CLOSE);
	    	mHtmlView.getSettings().setTextSize(TextSize.NORMAL);
	    }
		mHtmlView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		//mHtmlView.setAnimation(animation)
		//mHtmlView.addJavascriptInterface(obj, interfaceName)
		mHtmlView.setDownloadListener(new DownloadListener() {
            public void onDownloadStart(String url, String userAgent,
                            String contentDisposition, String mimetype,
                            long contentLength) {
            		final String CurrentURL = url;
            		final String CurrentType = mimetype;
            		Log.i(TAG,"the URL is : "+url);
            		Log.i(TAG,"the mimetype is : "+mimetype);
            		//if("audio/mp3,audio/x-mp3,audio/mpeg,audio/mp4,audio/mp4a-latm,audio/ogg".contains(mimetype)){
            			new AlertDialog.Builder(NewsInformationActivity.this) // build AlertDialog

            			.setTitle(R.string.choose_play_method) // title

            			.setItems(new String[]{getString(R.string.play_current),getString(R.string.download)}, new DialogInterface.OnClickListener() { //content

            			public void onClick(DialogInterface dialog, int position) {
            				if(position == 1){
            					//down load
            					 Intent doloadIntent = new Intent();
            					 doloadIntent.setClass(NewsInformationActivity.this,DownloaderActivity.class);
            					 doloadIntent.putExtra("path",CurrentURL);
            					 startActivity(doloadIntent);
            				}else{
            					try{
                        			Intent viewIntent = new Intent(Intent.ACTION_VIEW);
                                    viewIntent.setDataAndType(Uri.parse(CurrentURL), CurrentType);
                                    startActivity(viewIntent);
                        		}catch(ActivityNotFoundException e){
                        			  AlertDialog.Builder builder = new Builder(NewsInformationActivity.this); 
                        		        builder.setMessage(R.string.unsupport_type); 
                        		        builder.setTitle(R.string.message_title_indicate); 
                        		        builder.setPositiveButton(R.string.ok, 
                        		                new android.content.DialogInterface.OnClickListener() { 
                        							@Override
                        							public void onClick(DialogInterface dialog, int arg1) {
                        								 dialog.dismiss(); 
                        							} 
                        		                }); 
                        		        builder.show();
                        		}
            				}
            			}
            			})
            			.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
            			public void onClick(DialogInterface dialog, int which) {
            				dialog.dismiss(); //�ر�alertDialog
            				}
            			}).show();
            		//}
            		
            		
            }
    });
		NewsStatRequest(url);
		mwaittingBar.setVisibility(View.VISIBLE);
		CloseReceiver.registerCloseActivity(this);
		CommonUpdate.getInstance().registerForUpdateWeather(myHandler,CommonUpdate.EVENT_UPDATE_WEATHER,null);
	}
	private void NewsStatRequest(String url){
		HttpConnectionUtil connection = new HttpConnectionUtil(mContext);
		HashMap<String,String> map = new HashMap<String, String>();
		//map.put("action", "addarticleaccesscount");
		//map.put("article_url", url);
		String link = ReadConfigFile.getServerAddress(mContext)+"index.php?controller=article&action=addarticleaccesscount&article_url="+url;
		Log.i(TAG,"stat link = "+link);
		//Log.i(TAG,"param = "+map);
		connection.asyncConnect(link,map,HttpMethod.POST,new HttpConnectionCallback() {
			
			@Override
			public void execute(String response) {
				Log.i(TAG,"reponse = "+response);
			}
		});
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
		mHtmlView = (WebView)findViewById(R.id.news_webview_id);
		mLoadingBar = (ProgressBar)findViewById(R.id.process_view_id);
		mTitleIcon = (ImageView)findViewById(R.id.titlebar_icon_id);
		mTitleText = (TextView)findViewById(R.id.titlebar_text_id);
		mwaittingBar = (ProgressBar)findViewById(R.id.loading_progressBar);
		//View titleView = (View)findViewById(R.id.application_title_id);
		//titleView.setVisibility(View.VISIBLE);
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && mHtmlView.canGoBack()) {       
			mHtmlView.goBack();       
            return true;       
        }       
        return super.onKeyDown(keyCode, event);   
	}
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		return false;
	}
	public class NewswebView extends WebChromeClient{
        
        
        @Override
        public void onProgressChanged(WebView view, int newProgress)
        {
            // TODO Auto-generated method stub
            super.onProgressChanged(view, newProgress);
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message,
                JsResult result) {
            Log.i(TAG,"onJsAlert(WebView view, String url, String message,JsResult result)");
            return super.onJsAlert(view, url, message, result);
        }

        @Override
        public boolean onJsBeforeUnload(WebView view, String url,
                String message, JsResult result) {
            // TODO Auto-generated method stub
            Log.i(TAG,"onJsBeforeUnload");
            return super.onJsBeforeUnload(view, url, message, result);
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message,
                JsResult result) {
            // TODO Auto-generated method stub
            Log.i(TAG,"onJsConfirm");
            return super.onJsConfirm(view, url, message, result);
        }

        @Override
        public boolean onJsPrompt(WebView view, String url, String message,
                String defaultValue, JsPromptResult result) {
            // TODO Auto-generated method stub
            Log.i(TAG,"onJsPrompt");
            return super.onJsPrompt(view, url, message, defaultValue, result);
        }

        @Override
        public void onReceivedTouchIconUrl(WebView view, String url,
                boolean precomposed) {
            // TODO Auto-generated method stub
            Log.i(TAG,"onJsPrompt url = "+url);
            super.onReceivedTouchIconUrl(view, url, precomposed);
        }
        
        
    }
}
