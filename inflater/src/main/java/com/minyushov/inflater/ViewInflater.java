package com.minyushov.inflater;

import android.content.Context;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

class ViewInflater extends LayoutInflater {

  private static final String[] CLASS_PREFIXES = {
    "android.widget.",
    "android.view.",
    "android.webkit."
  };

  /**
   * Empty stack trace used to avoid log spam in re-throw exceptions.
   */
  private static final StackTraceElement[] EMPTY_STACK_TRACE = new StackTraceElement[0];

  private static final ClassLoader BOOT_CLASS_LOADER = LayoutInflater.class.getClassLoader();

  private static final Class<?>[] CONSTRUCTOR_SIGNATURE = new Class[] { Context.class, AttributeSet.class };
  private static final Map<String, Constructor<? extends View>> CONSTRUCTOR_MAP = new ArrayMap<>();

  private final Object[] constructorArgs = new Object[2];

  private Filter filter;
  private HashMap<String, Boolean> filterMap;

  ViewInflater(LayoutInflater original, Context newContext) {
    super(original, newContext);
    setFilter(original.getFilter());
  }

  @Override
  public LayoutInflater cloneInContext(Context newContext) {
    return new ViewInflater(this, newContext);
  }

  /**
   * @return The {@link Filter} currently used by this LayoutInflater to restrict the set of Views
   *   that are allowed to be inflated.
   */
  @Override
  public Filter getFilter() {
    return filter;
  }

  /**
   * Sets the {@link Filter} to by this LayoutInflater. If a view is attempted to be inflated
   * which is not allowed by the {@link Filter}, the {@link #inflate(int, ViewGroup)} call will
   * throw an {@link InflateException}. This filter will replace any previous filter set on this
   * LayoutInflater.
   *
   * @param filter
   *   The Filter which restricts the set of Views that are allowed to be inflated.
   *   This filter will replace any previous filter set on this LayoutInflater.
   */
  @Override
  public void setFilter(Filter filter) {
    this.filter = filter;
    if (filter != null) {
      filterMap = new HashMap<>();
    }
  }

  @Nullable
  final View createView(@NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
    try {
      final Object lastContext = constructorArgs[0];
      constructorArgs[0] = context;
      try {
        if (-1 == name.indexOf('.')) {
          View view = null;
          for (String prefix : CLASS_PREFIXES) {
            try {
              view = createFrameworkView(name, prefix, attrs);
              if (view != null) {
                break;
              }
            } catch (ClassNotFoundException e) {
              // In this case we want to let the base class take a crack at it.
            }
          }
          return view;
        } else {
          try {
            return createFrameworkView(name, null, attrs);
          } catch (InflateException e) {
            // In this case we want to let the base class take a crack at it.
            return null;
          }
        }
      } finally {
        constructorArgs[0] = lastContext;
      }
    } catch (InflateException e) {
      throw e;
    } catch (Exception e) {
      final InflateException ie = new InflateException(attrs.getPositionDescription() + ": Error inflating class " + name, e);
      ie.setStackTrace(EMPTY_STACK_TRACE);
      throw ie;
    }
  }

  private View createFrameworkView(String name, String prefix, AttributeSet attrs) throws ClassNotFoundException, InflateException {
    Constructor<? extends View> constructor = CONSTRUCTOR_MAP.get(name);
    if (constructor != null && !verifyClassLoader(constructor)) {
      constructor = null;
      CONSTRUCTOR_MAP.remove(name);
    }
    Context context = getContext();
    Class<? extends View> viewClass = null;
    String className = prefix != null ? (prefix + name) : name;
    try {
      if (constructor == null) {
        // Class not found in the cache, see if it's real, and try to add it
        viewClass = Class.forName(className, false, context.getClassLoader()).asSubclass(View.class);
        if (filter != null) {
          boolean allowed = filter.onLoadClass(viewClass);
          if (!allowed) {
            failNotAllowed(name, prefix, attrs);
          }
        }
        constructor = viewClass.getConstructor(CONSTRUCTOR_SIGNATURE);
        constructor.setAccessible(true);
        CONSTRUCTOR_MAP.put(name, constructor);
      } else {
        // If we have a filter, apply it to cached constructor
        if (filter != null) {
          // Have we seen this name before?
          Boolean allowedState = filterMap.get(name);
          if (allowedState == null) {
            // New class -- remember whether it is allowed
            viewClass = Class.forName(className, false, context.getClassLoader()).asSubclass(View.class);
            boolean allowed = filter.onLoadClass(viewClass);
            filterMap.put(name, allowed);
            if (!allowed) {
              failNotAllowed(name, prefix, attrs);
            }
          } else if (allowedState.equals(Boolean.FALSE)) {
            failNotAllowed(name, prefix, attrs);
          }
        }
      }
      Object lastContext = constructorArgs[0];
      if (constructorArgs[0] == null) {
        // Fill in the context if not already within inflation.
        constructorArgs[0] = context;
      }
      Object[] args = constructorArgs;
      args[1] = attrs;
      final View view = constructor.newInstance(args);
      if (view instanceof ViewStub) {
        // Use the same context when inflating ViewStub later.
        final ViewStub viewStub = (ViewStub) view;
        viewStub.setLayoutInflater(cloneInContext((Context) args[0]));
      }
      constructorArgs[0] = lastContext;
      return view;
    } catch (NoSuchMethodException e) {
      final InflateException ie = new InflateException(attrs.getPositionDescription() + ": Error inflating class " + (className), e);
      ie.setStackTrace(EMPTY_STACK_TRACE);
      throw ie;
    } catch (ClassCastException e) {
      // If loaded class is not a View subclass
      final InflateException ie = new InflateException(attrs.getPositionDescription() + ": Class is not a View " + (className), e);
      ie.setStackTrace(EMPTY_STACK_TRACE);
      throw ie;
    } catch (ClassNotFoundException e) {
      // If loadClass fails, we should propagate the exception.
      throw e;
    } catch (Exception e) {
      final InflateException ie = new InflateException(attrs.getPositionDescription() + ": Error inflating class " + (viewClass == null ? "<unknown>" : viewClass.getName()), e);
      ie.setStackTrace(EMPTY_STACK_TRACE);
      throw ie;
    }
  }

  private boolean verifyClassLoader(Constructor<? extends View> constructor) {
    final ClassLoader constructorLoader = constructor.getDeclaringClass().getClassLoader();
    if (constructorLoader == BOOT_CLASS_LOADER) {
      // fast path for boot class loader (most common case?) - always ok
      return true;
    }
    // in all normal cases (no dynamic code loading), we will exit the following loop on the
    // first iteration (i.e. when the declaring classloader is the contexts class loader).
    ClassLoader classLoader = getContext().getClassLoader();
    do {
      if (constructorLoader == classLoader) {
        return true;
      }
      classLoader = classLoader.getParent();
    } while (classLoader != null);
    return false;
  }

  /**
   * Throw an exception because the specified class is not allowed to be inflated.
   */
  private void failNotAllowed(String name, String prefix, AttributeSet attrs) {
    throw new InflateException(attrs.getPositionDescription() + ": Class not allowed to be inflated " + (prefix != null ? (prefix + name) : name));
  }
}