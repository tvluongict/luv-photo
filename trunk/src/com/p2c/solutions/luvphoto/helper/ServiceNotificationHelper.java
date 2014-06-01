package com.p2c.solutions.luvphoto.helper;

import android.content.Context;

import com.p2c.solutions.luvphoto.R;
import com.p2c.solutions.luvphoto.core.webservices.HttpMethod;
import com.p2c.solutions.luvphoto.core.webservices.WebServiceInvoker;
import com.p2c.solutions.luvphoto.core.webservices.WebServiceInvoker.JsonResult;

public class ServiceNotificationHelper {


	private static final String NAMESPACE = "Notification";
	
	private static final String GET_NOTIFICATION_METHOD = "GetNotificationLuvPhoto";
	@SuppressWarnings("unused")
	private Context context;
	private WebServiceInvoker invoker;
	
	public ServiceNotificationHelper(Context context){
		this.context = context;
		String url = context.getString(R.string.luv_webapi_url);
		invoker = new WebServiceInvoker(NAMESPACE, url);
	}
	
	public JsonResult GetNotification() {		
		invoker.setWebMethod(HttpMethod.GET);		
		return invoker.invokeMethod(GET_NOTIFICATION_METHOD);				
	}	
}
