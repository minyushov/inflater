package com.minyushov.inflater.sample;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

public class CustomTextView extends AppCompatTextView {
	public CustomTextView(Context context) {
		this(context, null);
	}

	public CustomTextView(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.textViewStyle);
	}

	public CustomTextView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setTypeface(Typeface.MONOSPACE);
	}
}