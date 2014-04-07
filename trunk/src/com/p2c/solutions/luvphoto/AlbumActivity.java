package com.p2c.solutions.luvphoto;

import greendroid.widget.ItemAdapter;
import greendroid.widget.item.DescriptionItem;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import android.app.Dialog;
import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.facebook.FacebookException;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
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
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.p2c.solutions.luvphoto.adapter.items.PhotoItem;
import com.p2c.solutions.luvphoto.core.models.Album;
import com.p2c.solutions.luvphoto.core.models.FavoritePhoto;
import com.p2c.solutions.luvphoto.core.models.Photo;
import com.p2c.solutions.luvphoto.core.webservices.JsonMessage;
import com.p2c.solutions.luvphoto.core.webservices.WebServiceInvoker.JsonResult;
import com.p2c.solutions.luvphoto.helper.AccountHelper;
import com.p2c.solutions.luvphoto.helper.FavoritePhotoHelper;
import com.p2c.solutions.luvphoto.helper.PhotoHelper;

@EActivity(R.layout.activity_album)
public class AlbumActivity extends BaseActivity implements OnRefreshListener<GridView>,AdapterView.OnItemClickListener,SensorEventListener{
	
	private int FAVORITE_CODE = 1000;
	private static final String STATE_POSITION = "STATE_POSITION";
	
	@ViewById(R.id.album_phot_listview)
	PullToRefreshGridView lvPhotos;
	
	@ViewById(R.id.appbar_tittle)
	LuvTextView appbarTitle;
	
	@ViewById(R.id.img_album)
	ImageView imgAlbum;
	
	@ViewById(R.id.navbar_option)
	LinearLayout navOption;
	
	@ViewById(R.id.btn_favorite)
	ImageView btnFavorite;
	@ViewById(R.id.btn_facebook)
	ImageView btnFacebook;
	@ViewById(R.id.btn_wallpaper)
	ImageView btnWallpaper;
	@ViewById(R.id.btn_download)
	ImageView btnDownload;
	
	private ItemAdapter adapter;
	PhotoHelper photoHelper;
	FavoritePhotoHelper favoritePhotoHelper;
	private FavoritePhoto favoritePhoto;
	private static Album album;
	
	@ViewById(R.id.pager)
	ViewPager pager;
	
	DisplayImageOptions options;
	private int pagerPosition;
	private static Bitmap currentPhotoBimap;	
	private ArrayList<Photo> photos;
	
	StringBuilder builder = new StringBuilder();
	private Photo currentPhoto;
	
	////////////////////////////////////////////////////////////////
	//Sensor
	private SensorManager sensorManager;
	private long lastUpdate;
	
	
	@AfterViews
	void afterViews() {
		
		photoHelper = new PhotoHelper(getApplicationContext());	
		loginAccount = new AccountHelper(getApplicationContext()).getLoginAccount();
		favoritePhotoHelper = new FavoritePhotoHelper(getApplicationContext());
					
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
	    lastUpdate = System.currentTimeMillis();
	    
		//OPTION FOR IMAGE LOADER
  		options = new DisplayImageOptions.Builder()
  			.showImageForEmptyUri(R.drawable.ic_empty)
  			.showImageOnFail(R.drawable.ic_error)
  			.resetViewBeforeLoading(true)
  			.cacheOnDisc(true)
  			.bitmapConfig(Bitmap.Config.RGB_565)
  			.considerExifParams(true)
  			.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
  			.displayer(new FadeInBitmapDisplayer(300))			
  			.build();		
		
		adapter = new ItemAdapter(this);
		adapter.setNotifyOnChange(false);
		lvPhotos.setAdapter(adapter);
		lvPhotos.setOnItemClickListener(this);		
		
		Intent i = getIntent();
		
		album = new Album();
		album.setId(i.getIntExtra("AlbumId", 0));
		album.setName(i.getStringExtra("AlbumName"));		
		album.setImage("Image");
					
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
			photos = result.toList(Photo.class);
			if(photos != null && photos.size()>0)
				displayOnViews(photos);
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
		
		///Pager
		String[] IMAGES = new String[photos.size()];
		
		for (int i=0; i< photos.size(); i++) {
			IMAGES[i] = photos.get(i).getPath();					
		}		
					
		ImagePagerAdapter imagePagerAdapter = new ImagePagerAdapter(IMAGES);
		pager.setAdapter(imagePagerAdapter);
		pager.setCurrentItem(pagerPosition);	
		pager.setOnPageChangeListener(imagePagerAdapter);
		
		checkFavoritePhotoProcess(photos.get(pagerPosition));
	}	
	
