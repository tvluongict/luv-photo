package com.p2c.solutions.luvphoto.helper;

import android.content.Context;

import com.p2c.solutions.luvphoto.R;
import com.p2c.solutions.luvphoto.core.webservices.HttpMethod;
import com.p2c.solutions.luvphoto.core.webservices.WebServiceInvoker;
import com.p2c.solutions.luvphoto.core.webservices.WebServiceInvoker.JsonResult;

public class AlbumHelper {

	private static final String NAMESPACE = "Album";
	
	private static final String GET_ALL_ALBUMS_METHOD = "GetAll";
	private Context context;
	private WebServiceInvoker invoker;
	
	public AlbumHelper(Context context){
		this.context = context;
		String url = context.getString(R.string.luv_webapi_url);
		invoker = new WebServiceInvoker(NAMESPACE, url);
	}
	
	public JsonResult getAlbums() {		
		invoker.setWebMethod(HttpMethod.GET);		
		return invoker.invokeMethod(GET_ALL_ALBUMS_METHOD);				
	}
	
}
