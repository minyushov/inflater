package com.minyushov.inflater;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import java.util.Objects;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class InflaterTest {
	@Test
	public void testInflationInterceptor() throws Exception {
		Context context = InstrumentationRegistry.getTargetContext();
		android.view.LayoutInflater inflater = (android.view.LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(com.minyushov.inflater.test.R.layout.test_layout, null);
		Assert.assertTrue("Default layout inflater is used, but inflated view is not system TextView", Objects.equals(view.getClass(), android.widget.TextView.class));

		context = new ContextWrapper.Builder(context)
				.addInterceptor(new ContextWrapper.InflationInterceptor() {
					@Nullable
					@Override
					public View onCreateView(@NonNull Context context, @Nullable View parent, @NonNull String name, @Nullable AttributeSet attrs) {
						if (Objects.equals(name, "TextView")) {
							return new CustomTextView(context, attrs);
						}
						return null;
					}
				})
				.build();

		inflater = (android.view.LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		view = inflater.inflate(com.minyushov.inflater.test.R.layout.test_layout, null);
		Assert.assertTrue("InflationInterceptor is used, but inflated class is not CustomTextView", Objects.equals(view.getClass(), CustomTextView.class));
	}

	@Test
	public void testPostInflationListener() throws Exception {
		Context context = InstrumentationRegistry.getTargetContext();
		android.view.LayoutInflater inflater = (android.view.LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		TextView view = (TextView) inflater.inflate(com.minyushov.inflater.test.R.layout.test_layout, null);
		view.setTypeface(Typeface.DEFAULT);
		Assert.assertTrue("Default layout inflater is used, and non-default typeface is set", Objects.equals(Typeface.DEFAULT, view.getTypeface()));

		context = new ContextWrapper.Builder(context)
				.addListener(new ContextWrapper.PostInflationListener() {
					@Override
					public void onViewCreated(@NonNull View view, @Nullable AttributeSet attrs) {
						if (view instanceof TextView) {
							((TextView) view).setTypeface(Typeface.MONOSPACE);
						}
					}
				})
				.build();

		inflater = (android.view.LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		view = (TextView) inflater.inflate(com.minyushov.inflater.test.R.layout.test_layout, null);
		Assert.assertTrue("PostInflationListener is used, but non-default typeface is not applied", Objects.equals(Typeface.MONOSPACE, view.getTypeface()));
	}
}