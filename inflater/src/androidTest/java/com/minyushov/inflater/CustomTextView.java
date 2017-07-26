package com.minyushov.inflater;

import android.content.Context;
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
	}
}