	@Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_NORMAL);
        
        setImage(btnFacebook, R.drawable.facebook);
        setImage(btnDownload, R.drawable.download);
        setImage(btnWallpaper, R.drawable.wallpaper);
        setImage(btnFavorite, R.drawable.heart);
    }
    
    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    ////////////////////////////////////////////////////////////////////////////
    
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
	
	
	///////////////////////////////////////////////////////////////////////////
	
	
	//CHECK PHOTO HIEN TAI CO PHAI LA FAVORICE PHOTO CUA USER KO
	@Background
	public void checkFavoritePhotoProcess(Photo photo){
	
		if(loginAccount != null)
		{
			favoritePhotoHelper = new FavoritePhotoHelper(getApplicationContext());			
			JsonResult result = favoritePhotoHelper.checkFavoritePhoto(photo.getId(),loginAccount.getId());			
			if (result.getMessage() == JsonMessage.SUCCESSFULL) {
				
				favoritePhoto = result.parse(FavoritePhoto.class);
				if(favoritePhoto != null)
				{
					isFavoritePhoto();
				}
				else
				{
					isNotFavoritePhoto();
				}
	  		} else if (result.getMessage() == JsonMessage.CONNECT_FAILED) {
				showToast(R.string.app_global_global_networkConnectionFailed);
			} else if (result.getMessage() == JsonMessage.RESULT_PARSE_ERROR) {
				showToast(R.string.app_global_global_parseWsResultFailed);
			} else {
				showToast(R.string.app_global_global_parameterWsInvalid);
			}
		}
	}
		
	@UiThread
	public void isFavoritePhoto(){
		btnFavorite.setImageResource(R.drawable.heart_focus);
	}
	
	@UiThread
	public void isNotFavoritePhoto(){
		btnFavorite.setImageResource(R.drawable.heart);
	}
	
	@Background
	public void getBitmapFromURL(String src) {
	    try {	    	
	    	
	        URL url = new URL(src);
	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	        connection.setDoInput(true);
	        connection.connect();
	        InputStream input = connection.getInputStream();
	        currentPhotoBimap = BitmapFactory.decodeStream(input);	        
	        Toast.makeText(getApplicationContext(), "Get bitmap from " + src, Toast.LENGTH_LONG).show();
	    } catch (IOException e) {
	    	Toast.makeText(getApplicationContext(), "get bitmap error", Toast.LENGTH_LONG).show();
	    }
	}
				
	@Background
	public void newFavoritePhotoProcess(int photoId, int accountId)
	{
		JsonResult result = favoritePhotoHelper.newFavoritePhoto(currentPhoto.getId(), loginAccount.getId());
		
		if(result.getMessage() == JsonMessage.SUCCESSFULL)
		{		
			favoritePhoto = result.parse(FavoritePhoto.class);
			if(favoritePhoto.getId()>0)
				newFavoritePhotoSuccess();
			else
				showToast(R.string.msg_error);		
		
		} else if (result.getMessage() == JsonMessage.CONNECT_FAILED) {			
			showToast(R.string.app_global_global_networkConnectionFailed);
			setImage(btnFavorite, R.drawable.heart);
		} else if (result.getMessage() == JsonMessage.RESULT_PARSE_ERROR) {			
			showToast(R.string.app_global_global_parseWsResultFailed);
			setImage(btnFavorite, R.drawable.heart);
		} else {			
			showToast(R.string.app_global_global_parameterWsInvalid);	
			setImage(btnFavorite, R.drawable.heart);
		}
	}
		
	@Background
	public void removeFavoritePhotoProcess(){
		
		FavoritePhotoHelper favoritePhotoHelper = new FavoritePhotoHelper(getApplicationContext());
		JsonResult result = favoritePhotoHelper.deleteFavoritePhoto(favoritePhoto.getId(),loginAccount.getId());
		
		if(result.getMessage() == JsonMessage.SUCCESSFULL)
		{
			removeFavoritePhotoSuccess();
		
		} else if (result.getMessage() == JsonMessage.CONNECT_FAILED) {
			showToast(R.string.app_global_global_networkConnectionFailed);
			setImage(btnFavorite, R.drawable.heart_focus);
		} else if (result.getMessage() == JsonMessage.RESULT_PARSE_ERROR) {
			showToast(R.string.app_global_global_parseWsResultFailed);
			setImage(btnFavorite, R.drawable.heart_focus);
		} else {
			showToast(R.string.app_global_global_parameterWsInvalid);
			setImage(btnFavorite, R.drawable.heart_focus);
		}	
		
	}
		
	@UiThread
	public void newFavoritePhotoSuccess()
	{		
		showToast(R.string.msg_add_favorite_success);
		isFavoritePhoto();
		
		setImage(btnFavorite, R.drawable.heart_focus);
	}
	
	@UiThread
	public void removeFavoritePhotoSuccess(){
		favoritePhoto = null;
		isNotFavoritePhoto();		
	}
				
	//SHOW DIALOG WHEN USER CLICK "ADD PHOTO FAVORITE"
	
	@Click(R.id.btn_facebook)
	public void facebookClicked()
	{		
		setImage(btnFacebook, R.drawable.loadingwhite);
		currentPhoto = photos.get(pagerPosition);
		if(currentPhoto!=null)
		{
			Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
			sharingIntent.setType("text/plain");
			String shareBody = currentPhoto.getPath();
			sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
			sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
			startActivity(Intent.createChooser(sharingIntent, "Share via"));
			setImage(btnFacebook, R.drawable.facebook);
		} 
	}
	
	@SuppressWarnings("unused")
	private void publishFeedDialog() {
	    Bundle params = new Bundle();
	    params.putString("name", "Facebook SDK for Android");
	    params.putString("caption", "Build great social apps and get more installs.");
	    params.putString("description", "The Facebook SDK for Android makes it easier and faster to develop Facebook integrated Android apps.");
	    params.putString("link", "https://developers.facebook.com/android");
	    params.putString("picture", "https://raw.github.com/fbsamples/ios-3.x-howtos/master/Images/iossdk_logo.png");

	    WebDialog feedDialog = (
	        new WebDialog.FeedDialogBuilder(AlbumActivity.this,
	            Session.getActiveSession(),
	            params))
	        .setOnCompleteListener(new OnCompleteListener() {

	            @Override
	            public void onComplete(Bundle values,
	                FacebookException error) {
	                if (error == null) {
	                    // When the story is posted, echo the success
	                    // and the post Id.
	                    final String postId = values.getString("post_id");
	                    if (postId != null) {
	                        Toast.makeText(AlbumActivity.this,
	                            "Posted story, id: "+postId,
	                            Toast.LENGTH_SHORT).show();
	                    } else {
	                        // User clicked the Cancel button
	                        Toast.makeText(AlbumActivity.this.getApplicationContext(), 
	                            "Publish cancelled", 
	                            Toast.LENGTH_SHORT).show();
	                    }	                
	                } else {	                    
	                    Toast.makeText(AlbumActivity.this.getApplicationContext(), 
	                        "Error posting story", 
	                        Toast.LENGTH_SHORT).show();
	                }
	            }

	        })
	        .build();
	    feedDialog.show();
	}
		public void showShareFacebookDialog()
		{
			// custom dialog
			final Dialog dialog = new Dialog(this,R.style.FullHeightDialog);
			dialog.setContentView(R.layout.luv_share_fb_dialog);
	 
			// set the custom dialog components - text, image and button
			LuvTextView tvTitle = (LuvTextView) dialog.findViewById(R.id.tvTitle);
			final LuvEditText txtMessage = (LuvEditText) dialog.findViewById(R.id.txtMessage);		
			tvTitle.setText("Share photo on Faccebook");
			
			Button dialogButtonOk = (Button) dialog.findViewById(R.id.dialogButtonOK);
			dialogButtonOk.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {					
						dialog.dismiss();										
						Session session = Session.getActiveSession();		
				        if (session != null & session.isOpened()) {			        			        	
				        	uploadPhotoToFacebook(session,currentPhotoBimap,txtMessage.getText().toString());			        	
				        } else {
				            authorizeFacenook();
				        }			        
					}
				});
			
			Button dialogButtonCancel = (Button) dialog.findViewById(R.id.dialogButtonCancel);
			dialogButtonCancel.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
			
			dialog.show();
		}	
		public void authorizeFacenook(){
			//Session.openActiveSession(this, true, callback);
		}
		public void uploadPhotoToFacebook(Session session, final Bitmap photoToPost, final String message) {    
			if(photoToPost!=null)
		    {
				List<String> PERMISSIONS = Arrays.asList("publish_actions");
		        session.requestNewPublishPermissions(new Session.NewPermissionsRequest(AlbumActivity.this, PERMISSIONS));
		        
		    	Request request = Request.newUploadPhotoRequest(session, photoToPost, new Request.Callback() {
				
		    		@Override
		    		public void onCompleted(Response response) {
						Toast.makeText(getApplicationContext(), "Photo have been shared to facebook.", Toast.LENGTH_LONG).show();
					}
		    	});	    
		    		    	
		    	Bundle params = request.getParameters();
				params.putString("message", message);
		    	
				request.setParameters(params);
		    	request.executeAsync();
		    }		
		} 
		public class SessionStatusCallback implements Session.StatusCallback {
		    @Override
		    public void call(Session session, SessionState state, Exception exception) {        	
		        showToast(R.string.app_global_global_data_error);
		    }
		}
		//SHOW DIALOG WHEN USER CLICK "ADD PHOTO FAVORITE"
		
		@Click(R.id.btn_favorite)
		public void favoriteClicked()
		{		
			setImage(btnFavorite, R.drawable.loadingwhite);
			currentPhoto = photos.get(pagerPosition);
			if(currentPhoto!=null)
			{
				if(loginAccount != null)
				{
					btnFavorite.setImageResource(R.drawable.loadingwhite);
					
					//IF THIS PHOTO WAS IN FAVORITEPHOTO
					//SHOW REMOVE PHOTO FROM FAVORITE CONFIRM DIALOG
					if(favoritePhoto != null)
						removeFavoritePhotoProcess();
					else
						newFavoritePhotoProcess(currentPhoto.getId(), loginAccount.getId());			
				}
				else
				{
					//IF NOT LOGIN : SHOW CONFIRM LOGIN DIALOGIG		
					showLoginConfirmLoginDialog();
				}
			}
		}
		public void showLoginConfirmLoginDialog()
		{
			// custom dialog
			final Dialog dialog = new Dialog(this,R.style.FullHeightDialog);
			dialog.setContentView(R.layout.luv_confirm_dialog);
	 
			// set the custom dialog components - text, image and button
			LuvTextView tvTitle = (LuvTextView) dialog.findViewById(R.id.tvTitle);
			LuvTextView tvContent = (LuvTextView) dialog.findViewById(R.id.tvContent);	
			tvTitle.setText("Notice");
			tvContent.setText("You not login. Do you want login to do this?");	
			
			Button dialogButtonOk = (Button) dialog.findViewById(R.id.dialogButtonOK);
			dialogButtonOk.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {					
						dialog.dismiss();					
						Intent i = new Intent(AlbumActivity.this, LoginActivity_.class);
						startActivityForResult(i, FAVORITE_CODE);
					}
				});
			
			Button dialogButtonCancel = (Button) dialog.findViewById(R.id.dialogButtonCancel);
			dialogButtonCancel.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
						setImage(btnFavorite, R.drawable.heart);
					}
				});
			
			dialog.show();
		}
		//SHOW DIALOG WHEN USER CLICK "ADD PHOTO FAVORITE"
		public void showRemoveFavoriteConfirmDialog()
		{
			// custom dialog
			final Dialog dialog = new Dialog(this,R.style.FullHeightDialog);
			dialog.setContentView(R.layout.luv_confirm_dialog);
	 
			// set the custom dialog components - text, image and button
			LuvTextView tvTitle = (LuvTextView) dialog.findViewById(R.id.tvTitle);
			LuvTextView tvContent = (LuvTextView) dialog.findViewById(R.id.tvContent);
			
			tvTitle.setText("Notice");
			tvContent.setText("Do you want remove photo from your favorite photos?");
					
			Button dialogButtonOk = (Button) dialog.findViewById(R.id.dialogButtonOK);
			dialogButtonOk.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {					
						dialog.dismiss();
						removeFavoritePhotoProcess();					
					}
				});
			
			Button dialogButtonCancel = (Button) dialog.findViewById(R.id.dialogButtonCancel);
			dialogButtonCancel.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
			
			dialog.show();
		}		

		//SHOW DIALOG WHEN USER CLICK DOWNLOAD PHOTO
		@Click(R.id.btn_download)
		public void downloadClicked()
		{	
			setImage(btnDownload, R.drawable.loadingwhite);
			currentPhoto = photos.get(pagerPosition);
			if(currentPhoto!=null)
			{			
				showDownloadConfirmDialog();
			}
		}
		public void showDownloadConfirmDialog()
		{
			// custom dialog
			final Dialog dialog = new Dialog(this,R.style.FullHeightDialog);
			dialog.setContentView(R.layout.luv_confirm_dialog);
	 
			// set the custom dialog components - text, image and button
			LuvTextView tvTitle = (LuvTextView) dialog.findViewById(R.id.tvTitle);
			LuvTextView tvContent = (LuvTextView) dialog.findViewById(R.id.tvContent);		
			tvTitle.setText("Download Photo");
			tvContent.setText("Do you want download this photo?");	
			
			Button dialogButtonOk = (Button) dialog.findViewById(R.id.dialogButtonOK);
			dialogButtonOk.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {					
						dialog.dismiss();
						if(currentPhotoBimap!=null)
						{
							MediaStore.Images.Media.insertImage(getContentResolver(), currentPhotoBimap, currentPhoto.getName() , getString(R.string.app_name));
							Toast.makeText(getApplicationContext(), getString(R.string.msg_download_photo_success), Toast.LENGTH_LONG).show();
							btnDownload.setImageResource(R.drawable.download);
							setImage(btnDownload, R.drawable.download);
						}
						else
						{
							DownloadExecute(photos.get(pagerPosition).getPath());
						}
					}
				});		
			Button dialogButtonCancel = (Button) dialog.findViewById(R.id.dialogButtonCancel);
			dialogButtonCancel.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
						setImage(btnDownload, R.drawable.download);
					}
				});
			
			dialog.show();
		}
		@Background
		public void DownloadExecute(String src)
		{
			try {	    	
		        URL url = new URL(src);
		        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		        connection.setDoInput(true);
		        connection.connect();
		        InputStream input = connection.getInputStream();
		        currentPhotoBimap = BitmapFactory.decodeStream(input);
		        DownloadFinish();
		    } catch (IOException e) {
		    	DownloadError();
		    }
		}
		@UiThread
		public void DownloadFinish()
		{
			MediaStore.Images.Media.insertImage(getContentResolver(), currentPhotoBimap, currentPhoto.getName() , getString(R.string.app_name));
			Toast.makeText(getApplicationContext(), getString(R.string.msg_download_photo_success), Toast.LENGTH_LONG).show();
			setImage(btnDownload, R.drawable.download);
		}
		@UiThread
		public void DownloadError()
		{
			Toast.makeText(getApplicationContext(), getString(R.string.msg_download_photo_unsuccess), Toast.LENGTH_LONG).show();
			setImage(btnDownload, R.drawable.download);
		}
		
		//SHOW DIALOG WHEN USER CLICK DOWNLOAD PHOTO
		@Click(R.id.btn_wallpaper)
		public void wallpaperClicked()
		{		
			setImage(btnWallpaper, R.drawable.loadingwhite);
			currentPhoto = photos.get(pagerPosition);
			if(currentPhoto != null)
			{
				btnWallpaper.setImageResource(R.drawable.loadingwhite);
				showSetWallPaperConfirmDialog();			
			}
		}
		public void showSetWallPaperConfirmDialog()
		{
			// custom dialog
			final Dialog dialog = new Dialog(this,R.style.FullHeightDialog);
			dialog.setContentView(R.layout.luv_confirm_dialog);
	 
			// set the custom dialog components - text, image and button
			LuvTextView tvTitle = (LuvTextView) dialog.findViewById(R.id.tvTitle);
			LuvTextView tvContent = (LuvTextView) dialog.findViewById(R.id.tvContent);		
			tvTitle.setText("Set Photo Wallpaper");
			tvContent.setText("Do you want set photo to home screen?");	
			
			Button dialogButtonOk = (Button) dialog.findViewById(R.id.dialogButtonOK);
			dialogButtonOk.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						
						dialog.dismiss();
						if(currentPhotoBimap!=null)
						{
							try{
								WallpaperManager wallpaperManager = WallpaperManager.getInstance(AlbumActivity.this);
								wallpaperManager.setBitmap(currentPhotoBimap);
								Toast.makeText(getApplicationContext(), getString(R.string.msg_set_wallpaper_unsuccess), Toast.LENGTH_LONG).show();							
							}catch(Exception ex){
								Toast.makeText(getApplicationContext(), getString(R.string.msg_set_wallpaper_unsuccess) + ex.getMessage(), Toast.LENGTH_LONG).show();						
							}
							
							setImage(btnWallpaper, R.drawable.wallpaper);
						}
						else
						{
							setWallpaperExecute(currentPhoto.getPath());
						}
					}
				});
			
			Button dialogButtonCancel = (Button) dialog.findViewById(R.id.dialogButtonCancel);
			dialogButtonCancel.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
						setImage(btnWallpaper, R.drawable.wallpaper);
					}
				});
			
			dialog.show();
		}
		@Background
		public void setWallpaperExecute(String src)
		{
			try {	    	
		        URL url = new URL(src);
		        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		        connection.setDoInput(true);
		        connection.connect();
		        InputStream input = connection.getInputStream();
		        currentPhotoBimap = BitmapFactory.decodeStream(input);
		        setwallpaperFinish();
		    } catch (IOException e) {
		    	setWallpaperError();
		    }
		}
		@UiThread
		public void setwallpaperFinish(){
			try{
				WallpaperManager wallpaperManager = WallpaperManager.getInstance(AlbumActivity.this);
				wallpaperManager.setBitmap(currentPhotoBimap);
				Toast.makeText(getApplicationContext(), getString(R.string.msg_set_wallpaper_success), Toast.LENGTH_LONG).show();
			}catch(Exception ex){
				Toast.makeText(getApplicationContext(), getString(R.string.msg_set_wallpaper_unsuccess) + ex.getMessage(), Toast.LENGTH_LONG).show();
			}
			
			setImage(btnWallpaper, R.drawable.wallpaper);
			
		}
		@UiThread
		public void setWallpaperError(){
			Toast.makeText(getApplicationContext(), getString(R.string.msg_set_wallpaper_unsuccess), Toast.LENGTH_LONG).show();
			setImage(btnWallpaper, R.drawable.wallpaper);
		}
		
		@UiThread
		void setImage(ImageView img,int id)
		{
			img.setImageResource(id);
			if(id == R.drawable.loadingwhite || id == R.drawable.loading)
				img.setEnabled(false);
			else
				img.setEnabled(true);
		}
		
		
		@Override
		protected void onActivityResult(int requestCode, int resultCode, Intent data) {		
			if(requestCode == FAVORITE_CODE)
			{
				loginAccount = new AccountHelper(getApplicationContext()).getLoginAccount();			
				checkFavoritePhotoProcess(photos.get(pagerPosition));
				btnFavorite.setEnabled(true);			
			}
		}
		
		@Override
	    protected void onSaveInstanceState(Bundle outState) {
	        super.onSaveInstanceState(outState);
	        Session session = Session.getActiveSession();
	        Session.saveSession(session, outState);
	        outState.putInt(STATE_POSITION, pager.getCurrentItem());
	    }
			
	
	
	////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void onItemClick(AdapterView<?> adpterView, View view, int position,	long id) {
		
		//View : la 1 hinh vuong trong grid					
		pager.setCurrentItem(position);
		pager.setVisibility(View.VISIBLE);
		
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
	
	@Override
	public void onBackPressed()
	{
	    if(navOption.getVisibility() == View.VISIBLE)
	    	navOption.setVisibility(View.GONE);
	    else if(pager.getVisibility() == View.VISIBLE)
	    	pager.setVisibility(View.GONE);
	    else
	    	super.onBackPressed();
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_MENU) {
	        
	    	if(pager.getVisibility() == View.VISIBLE)
	    	{
		    	if(navOption.getVisibility() == View.VISIBLE)
			    	navOption.setVisibility(View.GONE);
		    	else
		    		navOption.setVisibility(View.VISIBLE);
	    	}	        
	    }
	    
	    return super.onKeyUp(keyCode, event);
	}
	

	
	///////////////////////////////////////////////////////////////////////////////////
	
	

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
				
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
		      getAccelerometer(event);		    
		}		
	}
	
	private void getAccelerometer(SensorEvent event) {
	    
		long actualTime = System.currentTimeMillis();
		if (actualTime - lastUpdate < 200)
    		return;
		
		float[] values = event.values;
		
		// Movement
	    float x = values[0];
	    float y = values[1];
	    float z = values[2];
	    
	    float accelationSquareRoot = (x * x + y * y + z * z) / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);   
	    
	    if (accelationSquareRoot >= 2)
	    {	      
	    	lastUpdate = actualTime;
	    	pager.setCurrentItem(pagerPosition+1);
	    }	    
	}

	public static float Round(float Rval, int Rpl) {
        float p = (float)Math.pow(10,Rpl);
        Rval = Rval * p;
        float tmp = Math.round(Rval);
        return (float)tmp/p;
    }
	
	
	////////////////////////////////////////////////////////////////////////
	
	private class ImagePagerAdapter extends PagerAdapter implements ViewPager.OnPageChangeListener{

		private String[] images;
		private LayoutInflater inflater;

		ImagePagerAdapter(String[] images) {
			this.images = images;
			inflater = getLayoutInflater();
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public int getCount() {
			return images.length;
		}
		
		@Override
		public Object instantiateItem(ViewGroup view, int position) {
			
			View imageLayout = inflater.inflate(R.layout.item_pager_image, view, false);			
			assert imageLayout != null;
			
			TouchImageView imageView = (TouchImageView) imageLayout.findViewById(R.id.image);
			imageView.setViewPager(pager);
			imageView.setNavOption(navOption);
			
			imageView.setOnClickListener(new OnClickListener() {
	            @Override
	            public void onClick(View v) {	            	
	            	         	
	            	if(navOption.getVisibility() == View.GONE)
	            	{	     
	            		Animation animation = AnimationUtils.loadAnimation(AlbumActivity.this, R.anim.bottom_in);
	            		navOption.startAnimation(animation);
	            		navOption.setVisibility(View.VISIBLE);
	            	}
	            	else
	            	{
	            		navOption.setVisibility(View.GONE);
	            	}
	            }
	        });			
			
			final ProgressBar spinner = (ProgressBar) imageLayout.findViewById(R.id.loading);
			imageLoader.displayImage(images[position], imageView, options, new SimpleImageLoadingListener() {
				
				@Override
				public void onLoadingStarted(String imageUri, View view) {
					spinner.setVisibility(View.VISIBLE);
				}

				@Override
				public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
					String message = null;
					switch (failReason.getType()) {
						case IO_ERROR:
							message = "Input/Output error";
							break;
						case DECODING_ERROR:
							message = "Image can't be decoded";
							break;
						case NETWORK_DENIED:
							message = "Downloads are denied";
							break;
						case OUT_OF_MEMORY:
							message = "Out Of Memory error";
							break;
						case UNKNOWN:
							message = "Unknown error";
							break;
					}
					
					Toast.makeText(AlbumActivity.this, message, Toast.LENGTH_SHORT).show();
					spinner.setVisibility(View.GONE);
				}

				@Override
				public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
					spinner.setVisibility(View.GONE);
				}
			});
			
			view.addView(imageLayout, 0);
			return imageLayout;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view.equals(object);
		}

		@Override
		public void restoreState(Parcelable state, ClassLoader loader) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void onPageScrollStateChanged(int position) {
			navOption.setVisibility(View.GONE);
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			
		}
		
		@Override
		public void onPageSelected(int position) {
			
			pagerPosition  = position;
			checkFavoritePhotoProcess(photos.get(pagerPosition));
			currentPhoto = null;
			currentPhotoBimap = null;
		}
	}	
}
