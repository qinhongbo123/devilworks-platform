package com.surfing.channel;

import java.util.ArrayList;
import java.util.HashMap;

import com.surfing.R;
import com.surfing.TrainTicketSearch.TrainTicketSearch;
import com.surfing.contacts.ContactsMutiSelectActivity;
import com.surfing.util.TitleBarDisplay;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class MoreModuleActivity extends ActivityBase implements OnItemClickListener
{
    private static final String TAG = "MoreModuleActivity";
    private SimpleAdapter mAdapterList = null;
    private ListView    mList = null;
    private ArrayList<HashMap<String, Object>>  mListData = null;
    private Context mContext = null;
    
    private static final int ITEM_TRAIN_ID = 0;
    private static final int ITEM_ESTORE_ID = 1;
    private static final int ITEM_IMUSIC_ID = 2;
    private static final int ITEM_IGAME_ID = 3;
    private static final int ITEM_189_ID = 4;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
    	super.onCreate(savedInstanceState,TITLE_TYPE_COMPANY,null,0,true);
        
        mContext = getApplicationContext();
        setContentView(R.layout.listactivity_layout);
        
        setupView();
        
        mListData = new ArrayList<HashMap<String,Object>>();
        setData(mListData);
        mAdapterList = new SimpleAdapter(mContext,mListData,R.layout.listitem_layout,new String[]{"itemname"},new int[]{R.id.default_listitem_id});
        mList.setAdapter(mAdapterList);
        mList.setOnItemClickListener(this);
    }
    private void setData(ArrayList<HashMap<String, Object>> arraylist){
        String item[]={getString(R.string.item_train),getString(R.string.item_estore),
                getString(R.string.item_imusic),getString(R.string.item_igame),
                getString(R.string.item_189)};
        int itemId[] = {ITEM_TRAIN_ID,ITEM_ESTORE_ID,
                        ITEM_IMUSIC_ID,ITEM_IGAME_ID,
                        ITEM_189_ID};
        HashMap<String,Object> map = null;
        if(arraylist == null){
            return;
        }
        for(int i = 0;i<item.length;i++){
            map = new HashMap<String, Object>();
            map.put("itemname",item[i]);
            map.put("itemid", itemId[i]);
            arraylist.add(map);
        }
        
    }
    private void setupView(){
        mList = (ListView)findViewById(R.id.default_list_id);
        
    }
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }
    @Override
    public void onItemClick(AdapterView<?> arg0, View view, int position, long id)
    {
        HashMap<String,Object> map = mListData.get(position);
        int Id = Integer.parseInt(map.get("itemid").toString());
        switch(Id){
            case ITEM_TRAIN_ID:{
                String url = "http://train.qunar.com";
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
            break;
            case ITEM_ESTORE_ID:{
                
            }
            break;
            case ITEM_IMUSIC_ID:{
                
            }
            break;
            case ITEM_IGAME_ID:{
                
            }
            break;
            case ITEM_189_ID:{
                
            }
            break;
            default:
                break;
        }
        
    }
    
}
