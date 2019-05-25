package com.minyushov.inflater;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

final class LayoutInflater extends ViewInflater {
  @NonNull
  private final List<ContextWrapper.InflationInterceptor> interceptors = new ArrayList<>();
  @NonNull
  private final List<ContextWrapper.PostInflationListener> listeners = new ArrayList<>();

  private final FactoryWrapper factory = new FactoryWrapper();

  private Field constructorArguments;

  LayoutInflater(android.view.LayoutInflater inflater, Context context) {
    super(inflater, context);

    factory.factory = inflater.getFactory();
    factory.factory2 = inflater.getFactory2();

    if (inflater instanceof LayoutInflater) {
      this.interceptors.addAll(((LayoutInflater) inflater).interceptors);
      this.listeners.addAll(((LayoutInflater) inflater).listeners);
      this.constructorArguments = ((LayoutInflater) inflater).constructorArguments;
    }
    if (context instanceof ContextWrapper) {
      ContextWrapper contextWrapper = (ContextWrapper) context;
      this.interceptors.addAll(contextWrapper.interceptors);
      this.listeners.addAll(contextWrapper.listeners);
      if (contextWrapper.useDefaultFactory) {
        setFactory2(factory);
      }
    }
  }

  @Override
  public android.view.LayoutInflater cloneInContext(Context context) {
    return new LayoutInflater(this, context);
  }

  @Override
  public void setFactory(Factory factory) {
    if (getFactory() == null) {
      super.setFactory(this.factory);
    }

    if (factory instanceof FactoryWrapper) {
      this.factory.setFactory(((FactoryWrapper) factory).factory);
    } else {
      this.factory.setFactory(factory);
    }
  }

  @Override
  public void setFactory2(Factory2 factory) {
    if (getFactory() == null) {
      super.setFactory2(this.factory);
    }

    if (factory instanceof FactoryWrapper) {
      this.factory.setFactory2(((FactoryWrapper) factory).factory2);
    } else {
      this.factory.setFactory2(factory);
    }
  }

  @Nullable
  private View dispatchOnCreateView(View parent, @NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
    for (ContextWrapper.InflationInterceptor interceptor : interceptors) {
      View view = interceptor.onCreateView(context, parent, name, attrs);
      if (view != null) {
        return view;
      }
    }
    return null;
  }

  private void dispatchOnViewCreated(@NonNull View view, @NonNull AttributeSet attrs) {
    for (ContextWrapper.PostInflationListener listener : listeners) {
      listener.onViewCreated(view, attrs);
    }
  }

  private class FactoryWrapper implements Factory2 {
    @Nullable
    Factory factory;
    @Nullable
    Factory2 factory2;

    void setFactory(@Nullable Factory factory) {
      this.factory = factory;
    }

    void setFactory2(@Nullable Factory2 factory2) {
      this.factory = factory2;
      this.factory2 = factory2;
    }

    @Override
    public View onCreateView(@NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
      View view = dispatchOnCreateView(null, name, context, attrs);
      if (view == null) {
        if (factory != null) {
          view = factory.onCreateView(name, context, attrs);
        }
      }

      if (view == null) {
        view = createView(name, context, attrs);
      }

      if (view != null) {
        dispatchOnViewCreated(view, attrs);
      }

      return view;
    }

    @Override
    public View onCreateView(View parent, @NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
      View view = dispatchOnCreateView(parent, name, context, attrs);
      if (view == null) {
        if (factory2 != null) {
          view = factory2.onCreateView(parent, name, context, attrs);
        } else if (factory != null) {
          view = factory.onCreateView(name, context, attrs);
        }
      }

      if (view == null) {
        view = createView(name, context, attrs);
      }

      if (view != null) {
        dispatchOnViewCreated(view, attrs);
      }

      return view;
    }
  }
}