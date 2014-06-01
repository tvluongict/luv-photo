package com.p2c.solutions.luvphoto;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.UiThread;
import com.googlecode.androidannotations.annotations.ViewById;
import com.p2c.solutions.luvphoto.core.models.Account;
import com.p2c.solutions.luvphoto.core.webservices.JsonMessage;
import com.p2c.solutions.luvphoto.core.webservices.WebServiceInvoker.JsonResult;
import com.p2c.solutions.luvphoto.helper.AccountHelper;

@EActivity(R.layout.activity_login)
public class LoginActivity extends BaseActivity{

	@ViewById(R.id.tbx_password)
	EditText tbxPassword;
	
	@ViewById(R.id.tbx_email)
	EditText tbxEmail;	
	
	AccountHelper accountHelper;
	
	@AfterViews
	void afterViews() {			
		accountHelper = new AccountHelper(getApplicationContext());
	}
	
	@Override
	protected void onStart() {			
		super.onStart();
	}
	
	@Click(R.id.btn_login)
	void btnLoginClicked() {
		
		hideKeyboard(tbxEmail);
		hideKeyboard(tbxPassword);
		
		String emailValue = tbxEmail.getText().toString().trim();
		
		//CHECK EMAIL NOT NULL
		if(emailValue.equals(""))
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
		
		String passwordValue = tbxPassword.getText().toString().trim();
		
		//CHECK PASSWORD NOT NULL
		if(passwordValue.equals(""))
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
		
		//CALL FUNCTION TO CHECK LOGIN
		loginProcess(emailValue, passwordValue);
	}
	
	
	@Background
	void loginProcess(String email, String password) {
		JsonResult result = accountHelper.checkLogin(email, password);
		if(result.getMessage() == JsonMessage.SUCCESSFULL)
		{
			Account account = result.parse(Account.class);
			if(account!=null)
			{				
				loginSuccess(account);
			}	
			else
			{
				showToast(R.string.msg_login_incorrect_account);
			}
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
	void loginSuccess(Account account) {
		new AccountHelper(getApplicationContext()).setLoginAccount(account);
		setResult(RESULT_OK);        
		finish();
	}
	
	@UiThread
	void showToast(final int resId) {
		Toast.makeText(getApplicationContext(), resId,	Toast.LENGTH_LONG).show();
	}
	
	void showLoading() {
		//progressBar.setVisibility(View.VISIBLE);
		//loginPanel.setVisibility(View.GONE);
	}

	void hiddenLoading() {
		//progressBar.setVisibility(View.GONE);
		//loginPanel.setVisibility(View.VISIBLE);
	}
	
		
	@Click(R.id.btn_recovery)	
	public void recoveryPasswordClicked()
	{
		Intent i = new Intent(this, RecoveryActivity_.class);
        startActivity(i); 
	}
	
	@Click(R.id.btn_register)	
	public void registerClicked()
	{
		Intent i = new Intent(this, RegisterActivity_.class);
        startActivity(i); 
	}
	
	void hideKeyboard(View view) {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}
}
