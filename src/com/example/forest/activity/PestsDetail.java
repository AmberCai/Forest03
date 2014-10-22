package com.example.forest.activity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.example.forest.R;
import com.example.forest.dialog.NotificationExtend;
import com.example.forest.util.DBManager;
import com.example.forest.util.Forest;
import com.example.forest.util.Util;

public class PestsDetail extends Activity {

	// SharedPreferences config_preferences;
	Spinner pestsKinds, pestsStage, pestsAmount, pestsLevel, pestsAdvise;
	Button send;
	String datetime;

	List<String> kindslist_Number;
	List<String> kindslist_Name;
	List<String> stagelist_Number;
	List<String> stagelist_Name;
	List<String> amountlist_Number;
	List<String> amountlist_Name;
	List<String> levellist_Number;
	List<String> levellist_Name;
	List<String> adviselist_Number;
	List<String> adviselist_Name;

	/************ 最小化，后台 *********************************/
	NotificationExtend notification;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pests_detail);

		pestsKinds = (Spinner) findViewById(R.id.pestskinds);
		pestsStage = (Spinner) findViewById(R.id.pestsstage);
		pestsAmount = (Spinner) findViewById(R.id.pestsamount);
		pestsLevel = (Spinner) findViewById(R.id.pestslevel);
		pestsAdvise = (Spinner) findViewById(R.id.pestsadvise);

		Intent intent = getIntent();
		Bundle data = intent.getExtras();
		datetime = data.getString("datetime");

		// config_preferences = Forest.config_preferences;

		notification = new NotificationExtend(PestsDetail.this);

		DBManager dbManager = new DBManager(PestsDetail.this);
		dbManager.openDatabase();
		Map<String, String> kinds_map = dbManager.query_pestsKinds();
		Map<String, String> stage_map = dbManager.query_pestsStage();
		Map<String, String> amount_map = dbManager.query_pestsAmount();
		Map<String, String> level_map = dbManager.query_pestsLevel();
		Map<String, String> advise_map = dbManager.query_pestsAdvise();
		dbManager.closeDatabase();

		kindslist_Number = new ArrayList<String>();
		kindslist_Name = new ArrayList<String>();
		Iterator<String> it0 = kinds_map.keySet().iterator();
		while (it0.hasNext()) {
			String key = it0.next().toString();
			kindslist_Number.add(key);
			kindslist_Name.add(kinds_map.get(key));
		}

		stagelist_Number = new ArrayList<String>();
		stagelist_Name = new ArrayList<String>();
		Iterator<String> it1 = stage_map.keySet().iterator();
		while (it1.hasNext()) {
			String key = it1.next().toString();
			stagelist_Number.add(key);
			stagelist_Name.add(stage_map.get(key));
		}

		amountlist_Number = new ArrayList<String>();
		amountlist_Name = new ArrayList<String>();
		Iterator<String> it2 = amount_map.keySet().iterator();
		while (it2.hasNext()) {
			String key = it2.next().toString();
			amountlist_Number.add(key);
			amountlist_Name.add(amount_map.get(key));
		}

		levellist_Number = new ArrayList<String>();
		levellist_Name = new ArrayList<String>();
		Iterator<String> it3 = level_map.keySet().iterator();
		while (it3.hasNext()) {
			String key = it3.next().toString();
			levellist_Number.add(key);
			levellist_Name.add(level_map.get(key));
		}

		adviselist_Number = new ArrayList<String>();
		adviselist_Name = new ArrayList<String>();
		Iterator<String> it = advise_map.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next().toString();
			adviselist_Number.add(key);
			adviselist_Name.add(advise_map.get(key));
		}

		ArrayAdapter<String> kinds = new ArrayAdapter<String>(PestsDetail.this,
				android.R.layout.simple_spinner_item, kindslist_Name);
		ArrayAdapter<String> stage = new ArrayAdapter<String>(PestsDetail.this,
				android.R.layout.simple_spinner_item, stagelist_Name);
		ArrayAdapter<String> amount = new ArrayAdapter<String>(
				PestsDetail.this, android.R.layout.simple_spinner_item,
				amountlist_Name);
		ArrayAdapter<String> level = new ArrayAdapter<String>(PestsDetail.this,
				android.R.layout.simple_spinner_item, levellist_Name);
		ArrayAdapter<String> advise = new ArrayAdapter<String>(
				PestsDetail.this, android.R.layout.simple_spinner_item,
				adviselist_Name);

		/**
		 * @date 20140916
		 * @content 增加下拉列表样式
		 */
		kinds.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		stage.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		amount.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		level.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		advise.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		pestsKinds.setAdapter(kinds);
		pestsStage.setAdapter(stage);
		pestsAmount.setAdapter(amount);
		pestsLevel.setAdapter(level);
		pestsAdvise.setAdapter(advise);

		send = (Button) findViewById(R.id.send);
		send.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {

				StringBuilder sb = new StringBuilder();

				// sb.append(config_preferences.getString("phoneID",
				// "xxxxxxxx")+","); //手机编号
				sb.append(Util.phoneID + ","); // 手机编号
				sb.append(datetime + ","); // 日期、时间
				sb.append(kindslist_Number.get(pestsKinds
						.getSelectedItemPosition()) + ","); // 每个数字对应的种类
				sb.append(stagelist_Number.get(pestsStage
						.getSelectedItemPosition()) + ","); // 生长阶段对应数字
				sb.append(amountlist_Number.get(pestsAmount
						.getSelectedItemPosition()) + ","); // 受害数量对应数字
				sb.append(levellist_Number.get(pestsLevel
						.getSelectedItemPosition()) + ","); // 危害程度对应数字
				sb.append(adviselist_Number.get(pestsAdvise
						.getSelectedItemPosition())); // 处理建议对应数字

				final Util util = new Util(PestsDetail.this);
				final String msg = sb.toString();

				Thread detail_Thread = new Thread() {
					@Override
					public void run() {
						util.sendPestsDetail(msg);
					}
				};
				if (Forest.isNetConnect(PestsDetail.this)) {
					detail_Thread.start();
				}
				Intent i = new Intent(PestsDetail.this, Main.class);
				i.setFlags(i.FLAG_ACTIVITY_NO_USER_ACTION);
				startActivity(i);
			}
		});
	}

	@Override
	protected void onUserLeaveHint() {
		// TODO Auto-generated method stub
		super.onUserLeaveHint();
		notification.showNotification();
		moveTaskToBack(true);
	}

}
