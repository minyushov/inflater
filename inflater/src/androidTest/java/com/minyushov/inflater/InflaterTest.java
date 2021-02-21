package com.minyushov.inflater;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Objects;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

@RunWith(AndroidJUnit4.class)
public class InflaterTest {
  @Test
  public void testInflationInterceptor() {
    Context context = ApplicationProvider.getApplicationContext();

    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    Assert.assertNotNull("LayoutInflater is null", inflater);

    View view = inflater.inflate(com.minyushov.inflater.test.R.layout.test_layout, null);
    Assert.assertEquals("Default layout inflater is used, but inflated view is not system TextView", view.getClass(), TextView.class);

    context = new ContextWrapper.Builder(context)
      .setUseDefaultFactory(true)
      .addInterceptor((wrappedContext, parent, name, attrs) -> {
        if (Objects.equals(name, "TextView")) {
          return new CustomTextView(wrappedContext, attrs);
        }
        return null;
      })
      .build();

    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    Assert.assertNotNull("LayoutInflater is null", inflater);

    view = inflater.inflate(com.minyushov.inflater.test.R.layout.test_layout, null);
    Assert.assertEquals("InflationInterceptor is used, but inflated class is not CustomTextView", CustomTextView.class, view.getClass());
  }

  @Test
  public void testInflationInterceptorAppCompat() {
    Context context = ApplicationProvider.getApplicationContext();

    {
      LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      Assert.assertNotNull("LayoutInflater is null", inflater);

      View view = inflater.inflate(com.minyushov.inflater.test.R.layout.test_layout_app_compat, null);
      Assert.assertEquals("Default layout inflater is used, but inflated view is not AppCompatTextView", AppCompatTextView.class, view.getClass());
    }

    context = new ContextWrapper.Builder(context)
      .setUseDefaultFactory(true)
      .addInterceptor((wrappedContext, parent, name, attrs) -> {
        if (Objects.equals(name, "androidx.appcompat.widget.AppCompatTextView")) {
          return new CustomTextView(wrappedContext, attrs);
        }
        return null;
      })
      .build();

    {
      com.minyushov.inflater.LayoutInflater inflater = (com.minyushov.inflater.LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      Assert.assertNotNull("LayoutInflater is null", inflater);

      View view = inflater.inflate(com.minyushov.inflater.test.R.layout.test_layout_app_compat, null);
      Assert.assertEquals("InflationInterceptor is used, but inflated class is not CustomTextView", CustomTextView.class, view.getClass());
    }
  }

  @Test
  public void testPostInflationListener() {
    Context context = ApplicationProvider.getApplicationContext();

    android.view.LayoutInflater inflater = (android.view.LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    Assert.assertNotNull("LayoutInflater is null", inflater);

    TextView view = (TextView) inflater.inflate(com.minyushov.inflater.test.R.layout.test_layout, null);
    view.setTypeface(Typeface.DEFAULT);
    Assert.assertEquals("Default layout inflater is used, and non-default typeface is set", Typeface.DEFAULT, view.getTypeface());

    context = new ContextWrapper.Builder(context)
      .setUseDefaultFactory(true)
      .addListener((inflatedView, attrs) -> {
        if (inflatedView instanceof TextView) {
          ((TextView) inflatedView).setTypeface(Typeface.MONOSPACE);
        }
      })
      .build();

    inflater = (android.view.LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    Assert.assertNotNull("LayoutInflater is null", inflater);

    view = (TextView) inflater.inflate(com.minyushov.inflater.test.R.layout.test_layout, null);
    Assert.assertEquals("PostInflationListener is used, but non-default typeface is not applied", view.getTypeface(), Typeface.MONOSPACE);
  }
}