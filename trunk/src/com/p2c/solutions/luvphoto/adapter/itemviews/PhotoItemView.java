package com.p2c.solutions.luvphoto.adapter.itemviews;

import greendroid.widget.item.Item;
import greendroid.widget.itemview.ItemView;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.p2c.solutions.luvphoto.R;
import com.p2c.solutions.luvphoto.adapter.items.PhotoItem;
import com.p2c.solutions.luvphoto.core.models.Photo;

public class PhotoItemView extends RelativeLayout implements ItemView {

	private ImageView imgPath;
	
	public PhotoItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PhotoItemView(Context context) {
		super(context);
	}

	@Override
	public void prepareItemView() {
		imgPath = (ImageView) findViewById(R.id.photo_item);
	}

	@Override
	public void setObject(Item item) {
		Photo model = ((PhotoItem) item).getModel();
		ImageLoader.getInstance().displayImage(model.getThumb(), imgPath);
	}
	
}
