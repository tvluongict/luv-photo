package com.p2c.solutions.luvphoto;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;

public class LuvEditText extends EditText{

	public LuvEditText(Context context) {
	    super(context);
	    init();
	}
	
	public LuvEditText(Context context, AttributeSet attrs) {
	    super(context, attrs);
	    init();
	}
	
	public LuvEditText(Context context, AttributeSet attrs, int defStyle) {
	    super(context, attrs, defStyle);
	    init();
	}

	private void init() {
		
		Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/hel.ttf");
	    setTypeface(font);
	    setTextSize(12);
	}	
}
