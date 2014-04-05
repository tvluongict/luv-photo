package com.p2c.solutions.luvphoto;

import greendroid.widget.ItemAdapter;
import java.util.ArrayList;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.UiThread;
import com.googlecode.androidannotations.annotations.ViewById;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;
import com.p2c.solutions.luvphoto.adapter.items.PhotoItem;
import com.p2c.solutions.luvphoto.core.models.Photo;
import com.p2c.solutions.luvphoto.core.webservices.JsonMessage;
import com.p2c.solutions.luvphoto.core.webservices.WebServiceInvoker.JsonResult;
import com.p2c.solutions.luvphoto.helper.AccountHelper;
import com.p2c.solutions.luvphoto.helper.FavoritePhotoHelper;

@EActivity(R.layout.activity_account)
public class AccountActivity extends BaseActivity implements OnRefreshListener<GridView>,AdapterView.OnItemClickListener{

	private int LOGIN_CODE=1000;
	private int UPDATE_CODE=1100;
	private int FAVORITE_PHOTO_CODE=1200;
	
	@ViewById(R.id.lv_account_photo)
	PullToRefreshGridView lvPhotos;
	
	@ViewById(R.id.tv_profile_name)
	LuvTextView tvProfileName;
	
	@ViewById(R.id.tv_account_photo_number)
	LuvTextView accountPhotoNumber;
	
	AccountHelper accountHelper;
	
	private ItemAdapter adapter;
	FavoritePhotoHelper favoritePhotoHelper;
	
	@AfterViews
	void afterViews(){
			
		accountHelper = new AccountHelper(getApplicationContext());
		favoritePhotoHelper = new FavoritePhotoHelper(getApplicationContext());
		adapter = new ItemAdapter(this);
		adapter.setNotifyOnChange(false);
		lvPhotos.setAdapter(adapter);	
		loginAccount = accountHelper.getLoginAccount();	
		lvPhotos.setOnItemClickListener(this);
		
		if(loginAccount == null)
		{
			Intent i = new Intent(this, LoginActivity_.class);
			startActivityForResult(i, LOGIN_CODE);
		}
		else
		{							
	        showAccountInfo();        
	        loadDataProcess();
        }
	}	
	
	private void showAccountInfo(){	
		tvProfileName.setText(loginAccount.getName());
	}
		
	@Background
	public void loadDataProcess() {
		
		JsonResult result = favoritePhotoHelper.getPhotoByAccount(loginAccount.getId());
		
		if (result.getMessage() == JsonMessage.SUCCESSFULL) {		
			ArrayList<Photo> userPhotos = result.toList(Photo.class);
			if(userPhotos != null && userPhotos.size()>0)
				displayOnViews(userPhotos);
			else
				favoritePhotoEmpty();
		} else if (result.getMessage() == JsonMessage.CONNECT_FAILED) {
			showToast(R.string.app_global_global_networkConnectionFailed);
		} else if (result.getMessage() == JsonMessage.RESULT_PARSE_ERROR) {
			showToast(R.string.app_global_global_parseWsResultFailed);
		} else {
			showToast(R.string.app_global_global_parameterWsInvalid);
		}
	}	
	
	@UiThread
	void displayOnViews(ArrayList<Photo> data) {
				
		adapter.setNotifyOnChange(false);
		adapter.clear();		
		for (Photo photo : data) {
			PhotoItem item = new PhotoItem(photo);
			adapter.add(item);
		}		
		adapter.notifyDataSetChanged();		
		accountPhotoNumber.setText(getString(R.string.lb_favorite_photo) + ": " + data.size());
	}
	
	@UiThread
	public void favoritePhotoEmpty(){
		accountPhotoNumber.setText(getString(R.string.msg_favorite_photo_empty));
	}
	@Override
	public void onItemClick(AdapterView<?> adpterView, View view, int position,	long id) {
								
		Intent i = new Intent(this, FavoritePhotoActivity_.class);
		i.putExtra("PhotoPositon", position);				
		startActivityForResult(i, FAVORITE_PHOTO_CODE);
		overridePendingTransition(R.anim.right_in, R.anim.left_out);
	}	
	
	@Override
	public void onRefresh(PullToRefreshBase<GridView> refreshView) {
		loadDataProcess();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent i) {
		if(requestCode == LOGIN_CODE)
		{
			loginAccount = accountHelper.getLoginAccount();
			if(loginAccount!=null)
			{				
				showAccountInfo();
				loadDataProcess();
			}
			else
				finish();
		}
		else if(requestCode == FAVORITE_PHOTO_CODE)
		{
			
			if(i!=null && i.getBooleanExtra("isChange",false))
			{				
				adapter.clear();
				adapter.notifyDataSetChanged();
				loadDataProcess();
			}
		}
	}
	
	@Click(R.id.btn_logout)
	public void logoutClicked()
	{
		accountHelper.setLoginAccount(null);
		Intent i = new Intent(this, MainActivity_.class);
        startActivity(i); 
        finish();
	}
	
	@Click(R.id.btn_update_profile)
	public void updateProfileClicked()
	{
		Intent i = new Intent(this, UpdateProfileActivity_.class);
        startActivityForResult(i, UPDATE_CODE); 
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
	}
	
	@UiThread
	void showToast(final int resId) {
		Toast.makeText(getApplicationContext(), resId,	Toast.LENGTH_LONG).show();
	}
}
