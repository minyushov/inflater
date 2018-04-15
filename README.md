# Inflater
[![Download](https://api.bintray.com/packages/minyushov/android/inflater/images/download.svg)](https://bintray.com/minyushov/android/inflater/_latestVersion)

An implementation of ContextWrapper that provides pre- and post-inflation callbacks.

# Getting started

Include inflater as a Gradle compile dependency:
```groovy
repositories {
    maven {
        url  "http://dl.bintray.com/minyushov/android" 
    }
}

dependencies {
    compile 'com.minyushov.android:inflater:1.0.7'
}
```

Implement InflationInterceptor or PostInflationListener:
```java
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
```
```java
private final ContextWrapper.PostInflationListener postInflationListener = new ContextWrapper.PostInflationListener() {
  @Override
  public void onViewCreated(@NonNull View view, @Nullable AttributeSet attrs) {
    if (view instanceof TextView) {
      ((TextView) view).setTypeface(Typeface.MONOSPACE);
    }
  }
};
```

Attach interceptors and listeners to an Activity using ContextWrapper:
```java
public class MainActivity extends AppCompatActivity {
  @Override
  protected void attachBaseContext(Context baseContext) {
    super.attachBaseContext(new ContextWrapper.Builder(baseContext)
         .addInterceptor(inflationInterceptor)
         .addListener(postInflationListener)
         .build());
  }
}
```

From this point all TextViews will be replaced with CustomTextViews, and all TextViews will use monospace typeface.
