package com.p2c.solutions.luvphoto;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.UiThread;
import com.googlecode.androidannotations.annotations.ViewById;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.p2c.solutions.luvphoto.core.models.Photo;
import com.p2c.solutions.luvphoto.core.webservices.JsonMessage;
import com.p2c.solutions.luvphoto.core.webservices.WebServiceInvoker.JsonResult;
import com.p2c.solutions.luvphoto.helper.AccountHelper;
import com.p2c.solutions.luvphoto.helper.FavoritePhotoHelper;
import com.p2c.solutions.luvphoto.helper.PhotoHelper;

@EActivity(R.layout.activity_favorite_photo)
public class FavoritePhotoActivity extends BaseActivity implements SensorEventListener{

	private static final String STATE_POSITION = "STATE_POSITION";
	
	DisplayImageOptions options;
	private int pagerPosition;	
	PhotoHelper photoHelper;
	
	private Photo currentPhoto;
	
	@ViewById(R.id.navbar_option)
	LinearLayout navbarOption;
	
	@ViewById(R.id.pager)
	ViewPager pager;
	
	@ViewById(R.id.btn_delete)
	ImageView btnDelete;
	
	@ViewById(R.id.btn_download)
	ImageView btnDownload;
	
	@ViewById(R.id.btn_wallpaper)
	ImageView btnWallpaper;
	
	@ViewById(R.id.btn_facebook)
	ImageView btnFacebook;
	
	@ViewById(R.id.img_album)
	ImageView imgAlbum;
	
	@ViewById(R.id.appbar_tittle)
	LuvTextView appbarTitle;
	
	private static Bitmap currentPhotoBimap;	
	public Session.StatusCallback statusCallback = new SessionStatusCallback();
		
	private ArrayList<Photo> photos;	
	FavoritePhotoHelper favoritePhotoHelper;
		
	private boolean isChange;
	
	//Sensor
	private SensorManager sensorManager;
	private long lastUpdate;
	StringBuilder builder = new StringBuilder();
	private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 800;
    
	@AfterViews
	void afterViews()
	{		
		photoHelper = new PhotoHelper(getApplicationContext());		
		favoritePhotoHelper = new FavoritePhotoHelper(getApplicationContext());
		loginAccount = new AccountHelper(getApplicationContext()).getLoginAccount();
		
		
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
	    lastUpdate = System.currentTimeMillis();
	    
	    
		//OPTION FOR IMAGE LOADER
		options = new DisplayImageOptions.Builder()
			.showImageForEmptyUri(R.drawable.ic_empty)
			.showImageOnFail(R.drawable.ic_error)
			.resetViewBeforeLoading(true)
			.cacheOnDisc(true)
			.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.considerExifParams(true)
			.displayer(new FadeInBitmapDisplayer(300))
			.build();		
		
		//GET INFO FROM ALBUM NAVIGATE TO
		getInfo();
		loadDataProcess();
		
	}
	
	//GET THONG TIN PHOTO TU INTENT
	private void getInfo(){		
		Intent i = getIntent();		
		pagerPosition = i.getIntExtra("PhotoPositon", 0);
	}
	
