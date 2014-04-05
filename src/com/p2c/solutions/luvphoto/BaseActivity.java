package com.p2c.solutions.luvphoto;

import android.app.Activity;
import android.os.Bundle;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.p2c.solutions.luvphoto.core.models.Account;

public class BaseActivity extends Activity{
	
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	public Account loginAccount;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
}
