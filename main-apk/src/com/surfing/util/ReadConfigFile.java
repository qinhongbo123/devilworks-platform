package com.surfing.util;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.surfing.rssparse.ChannelInformation;
import com.surfing.rssparse.ChannelItem;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.util.Log;

public class ReadConfigFile {
	private static final String TAG = "ReadConfigFile";
	public static final int THEME_BLUE = 1;
	public static final int THEME_RED = 2;
	public static final int LOGIN_TYPE_NORMAL = 0;
	public static final int LOGIN_TYPE_GUEST = 1;
	public static int mLoginType = LOGIN_TYPE_NORMAL;
	
	public static String getServerAddress(Context context){
		String url = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try  
		{   
			Document dom = null;
			DocumentBuilder builder = factory.newDocumentBuilder();
			String configPath = Environment.getExternalStorageDirectory()
					+ "/DownFile/config.xml";
			File file =  new File(configPath);
			Log.e(TAG,"configPath == "+configPath);
			if(file.exists()){
				dom = builder.parse(file);
				Log.e(TAG,"read from sd card");
			}else{
				dom = builder.parse(context.getResources().getAssets().open("config.xml"));
				Log.e(TAG,"read form asset file");
			}
			
			Element root = dom.getDocumentElement();
 
			NodeList propertys = root.getElementsByTagName("serverinfo"); 
			NodeList channellist = root.getElementsByTagName("address");
			Element descNode = (Element)channellist.item(0);
			url = (String)descNode.getFirstChild().getNodeValue();
			
		}catch(Exception e) { 
			e.printStackTrace(); 
		}
		return url;
	}
	public static HashMap<String,String> getUserInfo(Context context){
		HashMap<String,String> map = new HashMap<String, String>();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Document dom = null;
		try{
			DocumentBuilder builder = factory.newDocumentBuilder();
			
			dom = builder.parse(context.getResources().getAssets().open("userinfo.xml"));
			Element root = dom.getDocumentElement();
			NodeList propertys = root.getElementsByTagName("userinfo"); 
			NodeList channellist = root.getElementsByTagName("name");
			Element descNode = (Element)channellist.item(0);
			map.put("user_name",(String)descNode.getFirstChild().getNodeValue());
			
			channellist = root.getElementsByTagName("passward");
			descNode = (Element)channellist.item(0);
			map.put("user_passwad",(String)descNode.getFirstChild().getNodeValue());
			
		}catch(Exception e) { 
			e.printStackTrace(); 
			return null;
		}
		
		mLoginType = LOGIN_TYPE_GUEST;
		Log.i(TAG,"user info = "+map.toString());
		return  map;
	}
	public static int getTheme(Context context){
	    SharedPreferences pref = context.getSharedPreferences("theme",Context.MODE_PRIVATE);
	    if("blue".equalsIgnoreCase(pref.getString("theme", "red"))){
	        return 1;
	    }else{
	        return 2;
	    }
	    
	}
	public static Context getCurrentThemeContext(int theme,Context context){
        Context mBlueThemeContext = null;
        if(theme == ReadConfigFile.THEME_BLUE){
            try{ 
                mBlueThemeContext = context.createPackageContext("com.theme.blue",Context.CONTEXT_IGNORE_SECURITY);
            }catch (Exception e) {
               e.printStackTrace();
            }
        } 
        return mBlueThemeContext;
    }
  public static int getMode(Context context){
      SharedPreferences prefMode = context.getSharedPreferences("mode",Context.MODE_PRIVATE);
      int mMode = prefMode.getInt("mode",0);
      return mMode;
  }
  public static void setSharedPreference(Context context,String segment,String title,String value){
	  SharedPreferences pref = context.getSharedPreferences(segment,
              Context.MODE_PRIVATE);
      Editor editor = pref.edit();
      editor.putString(title,value);
      editor.commit();
  }
  
  public static void setSharedPreference(Context context,String segment,String title,boolean value){
	  SharedPreferences pref = context.getSharedPreferences(segment,
              Context.MODE_PRIVATE);
      Editor editor = pref.edit();
      editor.putBoolean(title,value);
      editor.commit();
  }
  public static String getSharedPreferenceString(Context context,String segment,String title){
	  SharedPreferences pref = context.getSharedPreferences(segment,
              Context.MODE_PRIVATE);
	  return pref.getString(title, "");
  }
  public static boolean getSharedPreferenceBool(Context context,String segment,String title){
	  SharedPreferences pref = context.getSharedPreferences(segment,
              Context.MODE_PRIVATE);
	  return pref.getBoolean(title, false);
  }
}
