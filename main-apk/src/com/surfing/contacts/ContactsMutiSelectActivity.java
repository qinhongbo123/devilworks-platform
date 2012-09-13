package com.surfing.contacts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import com.surfing.R;
import com.surfing.channel.ActivityBase;
import com.surfing.channel.CloseReceiver;
import com.surfing.channel.CommonUpdate;
import com.surfing.channel.MenuTabActivity;
import com.surfing.channel.NewsInformationActivity;
import com.surfing.contacts.ContactsProviderMetaData.ContactsData;
import com.surfing.util.DisplayWeather;
import com.surfing.util.ReadConfigFile;
import com.surfing.util.ThemeUpdateUitl;
import com.surfing.util.TitleBarDisplay;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class ContactsMutiSelectActivity extends ActivityBase implements OnItemClickListener,OnClickListener{
    /** Called when the activity is first created. */
	private static final String TAG = "ContactsMutiSelectActivity";
	private ListView mList = null;
	private Button mBtn_Ok = null;
	private Button mBtn_Mark = null;
	private Button mBtn_Cancel = null;
	private Context mContext;
	public static final String SMS_CONTACTS_ACTION = "com.surfing.contacts.sms.contacts";
	public static final String SMS_DEPARTMENT_ACTION = "com.surfing.contacts.sms.department";
	private static int MODE_CONTACTS = 1;
	private static int MODE_DEPARTMENT =2;
	private int mMode = 0;
	private SimpleAdapter mListAdapter = null;
	private HashSet<String> mSelectSet = null;
    private int mTheme = 0;
    private Context    mCurrentContext;
    private Handler    myHandler = new Handler(){

        @Override
        public void handleMessage(Message msg)
        {
            switch(msg.what){
                case CommonUpdate.EVENT_UPDATE_WEATHER:{
                    DisplayWeather.updateWeatherDisplay(getApplicationContext(),ContactsMutiSelectActivity.this);
                }
                break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState,TITLE_TYPE_COMPANY,null,0,true);
        mContext = getApplicationContext();
        mSelectSet = new HashSet<String>();
        Intent intent = getIntent();
        if(SMS_CONTACTS_ACTION.equals(intent.getAction())){
            mMode = MODE_CONTACTS;
        }if(SMS_DEPARTMENT_ACTION.equals(intent.getAction())){
            mMode = MODE_DEPARTMENT;
        }
        setContentView(R.layout.muti_select_layout);
       
        mTheme = ReadConfigFile.getTheme(getApplicationContext());
        setupView();
        mCurrentContext = ReadConfigFile.getCurrentThemeContext(mTheme,getApplicationContext()); 
       
        Log.i(TAG,"mMode == "+mMode);
        if(mMode == MODE_CONTACTS){
            mListAdapter = new SimpleAdapter(mContext,getContactsPhone(),R.layout.muti_select_item,new String[]{"name","phone"},new int[]{R.id.pick_contact_name,R.id.pick_contact_number});
        }
        if(mMode == MODE_DEPARTMENT){
            mListAdapter = new SimpleAdapter(mContext,getDepartment(),R.layout.muti_select_item,new String[]{"depart"},new int[]{R.id.pick_contact_name});
        }
        mList.setAdapter(mListAdapter);
        mList.setOnItemClickListener(this);
        mBtn_Ok.setOnClickListener(this);
       // mBtn_Mark.setOnClickListener(this);
        mBtn_Cancel.setOnClickListener(this);
        CommonUpdate.getInstance().registerForUpdateWeather(myHandler,CommonUpdate.EVENT_UPDATE_WEATHER,null);
        CloseReceiver.registerCloseActivity(this);
    }
    @Override
    protected void onDestroy()
    {
        CommonUpdate.getInstance().unregisterForUpdateWeather(myHandler);
        CloseReceiver.unRegisterActivity(this);
        super.onDestroy();
    }
    private ArrayList<HashMap<String,String>> getDepartment(){
        ArrayList<HashMap<String,String>> arraylist = new ArrayList<HashMap<String,String>>();
        HashMap<String,String> map = null;
        HashSet<String> departSet = new HashSet<String>();
        String company = TitleBarDisplay.getEnterpriseName(mContext);
        Cursor cursor = getContentResolver().query(ContactsData.CONTENT_URI,new String[]{ContactsProviderMetaData.CONTACTS_DEPART},ContactsProviderMetaData.CONTACTS_COMPANY +"='"+company+"'",null,null);
        if((cursor != null) && (cursor.getCount()>0)){
            cursor.moveToPosition(-1);
            for(int i = 0;i<cursor.getCount();i++){
                cursor.moveToNext();
                departSet.add(cursor.getString(0));
            }
            cursor.close();
        }
        
        String dePartName = null;
        for (Iterator iterator = departSet.iterator(); iterator.hasNext();)
        {
            dePartName = (String) iterator.next();
            Log.i(TAG,"the department is : "+dePartName);
            map = new HashMap<String,String>();
            map.put("depart",dePartName);
            map.put("select","");
            arraylist.add(map);
            
        }
        return arraylist;
    }
    private ArrayList<HashMap<String,String>> getContactsPhone(){
        ArrayList<HashMap<String,String>> arraylist = new ArrayList<HashMap<String,String>>();
        HashMap<String,String> map = null;
        String company = TitleBarDisplay.getEnterpriseName(mContext);
        Cursor cursor = getContentResolver().query(ContactsData.CONTENT_URI,
                new String[]{ContactsProviderMetaData.CONTACTS_NAME,ContactsProviderMetaData.CONTACTS_PHONE1,ContactsProviderMetaData.CONTACTS_PHONE2,ContactsProviderMetaData.CONTACTS_PHONE3},
                ContactsProviderMetaData.CONTACTS_COMPANY +"='"+company+"'",null,null);
        if((cursor != null) && (cursor.getCount()>0)){
            cursor.moveToPosition(-1);
            for(int i = 0;i<cursor.getCount();i++){
                cursor.moveToNext();
                if(cursor.getString(1) != null){
                    map = new HashMap<String,String>();
                    map.put("name",cursor.getString(0));
                    map.put("phone",cursor.getString(1));
                    arraylist.add(map);
                }
                if(cursor.getString(2) != null){
                    map = new HashMap<String,String>();
                    map.put("name",cursor.getString(0));
                    map.put("phone",cursor.getString(2));
                    arraylist.add(map);
                }
                if(cursor.getString(3) != null){
                    map = new HashMap<String,String>();
                    map.put("name",cursor.getString(0));
                    map.put("phone",cursor.getString(3));
                    arraylist.add(map);
                }
            }
            cursor.close();
        }
        return arraylist;
    }
    private void setupView(){
    	mList = (ListView)findViewById(R.id.muti_list_id);
    	mBtn_Ok = (Button)findViewById(R.id.btn_ok);
    	mBtn_Cancel = (Button)findViewById(R.id.btn_cancel);
    }
	@Override
	public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
	    HashMap<String,String> map = null;
	    CheckBox checkBox = (CheckBox)v.findViewById(R.id.pick_contact_check);
	    checkBox.setChecked(!checkBox.isChecked());
		if(mMode == MODE_DEPARTMENT){
		    map = (HashMap<String, String>)mListAdapter.getItem(position);
		    if(checkBox.isChecked()){
		        mSelectSet.add(map.get("depart").toString());
		    }else{
		        mSelectSet.remove(map.get("depart").toString());
		    }
		    
		}else if(mMode == MODE_CONTACTS){
		    map = (HashMap<String, String>)mListAdapter.getItem(position);
		    if(checkBox.isChecked()){
                mSelectSet.add(map.get("phone").toString());
            }else{
                mSelectSet.remove(map.get("phone").toString());
            }
		}
	}
	private void SelectAll(boolean blSelect){
	    if(mMode == MODE_DEPARTMENT){
	        if(blSelect){
	            //mListAdapter.getView(position, convertView, parent)
	        }
	    }else if(mMode == MODE_CONTACTS){
            
        }
	}
	@Override
	public void onClick(View v) {
		if(v == mBtn_Ok){
		    if(mMode == MODE_DEPARTMENT){
		        StringBuilder select = new StringBuilder();
    		    String dePartName = null;
    		    for (Iterator iterator = mSelectSet.iterator(); iterator.hasNext();)
    	        {
    	            dePartName = (String) iterator.next();
    	            Log.i(TAG,"select department is : "+dePartName);
    	            if(select.length() == 0){
    	                select.append(ContactsProviderMetaData.CONTACTS_DEPART+"='");
    	                select.append(dePartName);
    	                select.append("'");
    	            }else{ 
    	                select.append(" OR ");
    	                select.append(ContactsProviderMetaData.CONTACTS_DEPART+"='");
                        select.append(dePartName);
                        select.append("'");
    	            }
    	        }
    		    Log.i(TAG,"select : "+select.toString());
    		    Cursor cursor = getContentResolver().query(ContactsData.CONTENT_URI,
    		            new String[]{ContactsProviderMetaData.CONTACTS_PHONE1,ContactsProviderMetaData.CONTACTS_PHONE2,ContactsProviderMetaData.CONTACTS_PHONE3},
    		            select.toString(), 
    		            null, 
    		            null);
    		    StringBuilder phoneNum = new StringBuilder();
    		    String num = null;
    		    if((cursor != null) && (cursor.getCount()>0)){
    		        Log.i(TAG,"the contacts count = "+cursor.getCount());
    		        cursor.moveToPosition(-1);
    		        for(int i = 0;i<cursor.getCount();i++){
    		            cursor.moveToNext();
    		            num = cursor.getString(0);
    		            if((num != null) && (num.length() != 0)){
    		                phoneNum.append(num);
    		                phoneNum.append(",");
    		            }
    		            num = cursor.getString(1);
                        if((num != null) && (num.length() != 0)){
                            phoneNum.append(num);
                            phoneNum.append(",");
                        }
                        num = cursor.getString(2);
                        if((num != null) && (num.length() != 0)){
                            phoneNum.append(num);
                            phoneNum.append(",");
                        }
    		            
    		        }
    		        cursor.close();
    		    }
    		    if(phoneNum.length() != 0){
    		        phoneNum.deleteCharAt(phoneNum.length()-1);
    		    }
    		    Log.i(TAG,"send phone num : "+phoneNum.toString());
                Intent mIntent = new Intent(Intent.ACTION_VIEW);
                mIntent.putExtra("address", phoneNum.toString()); 
                mIntent.setType("vnd.android-dir/mms-sms");
                startActivity(mIntent);
		    }else if(mMode == MODE_CONTACTS){
		        StringBuilder select = new StringBuilder();
		        String phoneNum = null;
		        for (Iterator iterator = mSelectSet.iterator(); iterator.hasNext();){
		            phoneNum = (String) iterator.next();
		            select.append(phoneNum);
		            select.append(",");
		        }
		        if(select.length() != 0){
		            select.deleteCharAt(phoneNum.length()-1);
                }
		        Log.i(TAG,"send phone num : "+select.toString());
                Intent mIntent = new Intent(Intent.ACTION_VIEW);
                mIntent.putExtra("address", select.toString()); 
                mIntent.setType("vnd.android-dir/mms-sms");
                startActivity(mIntent);
		    }
		}else if(v == mBtn_Cancel){
		    
		   finish(); 
		}else if(v == mBtn_Mark){
		    
		}
	}
}