package com.p2c.solutions.luvphoto.helper;

import android.content.Context;

import com.p2c.solutions.luvphoto.R;
import com.p2c.solutions.luvphoto.core.webservices.HttpMethod;
import com.p2c.solutions.luvphoto.core.webservices.WebServiceInvoker;
import com.p2c.solutions.luvphoto.core.webservices.WebServiceInvoker.JsonResult;
import com.p2c.solutions.luvphoto.core.webservices.WebServiceParam;

public class FavoritePhotoHelper {

private static final String NAMESPACE = "FavoritePhoto";
	
	private static final String NEW_FAVORITE_PHOTO_METHOD = "NewFavoritePhoto";
	private static final String CHECK_FAVORITE_PHOTO_METHOD = "CheckFavoritePhoto";
	private static final String REMOVE_FAVORITE_PHOTO_METHOD = "DeleteFavoritePhoto";
	private static final String GET_PHOTO_BY_ACCOUNT_METHOD = "GetByAccount";
	
	@SuppressWarnings("unused")
	private Context context;
	private WebServiceInvoker invoker;
	
	public FavoritePhotoHelper(Context context){
		this.context = context;
		String url = context.getString(R.string.luv_webapi_url);
		invoker = new WebServiceInvoker(NAMESPACE, url);
	}
	
	public JsonResult newFavoritePhoto(int photoId, int accountId) {	
		
		WebServiceParam webServiceParam = new WebServiceParam();
		webServiceParam.addProperty("PhotoId", photoId);
		webServiceParam.addProperty("AccountId", accountId);
		
		invoker.setWebMethod(HttpMethod.POST);		
		return invoker.invokeMethod(NEW_FAVORITE_PHOTO_METHOD,webServiceParam);				
	}
	
	public JsonResult checkFavoritePhoto(int photoId, int accountId) {	
		
		WebServiceParam webServiceParam = new WebServiceParam();
		webServiceParam.addProperty("PhotoId", photoId);
		webServiceParam.addProperty("AccountId", accountId);
		
		invoker.setWebMethod(HttpMethod.GET);		
		return invoker.invokeMethod(CHECK_FAVORITE_PHOTO_METHOD,webServiceParam);				
	}
	
	public JsonResult deleteFavoritePhoto(int id,int accountId) {	
		
		WebServiceParam webServiceParam = new WebServiceParam();
		webServiceParam.addProperty("Id", id);
		webServiceParam.addProperty("AccountId", accountId);
		
		invoker.setWebMethod(HttpMethod.DELETE);		
		return invoker.invokeMethod(REMOVE_FAVORITE_PHOTO_METHOD,webServiceParam);				
	}
	
	public JsonResult getPhotoByAccount(int accountId) {	
		
		WebServiceParam webServiceParam = new WebServiceParam();
		webServiceParam.addProperty("accountId", accountId);
		
		invoker.setWebMethod(HttpMethod.GET);		
		return invoker.invokeMethod(GET_PHOTO_BY_ACCOUNT_METHOD,webServiceParam);				
	}
}
