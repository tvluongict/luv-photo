package com.p2c.solutions.luvphoto;

import android.widget.ImageView;
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

@EActivity(R.layout.activity_update_profile)
public class UpdateProfileActivity extends BaseActivity{

	AccountHelper accountHelper;
	
	@ViewById(R.id.txtEmail)
	LuvEditText txtEmail;
	
	@ViewById(R.id.txtPassword)
	LuvEditText txtPassword;
	
	@ViewById(R.id.txtName)
	LuvEditText txtName;
	
	@ViewById(R.id.btn_update)
	ImageView btnUpdate;
	
	@AfterViews
	void afterViews(){
		
		accountHelper = new AccountHelper(getApplicationContext());
		loginAccount = accountHelper.getLoginAccount();
		loadDataProcess(loginAccount.getEmail());
	}
	
	@Background
	void loadDataProcess(String email) {
		JsonResult result = accountHelper.getByEmail(email);
		if(result.getMessage() == JsonMessage.SUCCESSFULL)
		{
			Account account = result.parse(Account.class);
			if(account!=null)
			{				
				dispayOnView(account);
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
	void dispayOnView(Account account) {
		txtEmail.setText(account.getEmail());
		txtName.setText(account.getName());
	}
	
	@Click(R.id.btn_update)
	public void updateClicked(){
		
		btnUpdate.setEnabled(false);
		
		btnUpdate.setImageResource(R.drawable.loadingwhite);
		
		String passwordValue = String.valueOf(txtPassword.getText()).trim();
		String nameValue = String.valueOf(txtName.getText()).trim();
		if(passwordValue.length()>0)
		{
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
		
			//CHECK NAME NOT NULL
			if(nameValue == null || nameValue.equals(""))
			{
				Toast.makeText(this, R.string.validate_email_empty,	Toast.LENGTH_LONG).show();
				return;
			}
			
			updateProcess(loginAccount.getId(), passwordValue, nameValue);
		}	
		else
		{
			//CHECK NAME NOT NULL
			if(nameValue == null || nameValue.equals(""))
			{
				Toast.makeText(this, R.string.validate_email_empty,	Toast.LENGTH_LONG).show();
				return;
			}
			
			updateProcess(loginAccount.getId(), null, nameValue);
		}
		
	}
	
	@Background
	public void updateProcess(int id, String password, String name)
	{
		JsonResult result;
		if(password==null)
			result = accountHelper.updateAccount(id,name);
		else
			result = accountHelper.updateAccountAndPassword(id,password, name);
		
		if(result.getMessage() == JsonMessage.SUCCESSFULL)
		{
			Account account = 	loginAccount = result.parse(Account.class);				
			if(account!=null)
				updateSuccess(account);
			else
				updateFail(R.string.msg_update_profie_unsuccess);			
		}
		else if(result.getMessage() == JsonMessage.CONNECT_FAILED)
		{
			updateFail(R.string.app_global_global_networkConnectionFailed);
		}
		else if(result.getMessage() == JsonMessage.PARAMETER_INVALID)
		{
			updateFail(R.string.app_global_global_parameterWsInvalid);
		}
		else if(result.getMessage() == JsonMessage.PROTOCOL_INVALID)
		{
			updateFail(R.string.app_global_global_protocolInvalid);
		}
		else if(result.getMessage() == JsonMessage.RESULT_PARSE_ERROR)
		{
			updateFail(R.string.app_global_global_parseWsResultFailed);
		}
	}
	
	@UiThread
	void updateSuccess(Account account){
		accountHelper.setLoginAccount(account);
		btnUpdate.setImageResource(R.drawable.update);
		showToast(R.string.msg_update_profie_success);
		btnUpdate.setEnabled(true);
	}
	
	@UiThread
	void updateFail(int resId){	
		showToast(resId);
		btnUpdate.setEnabled(true);
	}
	
	@Override
	public void onBackPressed() {
	    finish();
	}
	
	@UiThread
	void showToast(final int resId) {
		Toast.makeText(getApplicationContext(), resId,	Toast.LENGTH_LONG).show();
	}	
}
