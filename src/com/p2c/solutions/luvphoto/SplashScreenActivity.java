package com.p2c.solutions.luvphoto;

import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.UiThread;
import com.p2c.solutions.luvphoto.core.models.Account;
import com.p2c.solutions.luvphoto.core.webservices.JsonMessage;
import com.p2c.solutions.luvphoto.core.webservices.WebServiceInvoker.JsonResult;
import com.p2c.solutions.luvphoto.helper.AccountHelper;

@EActivity(R.layout.activity_splash)
public class SplashScreenActivity extends BaseActivity{

	private static int SPLASH_TIME_OUT = 2000;
	AccountHelper accountHelper;
	
	@AfterViews
	void afterViews(){		
		
		accountHelper = new AccountHelper(getApplicationContext());
		checkLogin();
					
		new Handler().postDelayed(new Runnable() {
			 
            @Override
            public void run() {            	
            	Intent i = new Intent(SplashScreenActivity.this,MainActivity_.class);
        		startActivity(i);
        		finish();
            }
        }, SPLASH_TIME_OUT);
	}
	
	private void checkLogin(){
		int id = accountHelper.getLoginRemember();
		if(id>0)
			loginProcess(id);
	}
	
	@Background
	public void loginProcess(int id)
	{
		JsonResult result = accountHelper.getById(id);
		
		if(result.getMessage() == JsonMessage.SUCCESSFULL)
		{			
			loginAccount = result.parse(Account.class);
			if(loginAccount != null)
				accountHelper.setLoginAccount(loginAccount);			
			
		} else if (result.getMessage() == JsonMessage.CONNECT_FAILED) {
			showToast(R.string.app_global_global_networkConnectionFailed);
		} else if (result.getMessage() == JsonMessage.RESULT_PARSE_ERROR) {
			showToast(R.string.app_global_global_parseWsResultFailed);
		} else {
			showToast(R.string.app_global_global_parameterWsInvalid);
		}
	}
	
	@UiThread
	void showToast(final int resId) {
		Toast.makeText(getApplicationContext(), resId,	Toast.LENGTH_LONG).show();
	}
}
