package com.p2c.solutions.luvphoto.helper;

import android.content.Context;

import com.p2c.solutions.luvphoto.R;
import com.p2c.solutions.luvphoto.core.webservices.HttpMethod;
import com.p2c.solutions.luvphoto.core.webservices.WebServiceInvoker;
import com.p2c.solutions.luvphoto.core.webservices.WebServiceInvoker.JsonResult;
import com.p2c.solutions.luvphoto.core.webservices.WebServiceParam;

public class PhotoHelper {

	private static final String NAMESPACE = "Photo";
	
	private static final String GET_PHOTO_BY_ALBUM_METHOD = "GetByAlbum";	
	
	private Context context;
	private WebServiceInvoker invoker;
	
	public PhotoHelper(Context context){
		this.context = context;
		String url = context.getString(R.string.luv_webapi_url);
		invoker = new WebServiceInvoker(NAMESPACE, url);
	}
	
	public JsonResult getPhotoByAlbum(int albumId) {	
		
		WebServiceParam webServiceParam = new WebServiceParam();
		webServiceParam.addProperty("id", albumId);
		
		invoker.setWebMethod(HttpMethod.GET);		
		return invoker.invokeMethod(GET_PHOTO_BY_ALBUM_METHOD,webServiceParam);				
	}	
}
