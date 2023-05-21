// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package com.example.newsfeed_android;

import static com.example.newsfeed_android.FlutterActivityLaunchConfigsTest.DART_ENTRYPOINT_META_DATA_KEY;
import static com.example.newsfeed_android.FlutterActivityLaunchConfigsTest.DART_ENTRYPOINT_URI_META_DATA_KEY;
import static com.example.newsfeed_android.FlutterActivityLaunchConfigsTest.DEFAULT_BACKGROUND_MODE;
import static com.example.newsfeed_android.FlutterActivityLaunchConfigsTest.DEFAULT_DART_ENTRYPOINT;
import static com.example.newsfeed_android.FlutterActivityLaunchConfigsTest.DEFAULT_INITIAL_ROUTE;
import static com.example.newsfeed_android.FlutterActivityLaunchConfigsTest.EXTRA_BACKGROUND_MODE;
import static com.example.newsfeed_android.FlutterActivityLaunchConfigsTest.EXTRA_CACHED_ENGINE_ID;
import static com.example.newsfeed_android.FlutterActivityLaunchConfigsTest.EXTRA_DART_ENTRYPOINT_ARGS;
import static com.example.newsfeed_android.FlutterActivityLaunchConfigsTest.EXTRA_DESTROY_ENGINE_WITH_ACTIVITY;
import static com.example.newsfeed_android.FlutterActivityLaunchConfigsTest.EXTRA_ENABLE_STATE_RESTORATION;
import static com.example.newsfeed_android.FlutterActivityLaunchConfigsTest.EXTRA_INITIAL_ROUTE;
import static com.example.newsfeed_android.FlutterActivityLaunchConfigsTest.HANDLE_DEEPLINKING_META_DATA_KEY;
import static com.example.newsfeed_android.FlutterActivityLaunchConfigsTest.INITIAL_ROUTE_META_DATA_KEY;
import static com.example.newsfeed_android.FlutterActivityLaunchConfigsTest.NORMAL_THEME_META_DATA_KEY;
import static com.example.newsfeed_android.FlutterActivityLaunchConfigsTest.SPLASH_SCREEN_META_DATA_KEY;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.window.OnBackInvokedCallback;
import android.window.OnBackInvokedDispatcher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import io.flutter.Log;
import com.example.newsfeed_android.FlutterActivityLaunchConfigsTest.BackgroundMode;
import io.flutter.embedding.android.DrawableSplashScreen;
import io.flutter.embedding.android.ExclusiveAppComponent;
import io.flutter.embedding.android.FlutterSurfaceView;
import io.flutter.embedding.android.FlutterTextureView;
import io.flutter.embedding.android.RenderMode;
import io.flutter.embedding.android.SplashScreen;
import io.flutter.embedding.android.TransparencyMode;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.embedding.engine.FlutterShellArgs;
import io.flutter.embedding.engine.plugins.activity.ActivityControlSurface;
import io.flutter.embedding.engine.plugins.util.GeneratedPluginRegister;
import io.flutter.plugin.platform.PlatformPlugin;
import io.flutter.util.ViewUtils;
import live.hms.hmssdk_flutter.Constants;
import live.hms.hmssdk_flutter.methods.HMSPipAction;

import java.util.ArrayList;
import java.util.List;

