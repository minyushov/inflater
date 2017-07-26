package com.minyushov.inflater.sample;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.View;

import com.minyushov.inflater.ContextWrapper;

public class MainActivity extends AppCompatActivity {
	private final ContextWrapper.InflationInterceptor inflationInterceptor = new ContextWrapper.InflationInterceptor() {
		@Nullable
		@Override
		public View onCreateView(@NonNull Context context, @Nullable View parent, @NonNull String name, @Nullable AttributeSet attrs) {
			if (name.equals("TextView")) {
				return new CustomTextView(context, attrs);
			}
			return null;
		}
	};

	private final ContextWrapper.PostInflationListener postInflationListener = new ContextWrapper.PostInflationListener() {
		@Override
		public void onViewCreated(@NonNull View view, @Nullable AttributeSet attrs) {
			if (view instanceof CustomTextView) {
				((CustomTextView) view).setTextColor(Color.RED);
			}
		}
	};

	@Override
	protected void attachBaseContext(Context baseContext) {
		super.attachBaseContext(new ContextWrapper.Builder(baseContext)
				.addInterceptor(inflationInterceptor)
				.addListener(postInflationListener)
				.build());
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.a_main);
	}
}