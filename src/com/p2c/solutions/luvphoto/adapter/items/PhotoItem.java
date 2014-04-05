package com.p2c.solutions.luvphoto.adapter.items;

import android.content.Context;
import android.view.ViewGroup;

import com.p2c.solutions.luvphoto.R;
import com.p2c.solutions.luvphoto.core.models.Photo;

import greendroid.widget.item.Item;
import greendroid.widget.itemview.ItemView;

public class PhotoItem extends Item{

	private Photo model;
	
	public PhotoItem(Photo model) {
		this.model = model;
	}

	@Override
	public ItemView newView(Context context, ViewGroup parent) {
		return createCellFromXml(context, R.layout.luv_photo_item_view, parent);
	}

	public Photo getModel() {
		return model;
	}
	
}
