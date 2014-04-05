package com.p2c.solutions.luvphoto;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

public class LuvButton extends Button{

	public LuvButton(Context context) {
	    super(context);
	    init();
	}
	
	public LuvButton(Context context, AttributeSet attrs) {
	    super(context, attrs);
	    init();
	}
	
	public LuvButton(Context context, AttributeSet attrs, int defStyle) {
	    super(context, attrs, defStyle);
	    init();
	}

	private void init() {
		Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/hel.ttf");
	    setTypeface(font);
	    setTextSize(12);
	}
}
