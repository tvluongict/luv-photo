package com.p2c.solutions.luvphoto.adapter.items;

import greendroid.widget.item.Item;
import greendroid.widget.itemview.ItemView;
import android.content.Context;
import android.view.ViewGroup;

import com.p2c.solutions.luvphoto.R;
import com.p2c.solutions.luvphoto.core.models.Album;

public class AlbumItem extends Item{

	private Album model;
	
	public AlbumItem(Album model) {
		this.model = model;
	}

	@Override
	public ItemView newView(Context context, ViewGroup parent) {
		return createCellFromXml(context, R.layout.luv_album_item_view, parent);
	}

	public Album getModel() {
		return model;
	}
	
}
