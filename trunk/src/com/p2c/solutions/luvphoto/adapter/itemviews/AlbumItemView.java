package com.p2c.solutions.luvphoto.adapter.itemviews;

import greendroid.widget.item.Item;
import greendroid.widget.itemview.ItemView;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.p2c.solutions.luvphoto.R;
import com.p2c.solutions.luvphoto.adapter.items.AlbumItem;
import com.p2c.solutions.luvphoto.core.models.Album;

public class AlbumItemView  extends LinearLayout implements ItemView {

	private TextView albumName;
	private ImageView albumImage;
	
	public AlbumItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AlbumItemView(Context context) {
		super(context);
	}

	@Override
	public void prepareItemView() {
		albumName = (TextView) findViewById(R.id.album_title);
		albumImage = (ImageView) findViewById(R.id.album_img);
	}

	@Override
	public void setObject(Item item) {
		Album model = ((AlbumItem) item).getModel();		
		albumName.setText(model.getName());			
		ImageLoader.getInstance().displayImage(model.getImage(), albumImage);
	}	
}
