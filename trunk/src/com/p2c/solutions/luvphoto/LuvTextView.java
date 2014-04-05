package com.p2c.solutions.luvphoto;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class LuvTextView extends TextView {

	public LuvTextView(Context context) {
	    super(context);
	    init();
	}
	
	public LuvTextView(Context context, AttributeSet attrs) {
	    super(context, attrs);
	    init();
	}
	
	public LuvTextView(Context context, AttributeSet attrs, int defStyle) {
	    super(context, attrs, defStyle);
	    init();
	}

	private void init() {
		Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/hel.ttf");
	    setTypeface(font);
	}
}
