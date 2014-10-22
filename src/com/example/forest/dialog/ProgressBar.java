package com.example.forest.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import com.eton.czh.GifView;
import com.example.forest.R;

public class ProgressBar extends Dialog {

	Context context;
	GifView progress;
	TextView bartext;
	
	public ProgressBar(Context context, int theme) {
		super(context, theme);
		this.context = context;		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);		
	}

	
	public void init(ProgressBar progressbar,String text)
	{
		this.progress = (GifView)progressbar.findViewById(R.id.progress);
		this.progress.setGifImage(R.drawable.progressbar);
		this.bartext = (TextView)progressbar.findViewById(R.id.bar_text);
		this.bartext.setText(text);
	}
	
}
