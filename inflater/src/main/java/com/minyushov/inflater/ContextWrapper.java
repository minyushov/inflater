package com.minyushov.inflater;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of ContextWrapper that provides pre- and post-inflation callbacks.
 */
public final class ContextWrapper extends android.content.ContextWrapper {
  /**
   * Interceptor that will be called before a view is created during inflation.
   */
  public interface InflationInterceptor {
    /**
     * This method is called before a view is created during inflation.
     *
     * @param context
     *     {@link Context} that will be used to create a view.
     * @param parent
     *     The future parent of the returned view. May be null.
     * @param name
     *     Class name of the View to be created.
     * @param attrs
     *     An AttributeSet of attributes to apply to the View. May be null.
     *
     * @return New instance of the View or null if interception is not applied.
     */
    @Nullable
    View onCreateView(@NonNull Context context, @Nullable View parent, @NonNull String name, @Nullable AttributeSet attrs);
  }

  /**
   * Listener that will be called after a view is created during inflation.
   */
  public interface PostInflationListener {
    /**
     * This method is called after a view is created during inflation.
     *
     * @param view
     *     Created view.
     * @param attrs
     *     {@link AttributeSet} applied to the view. May be null.
     */
    void onViewCreated(@NonNull View view, @Nullable AttributeSet attrs);
  }

  @NonNull
  final List<InflationInterceptor> interceptors = new ArrayList<>();
  @NonNull
  final List<PostInflationListener> listeners = new ArrayList<>();

  private LayoutInflater inflater;

  private ContextWrapper(Context base) {
    super(base);
  }

  @Override
  public Object getSystemService(String name) {
    if (LAYOUT_INFLATER_SERVICE.equals(name)) {
      if (inflater == null) {
        inflater = new LayoutInflater(android.view.LayoutInflater.from(getBaseContext()), this);
      }
      return inflater;
    }
    return super.getSystemService(name);
  }

  /**
   * Builder for {@link ContextWrapper}.
   */
  public static final class Builder {
    @NonNull
    private final Context context;
    @NonNull
    private final List<InflationInterceptor> interceptors = new ArrayList<>();
    @NonNull
    private final List<PostInflationListener> listeners = new ArrayList<>();

    /**
     * Creates builder for {@link ContextWrapper}.
     *
     * @param context
     *     {@link Context} that will be wrapped.
     */
    public Builder(@NonNull Context context) {
      this.context = context;
    }

    /**
     * Adds interceptor that will be called before a view is created during inflation.
     *
     * @param interceptor
     *     Inflation interceptor.
     *
     * @return Current instance of the Builder.
     */
    public Builder addInterceptor(InflationInterceptor interceptor) {
      interceptors.add(interceptor);
      return this;
    }

    /**
     * Adds listener that will be called after a view is created during inflation.
     *
     * @param listener
     *     Post-inflation listener.
     *
     * @return Current instance of the Builder.
     */
    public Builder addListener(PostInflationListener listener) {
      listeners.add(listener);
      return this;
    }

    /**
     * @return New configured instance of {@link ContextWrapper}.
     */
    public android.content.ContextWrapper build() {
      ContextWrapper wrapper = new ContextWrapper(context);
      wrapper.interceptors.addAll(interceptors);
      wrapper.listeners.addAll(listeners);
      return wrapper;
    }
  }
}