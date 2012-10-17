package com.surfing.TrainTicketSearch;

import android.app.Activity;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.surfing.R;
import com.surfing.channel.ActivityBase;
import com.surfing.channel.CloseReceiver;
import com.surfing.httpconnection.HttpConnectionCallback;
import com.surfing.httpconnection.HttpConnectionUtil;
import com.surfing.httpconnection.HttpConnectionUtil.HttpMethod;
import com.surfing.util.ReadConfigFile;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class TrainTicketSearch extends ActivityBase implements OnClickListener{
	final String TAG="TrainTicketSearch";
	TextView startDataLabel = null;
	Button setStartDataButton = null;
	TextView startStationLabel = null;
	TextView arriveStationLabel = null;
	Button setStartArriveStationButton = null;
	EditText trainCodeText = null;
	Button setTrainCodeButton = null;
	TextView trainTypeLabel = null;
	Button setTrainTypeButton = null;
	// Spinner searcherSpinner = null;
	Button searcherButton = null;

	EditText startStationText = null;
	EditText arriveStationText = null;
	
	ProgressDialog progressDialog;

	ListView trainTypeList = null;
	String resultStr = "";
	String refreshTime = "";
	String trainCodes = "";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.searchticket);

		startStationLabel = (TextView) this
				.findViewById(R.id.startStation_label);
		arriveStationLabel = (TextView) this
				.findViewById(R.id.arriveStation_label);
		setStartArriveStationButton = (Button) this
				.findViewById(R.id.start_arrive_button);
		setStartArriveStationButton.setOnClickListener(this);
		
		trainCodeText = (EditText) this.findViewById(R.id.trainCode_editText);
		setTrainCodeButton = (Button) this.findViewById(R.id.trainCode_button);
		setTrainCodeButton.setOnClickListener(this);
		
		trainTypeLabel = (TextView) this.findViewById(R.id.traintype_label);
		setTrainTypeButton = (Button) this.findViewById(R.id.traintype_button);
		setTrainTypeButton.setOnClickListener(this);
		
		// searcherSpinner = (Spinner) this.findViewById(R.id.lx_Search);
		searcherButton = (Button) this.findViewById(R.id.ticketSearch_button);
		searcherButton.setOnClickListener(this);
		CloseReceiver.registerCloseActivity(this);
	}
	
	@Override
    protected void onDestroy()
    {
        // TODO Auto-generated method stub
	    CloseReceiver.unRegisterActivity(this);
        super.onDestroy();
    }

    @Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.start_arrive_button:
			LayoutInflater factory = LayoutInflater.from(TrainTicketSearch.this);
			View myView = factory.inflate(R.layout.start_arrive_station, null);
			startStationText = (EditText) myView
					.findViewById(R.id.startStationEditText);
			arriveStationText = (EditText) myView
					.findViewById(R.id.arriveStationEditText);
			final AlertDialog dialog = new AlertDialog.Builder(this).setTitle(
			R.string.train_start_station).setView(myView).setPositiveButton(R.string.ok,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							String start = startStationText.getText()
									.toString();
							String arrive = arriveStationText.getText()
									.toString();
							if (start.trim().equals("")
									|| start.trim().equals("")) {
								displayText(getString(R.string.train_ques_tip));
							} else {
								TrainTicketSearch.this.startStationLabel
										.setText(start);
								TrainTicketSearch.this.arriveStationLabel
										.setText(arrive);
							}
						}

			}).setNegativeButton(R.string.btn_cancel,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					}).create();
			dialog.show();
			break;
		case R.id.trainCode_button:
			TrainTicketSearch.this.progressDialog = new ProgressDialog(
					TrainTicketSearch.this);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setTitle(R.string.train_wait);
			progressDialog.setMessage(getString(R.string.train_searching));
			progressDialog.setCancelable(true);
			progressDialog.show();
			Thread trainCodeThread = new Thread() {
				public void run() {
					final String startStation = TrainTicketSearch.this.startStationLabel
							.getText().toString().trim();
					final String arriveStation = TrainTicketSearch.this.arriveStationLabel
							.getText().toString().trim();
					final String trainCode = TrainTicketSearch.this.trainCodeText
						.getText().toString().trim();

					if (trainCode.trim().equals("")
							|| trainCode.trim().equals("")) {
						displayText(getString(R.string.train_code_tip));
						return;
					} 
//
//					<item>
//					<Train_Num>"1043"</Train_Num>
//					<Type>"普快"</Type>
//					<S_No>2</S_No>
//					<Station>"咸阳"</Station>
//					<Day>1</Day>
//					<A_Time>1899-12-30 21:43:00</A_Time>
//					<D_Time>1899-12-30 21:45:00</D_Time>
//					<Distance>23</Distance>
//					<P1>4</P1>
//					<P2>41</P2>
//					<P3>0</P3>
//					<P4>58</P4>
//					</item> 

					Map<String, String> params = new HashMap<String, String>();
					params.put("search_type","1010");
					params.put("data",trainCode);
					
					//HttpConnectionUtil connect = new HttpConnectionUtil();					
					//ConnectWeb(connect, TrainInfo.url, params);
					//StartDisplayResult("");
				}
			};

			trainCodeThread.start();
			break;
		case R.id.traintype_button:
			Intent intent = new Intent();
			Bundle mBundle = new Bundle();
			mBundle.putString("data", "");
			intent.putExtras(mBundle);
			intent.setClass(TrainTicketSearch.this, TrainTypes.class);
			//TrainTicketSearch.this.startActivity(intent);

			TrainTicketSearch.this.startActivityForResult(intent, 1);
			break;
		case R.id.ticketSearch_button:
			TrainTicketSearch.this.progressDialog = new ProgressDialog(
					TrainTicketSearch.this);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setTitle(R.string.train_wait);
			progressDialog.setMessage(getString(R.string.train_searching));
			progressDialog.setCancelable(true);
			progressDialog.show();

			final String[] trainTypes = TrainTicketSearch.this.trainTypeLabel
					.getText().toString().trim().split(";");

			String aa = trainTypes[0];
			if (trainTypes.length >= 1) 
			{
				List<String> tFlags = new ArrayList<String>();
				for (int i = 0; i < trainTypes.length; i++) {
					if (trainTypes[i]
							.equals(TrainHelper.Train_DCType_Text)) {
						tFlags.add("DC");
					}
					if (trainTypes[i]
							.equals(TrainHelper.Train_ZType_Text)) {
						tFlags.add("Z");
					}
					if (trainTypes[i]
							.equals(TrainHelper.Train_TType_Text)) {
						tFlags.add("T");
					}
					if (trainTypes[i]
							.equals(TrainHelper.Train_KType_Text)) {
						tFlags.add("K");
					}
					if (trainTypes[i]
							.equals(TrainHelper.Train_PKType_Text)) {
						tFlags.add("PK");
					}
					if (trainTypes[i]
							.equals(TrainHelper.Train_PKEType_Text)) {
						tFlags.add("PKE");
					}
					if (trainTypes[i]
							.equals(TrainHelper.Train_LKType_Text)) {
						tFlags.add("LK");
					}
				}
			}
			Thread th = new Thread() {
				public void run() {
					final String startStation = TrainTicketSearch.this.startStationLabel
							.getText().toString().trim();
					final String arriveStation = TrainTicketSearch.this.arriveStationLabel
							.getText().toString().trim();
					final String[] trainTypes = TrainTicketSearch.this.trainTypeLabel
							.getText().toString().trim().split(";");

					String start = startStationText.getText()
							.toString();
					String arrive = arriveStationText.getText()
							.toString();
					if (start.trim().equals("")
							|| start.trim().equals("")) {
						displayText(getString(R.string.train_ques_tip));
						return;
					} 
					Map<String, String> params = new HashMap<String, String>();
					params.put("search_type","1011");//通过始发站查询
					//params.put("search_type","1012");//通过始发站和终点站查询
					params.put("data",start);
					params.put("data_1",arrive);
					
					//HttpConnectionUtil connect = new HttpConnectionUtil();					
					//ConnectWeb(connect, TrainInfo.url, params);
				}
			};
			th.start();
			break;
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) {
		case RESULT_OK:
			this.trainTypeLabel.setText(data.getExtras().getString("trainTypes"));
			break;
		case 1111111:
			this.trainCodeText.setText(data.getExtras().getString("trainCode"));
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, 0, 0, "Network");
		menu.add(0, 2, 0, "About");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case 0:
			intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
			startActivity(intent);
			return true;
		case 1:
			displayText("1");
			return true;
		case 2:
			intent = new Intent();
			//intent.setClass(TrainTicketSearch.this, About.class);
			TrainTicketSearch.this.startActivity(intent);
			return true;
		}
		return false;
	}

	protected void displayText(String string) {
		Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
	}


	public void ConnectWeb(HttpConnectionUtil connect,String geturl, Map<String, String> params )
	{
		Log.i(TAG,"the url is : "+geturl);
		connect.asyncConnect(geturl, params, HttpMethod.POST, new HttpConnectionCallback()
		{			
			@Override
			public void execute(String response) 
			{
				//mwaittingBar.setVisibility(View.GONE);
				if(response.equals(HttpConnectionUtil.CONNECT_FAILED))
				{
					Log.i(TAG,"connection failed");
					TrainTicketSearch.this.progressDialog.cancel();
					return;
				}
				try
				{
					int nindex = 0;
					if((response == null) || (response.length() == 0))
					{
						Log.i(TAG,"response is null");
						TrainTicketSearch.this.progressDialog.cancel();
						return;
					}
					while(response.getBytes()[nindex] != '<')
					{
						nindex++;
					}
					InputStream stream = new ByteArrayInputStream(response.substring(nindex).getBytes());
					StartDisplayResult("");
				}
				catch (Exception e) 
				{
					e.printStackTrace();
				}
			}
		});
	}
	
	private void StartDisplayResult(String result)
	{
		Intent intent = new Intent();
		Bundle mBundle = new Bundle();
		mBundle.putString("data", result);
		intent.putExtras(mBundle);
		TrainTicketSearch.this.progressDialog.cancel();
		intent.setClass(TrainTicketSearch.this, TrainCodes.class);
		startActivity(intent);
	}
}
