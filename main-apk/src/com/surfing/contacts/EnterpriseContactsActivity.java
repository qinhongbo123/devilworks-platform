package com.surfing.contacts;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.zip.Inflater;

import com.surfing.R;
import com.surfing.Notification.NotificationListActivity;
import com.surfing.Notification.NotificationProviderMetaData;
import com.surfing.Notification.NotificationProviderMetaData.UserTableMetaData;
import com.surfing.channel.ActivityBase;
import com.surfing.channel.CloseReceiver;
import com.surfing.channel.CommonUpdate;
import com.surfing.channel.MenuTabActivity;
import com.surfing.contacts.ContactsProviderMetaData.ContactsData;
import com.surfing.httpconnection.HttpConnectionCallback;
import com.surfing.httpconnection.HttpConnectionUtil;
import com.surfing.httpconnection.ImageDownloader;
import com.surfing.httpconnection.HttpConnectionUtil.HttpMethod;
import com.surfing.rssparse.ChannelInformation;
import com.surfing.rssparse.ChannelItem;
import com.surfing.rssparse.ContactsInfo;
import com.surfing.rssparse.DomXMLReader;
import com.surfing.util.DisplayWeather;
import com.surfing.util.ReadConfigFile;
import com.surfing.util.ThemeUpdateUitl;
import com.surfing.util.TitleBarDisplay;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.AsyncQueryHandler;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewDebug.FlagToString;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

