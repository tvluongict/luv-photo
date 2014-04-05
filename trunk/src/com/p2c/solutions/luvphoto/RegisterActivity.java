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
import com.p2c.solutions.luvphoto.R;
import com.p2c.solutions.luvphoto.core.models.Account;
import com.p2c.solutions.luvphoto.core.webservices.JsonMessage;
import com.p2c.solutions.luvphoto.core.webservices.WebServiceInvoker.JsonResult;
import com.p2c.solutions.luvphoto.helper.AccountHelper;

@EActivity(R.layout.activity_register)
public class RegisterActivity extends BaseActivity{

	@ViewById(R.id.txtEmail)
	LuvEditText txtEmail;
	
	@ViewById(R.id.txtPassword)
	LuvEditText txtPassword;
	
	@ViewById(R.id.txtName)
	LuvEditText txtName;
	
	AccountHelper accountHelper;
	
	
	@AfterViews
	void afterViews(){		
		accountHelper = new AccountHelper(getApplication());
	}
	
	@Click(R.id.btn_register)
	public void registerClicked()
	{
		String emailValue = String.valueOf(txtEmail.getText());
		
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
		
		String passwordValue = String.valueOf(txtPassword.getText()).trim();
		
		//CHECK PASSWORD NOT NULL
		if(passwordValue == null || passwordValue.equals(""))
		{
			Toast.makeText(this, R.string.validate_email_empty,	Toast.LENGTH_LONG).show();
			return;
		}
		
		//CHECK PASSWORD LENGTH
		if(passwordValue.length()<6 || passwordValue.length() > 32)
		{
			Toast.makeText(this, R.string.validate_password_length,	Toast.LENGTH_LONG).show();
			return;
		}
		
		
		String nameValue = String.valueOf(txtName.getText()).trim();
		
		//CHECK NAME NOT NULL
		if(nameValue == null || nameValue.equals(""))
		{
			Toast.makeText(this, R.string.validate_email_empty,	Toast.LENGTH_LONG).show();
			return;
		}
		
		registerProcess(emailValue, passwordValue, nameValue);
		
	}
	
	@Background
	public void registerProcess(String email, String password, String name)
	{
		JsonResult result = accountHelper.register(email, password,name);
		
		if(result.getMessage() == JsonMessage.SUCCESSFULL)
		{
			Account account = 	loginAccount = result.parse(Account.class);				
			if(account.getId()>0)
				registerSuccess();
			else
				showToast(R.string.msg_register_unsuccess);			
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
	void registerSuccess()
	{				
		showRegisterConfirmDialog();
		resetForm();
	}
	
	@Click(R.id.btn_login)
	public void loginClicked()
	{
		Intent i = new Intent(RegisterActivity.this, LoginActivity_.class);
		startActivity(i);
        finish();
	}	
		
	@UiThread
	void showToast(final int resId) {
		Toast.makeText(getApplicationContext(), resId,	Toast.LENGTH_LONG).show();
	}
	
	//RESET FORM REGISTER
	private void resetForm()
	{
		txtEmail.setText(null);
		txtPassword.setText(null);
		txtName.setText(null);
	}
	
	//DIALOG AFTER REGISTER SUCCESS
	public void showRegisterConfirmDialog()
	{
		// custom dialog
		final Dialog dialog = new Dialog(this,R.style.FullHeightDialog);
		dialog.setContentView(R.layout.luv_confirm_dialog);
 
		// set the custom dialog components - text, image and button
				
		LuvTextView tvTitle = (LuvTextView) dialog.findViewById(R.id.tvTitle);
		LuvTextView tvContent = (LuvTextView) dialog.findViewById(R.id.tvContent);
		
		tvTitle.setText("Register success");
		tvContent.setText("Your account have been created. You can login now.");
		
		
		Button dialogButtonOk = (Button) dialog.findViewById(R.id.dialogButtonOK);
		dialogButtonOk.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					
					Intent i = new Intent(RegisterActivity.this, LoginActivity_.class);
					startActivity(i);
					finish();
					
				}
			});
		
		Button dialogButtonCancel = (Button) dialog.findViewById(R.id.dialogButtonCancel);
		dialogButtonCancel.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
		
		dialog.show();
	}
}
