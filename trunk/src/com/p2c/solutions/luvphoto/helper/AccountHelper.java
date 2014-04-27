package com.p2c.solutions.luvphoto.helper;

import java.util.Date;

import com.p2c.solutions.luvphoto.R;
import com.p2c.solutions.luvphoto.core.models.Account;
import com.p2c.solutions.luvphoto.core.utilities.CryptoUtilities;
import com.p2c.solutions.luvphoto.core.utilities.Sessions;
import com.p2c.solutions.luvphoto.core.webservices.HttpMethod;
import com.p2c.solutions.luvphoto.core.webservices.WebServiceInvoker;
import com.p2c.solutions.luvphoto.core.webservices.WebServiceInvoker.JsonResult;
import com.p2c.solutions.luvphoto.core.webservices.WebServiceParam;

import android.content.Context;

public class AccountHelper {

	private static final String NAMESPACE = "Account";
	
	private static final String GET_BY_ID_METHOD = "GetById";
	private static final String GET_BY_EMAIL_METHOD = "GetByEmail";
	private static final String LOGIN_METHOD = "Login";
	private static final String REGISTER_METHOD = "Register";
	private static final String RECOVERY_METHOD = "Recovery";
	private static final String UPDATE_ACCOUNT_METHOD = "UpdateAccount";
	private static final String UPDATE_ACCOUNT_PASSWORD_METHOD = "UpdateAccountAndPassword";
	
	private static Account loginAccount;
	private Context context;
	private WebServiceInvoker invoker;
	private Sessions session;
		
	
	public Account getLoginAccount() {
		return loginAccount;
	}

	public void setLoginAccount(Account account) {
		loginAccount = account;
		if(account!=null)
			setLoginRemember(loginAccount.getId(), loginAccount.getEmail());
	}

	public static boolean isLogined(){
		return loginAccount != null;
	}
	
	public AccountHelper(Context context){
		this.context = context;
		String url = context.getString(R.string.luv_webapi_url);
		invoker = new WebServiceInvoker(NAMESPACE, url);
		session = Sessions.getInstance(this.context);
	}
	
	public JsonResult getById(int id) {
		
		WebServiceParam params = new WebServiceParam();
		params.addProperty("Id", id);
		
		invoker.setWebMethod(HttpMethod.GET);
		JsonResult result = invoker.invokeMethod(GET_BY_ID_METHOD, params);
		
		return result;		
	}
	
	public JsonResult getByEmail(String email) {
		
		WebServiceParam params = new WebServiceParam();
		params.addProperty("Email", email);
		
		invoker.setWebMethod(HttpMethod.GET);
		JsonResult result = invoker.invokeMethod(GET_BY_EMAIL_METHOD, params);
		
		return result;		
	}
	
	public JsonResult checkLogin(String email, String password) {
				
		WebServiceParam params = new WebServiceParam();
		params.addProperty("Email", email);
		params.addProperty("Password", CryptoUtilities.Md5(password));
		
		invoker.setWebMethod(HttpMethod.POST);
		JsonResult result = invoker.invokeMethod(LOGIN_METHOD, params);
		
		return result;		
	}
		
	public JsonResult register(String email, String password, String name)
	{
		WebServiceParam params = new WebServiceParam();
		params.addProperty("Email", email);
		params.addProperty("Password", CryptoUtilities.Md5(password));
		params.addProperty("Name", name);
		params.addProperty("DateCreated", new Date());
		invoker.setWebMethod(HttpMethod.POST);
		JsonResult result = invoker.invokeMethod(REGISTER_METHOD, params);
		
		return result;	
	}
	
	public JsonResult updateAccount(int id, String name)
	{
		WebServiceParam params = new WebServiceParam();
		params.addProperty("Id", id);
		params.addProperty("Name", name);
		invoker.setWebMethod(HttpMethod.POST);
		JsonResult result = invoker.invokeMethod(UPDATE_ACCOUNT_METHOD, params);		
		return result;	
	}
	
	public JsonResult updateAccountAndPassword(int id, String password, String name)
	{
		WebServiceParam params = new WebServiceParam();
		params.addProperty("Id", id);
		params.addProperty("Password", CryptoUtilities.Md5(password));
		params.addProperty("Name", name);
		invoker.setWebMethod(HttpMethod.POST);
		JsonResult result = invoker.invokeMethod(UPDATE_ACCOUNT_PASSWORD_METHOD, params);		
		return result;	
	}
	
	public JsonResult recovery(String email)
	{
		WebServiceParam params = new WebServiceParam();
		params.addProperty("Email", email);
		
		invoker.setWebMethod(HttpMethod.POST);
		JsonResult result = invoker.invokeMethod(RECOVERY_METHOD,params);		
		return result;	
	}
	
	public void logout(){
		loginAccount = null;
	}
	
	public void setLoginRemember(int id, String email){
		session.put(SessionConfig.LOGIN_REMEMBER_ID, id);
		session.put(SessionConfig.LOGIN_REMEMBER_EMAIL, email);
	}
	
	public int getLoginRemember(){
		return session.get(SessionConfig.LOGIN_REMEMBER_ID, 0);
	}
}