public class EnterpriseContactsActivity extends ActivityBase implements OnClickListener,OnItemClickListener{
	private static final String TAG = "EnterpriseContactsActivity";
	static final int QUERY_CONTACTS_ID = 0;
    static final int QUERY_CONTACTS_NAME = 1;
    static final int QUERY_CONTACTS_PHONE1 = 2;
    static final int QUERY_CONTACTS_PHONE2 = 3;
    static final int QUERY_CONTACTS_PHONE3 = 4;
    static final int QUERY_CONTACTS_DEPART = 5;
    static final int QUERY_CONTACTS_TITLE = 6;
	private ContactAdapter mAdapter = null;
	private ContactHandler mHandler = null;
	private ListView	mList = null;
	private Context 	mContext = null;
	private ProgressBar mwaittingBar = null;
	private AutoCompleteTextView mSeartText = null;
	private Spinner mSpinner = null;
	private ArrayAdapter< String> mSpinnerAdapter = null;
	private Context    mCurrentContext;
    private int mTheme = 0;
    static final int MENU_ITEM_CALL = 1;
    static final int MENU_ITEM_SEND_SMS = 2;
    public String mDepartment = null;
    public String mSearchName = null;
    private View mIndicateView = null;
    private Button mSynBtn = null;
    private int mMode = 0;
    private boolean mBlSycQuery = false;
    private int mSelectNumId = 0;
    private Handler    myHandler = new Handler(){

        @Override
        public void handleMessage(Message msg)
        {
            switch(msg.what){
                case CommonUpdate.EVENT_UPDATE_WEATHER:{
                    if(ReadConfigFile.getTheme(mContext) == 1){
                        DisplayWeather.updateWeatherDisplay(getApplicationContext(),EnterpriseContactsActivity.this);
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
		// TODO Auto-generated method stub
		mContext = getApplicationContext();
		SharedPreferences prefMode = mContext.getSharedPreferences("mode",MODE_PRIVATE);
		int mMode = prefMode.getInt("mode",0);
		if(mMode == 1){	
			super.onCreate(savedInstanceState,TITLE_TYPE_COMPANY,null,0,true);
		}else{
			super.onCreate(savedInstanceState);
			
		}
		setContentView(R.layout.enterprise_contacts_layout);
		setupView();
		Log.i(TAG,"create the Conacts list");
		mTheme = ReadConfigFile.getTheme(getApplicationContext());
        mCurrentContext = ReadConfigFile.getCurrentThemeContext(mTheme,getApplicationContext()); 
		
		mSynBtn.setOnClickListener(this);
		mHandler = new ContactHandler(getContentResolver());
		mAdapter = new ContactAdapter(mContext,null);
		mList.setAdapter(mAdapter);
		mList.setOnItemClickListener(this);
		//mList.setOnCreateContextMenuListener(this);
		
		setDepartment();
        mSeartText.addTextChangedListener(new TextChangedListener());
        
        mSeartText.setOnItemClickListener(new OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position,
                    long id)
            {
                Adapter adapterSearch = mSeartText.getAdapter();
                mSearchName = (String)adapterSearch.getItem(position);
                mHandler.startQuery(42,null,ContactsData.CONTENT_URI,getProjection(),getSelection(),getSeletionArgs(),null);
            }
        });
        Cursor cursor = getContentResolver().query(ContactsData.CONTENT_URI,getProjection(),getSelection(),getSeletionArgs(),null);
        if(cursor != null){
            updateDisplay(cursor.getCount());
            cursor.close();
            
        }else{
            updateDisplay(0);
        }
		CloseReceiver.registerCloseActivity(this);
		CommonUpdate.getInstance().registerForUpdateWeather(myHandler,CommonUpdate.EVENT_UPDATE_WEATHER,null);
	}
	private void setDepartment(){
		ArrayList<String> departList = getDepartment();
        String ItemList[] =  (String[])departList.toArray(new String[departList.size()]);
        mSpinnerAdapter = new ArrayAdapter<String>(this,R.layout.spinner_item ,ItemList) ;
        mSpinner.setAdapter(mSpinnerAdapter);
        mSpinnerAdapter.setDropDownViewResource(R.layout.spinner_item_list);
        mSpinner.setOnItemSelectedListener(new SpinnerListener());
        if(!mSpinnerAdapter.isEmpty()){
            mSpinner.setSelection(0);
        }
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
	private ArrayList<String> getDepartment(){
        ArrayList<String> arraylist = new ArrayList<String>();
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
        //departSet.add(getString(R.string.all_department));
        arraylist.add(getString(R.string.all_department));
        String dePartName = null;
        for (Iterator iterator = departSet.iterator(); iterator.hasNext();)
        {
            dePartName = (String)iterator.next();
            arraylist.add(dePartName);
            
        }
        return arraylist;
    }
	private void createContactsDialog(final Cursor cursor){
	    String name = cursor.getString(1);
	    String items[] = null;//{getString(R.string.menu_call),getString(R.string.menu_sendSMS)};
	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setTitle(name);
	    //fill the item
	    final ArrayList<String> phonelist = new ArrayList<String>();
        if((cursor.getString(2) != null) && (cursor.getString(2).length() != 0)){
            phonelist.add(cursor.getString(2));
        }
        if((cursor.getString(3) != null) && (cursor.getString(3).length() != 0)){
            phonelist.add(cursor.getString(3));
        }
        if((cursor.getString(4) != null) && (cursor.getString(4).length() != 0)){
            phonelist.add(cursor.getString(4));
        }
        int phonenum = phonelist.size();
        mSelectNumId = 0;
        items = new String[phonenum];
        for(int i = 0;i<phonenum;i++){
            items[i] = phonelist.get(i);
        }
        if(phonenum == 0){
            builder.setMessage("没有号码");
            builder.setNegativeButton(R.string.btn_cancel,null);
        }else{
            builder.setSingleChoiceItems(items,0,new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface arg0, int position)
                {
                    mSelectNumId = position;
                }
            });
            builder.setNegativeButton(getString(R.string.menu_sendSMS),new DialogInterface.OnClickListener()
            {
                
                @Override
                public void onClick(DialogInterface dialog, int arg1)
                {
                    String num = phonelist.get(mSelectNumId);
                    Intent mIntent = new Intent(Intent.ACTION_VIEW);
                    mIntent.putExtra("address",num); 
                    mIntent.setType("vnd.android-dir/mms-sms");
                    startActivity(mIntent);
                    dialog.dismiss();
                }
            });
            builder.setPositiveButton(R.string.menu_call,new DialogInterface.OnClickListener()
            {
                
                @Override
                public void onClick(DialogInterface dialog, int arg1)
                {
                    
                    String num = phonelist.get(mSelectNumId);
                    Intent myIntent = new Intent("android.intent.action.CALL",Uri.parse("tel:"+num));
                    startActivity(myIntent);
                    dialog.dismiss();
                }
            });
        }
	   
	    
	    builder.show();
	}
	private void ContactDialogSelect(Cursor cursor,int id){
	    final ArrayList<String> phonelist = new ArrayList<String>();
        if((cursor.getString(2) != null) && (cursor.getString(2).length() != 0)){
            phonelist.add(cursor.getString(2));
        }
        if((cursor.getString(3) != null) && (cursor.getString(3).length() != 0)){
            phonelist.add(cursor.getString(3));
        }
        if((cursor.getString(4) != null) && (cursor.getString(4).length() != 0)){
            phonelist.add(cursor.getString(4));
        }
        if(phonelist.size() > 1){
            
            AlertDialog.Builder builder = new AlertDialog.Builder(EnterpriseContactsActivity.this);
            builder.setTitle("选择号码");
            if(id == MENU_ITEM_CALL){
                String ItemList[] =  (String[])phonelist.toArray(new String[phonelist.size()]);
                builder.setSingleChoiceItems(ItemList,-1,new DialogInterface.OnClickListener()
                {
                    
                    @Override
                    public void onClick(DialogInterface dialog, int position)
                    {
                         Intent myIntent = new Intent("android.intent.action.CALL",Uri.parse("tel:"+phonelist.get(position)));
                         startActivity(myIntent);
                         dialog.dismiss();
                         
                    }
                });
            }else{
                final boolean checkedItems[] = new boolean[phonelist.size()];
                String ItemList[] =  (String[])phonelist.toArray(new String[phonelist.size()]);
                builder.setMultiChoiceItems(ItemList, checkedItems, new DialogInterface.OnMultiChoiceClickListener()
                {
                    
                    @Override
                    public void onClick(DialogInterface dialog, int position, boolean ischeck)
                    {
                        checkedItems[position] = ischeck;
                        
                    }
                });
                builder.setPositiveButton(R.string.ok,new DialogInterface.OnClickListener()
                {
                    
                    @Override
                    public void onClick(DialogInterface arg0, int arg1)
                    {
                        StringBuilder phoneNum = new StringBuilder();
                        for(int i = 0;i<phonelist.size();i++){
                            if(checkedItems[i]){
                                phoneNum.append(phonelist.get(i));
                                phoneNum.append(",");
                            }
                        }
                        if(phoneNum.length() > 1){
                            phoneNum.deleteCharAt(phoneNum.length()-1);
                            Log.i(TAG,"send phone num : "+phoneNum.toString());
                            Intent mIntent = new Intent(Intent.ACTION_VIEW);
                            mIntent.putExtra("address", phoneNum.toString()); 
                            mIntent.setType("vnd.android-dir/mms-sms");
                            startActivity(mIntent);
                        }
                        
                    }
                });
                builder.setNegativeButton(R.string.btn_cancel,null);
            }
            builder.create();
            builder.show();
            
        }else if(phonelist.size() == 1){
            String number = phonelist.get(0);
            if(id == MENU_ITEM_CALL){
                Intent myIntent = new Intent("android.intent.action.CALL",Uri.parse("tel:"+number));
                startActivity(myIntent);  
            }else{
                Intent mIntent = new Intent(Intent.ACTION_VIEW);
                mIntent.putExtra("address",number); 
                mIntent.setType("vnd.android-dir/mms-sms");
                startActivity(mIntent);
            }
        }
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
        SharedPreferences prefMode = mContext.getSharedPreferences("mode",MODE_PRIVATE);
        int mMode = prefMode.getInt("mode",0);
        if(mMode == 1){
            DisplayWeather.updateWeatherDisplay(getApplicationContext(),this);
        }
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, menu.FIRST, Menu.NONE, getString(R.string.synchronous));
		menu.add(0, Menu.FIRST+1, 0, R.string.send_sms_muti);
		menu.add(0,menu.FIRST+2,0,R.string.menu_into_local);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case Menu.FIRST:{ //synchronous
			mwaittingBar.setVisibility(View.VISIBLE);
			HttpConnectionUtil connect = new HttpConnectionUtil(getApplicationContext());
			ConnectWeb(connect,getUrl());
//		    ArrayList<ContactsInfo> contactsList = null;
//		    InputStream stream = null;
//		    ContentResolver resolver = getContentResolver();
//		    try
//            {
//                stream = mContext.getResources().getAssets().open("tel.xml");
//            }
//            catch (IOException e)
//            {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//		    contactsList = DomXMLReader.readXMLContact(mContext,stream);
//		    if(contactsList !=  null){
//		        ArrayList<ContentProviderOperation> operationList  = new ArrayList<ContentProviderOperation>();
//	            ContentProviderOperation.Builder builder = null;
//	            ContentValues values = null;
//	            String enterprise = TitleBarDisplay.getEnterpriseName(mContext);
//	            SharedPreferences userpref = mContext.getSharedPreferences("user",MODE_PRIVATE);
//	            String user_name = userpref.getString("user_name", "");
//	            for(int i = 0;i<contactsList.size();i++){
//	                builder = ContentProviderOperation.newInsert(ContactsData.CONTENT_URI); 
//	                values = new ContentValues();
//	                values.put(ContactsProviderMetaData.CONTACTS_USER,user_name);
//	                values.put(ContactsProviderMetaData.CONTACTS_NAME,contactsList.get(i).getmName());
//	                values.put(ContactsProviderMetaData.CONTACTS_PHONE1,contactsList.get(i).getmPhone1());
//	                values.put(ContactsProviderMetaData.CONTACTS_PHONE2,contactsList.get(i).getmPhone2());
//	                values.put(ContactsProviderMetaData.CONTACTS_PHONE3,contactsList.get(i).getmPhone3());
//	                values.put(ContactsProviderMetaData.CONTACTS_DEPART,contactsList.get(i).getmDepartMentName());
//	                values.put(ContactsProviderMetaData.CONTACTS_COMPANY,enterprise);
//	                builder.withValues(values);
//	                operationList.add(builder.build());
//	            }
//	            resolver.delete(ContactsData.CONTENT_URI,null,null);
//	            try
//                {
//                    resolver.applyBatch(ContactsProviderMetaData.AUTHORIY,operationList);
//                }
//                catch (RemoteException e)
//                {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//                catch (OperationApplicationException e)
//                {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//	            mHandler.startQuery(42,null,ContactsData.CONTENT_URI,getProjection(),getSelection(),getSeletionArgs(),null);
//	            
//		    }
		    
		}
		break;
		case Menu.FIRST+1:{
            String sendType[] = {getString(R.string.send_type_department),getString(R.string.send_type_undefine)};
            boolean blSelect[] = null;
            blSelect = new boolean[sendType.length];
            new AlertDialog.Builder(EnterpriseContactsActivity.this)
               .setTitle(R.string.send_sms_muti)
               .setSingleChoiceItems(sendType,-1,new DialogInterface.OnClickListener() {
                   
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       Intent myIntent = new Intent();
                       if(which == 0){
                           myIntent.setAction(ContactsMutiSelectActivity.SMS_DEPARTMENT_ACTION);
                       }else if(which == 1){
                           myIntent.setAction(ContactsMutiSelectActivity.SMS_CONTACTS_ACTION);
                       }
                       myIntent.setClass(EnterpriseContactsActivity.this,ContactsMutiSelectActivity.class);
                       startActivity(myIntent);
                       dialog.dismiss();
                   }
               })
               .setOnCancelListener(null)
               .create()
               .show();
        }
        break;
		case Menu.FIRST+2:{
		    Intent myIntent = new Intent("com.android.contacts.action.LIST_CONTACTS");
		    startActivity(myIntent);
		}
		break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	private String getUrl(){
		SharedPreferences userpref = this.getApplicationContext().getSharedPreferences("user",MODE_PRIVATE);
		String user_name = userpref.getString("user_name", "");
		String url = ReadConfigFile.getServerAddress(mContext)+"index.php?controller=telephonebook&action=PhoneRequireTels&user_name="+user_name;
		return url;
	}
	public void ConnectWeb(HttpConnectionUtil connect,String geturl){
		Log.i(TAG,"the url is : "+ geturl);
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
			    ContentResolver resolver = getContentResolver();
				Log.i(TAG,"connect success");
				InputStream stream = new ByteArrayInputStream(response.getBytes());
				Log.i(TAG,"response string = "+response);
				//SaveRssFile savefile = new SaveRssFile(mContext);
				//savefile.SaveFile(response.substring(nindex),"news.xml");
				ArrayList<ContactsInfo> contactsList = null;
				contactsList = DomXMLReader.readXMLContact(mContext,stream);
	            if(contactsList !=  null){
	                ArrayList<ContentProviderOperation> operationList  = new ArrayList<ContentProviderOperation>();
	                ContentProviderOperation.Builder builder = null;
	                ContentValues values = null;
	                String enterprise = TitleBarDisplay.getEnterpriseName(mContext);
	                SharedPreferences userpref = mContext.getSharedPreferences("user",MODE_PRIVATE);
	                String user_name = userpref.getString("user_name", "");
	                for(int i = 0;i<contactsList.size();i++){
	                    builder = ContentProviderOperation.newInsert(ContactsData.CONTENT_URI); 
	                    values = new ContentValues();
	                    values.put(ContactsProviderMetaData.CONTACTS_USER,user_name);
	                    values.put(ContactsProviderMetaData.CONTACTS_NAME,contactsList.get(i).getmName());
	                    values.put(ContactsProviderMetaData.CONTACTS_PHONE1,contactsList.get(i).getmPhone1());
	                    values.put(ContactsProviderMetaData.CONTACTS_PHONE2,contactsList.get(i).getmPhone2());
	                    values.put(ContactsProviderMetaData.CONTACTS_PHONE3,contactsList.get(i).getmPhone3());
	                    values.put(ContactsProviderMetaData.CONTACTS_DEPART,contactsList.get(i).getmDepartMentName());
	                    values.put(ContactsProviderMetaData.CONTACTS_COMPANY,enterprise);
	                    values.put(ContactsProviderMetaData.CONTACTS_TITLE,contactsList.get(i).getmTitle());
	                    builder.withValues(values);
	                    operationList.add(builder.build());
	                }
	                resolver.delete(ContactsData.CONTENT_URI,null,null);
	                resolver.applyBatch(ContactsProviderMetaData.AUTHORIY,operationList);
	                mBlSycQuery = true;
	                mHandler.startQuery(42,null,ContactsData.CONTENT_URI,getProjection(),getSelection(),getSeletionArgs(),null);
	            }
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	});
}
	
	private String[] getProjection(){
		return new String[]{ContactsProviderMetaData.CONTACTS_ID,
							ContactsProviderMetaData.CONTACTS_NAME,
							ContactsProviderMetaData.CONTACTS_PHONE1,
							ContactsProviderMetaData.CONTACTS_PHONE2,
							ContactsProviderMetaData.CONTACTS_PHONE3,
							ContactsProviderMetaData.CONTACTS_DEPART,
							ContactsProviderMetaData.CONTACTS_TITLE};
	}
	private String getSelection(){
		SharedPreferences userpref = mContext.getSharedPreferences("user",MODE_PRIVATE);
		String user_name = userpref.getString("user_name", "");
		String selectString = ContactsProviderMetaData.CONTACTS_USER+"='"+user_name+"'";
		if(mDepartment != null){
		    selectString += " AND "
		            +ContactsProviderMetaData.CONTACTS_DEPART+"='"+mDepartment+"'";
		}
		if(mSearchName != null){
		    selectString += " AND "+ContactsProviderMetaData.CONTACTS_NAME+"='"+mSearchName+"'";
		}
		Log.i(TAG,"the selection is :"+selectString);
		return selectString;
	}
	private String[] getSeletionArgs(){
		return null;
	}
	private void setupView(){
		mList = (ListView)findViewById(R.id.contact_list_id);
		mwaittingBar = (ProgressBar)findViewById(R.id.loading_progressBar);
		mSpinner = (Spinner)findViewById(R.id.department_list_id);
		mSeartText = (AutoCompleteTextView)findViewById(R.id.search_text_id);
		mIndicateView = findViewById(R.id.promp_layout_id);
		mSynBtn = (Button)findViewById(R.id.btn_syn_id);
	}
	 private Cursor queryPhoneNumbers(long contactId) {
	        Uri baseUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
	        Uri dataUri = Uri.withAppendedPath(baseUri, Contacts.Data.CONTENT_DIRECTORY);

	        Cursor c = getContentResolver().query(dataUri,
	                new String[] {Phone._ID, Phone.NUMBER, Phone.IS_SUPER_PRIMARY,
	                        RawContacts.ACCOUNT_TYPE, Phone.TYPE, Phone.LABEL},
	                Data.MIMETYPE + "=?", new String[] {Phone.CONTENT_ITEM_TYPE}, null);
	        if (c != null) {
	            if (c.moveToFirst()) {
	                return c;
	            }
	            c.close();
	        }
	        return null;
	    }
	private class ContactAdapter extends CursorAdapter{

		public ContactAdapter(Context context, Cursor c) {
			super(context, c);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void bindView(View v, Context context, Cursor cursor) {
			
			TextView nameText = (TextView)v.findViewById(R.id.contact_item_name_id);
			TextView numText = (TextView)v.findViewById(R.id.contact_item_num_id);
			TextView depText = (TextView)v.findViewById(R.id.contact_item_dep_id);
			nameText.setText(cursor.getString(QUERY_CONTACTS_NAME));
			numText.setText(cursor.getString(QUERY_CONTACTS_TITLE));
			depText.setText(cursor.getString(QUERY_CONTACTS_DEPART));
			v.setBackgroundResource(R.drawable.listitem_bg);
			Log.i(TAG,"the title = "+cursor.getString(QUERY_CONTACTS_TITLE));
			
		}

		@Override
		public View newView(Context arg0, Cursor arg1, ViewGroup v) {
			LayoutInflater mLayoutInflater  = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
			View itemView = mLayoutInflater.inflate(R.layout.contactlist_item_layout,v,false);
			
			return itemView;
		}
		
	}
	
	private class ContactHandler extends AsyncQueryHandler{

		public ContactHandler(ContentResolver cr) {
			super(cr);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			mAdapter.changeCursor(cursor);
			
			if(mSeartText.getText().length() == 0){
			    
			    ArrayList<String> list = new ArrayList<String>();
	            cursor.moveToPosition(-1);
	            for(int i = 0;i<cursor.getCount();i++){
	                cursor.moveToNext();
	                if(cursor.getString(QUERY_CONTACTS_NAME) != null){
	                    list.add(cursor.getString(QUERY_CONTACTS_NAME));
	                }
	            }
	            String string[] = list.toArray(new String[list.size()]);
	            ArrayAdapter<String> adapter=new ArrayAdapter<String>(mContext,
	                    android.R.layout.simple_dropdown_item_1line,string);
	            mSeartText.setAdapter(adapter);
	            if(mBlSycQuery){
	            	setDepartment();
	            	mBlSycQuery = false;
	            }
	            
	            updateDisplay(cursor.getCount());
			}
		}
		
	}
	private void updateDisplay(int Count){
	    if(Count > 0){
	        mIndicateView.setVisibility(View.GONE);
	        mList.setVisibility(View.VISIBLE);
	        mSpinner .setVisibility(View.VISIBLE);
	        mSeartText.setVisibility(View.VISIBLE);
	    }else{
	        mIndicateView.setVisibility(View.VISIBLE);
            mList.setVisibility(View.GONE);
            mSpinner .setVisibility(View.GONE);
            mSeartText.setVisibility(View.GONE);
	    }
	}
	@Override
	public void onClick(View v) {
		if(v == mSynBtn){
		    mwaittingBar.setVisibility(View.VISIBLE);
            HttpConnectionUtil connect = new HttpConnectionUtil(getApplicationContext());
            ConnectWeb(connect,getUrl());
		}
	}
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
//		Cursor c = mAdapter.getCursor();
//        if (c != null) {
//        	Log.i(TAG,"the position is : "+position);
//            c.moveToPosition(position);
//            Intent intent = new Intent("android.intent.action.CALL",
//            Uri.fromParts("tel",c.getString(2), null));
//            startActivity(intent);
//            
//            
//        }
	    Cursor c = mAdapter.getCursor();
	    c.moveToPosition(position);
	    createContactsDialog(c);
	}
	public class TextChangedListener implements TextWatcher{

        @Override
        public void afterTextChanged(Editable arg0)
        {
            if(mSeartText.getText().length() == 0){
                mSearchName = null;
                mHandler.startQuery(42,null,ContactsData.CONTENT_URI,getProjection(),getSelection(),getSeletionArgs(),null);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                int arg3)
        {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2,
                int arg3)
        {
            // TODO Auto-generated method stub
            
        }
	    
	} 
	private class SpinnerListener implements OnItemSelectedListener{

        @Override
        public void onItemSelected(AdapterView<?> arg0, View v, int position,
                long id)
        {
            mDepartment = mSpinnerAdapter.getItem(position);
            if(getString(R.string.all_department).equals(mDepartment)){
                mDepartment = null;   
            }
            mHandler.startQuery(42,null,ContactsData.CONTENT_URI,getProjection(),getSelection(),getSeletionArgs(),null);
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0)
        {
            // TODO Auto-generated method stub
            
        }
	    
	}
}
