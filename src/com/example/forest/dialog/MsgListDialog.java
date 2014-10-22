package com.example.forest.dialog;

import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.forest.R;
import com.example.forest.util.DBManager;

//自定义的对话框
public class MsgListDialog extends Dialog {
	Context context;
	ListView msgList;

	/********************** 20140915 增加以下代码 开始 ***********************/
	// 为下拉列表定义对应的适配器
	private ArrayAdapter<String> adapter;

	/******************************* 结束 *******************************/

	// 构造函数
	public MsgListDialog(Context context, int theme) {
		// 继承公共构造函数
		super(context, theme);
		this.context = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.msglist);
		msgList = (ListView) findViewById(R.id.msglist);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

		DBManager dbManager = new DBManager(context);
		dbManager.openDatabase();
		final List<String> list = dbManager.query_unreadMsg();
		dbManager.closeDatabase();

		/********************** 20140915 修改以下代码 开始 ***********************/
		adapter = new ArrayAdapter<String>(context, R.layout.msgdetail,
				R.id.mesgdetail, list) {

			@Override
			public View getView(final int position, View convertView,
					ViewGroup parent) {
				// TODO Auto-generated method stub
				View view = super.getView(position, convertView, parent);
				TextView textView = (TextView) view
						.findViewById(R.id.mesgdetail);
				if (list.get(position) != null) {
					textView.setText(list.get(position));
					textView.setOnClickListener(new View.OnClickListener() {
						// 点击通告消息详情之后的逻辑：更新数据库中的已读数据并删除
						@Override
						public void onClick(View arg0) {
							DBManager dbManager = new DBManager(context);
							dbManager.openDatabase();
							dbManager.update_readedMsg(list.get(position));
							dbManager.delete_readedMsg();
							dbManager.closeDatabase();
						}
					});
				}
				textView.setTextColor(Color.BLACK);

				return view;
			}

		};
		// 为适配器设置下拉列表下拉时的下拉样式
		adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
		msgList.setAdapter(adapter);

		/******************************* 结束 *******************************/
	}

}
