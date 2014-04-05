package com.p2c.solutions.luvphoto;

import greendroid.widget.ItemAdapter;
import greendroid.widget.item.DescriptionItem;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.UiThread;
import com.googlecode.androidannotations.annotations.ViewById;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;
import com.p2c.solutions.luvphoto.adapter.items.PhotoItem;
import com.p2c.solutions.luvphoto.core.models.Album;
import com.p2c.solutions.luvphoto.core.models.Photo;
import com.p2c.solutions.luvphoto.core.webservices.JsonMessage;
import com.p2c.solutions.luvphoto.core.webservices.WebServiceInvoker.JsonResult;
import com.p2c.solutions.luvphoto.helper.PhotoHelper;

@EActivity(R.layout.activity_album)
public class AlbumActivity extends BaseActivity implements OnRefreshListener<GridView>,AdapterView.OnItemClickListener{
	
	@ViewById(R.id.album_phot_listview)
	PullToRefreshGridView lvPhotos;
	
	@ViewById(R.id.appbar_tittle)
	LuvTextView appbarTitle;
	
	@ViewById(R.id.img_album)
	ImageView imgAlbum;
	
	private ItemAdapter adapter;
	PhotoHelper photoHelper;
	
	private static Album album;
	
	@AfterViews
	void afterViews() {
		photoHelper = new PhotoHelper(getApplicationContext());
		adapter = new ItemAdapter(this);
		adapter.setNotifyOnChange(false);
		lvPhotos.setAdapter(adapter);
		lvPhotos.setOnItemClickListener(this);		
		
		Intent i = getIntent();
		
		album = new Album();
		album.setId(i.getIntExtra("AlbumId", 0));
		album.setName(i.getStringExtra("AlbumName"));		
		album.setImage(i.getStringExtra("Image"));
					
		loadDataProcess();		
	}
	
	@UiThread
	void enablePullToRefresh(){
		if(lvPhotos.getMode() == Mode.DISABLED)
			lvPhotos.setMode(Mode.PULL_FROM_START);
	}
	
	@Override
	public void onRefresh(PullToRefreshBase<GridView> refreshView) {
		loadDataProcess();
	}

	@Background
	public void loadDataProcess() {
		
		JsonResult result = photoHelper.getPhotoByAlbum(album.getId());
		
		if (result.getMessage() == JsonMessage.SUCCESSFULL) {		
			ArrayList<Photo> albums = result.toList(Photo.class);
			if(albums != null && albums.size()>0)
				displayOnViews(albums);
			else
				showToast(R.string.msg_list_items_null);
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
		
		if (data != null && data.size() > 0) {
			for (Photo photo : data) {
				PhotoItem item = new PhotoItem(photo);
				adapter.add(item);
			}
		} else {
			DescriptionItem empty = new DescriptionItem(getString(R.string.msg_photo_list_empty));
			adapter.add(empty);
		}
		adapter.notifyDataSetChanged();
		
		appbarTitle.setText(album.getName() + ": " + data.size() + " photos");
		showAlbumImage(album.getImage());
	}	
	
	@Background
	public void showAlbumImage(String src) {
	    try {
	    	
	        URL url = new URL(src);
	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	        connection.setDoInput(true);
	        connection.connect();
	        InputStream input = connection.getInputStream();
	        Bitmap currentPhotoBimap = BitmapFactory.decodeStream(input);	        
	        displayAlbumImage(currentPhotoBimap);	        
	    } catch (IOException e) {
	    	Toast.makeText(getApplicationContext(), "get bitmap error", Toast.LENGTH_LONG).show();
	    }
	}
	@UiThread
	public void displayAlbumImage(Bitmap albumBitmap)
	{
		imgAlbum.setImageBitmap(albumBitmap);		
	}
	
	@Override
	public void onItemClick(AdapterView<?> adpterView, View view, int position,	long id) {
					
		PhotoItem item = (PhotoItem) adapter.getItem(position);
		Photo photo = item.getModel();
		
		Intent i = new Intent(this, PhotoActivity_.class);
		i.putExtra("PhotoPositon", position);
		/*i.putExtra("Id", photo.getId());
		i.putExtra("Name", photo.getName());
		i.putExtra("Path", photo.getPath());*/
		i.putExtra("AlbumId", photo.getAlbumId());
		i.putExtra("AlbumName", album.getName());
		i.putExtra("AlbumImage", album.getImage());
		startActivity(i);
		overridePendingTransition(R.anim.right_in, R.anim.left_out);
	}
	
	@Click(R.id.btn_profile)
	public void profileClicked(){
		Intent i = new Intent(AlbumActivity.this, AccountActivity_.class);
		startActivity(i);
	}
	
	@UiThread
	void showToast(final int resId) {
		Toast.makeText(getApplicationContext(), resId,	Toast.LENGTH_LONG).show();
	}
}
