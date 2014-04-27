package com.p2c.solutions.luvphoto;

import android.app.Dialog;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.UiThread;
import com.googlecode.androidannotations.annotations.ViewById;
import com.p2c.solutions.luvphoto.core.webservices.JsonMessage;
import com.p2c.solutions.luvphoto.core.webservices.WebServiceInvoker.JsonResult;
import com.p2c.solutions.luvphoto.helper.AccountHelper;

@EActivity(R.layout.activity_recoverypassword)
public class RecoveryActivity extends BaseActivity{

	@ViewById(R.id.tbx_email)
	LuvEditText tbxEmail;
	
	AccountHelper accountHelper;
	
	@AfterViews
	void afterView(){
		
		accountHelper = new AccountHelper(getApplication());
	}
	
	@Click(R.id.btn_recovery)
	public void recoveryCliked()
	{
		String emailValue = String.valueOf(tbxEmail.getText());
		
		//CHECK EMAIL NOT NULL
		if(emailValue == null || emailValue.equals(""))
		{
			Toast.makeText(this, R.string.validate_email_empty,	Toast.LENGTH_LONG).show();
			return;
		}
		
		//CHECK EMAIL FORMAT
		if(!android.util.Patterns.EMAIL_ADDRESS.matcher(emailValue).matches())
		{
			Toast.makeText(this, R.string.validate_email_invalid_format, Toast.LENGTH_LONG).show();
			return;
		}
		
		recoveryProcess(emailValue);
	}
		
	@Background
	void recoveryProcess(String email)
	{
		JsonResult result = accountHelper.recovery(email);
		
		if(result.getMessage() == JsonMessage.SUCCESSFULL)
		{
			recoverySuccess();			
		}
		else if(result.getMessage() == JsonMessage.CONNECT_FAILED)
		{
			showToast(R.string.app_global_global_networkConnectionFailed);
		}
		else if(result.getMessage() == JsonMessage.PARAMETER_INVALID)
		{
			showToast(R.string.app_global_global_parameterWsInvalid);
		}
		else if(result.getMessage() == JsonMessage.PROTOCOL_INVALID)
		{
			showToast(R.string.app_global_global_protocolInvalid);
		}
		else if(result.getMessage() == JsonMessage.RESULT_PARSE_ERROR)
		{
			showToast(R.string.app_global_global_parseWsResultFailed);
		}
	}
	
	@UiThread
	void recoverySuccess()
	{		
		resetForm();
		showRecoveryAlertDialog();		
	}
	
	//RESET FORM
	private void resetForm()
	{
		tbxEmail.setText(null);
	}
	
	@Click(R.id.btn_login)
	public void loginClicked(){
		Intent i = new Intent(RecoveryActivity.this,LoginActivity_.class);
		startActivity(i);
		finish();
	}
	
	@UiThread
	void showToast(final int resId) {
		Toast.makeText(getApplicationContext(), resId,	Toast.LENGTH_LONG).show();
	}
	
	//SHOW DIALOG AFTER SEND EMAIL RECOVERY
	public void showRecoveryAlertDialog()
	{
		// custom dialog
		final Dialog dialog = new Dialog(this,R.style.FullHeightDialog);
		dialog.setContentView(R.layout.luv_alert_dialog);
 
		// set the custom dialog components - text, image and button
				
		LuvTextView tvTitle = (LuvTextView) dialog.findViewById(R.id.tvTitle);
		LuvTextView tvContent = (LuvTextView) dialog.findViewById(R.id.tvContent);
		
		tvTitle.setText("Success");
		tvContent.setText("An email have been send your mailbox. Please check email.");	
		
		Button dialogButtonOk = (Button) dialog.findViewById(R.id.dialogButtonOK);
		dialogButtonOk.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					finish();
				}
			});
		dialog.show();
	}
}
