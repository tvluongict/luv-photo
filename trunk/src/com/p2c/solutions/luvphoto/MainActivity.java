package com.p2c.solutions.luvphoto;

import greendroid.widget.ItemAdapter;
import greendroid.widget.item.DescriptionItem;
import java.util.ArrayList;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.UiThread;
import com.googlecode.androidannotations.annotations.ViewById;
import com.p2c.solutions.luvphoto.adapter.items.AlbumItem;
import com.p2c.solutions.luvphoto.core.models.Album;
import com.p2c.solutions.luvphoto.core.webservices.JsonMessage;
import com.p2c.solutions.luvphoto.core.webservices.WebServiceInvoker.JsonResult;
import com.p2c.solutions.luvphoto.helper.AccountHelper;
import com.p2c.solutions.luvphoto.helper.AlbumHelper;
import com.p2c.solutions.luvphoto.services.LuvPhotoNotificationService;

@EActivity(R.layout.activity_main)
public class MainActivity extends BaseActivity {

	private int LOGIN_CODE=1000;
	
	@ViewById(R.id.lv_album)
	LuvHorizontalListView lvAlbum;
	
	@ViewById(R.id.btn_login)
	LuvTextView btnLogin;
	
	@ViewById(R.id.panel_refresh)
	LinearLayout panelRefresh;
	
	@ViewById(R.id.btn_refresh)
	LuvTextView btnRefresh;
	
	private ItemAdapter adapter;
	AlbumHelper albumHelper;
		
	@ViewById(R.id.adView)
	AdView adView;
		
	
	@AfterViews
	void afterViews() {			
		
		loginAccount = new AccountHelper(getApplicationContext()).getLoginAccount();				
				
		albumHelper = new AlbumHelper(getApplicationContext());
		adapter = new ItemAdapter(this);
		
		adapter.setNotifyOnChange(false);
		lvAlbum.setAdapter(adapter);	
		loadDataProcess();
		
		lvAlbum.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int position,long id) {
				
				setItemStatus(false);
				
				AlbumItem item = (AlbumItem) adapter.getItemAtPosition(position);
				Album album = item.getModel();
				
				Intent i = new Intent(MainActivity.this, AlbumActivity_.class);
				i.putExtra("AlbumId", album.getId());
				i.putExtra("AlbumName", album.getName());
				i.putExtra("Image", album.getImage());
				startActivity(i);
			}

		});
		
		Intent intent = new Intent(this, LuvPhotoNotificationService.class);
        startService(intent);
        
        adView.loadAd(new AdRequest());
	}
	
		
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    return super.onCreateOptionsMenu(menu);
	}
		
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.action_refresh:
	            return true;
	        case R.id.action_setting:
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	@Background
	public void loadDataProcess() {
		
		JsonResult result = albumHelper.getAlbums();
		
		if (result.getMessage() == JsonMessage.SUCCESSFULL) {						
						
			ArrayList<Album> albums  = result.toList(Album.class);
			if(albums != null && albums.size()>0)
				displayOnViews(albums);
			else
				loadDataProcessFail(R.string.msg_list_album_null);
			
		} else if (result.getMessage() == JsonMessage.CONNECT_FAILED) {
			loadDataProcessFail(R.string.app_global_global_networkConnectionFailed);
		} else if (result.getMessage() == JsonMessage.RESULT_PARSE_ERROR) {
			loadDataProcessFail(R.string.app_global_global_parseWsResultFailed);
		} else {
			loadDataProcessFail(R.string.app_global_global_parameterWsInvalid);
		}
	}
	
	@UiThread
	public void loadDataProcessFail(int id){
		showToast(id);
		
		Animation animation = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_in_from_bottom);
		panelRefresh.setVisibility(View.VISIBLE);	
		panelRefresh.startAnimation(animation);
		
		btnLogin.setVisibility(View.GONE);
	}
	
	@UiThread
	void displayOnViews(ArrayList<Album> data) {
		btnLogin.setVisibility(View.VISIBLE);
		adapter.setNotifyOnChange(false);
		adapter.clear();
		
		if (data != null && data.size() > 0) {
			for (Album album : data) {
				AlbumItem item = new AlbumItem(album);
				adapter.add(item);
			}
		} else {
			DescriptionItem empty = new DescriptionItem(getString(R.string.msg_photo_list_empty));
			adapter.add(empty);
		}
		adapter.notifyDataSetChanged();
		
		if(loginAccount!=null)
		{				
			btnLogin.setText("Hello: " + loginAccount.getName());
		}
		else
		{
			btnLogin.setText(getString(R.string.lb_login));
		}
		
		if(panelRefresh.getVisibility() == View.VISIBLE)
		{
			Animation animation = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_out_to_bottom);			
			panelRefresh.setVisibility(View.GONE);
			panelRefresh.startAnimation(animation);
		}
	}
		
	
	@Override
	protected void onResume() {
	    super.onResume();
	    
	    loginAccount = new AccountHelper(getApplicationContext()).getLoginAccount();
		if(loginAccount!=null)
		{				
			btnLogin.setText("Hello: " + loginAccount.getName());
		}
		else
		{
			btnLogin.setText(getString(R.string.lb_login));
		}
		
		setItemStatus(true);
	}
	
	@Override
	protected void onDestroy() {
		if (adView != null) {
	      adView.destroy();
	    }
		super.onDestroy();
	}
	
	@UiThread
	void showToast(final int resId) {
		Toast.makeText(getApplicationContext(), resId,	Toast.LENGTH_LONG).show();		
	}	
	@Click(R.id.btn_login)
	public void loginClicked(){
		
		setItemStatus(false);
		
		if(loginAccount!=null)
		{
			Intent i = new Intent(MainActivity.this, AccountActivity_.class);
			startActivity(i);
		}
		else
		{
			Intent i = new Intent(MainActivity.this, LoginActivity_.class);
			startActivityForResult(i, LOGIN_CODE);
		}
	}
	
	@Click(R.id.btn_refresh)
	public void refreshClicked(){		
		loadDataProcess();		
	}
	
	private void setItemStatus(Boolean status){
		btnLogin.setEnabled(status);
		lvAlbum.setEnabled(status);
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if(requestCode == LOGIN_CODE)
		{
			loginAccount = new AccountHelper(getApplicationContext()).getLoginAccount();
			if(loginAccount!=null)
			{				
				btnLogin.setText("Hello: " + loginAccount.getName());
			}
			else
			{
				btnLogin.setText(getString(R.string.lb_login));
			}
		}
	}
}
