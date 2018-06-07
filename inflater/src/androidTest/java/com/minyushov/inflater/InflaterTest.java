package com.minyushov.inflater;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

@RunWith(AndroidJUnit4.class)
public class InflaterTest {
  @Test
  public void testInflationInterceptor() {
    Context context = InstrumentationRegistry.getTargetContext();

    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    Assert.assertNotNull("LayoutInflater is null", inflater);

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

    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    Assert.assertNotNull("LayoutInflater is null", inflater);

    view = inflater.inflate(com.minyushov.inflater.test.R.layout.test_layout, null);
    Assert.assertTrue("InflationInterceptor is used, but inflated class is not CustomTextView", Objects.equals(view.getClass(), CustomTextView.class));
  }

  @Test
  public void testInflationInterceptorAppCompat() {
    Context context = InstrumentationRegistry.getTargetContext();

    {
      LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      Assert.assertNotNull("LayoutInflater is null", inflater);

      View view = inflater.inflate(com.minyushov.inflater.test.R.layout.test_layout_app_compat, null);
      Assert.assertTrue("Default layout inflater is used, but inflated view is not AppCompatTextView", Objects.equals(view.getClass(), androidx.appcompat.widget.AppCompatTextView.class));
    }

    context = new ContextWrapper.Builder(context)
        .addInterceptor(new ContextWrapper.InflationInterceptor() {
          @Nullable
          @Override
          public View onCreateView(@NonNull Context context, @Nullable View parent, @NonNull String name, @Nullable AttributeSet attrs) {
            if (Objects.equals(name, "androidx.appcompat.widget.AppCompatTextView")) {
              return new CustomTextView(context, attrs);
            }
            return null;
          }
        })
        .build();

    {
      com.minyushov.inflater.LayoutInflater inflater = (com.minyushov.inflater.LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      Assert.assertNotNull("LayoutInflater is null", inflater);

      inflater.setFactory2(inflater);

      View view = inflater.inflate(com.minyushov.inflater.test.R.layout.test_layout_app_compat, null);
      Assert.assertTrue("InflationInterceptor is used, but inflated class is not CustomTextView", Objects.equals(view.getClass(), CustomTextView.class));
    }
  }

  @Test
  public void testPostInflationListener() {
    Context context = InstrumentationRegistry.getTargetContext();

    android.view.LayoutInflater inflater = (android.view.LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    Assert.assertNotNull("LayoutInflater is null", inflater);

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
    Assert.assertNotNull("LayoutInflater is null", inflater);

    view = (TextView) inflater.inflate(com.minyushov.inflater.test.R.layout.test_layout, null);
    Assert.assertTrue("PostInflationListener is used, but non-default typeface is not applied", Objects.equals(Typeface.MONOSPACE, view.getTypeface()));
  }
}