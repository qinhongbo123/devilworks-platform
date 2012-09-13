package com.surfing.TrainTicketSearch;

import android.app.Activity;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.surfing.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class TrainCodes extends Activity {

	ListView list;
	ArrayList<HashMap<String, Object>> listItem;
	HashMap<String, Object> map;
	SimpleAdapter listItemAdapter;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setContentView(R.layout.traincodeslist);
		// ��Layout�����ListView
		list = (ListView) findViewById(R.id.trainCodesList);
		TextView refreshTime_Label = (TextView) this.findViewById(R.id.refreshTime_Label1);
		
		Bundle mBundle = this.getIntent().getExtras();
		String data = mBundle.getString("data");
		String title = mBundle.getString("start_arriveStation");
		String refreshTime = mBundle.getString("refreshTime");
		
		this.setTitle( title + "�ĳ���");
		refreshTime_Label.setText("����������ÿСʱ����һ�Σ��˴θ���ʱ��:" + refreshTime);
		
		List<TrainInfo> trainArray = GetTestData();

		listItem = new ArrayList<HashMap<String, Object>>();

		listItemAdapter = new SimpleAdapter(this, listItem, R.layout.traincodeslistitem, 
					new String[] { "ItemTitle", "Start_ArriveStation", "Start_ArriveTime" }, 
					new int[] { R.id.trainCodeItemTitle, 
				R.id.start_arriveStation1, 
				R.id.start_arrive_time1});
		// ��Ӳ�����ʾ
		list.setAdapter(listItemAdapter);
		
		
		for (int i = 0; i < trainArray.size(); i++) {
			map = new HashMap<String, Object>();
			
			map.put("ItemTitle", trainArray.get(i).getTrainCode().replace(">", ""));
			
			String Start_ArriveStation =" " + trainArray.get(i).getStartStation() + "-" + trainArray.get(i).getArriveStation();
			map.put("Start_ArriveStation", Start_ArriveStation);
			
			String itemText1 = " ��ʱ:" + trainArray.get(i).getStartTime()
					+ "  ��ʱ:" + trainArray.get(i).getArrtiveTime() + "  ��ʱ:"
					+ trainArray.get(i).getUsedTime();
			map.put("Start_ArriveTime", itemText1);


			listItem.add(map);
			listItemAdapter.notifyDataSetChanged();
		}
		
		
		// ��ӵ��
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long arg3) {
				String trainCode = ((TextView)view.findViewById(R.id.trainCodeItemTitle)).getText().toString().trim();
				Intent intent = new Intent();
		        Bundle bundle = new Bundle();
		        bundle.putString("trainCode", trainCode);
		        intent.putExtras(bundle);
		        setResult(1111111, intent);
				finish();  
			}
		});
	}
	
	List<TrainInfo> GetTestData()
	{
		List<TrainInfo> TrainArray = new ArrayList<TrainInfo>();
		for(int i=0; i<10; i++)
		{
			TrainInfo trainInfo = new TrainInfo();
			
			trainInfo.setTrainCode("1010");
			trainInfo.setStartStation("西安");
			trainInfo.setArriveStation("北京");
	
			trainInfo.setStartTime("1010");
			trainInfo.setArriveTime("1010");
			trainInfo.setUsedTime("1010");
	
			trainInfo.setHardSeatCount("1010");
			trainInfo.setSoftSeatCount("1010");
			trainInfo.setHardCouchetteCount("1010");
			trainInfo.setSoftCouchetteCount("1010");
			trainInfo.setFirstClassSeatCount("1010");
			trainInfo.setSecondClassSeatCount("1010");
			trainInfo.setPremiumCouchetteCount("1010");
	
			TrainArray.add(trainInfo);
		}
		
		return TrainArray;
	}
	
}
