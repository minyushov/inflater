package com.minyushov.inflater;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

final class LayoutInflater extends PhoneLayoutInflater {
	private static final String TAG = "Inflater";

	@NonNull
	private final List<ContextWrapper.InflationInterceptor> interceptors = new ArrayList<>();
	@NonNull
	private final List<ContextWrapper.PostInflationListener> listeners = new ArrayList<>();

	private Field constructorArguments;

	LayoutInflater(android.view.LayoutInflater inflater, Context context) {
		super(inflater, context);
		if (inflater instanceof LayoutInflater) {
			this.interceptors.addAll(((LayoutInflater) inflater).interceptors);
			this.listeners.addAll(((LayoutInflater) inflater).listeners);
		}
		if (context instanceof ContextWrapper) {
			this.interceptors.addAll(((ContextWrapper) context).interceptors);
			this.listeners.addAll(((ContextWrapper) context).listeners);
		}
	}

	@Override
	public android.view.LayoutInflater cloneInContext(Context context) {
		return new LayoutInflater(this, context);
	}

	@Override
	public void setFactory(Factory factory) {
		if (!(factory instanceof WrapperFactory)) {
			super.setFactory(new WrapperFactory(factory));
		} else {
			super.setFactory(factory);
		}
	}

	@Override
	public void setFactory2(Factory2 factory) {
		if (!(factory instanceof WrapperFactory2)) {
			super.setFactory2(new WrapperFactory2(factory));
		} else {
			super.setFactory2(factory);
		}
	}

	@Override
	protected View onCreateView(String name, AttributeSet attrs) throws ClassNotFoundException {
		View view = LayoutInflater.this.onCreateView(getContext(), null, name, attrs);
		if (view == null) {
			view = super.onCreateView(name, attrs);
		}
		if (view != null) {
			onViewCreated(view, attrs);
		}
		return view;
	}

	@Override
	protected View onCreateView(View parent, String name, AttributeSet attrs) throws ClassNotFoundException {
		View view = LayoutInflater.this.onCreateView(getContext(), parent, name, attrs);
		if (view == null) {
			view = super.onCreateView(parent, name, attrs);
		}
		if (view != null) {
			onViewCreated(view, attrs);
		}
		return view;
	}

	@Nullable
	private View onCreateView(@NonNull Context context, @Nullable View parent, @NonNull String name, @Nullable AttributeSet attrs) {
		for (ContextWrapper.InflationInterceptor interceptor : interceptors) {
			View view = interceptor.onCreateView(context, parent, name, attrs);
			if (view != null) {
				return view;
			}
		}
		return createView(context, parent, name, attrs);
	}

	private void onViewCreated(@NonNull View view, @NonNull AttributeSet attrs) {
		for (ContextWrapper.PostInflationListener listener : listeners) {
			listener.onViewCreated(view, attrs);
		}
	}

	private View createView(@NonNull Context context, @Nullable View parent, @NonNull String name, @Nullable AttributeSet attrs) {
		if (name.indexOf('.') > -1) {
			if (constructorArguments == null) {
				constructorArguments = ReflectionUtils.getField(android.view.LayoutInflater.class, "mConstructorArgs");
			}

			Object[] constructorArgs = (Object[]) ReflectionUtils.getValue(constructorArguments, this);
			Object currentContext = null;
			if (constructorArgs != null) {
				currentContext = constructorArgs[0];
				constructorArgs[0] = context;
				ReflectionUtils.setValue(constructorArguments, this, constructorArgs);
			}
			try {
				return createView(name, null, attrs);
			} catch (ClassNotFoundException exception) {
				Log.w(TAG, "Unable to inflate view", exception);
			} finally {
				if (constructorArgs != null) {
					constructorArgs[0] = currentContext;
				}
				ReflectionUtils.setValue(constructorArguments, this, constructorArgs);
			}
		}

		return null;
	}

	private class WrapperFactory implements Factory {
		private final Factory factory;

		WrapperFactory(Factory factory) {
			this.factory = factory;
		}

		@Override
		public View onCreateView(String name, Context context, AttributeSet attrs) {
			View view = LayoutInflater.this.onCreateView(context, null, name, attrs);
			if (view == null) {
				view = factory.onCreateView(name, context, attrs);
			}
			if (view != null) {
				LayoutInflater.this.onViewCreated(view, attrs);
			}
			return view;
		}
	}

	private class WrapperFactory2 implements Factory2 {
		private final Factory2 factory;

		WrapperFactory2(Factory2 factory) {
			this.factory = factory;
		}

		@Override
		public View onCreateView(String name, Context context, AttributeSet attrs) {
			View view = LayoutInflater.this.onCreateView(context, null, name, attrs);
			if (view == null) {
				view = factory.onCreateView(name, context, attrs);
			}
			if (view != null) {
				LayoutInflater.this.onViewCreated(view, attrs);
			}
			return view;
		}

		@Override
		public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
			View view = LayoutInflater.this.onCreateView(context, parent, name, attrs);
			if (view == null) {
				view = factory.onCreateView(parent, name, context, attrs);
			}
			if (view != null) {
				LayoutInflater.this.onViewCreated(view, attrs);
			}
			return view;
		}
	}
}