public class TestFlutterActivity extends Activity
        implements FlutterActivityAndFragmentDelegateTest.Host, LifecycleOwner {
    private static final String TAG = "TestFlutterActivity";

    /**
     * The ID of the {@code FlutterView} created by this activity.
     *
     * <p>This ID can be used to lookup {@code FlutterView} in the Android view hierarchy. For more,
     * see {@link android.view.View#findViewById}.
     */
    public static final int FLUTTER_VIEW_ID = ViewUtils.generateViewId(0xF1F2);

    /**
     * Creates an {@link Intent} that launches a {@code TestFlutterActivity}, which creates a {@link
     * FlutterEngine} that executes a {@code main()} Dart entrypoint, and displays the "/" route as
     * Flutter's initial route.
     *
     * <p>Consider using the {@link #withCachedEngine(String)} {@link Intent} builder to control when
     * the {@link io.flutter.embedding.engine.FlutterEngine} should be created in your application.
     *
     * @param launchContext The launch context. e.g. An Activity.
     * @return The default intent.
     */
    @NonNull
    public static Intent createDefaultIntent(@NonNull Context launchContext) {
        return withNewEngine().build(launchContext);
    }

    /**
     * Creates an {@link NewEngineIntentBuilder}, which can be used to configure an {@link Intent} to
     * launch a {@code TestFlutterActivity} that internally creates a new {@link
     * io.flutter.embedding.engine.FlutterEngine} using the desired Dart entrypoint, initial route,
     * etc.
     *
     * @return The engine intent builder.
     */
    @NonNull
    public static NewEngineIntentBuilder withNewEngine() {
        return new NewEngineIntentBuilder(TestFlutterActivity.class);
    }

    /**
     * Builder to create an {@code Intent} that launches a {@code TestFlutterActivity} with a new {@link
     * FlutterEngine} and the desired configuration.
     */
    public static class NewEngineIntentBuilder {
        private final Class<? extends TestFlutterActivity> activityClass;
        private String initialRoute = DEFAULT_INITIAL_ROUTE;
        private String backgroundMode = DEFAULT_BACKGROUND_MODE;
        @Nullable private List<String> dartEntrypointArgs;

 
        public NewEngineIntentBuilder(@NonNull Class<? extends TestFlutterActivity> activityClass) {
            this.activityClass = activityClass;
        }

        @NonNull
        public NewEngineIntentBuilder initialRoute(@NonNull String initialRoute) {
            this.initialRoute = initialRoute;
            return this;
        }

        @NonNull
        public NewEngineIntentBuilder backgroundMode(@NonNull BackgroundMode backgroundMode) {
            this.backgroundMode = backgroundMode.name();
            return this;
        }

        /**
         * The Dart entrypoint arguments will be passed as a list of string to Dart's entrypoint
         * function.
         *
         * <p>A value of null means do not pass any arguments to Dart's entrypoint function.
         *
         * @param dartEntrypointArgs The Dart entrypoint arguments.
         * @return The engine intent builder.
         */
        @NonNull
        public NewEngineIntentBuilder dartEntrypointArgs(@Nullable List<String> dartEntrypointArgs) {
            this.dartEntrypointArgs = dartEntrypointArgs;
            return this;
        }

        /**
         * Creates and returns an {@link Intent} that will launch a {@code TestFlutterActivity} with the
         * desired configuration.
         *
         * @param context The context. e.g. An Activity.
         * @return The intent.
         */
        @NonNull
        public Intent build(@NonNull Context context) {
            Intent intent =
                    new Intent(context, activityClass)
                            .putExtra(EXTRA_INITIAL_ROUTE, initialRoute)
                            .putExtra(EXTRA_BACKGROUND_MODE, backgroundMode)
                            .putExtra(EXTRA_DESTROY_ENGINE_WITH_ACTIVITY, true);
            if (dartEntrypointArgs != null) {
                intent.putExtra(EXTRA_DART_ENTRYPOINT_ARGS, new ArrayList(dartEntrypointArgs));
            }
            return intent;
        }
    }

    /**
     * Creates a {@link CachedEngineIntentBuilder}, which can be used to configure an {@link Intent}
     * to launch a {@code TestFlutterActivity} that internally uses an existing {@link
     * io.flutter.embedding.engine.FlutterEngine} that is cached in {@link
     * io.flutter.embedding.engine.FlutterEngineCache}.
     *
     * @param cachedEngineId A cached engine ID.
     * @return The builder.
     */
    public static CachedEngineIntentBuilder withCachedEngine(@NonNull String cachedEngineId) {
        return new CachedEngineIntentBuilder(TestFlutterActivity.class, cachedEngineId);
    }

    /**
     * Builder to create an {@code Intent} that launches a {@code TestFlutterActivity} with an existing
     * {@link io.flutter.embedding.engine.FlutterEngine} that is cached in {@link
     * io.flutter.embedding.engine.FlutterEngineCache}.
     */
    public static class CachedEngineIntentBuilder {
        private final Class<? extends TestFlutterActivity> activityClass;
        private final String cachedEngineId;
        private boolean destroyEngineWithActivity = false;
        private String backgroundMode = DEFAULT_BACKGROUND_MODE;

        /**
         * Constructor that allows this {@code CachedEngineIntentBuilder} to be used by subclasses of
         * {@code TestFlutterActivity}.
         *
         * <p>Subclasses of {@code TestFlutterActivity} should provide their own static version of {@link
         * TestFlutterActivity#withCachedEngine(String)}, which returns an instance of {@code
         * CachedEngineIntentBuilder} constructed with a {@code Class} reference to the {@code
         * TestFlutterActivity} subclass, e.g.:
         *
         * <p>{@code return new CachedEngineIntentBuilder(MyTestFlutterActivity.class, engineId); }
         *
         * @param activityClass A subclass of {@code TestFlutterActivity}.
         * @param engineId The engine id.
         */
        public CachedEngineIntentBuilder(
                @NonNull Class<? extends TestFlutterActivity> activityClass, @NonNull String engineId) {
            this.activityClass = activityClass;
            this.cachedEngineId = engineId;
        }

        /**
         * Whether the cached {@link io.flutter.embedding.engine.FlutterEngine} should be destroyed and
         * removed from the cache when this {@code TestFlutterActivity} is destroyed.
         *
         * <p>The default value is {@code false}.
         *
         * @param destroyEngineWithActivity Whether to destroy the engine.
         * @return The builder.
         */
        public CachedEngineIntentBuilder destroyEngineWithActivity(boolean destroyEngineWithActivity) {
            this.destroyEngineWithActivity = destroyEngineWithActivity;
            return this;
        }

        @NonNull
        public CachedEngineIntentBuilder backgroundMode(@NonNull BackgroundMode backgroundMode) {
            this.backgroundMode = backgroundMode.name();
            return this;
        }

        /**
         * Creates and returns an {@link Intent} that will launch a {@code TestFlutterActivity} with the
         * desired configuration.
         *
         * @param context The context. e.g. An Activity.
         * @return The intent.
         */
        @NonNull
        public Intent build(@NonNull Context context) {
            return new Intent(context, activityClass)
                    .putExtra(EXTRA_CACHED_ENGINE_ID, cachedEngineId)
                    .putExtra(EXTRA_DESTROY_ENGINE_WITH_ACTIVITY, destroyEngineWithActivity)
                    .putExtra(EXTRA_BACKGROUND_MODE, backgroundMode);
        }
    }

    // Delegate that runs all lifecycle and OS hook logic that is common between
    // TestFlutterActivity and FlutterFragment. See the FlutterActivityAndFragmentDelegateTest
    // implementation for details about why it exists.
    @VisibleForTesting protected FlutterActivityAndFragmentDelegateTest delegate;

    @NonNull private LifecycleRegistry lifecycle;

    public TestFlutterActivity() {
        lifecycle = new LifecycleRegistry(this);
    }

    /**
     * This method exists so that JVM tests can ensure that a delegate exists without putting this
     * Activity through any lifecycle events, because JVM tests cannot handle executing any lifecycle
     * methods, at the time of writing this.
     *
     * <p>The testing infrastructure should be upgraded to make TestFlutterActivity tests easy to write
     * while exercising real lifecycle methods. At such a time, this method should be removed.
     *
     * @param delegate The delegate.
     */
    // TODO(mattcarroll): remove this when tests allow for it
    // (https://github.com/flutter/flutter/issues/43798)
    @VisibleForTesting
    /* package */ void setDelegate(@NonNull FlutterActivityAndFragmentDelegateTest delegate) {
        this.delegate = delegate;
    }

    /**
     * Returns the Android App Component exclusively attached to {@link
     * io.flutter.embedding.engine.FlutterEngine}.
     */
    @Override
    public ExclusiveAppComponent<Activity> getExclusiveAppComponent() {
        return delegate;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        switchLaunchThemeForNormalTheme();

        super.onCreate(savedInstanceState);

        delegate = new FlutterActivityAndFragmentDelegateTest(this);
        delegate.onAttach(this);
        delegate.onRestoreInstanceState(savedInstanceState);

        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_CREATE);

        registerOnBackInvokedCallback();

        configureWindowForTransparency();

        setContentView(createFlutterView());

        configureStatusBarForFullscreenFlutterExperience();
    }

    /**
     * Registers the callback with OnBackInvokedDispatcher to capture back navigation gestures and
     * pass them to the framework.
     *
     * <p>This replaces the deprecated onBackPressed method override in order to support API 33's
     * predictive back navigation feature.
     *
     * <p>The callback must be unregistered in order to prevent unpredictable behavior once outside
     * the Flutter app.
     */
    @VisibleForTesting
    public void registerOnBackInvokedCallback() {
        if (Build.VERSION.SDK_INT >= 33) {
            getOnBackInvokedDispatcher()
                    .registerOnBackInvokedCallback(
                            OnBackInvokedDispatcher.PRIORITY_DEFAULT, onBackInvokedCallback);
        }
    }

    /**
     * Unregisters the callback from OnBackInvokedDispatcher.
     *
     * <p>This should be called when the activity is no longer in use to prevent unpredictable
     * behavior such as being stuck and unable to press back.
     */
    @VisibleForTesting
    public void unregisterOnBackInvokedCallback() {
        if (Build.VERSION.SDK_INT >= 33) {
            getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback(onBackInvokedCallback);
        }
    }

    private final OnBackInvokedCallback onBackInvokedCallback =
            Build.VERSION.SDK_INT >= 33
                    ? new OnBackInvokedCallback() {
                // TODO(garyq): Remove SuppressWarnings annotation. This was added to workaround
                // a google3 bug where the linter is not properly running against API 33, causing
                // a failure here. See b/243609613 and https://github.com/flutter/flutter/issues/111295
                @SuppressWarnings("Override")
                @Override
                public void onBackInvoked() {
                    onBackPressed();
                }
            }
                    : null;

    /**
     * Switches themes for this {@code Activity} from the theme used to launch this {@code Activity}
     * to a "normal theme" that is intended for regular {@code Activity} operation.
     *
     * <p>This behavior is offered so that a "launch screen" can be displayed while the application
     * initially loads. To utilize this behavior in an app, do the following:
     *
     * <ol>
     *   <li>Create 2 different themes in style.xml: one theme for the launch screen and one theme for
     *       normal display.
     *   <li>In the launch screen theme, set the "windowBackground" property to a {@code Drawable} of
     *       your choice.
     *   <li>In the normal theme, customize however you'd like.
     *   <li>In the AndroidManifest.xml, set the theme of your {@code TestFlutterActivity} to your launch
     *       theme.
     *   <li>Add a {@code <meta-data>} property to your {@code TestFlutterActivity} with a name of
     *       "io.flutter.embedding.android.NormalTheme" and set the resource to your normal theme,
     *       e.g., {@code android:resource="@style/MyNormalTheme}.
     * </ol>
     *
     * With the above settings, your launch theme will be used when loading the app, and then the
     * theme will be switched to your normal theme once the app has initialized.
     *
     * <p>Do not change aspects of system chrome between a launch theme and normal theme. Either
     * define both themes to be fullscreen or not, and define both themes to display the same status
     * bar and navigation bar settings. If you wish to adjust system chrome once your Flutter app
     * renders, use platform channels to instruct Android to do so at the appropriate time. This will
     * avoid any jarring visual changes during app startup.
     */
    private void switchLaunchThemeForNormalTheme() {
        try {
            Bundle metaData = getMetaData();
            if (metaData != null) {
                int normalThemeRID = metaData.getInt(NORMAL_THEME_META_DATA_KEY, -1);
                if (normalThemeRID != -1) {
                    setTheme(normalThemeRID);
                }
            } else {
                Log.v(TAG, "Using the launch theme as normal theme.");
            }
        } catch (PackageManager.NameNotFoundException exception) {
            Log.e(
                    TAG,
                    "Could not read meta-data for TestFlutterActivity. Using the launch theme as normal theme.");
        }
    }

    @Nullable
    @Override
    public SplashScreen provideSplashScreen() {
        Drawable manifestSplashDrawable = getSplashScreenFromManifest();
        if (manifestSplashDrawable != null) {
            return new DrawableSplashScreen(manifestSplashDrawable);
        } else {
            return null;
        }
    }

    @Nullable
    private Drawable getSplashScreenFromManifest() {
        try {
            Bundle metaData = getMetaData();
            int splashScreenId = metaData != null ? metaData.getInt(SPLASH_SCREEN_META_DATA_KEY) : 0;
            return splashScreenId != 0
                    ? ResourcesCompat.getDrawable(getResources(), splashScreenId, getTheme())
                    : null;
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Splash screen not found. Ensure the drawable exists and that it's valid.");
            throw e;
        } catch (PackageManager.NameNotFoundException e) {
            // This is never expected to happen.
            return null;
        }
    }

    /**
     * Sets this {@code Activity}'s {@code Window} background to be transparent, and hides the status
     * bar, if this {@code Activity}'s desired {@link BackgroundMode} is {@link
     * BackgroundMode#transparent}.
     *
     * <p>For {@code Activity} transparency to work as expected, the theme applied to this {@code
     * Activity} must include {@code <item name="android:windowIsTranslucent">true</item>}.
     */
    private void configureWindowForTransparency() {
        BackgroundMode backgroundMode = getBackgroundMode();
        if (backgroundMode == BackgroundMode.transparent) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    @NonNull
    private View createFlutterView() {
        return delegate.onCreateView(
                /* inflater=*/ null,
                /* container=*/ null,
                /* savedInstanceState=*/ null,
                /*flutterViewId=*/ FLUTTER_VIEW_ID,
                /*shouldDelayFirstAndroidViewDraw=*/ getRenderMode() == RenderMode.surface);
    }

    private void configureStatusBarForFullscreenFlutterExperience() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(0x40000000);
            window.getDecorView().setSystemUiVisibility(PlatformPlugin.DEFAULT_SYSTEM_UI);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START);
        if (stillAttachedForEvent("onStart")) {
            delegate.onStart();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME);
        if (stillAttachedForEvent("onResume")) {
            delegate.onResume();
        }
    }

    @Override
    public void onPostResume() {
        super.onPostResume();
        if (stillAttachedForEvent("onPostResume")) {
            delegate.onPostResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (stillAttachedForEvent("onPause")) {
            delegate.onPause();
        }
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (stillAttachedForEvent("onStop")) {
            delegate.onStop();
        }
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_STOP);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (stillAttachedForEvent("onSaveInstanceState")) {
            delegate.onSaveInstanceState(outState);
        }
    }

    /**
     * Irreversibly release this activity's control of the {@link
     * io.flutter.embedding.engine.FlutterEngine} and its subcomponents.
     *
     * <p>Calling will disconnect this activity's view from the Flutter renderer, disconnect this
     * activity from plugins' {@link ActivityControlSurface}, and stop system channel messages from
     * this activity.
     *
     * <p>After calling, this activity should be disposed immediately and not be re-used.
     */
    @VisibleForTesting
    public void release() {
        unregisterOnBackInvokedCallback();
        if (delegate != null) {
            delegate.release();
            delegate = null;
        }
    }

    @Override
    public void detachFromFlutterEngine() {
        Log.w(
                TAG,
                "TestFlutterActivity "
                        + this
                        + " connection to the engine "
                        + getFlutterEngine()
                        + " evicted by another attaching activity");
        if (delegate != null) {
            delegate.onDestroyView();
            delegate.onDetach();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (stillAttachedForEvent("onDestroy")) {
            delegate.onDestroyView();
            delegate.onDetach();
        }
        release();
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY);
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        Log.v("Vkohli","onPictureInPictureModeChanged called");
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        if (isInPictureInPictureMode) {

            if (HMSPipAction.Companion.getPipResult() != null) {
                HMSPipAction.Companion.getPipResult().success(true);
                HMSPipAction.Companion.setPipResult(null);
            }
        } else {
            Log.i("PIP Mode", "Exited PIP Mode");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v("Vkohli",""+requestCode+"-ScreenShare called");
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.SCREEN_SHARE_INTENT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data.setAction(Constants.HMSSDK_RECEIVER);
            Log.v("Vkohli",""+requestCode+"-Request code matched");
            this.sendBroadcast(data.putExtra(Constants.METHOD_CALL, Constants.SCREEN_SHARE_REQUEST));
        }

        if (requestCode == Constants.AUDIO_SHARE_INTENT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data.setAction(Constants.HMSSDK_RECEIVER);
            this.sendBroadcast(data.putExtra(Constants.METHOD_CALL, Constants.AUDIO_SHARE_REQUEST));
        }
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        // TODO(mattcarroll): change G3 lint rule that forces us to call super
        super.onNewIntent(intent);
        if (stillAttachedForEvent("onNewIntent")) {
            delegate.onNewIntent(intent);
        }
    }

    @Override
    public void onBackPressed() {
        if (stillAttachedForEvent("onBackPressed")) {
            delegate.onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (stillAttachedForEvent("onRequestPermissionsResult")) {
            delegate.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            HMSPipAction.Companion.autoEnterPipMode(this);
        }
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (stillAttachedForEvent("onTrimMemory")) {
            delegate.onTrimMemory(level);
        }
    }

    /**
     * {@link FlutterActivityAndFragmentDelegateTest.Host} method that is used by {@link
     * FlutterActivityAndFragmentDelegateTest} to obtain a {@code Context} reference as needed.
     */
    @Override
    @NonNull
    public Context getContext() {
        return this;
    }

    @Override
    @NonNull
    public Activity getActivity() {
        return this;
    }

    /**
     * {@link FlutterActivityAndFragmentDelegateTest.Host} method that is used by {@link
     * FlutterActivityAndFragmentDelegateTest} to obtain a {@code Lifecycle} reference as needed. This
     * reference is used by the delegate to provide Flutter plugins with access to lifecycle events.
     */
    @Override
    @NonNull
    public Lifecycle getLifecycle() {
        return lifecycle;
    }

    /**
     * {@link FlutterActivityAndFragmentDelegateTest.Host} method that is used by {@link
     * FlutterActivityAndFragmentDelegateTest} to obtain Flutter shell arguments when initializing
     * Flutter.
     */
    @NonNull
    @Override
    public FlutterShellArgs getFlutterShellArgs() {
        return FlutterShellArgs.fromIntent(getIntent());
    }

    /**
     * Returns the ID of a statically cached {@link io.flutter.embedding.engine.FlutterEngine} to use
     * within this {@code TestFlutterActivity}, or {@code null} if this {@code TestFlutterActivity} does not
     * want to use a cached {@link io.flutter.embedding.engine.FlutterEngine}.
     */
    @Override
    @Nullable
    public String getCachedEngineId() {
        return getIntent().getStringExtra(EXTRA_CACHED_ENGINE_ID);
    }

    /**
     * Returns false if the {@link io.flutter.embedding.engine.FlutterEngine} backing this {@code
     * TestFlutterActivity} should outlive this {@code TestFlutterActivity}, or true to be destroyed when the
     * {@code TestFlutterActivity} is destroyed.
     *
     * <p>The default value is {@code true} in cases where {@code TestFlutterActivity} created its own
     * {@link io.flutter.embedding.engine.FlutterEngine}, and {@code false} in cases where a cached
     * {@link io.flutter.embedding.engine.FlutterEngine} was provided.
     */
    @Override
    public boolean shouldDestroyEngineWithHost() {
        boolean explicitDestructionRequested =
                getIntent().getBooleanExtra(EXTRA_DESTROY_ENGINE_WITH_ACTIVITY, false);
        if (getCachedEngineId() != null || delegate.isFlutterEngineFromHost()) {
            // Only destroy a cached engine if explicitly requested by app developer.
            return explicitDestructionRequested;
        } else {
            // If this Activity created the FlutterEngine, destroy it by default unless
            // explicitly requested not to.
            return getIntent().getBooleanExtra(EXTRA_DESTROY_ENGINE_WITH_ACTIVITY, true);
        }
    }

    @NonNull
    public String getDartEntrypointFunctionName() {
        try {
            Bundle metaData = getMetaData();
            String desiredDartEntrypoint =
                    metaData != null ? metaData.getString(DART_ENTRYPOINT_META_DATA_KEY) : null;
            return desiredDartEntrypoint != null ? desiredDartEntrypoint : DEFAULT_DART_ENTRYPOINT;
        } catch (PackageManager.NameNotFoundException e) {
            return DEFAULT_DART_ENTRYPOINT;
        }
    }

    /**
     * The Dart entrypoint arguments will be passed as a list of string to Dart's entrypoint function.
     *
     * <p>A value of null means do not pass any arguments to Dart's entrypoint function.
     *
     * <p>Subclasses may override this method to directly control the Dart entrypoint arguments.
     */
    @Nullable
    public List<String> getDartEntrypointArgs() {
        return (List<String>) getIntent().getSerializableExtra(EXTRA_DART_ENTRYPOINT_ARGS);
    }

    @Nullable
    public String getDartEntrypointLibraryUri() {
        try {
            Bundle metaData = getMetaData();
            String desiredDartLibraryUri =
                    metaData != null ? metaData.getString(DART_ENTRYPOINT_URI_META_DATA_KEY) : null;
            return desiredDartLibraryUri;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }


    public String getInitialRoute() {
        if (getIntent().hasExtra(EXTRA_INITIAL_ROUTE)) {
            return getIntent().getStringExtra(EXTRA_INITIAL_ROUTE);
        }

        try {
            Bundle metaData = getMetaData();
            String desiredInitialRoute =
                    metaData != null ? metaData.getString(INITIAL_ROUTE_META_DATA_KEY) : null;
            return desiredInitialRoute;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    /**
     * A custom path to the bundle that contains this Flutter app's resources, e.g., Dart code
     * snapshots.
     *
     * <p>When this {@code TestFlutterActivity} is run by Flutter tooling and a data String is included in
     * the launching {@code Intent}, that data String is interpreted as an app bundle path.
     *
     * <p>When otherwise unspecified, the value is null, which defaults to the app bundle path defined
     * in {@link io.flutter.embedding.engine.loader.FlutterLoader#findAppBundlePath()}.
     *
     * <p>Subclasses may override this method to return a custom app bundle path.
     */
    @NonNull
    public String getAppBundlePath() {
        // If this Activity was launched from tooling, and the incoming Intent contains
        // a custom app bundle path, return that path.
        // TODO(mattcarroll): determine if we should have an explicit FlutterTestActivity instead of
        // conflating.
        if (isDebuggable() && Intent.ACTION_RUN.equals(getIntent().getAction())) {
            String appBundlePath = getIntent().getDataString();
            if (appBundlePath != null) {
                return appBundlePath;
            }
        }

        return null;
    }

    /**
     * Returns true if Flutter is running in "debug mode", and false otherwise.
     *
     * <p>Debug mode allows Flutter to operate with hot reload and hot restart. Release mode does not.
     */
    private boolean isDebuggable() {
        return (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }

    @NonNull
    @Override
    public RenderMode getRenderMode() {
        return getBackgroundMode() == BackgroundMode.opaque ? RenderMode.surface : RenderMode.texture;
    }

    @NonNull
    @Override
    public TransparencyMode getTransparencyMode() {
        return getBackgroundMode() == BackgroundMode.opaque
                ? TransparencyMode.opaque
                : TransparencyMode.transparent;
    }

    /**
     * The desired window background mode of this {@code Activity}, which defaults to {@link
     * BackgroundMode#opaque}.
     *
     * @return The background mode.
     */
    @NonNull
    protected BackgroundMode getBackgroundMode() {
        if (getIntent().hasExtra(EXTRA_BACKGROUND_MODE)) {
            return BackgroundMode.valueOf(getIntent().getStringExtra(EXTRA_BACKGROUND_MODE));
        } else {
            return BackgroundMode.opaque;
        }
    }

    /**
     * Hook for subclasses to easily provide a custom {@link
     * io.flutter.embedding.engine.FlutterEngine}.
     *
     * <p>This hook is where a cached {@link io.flutter.embedding.engine.FlutterEngine} should be
     * provided, if a cached {@link FlutterEngine} is desired.
     */
    @Nullable
    @Override
    public FlutterEngine provideFlutterEngine(@NonNull Context context) {
        // No-op. Hook for subclasses.
        return null;
    }

    /**
     * Hook for subclasses to obtain a reference to the {@link
     * io.flutter.embedding.engine.FlutterEngine} that is owned by this {@code TestFlutterActivity}.
     *
     * @return The Flutter engine.
     */
    @Nullable
    protected FlutterEngine getFlutterEngine() {
        return delegate.getFlutterEngine();
    }

    /**
     * Retrieves the meta data specified in the AndroidManifest.xml.
     *
     * @return The meta data.
     * @throws PackageManager.NameNotFoundException if a package with the given name cannot be found
     *     on the system.
     */
    @Nullable
    protected Bundle getMetaData() throws PackageManager.NameNotFoundException {
        ActivityInfo activityInfo =
                getPackageManager().getActivityInfo(getComponentName(), PackageManager.GET_META_DATA);
        return activityInfo.metaData;
    }

    @Nullable
    @Override
    public PlatformPlugin providePlatformPlugin(
            @Nullable Activity activity, @NonNull FlutterEngine flutterEngine) {
        return new PlatformPlugin(getActivity(), flutterEngine.getPlatformChannel(), this);
    }

    /**
     * Hook for subclasses to easily configure a {@code FlutterEngine}.
     *
     * <p>This method is called after {@link #provideFlutterEngine(Context)}.
     *
     * <p>All plugins listed in the app's pubspec are registered in the base implementation of this
     * method unless the FlutterEngine for this activity was externally created. To avoid the
     * automatic plugin registration for implicitly created FlutterEngines, override this method
     * without invoking super(). To keep automatic plugin registration and further configure the
     * FlutterEngine, override this method, invoke super(), and then configure the FlutterEngine as
     * desired.
     */
    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        if (delegate.isFlutterEngineFromHost()) {
            // If the FlutterEngine was explicitly built and injected into this TestFlutterActivity, the
            // builder should explicitly decide whether to automatically register plugins via the
            // FlutterEngine's construction parameter or via the AndroidManifest metadata.
            return;
        }

        GeneratedPluginRegister.registerGeneratedPlugins(flutterEngine);
    }

    /**
     * Hook for the host to cleanup references that were established in {@link
     * #configureFlutterEngine(FlutterEngine)} before the host is destroyed or detached.
     *
     * <p>This method is called in {@link #onDestroy()}.
     */
    @Override
    public void cleanUpFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        // No-op. Hook for subclasses.
    }

   
    @Override
    public boolean shouldAttachEngineToActivity() {
        return true;
    }

    @Override
    public boolean shouldHandleDeeplinking() {
        try {
            Bundle metaData = getMetaData();
            boolean shouldHandleDeeplinking =
                    metaData != null ? metaData.getBoolean(HANDLE_DEEPLINKING_META_DATA_KEY) : false;
            return shouldHandleDeeplinking;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Override
    public void onFlutterSurfaceViewCreated(@NonNull FlutterSurfaceView flutterSurfaceView) {
        // Hook for subclasses.
    }

    @Override
    public void onFlutterTextureViewCreated(@NonNull FlutterTextureView flutterTextureView) {
        // Hook for subclasses.
    }

    @Override
    public void onFlutterUiDisplayed() {
        // Notifies Android that we're fully drawn so that performance metrics can be collected by
        // Flutter performance tests. A few considerations:
        // * reportFullyDrawn was supported in KitKat (API 19), but has a bug around requiring
        // permissions in some Android versions.
        // * reportFullyDrawn behavior isn't tested on pre-Q versions.
        // See https://github.com/flutter/flutter/issues/46172, and
        // https://github.com/flutter/flutter/issues/88767.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            reportFullyDrawn();
        }
    }

    @Override
    public void onFlutterUiNoLongerDisplayed() {
        // no-op
    }

    @Override
    public boolean shouldRestoreAndSaveState() {
        if (getIntent().hasExtra(EXTRA_ENABLE_STATE_RESTORATION)) {
            return getIntent().getBooleanExtra(EXTRA_ENABLE_STATE_RESTORATION, false);
        }
        if (getCachedEngineId() != null) {
            // Prevent overwriting the existing state in a cached engine with restoration state.
            return false;
        }
        return true;
    }

    /**
     * Give the host application a chance to take control of the app lifecycle events.
     *
     * <p>Return {@code false} means the host application dispatches these app lifecycle events, while
     * return {@code true} means the engine dispatches these events.
     *
     * <p>Defaults to {@code true}.
     */
    @Override
    public boolean shouldDispatchAppLifecycleState() {
        return true;
    }

    @Override
    public boolean popSystemNavigator() {
        // Hook for subclass. No-op if returns false.
        return false;
    }

    @Override
    public void updateSystemUiOverlays() {
        if (delegate != null) {
            delegate.updateSystemUiOverlays();
        }
    }

    private boolean stillAttachedForEvent(String event) {
        if (delegate == null) {
            Log.w(TAG, "TestFlutterActivity " + hashCode() + " " + event + " called after release.");
            return false;
        }
        if (!delegate.isAttached()) {
            Log.w(TAG, "TestFlutterActivity " + hashCode() + " " + event + " called after detach.");
            return false;
        }
        return true;
    }
}