	@Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_NORMAL);
    }
    
    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }
    
	//GET DANH SACH PHOTOS TU WEBSERVICES
	@Background
	public void loadDataProcess(){
		
		JsonResult result = favoritePhotoHelper.getPhotoByAccount(loginAccount.getId());
		
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
	
	//HIEN THI PHOTO LEN GIAO DIEN
	@UiThread
	public void displayOnViews(ArrayList<Photo> photos){
		
		appbarTitle.setText("Your favorite photo: " + photos.size());
		
		String[] IMAGES = new String[photos.size()];
		
		for (int i=0; i< photos.size(); i++) {
			IMAGES[i] = photos.get(i).getPath();					
		}		
		ImagePagerAdapter imagePagerAdapter = new ImagePagerAdapter(IMAGES);
		pager.setAdapter(imagePagerAdapter);
		pager.setCurrentItem(pagerPosition);	
		pager.setOnPageChangeListener(imagePagerAdapter);		
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
	public void removeFavoritePhotoProcess(){
		
		FavoritePhotoHelper favoritePhotoHelper = new FavoritePhotoHelper(getApplicationContext());
		JsonResult result = favoritePhotoHelper.deleteFavoritePhoto(currentPhoto.getId(),loginAccount.getId());
		
		if(result.getMessage() == JsonMessage.SUCCESSFULL)
		{
			removeFavoritePhotoSuccess();
		
		} else if (result.getMessage() == JsonMessage.CONNECT_FAILED) {
			showToast(R.string.app_global_global_networkConnectionFailed);
		} else if (result.getMessage() == JsonMessage.RESULT_PARSE_ERROR) {
			showToast(R.string.app_global_global_parseWsResultFailed);
		} else {
			showToast(R.string.app_global_global_parameterWsInvalid);
		}
		
		setImage(btnDelete, R.drawable.trash);
	}
	
	@UiThread
	public void removeFavoritePhotoSuccess(){
		isChange=true;
		photos.remove(pagerPosition);
		if(photos.size()==0)
		{
			finish();
		}
		else
		{
			displayOnViews(photos);			
		}		
	}
	
	
	@UiThread
	void showToast(final int resId) {
		Toast.makeText(getApplicationContext(), resId,	Toast.LENGTH_LONG).show();
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
			sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "A girl from luv photo.");
			sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
			startActivity(Intent.createChooser(sharingIntent, "Share via"));
			setImage(btnFacebook, R.drawable.facebook);
		} 
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
					btnFacebook.setImageResource(R.drawable.facebook);
				}
			});
		
		dialog.show();
	}	
	public void authorizeFacenook(){
		Session.openActiveSession(this, true, statusCallback);
	}
	public void uploadPhotoToFacebook(final Session session, final Bitmap photoToPost, final String message) {    
		if(photoToPost!=null)
	    {
			List<String> PERMISSIONS = Arrays.asList("publish_actions");
	        session.requestNewPublishPermissions(new Session.NewPermissionsRequest(FavoritePhotoActivity.this, PERMISSIONS));
	        
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

	//SHOW DIALOG WHEN USER CLICK "ADD PHOTO FAVORITE"
	
	@Click(R.id.btn_delete)
	public void favoriteClicked()
	{
		setImage(btnDelete, R.drawable.loadingwhite);
		currentPhoto = photos.get(pagerPosition);		
		if(currentPhoto!=null)
		{
			showRemoveFavoriteConfirmDialog();
		}
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
					setImage(btnDelete, R.drawable.trash);
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
			showDownloadConfirmDialog();
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
			showSetWallPaperConfirmDialog();			
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
							WallpaperManager wallpaperManager = WallpaperManager.getInstance(FavoritePhotoActivity.this);
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
			WallpaperManager wallpaperManager = WallpaperManager.getInstance(FavoritePhotoActivity.this);
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
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {		
		Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	}
	
	@Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Session session = Session.getActiveSession();
        Session.saveSession(session, outState);
        outState.putInt(STATE_POSITION, pager.getCurrentItem());
    }
			
	public class SessionStatusCallback implements Session.StatusCallback {
        @Override
        public void call(Session session, SessionState state, Exception exception) {        	
            
        }
    }
	
	@Override
	public void onBackPressed(){
		if(isChange)
		{
			Intent returnIntent = new Intent();
			returnIntent.putExtra("isChange",isChange);
			setResult(RESULT_OK,returnIntent);  
			finish();
		}
		super.onBackPressed();
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
		long diffTime = (actualTime - lastUpdate);
		
		// Movement
	    float x = values[0];
	    float y = values[1];
	    float z = values[2];
	    
	    float accelationSquareRoot = (x * x + y * y + z * z) / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);   
	    
	    if (accelationSquareRoot >= 2)
	    {	      
	    	lastUpdate = actualTime;	    	
	    	float speed = Math.abs(x+y+z - last_x - last_y - last_z) / diffTime * 10000;
	        
	        if (speed > SHAKE_THRESHOLD) {
	        	
	            	            
	            if(Round(x,4)> 8.0000){            
		            //Toast.makeText(this, "Left shake detected", Toast.LENGTH_SHORT).show();
		            pager.setCurrentItem(pagerPosition-1);
		        }
		        else if(Round(x,4)<-8.0000){            
		            //Toast.makeText(this, "Right shake detected", Toast.LENGTH_SHORT).show();
		            pager.setCurrentItem(pagerPosition+1);
		        }
		        else if(Round(y,4) < 8.0){
		        	Toast.makeText(this, "Up", Toast.LENGTH_SHORT).show();
		         }

		         else if(Round(y,4) < -8.0){
		        	 //Toast.makeText(this, "Down", Toast.LENGTH_SHORT).show();
		         }
		         else if (z >9 && z < 10)
		         {
		        	 //Toast.makeText(this, "Down 2", Toast.LENGTH_SHORT).show();
		         }
	        }
	    }    
	    
        last_x = x;
        last_y = y;
        last_z = z;
	}
	
	public static float Round(float Rval, int Rpl) {
        float p = (float)Math.pow(10,Rpl);
        Rval = Rval * p;
        float tmp = Math.round(Rval);
        return (float)tmp/p;
    }
	
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
			
			
			imageView.setOnClickListener(new OnClickListener() {
	            @Override
	            public void onClick(View v) {
	            	
	            	LinearLayout options = (LinearLayout)findViewById(R.id.navbar_option);	            	
	            	if(options.getVisibility() == View.GONE)
	            	{	            		
	            		options.setVisibility(View.VISIBLE);	                    
	            		Animation animation = AnimationUtils.loadAnimation(FavoritePhotoActivity.this, android.R.anim.fade_in);
	            		options.startAnimation(animation);
	            	}
	            	else
	            	{
	            		options.setVisibility(View.GONE);
	            	}	        		
	        		
	        		//ImageView btnOption = (ImageView)findViewById(R.id.btn_photo_more_action);
	        		//btnOption.setVisibility(View.VISIBLE);
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
					
					Toast.makeText(FavoritePhotoActivity.this, message, Toast.LENGTH_SHORT).show();
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
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			
		}
		
		@Override
		public void onPageSelected(int position) {
			
			pagerPosition  = position;			
			currentPhoto = null;
			currentPhotoBimap = null;
			
		}
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		
	}
}
