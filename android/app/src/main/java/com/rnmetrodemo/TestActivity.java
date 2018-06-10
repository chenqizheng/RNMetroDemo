package com.rnmetrodemo;

import android.app.Activity;
import android.os.Bundle;

import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactRootView;
import com.facebook.react.bridge.CatalystInstanceImpl;
import com.facebook.react.bridge.JSBundleLoader;
import com.facebook.react.common.LifecycleState;
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;
import com.facebook.react.shell.MainReactPackage;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

/**
 * Created by Chen on 2018/6/9.
 */

public class TestActivity extends Activity implements DefaultHardwareBackBtnHandler {
    private ReactRootView mReactRootView;
    private ReactInstanceManager mReactInstanceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mReactRootView = new ReactRootView(this);
        mReactInstanceManager = ReactInstanceManager.builder()
                .setApplication(getApplication())
                .setBundleAssetName("index.android.bundle")
                .setJSMainModulePath("index")
                .addPackage(new MainReactPackage())
                .setUseDeveloperSupport(BuildConfig.DEBUG)
                .setInitialLifecycleState(LifecycleState.RESUMED)
                .build();
        ReactInstanceManager reactInstanceManager = mReactInstanceManager;
        Class<?> cls = ReactInstanceManager.class;
        Field f = null;
        try {
            f = cls.getDeclaredField("mBundleLoader");
            f.setAccessible(true);
            f.set(reactInstanceManager, new BaseBundleLoader("/sdcard/index.android.bundle"));
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        // 注意这里的MyReactNativeApp必须对应“index.js”中的
        // “AppRegistry.registerComponent()”的第一个参数
        mReactRootView.startReactApplication(mReactInstanceManager, "RNMetroDemo", null);

        setContentView(mReactRootView);
    }

    @Override
    public void invokeDefaultOnBackPressed() {
        super.onBackPressed();
    }


    private class BaseBundleLoader extends JSBundleLoader {
        private static final String BASE_BUNDLE_NAME = "base.android.bundle";
        private static final String BUNDLE_ASSET_FOLDER = "base";
        private final String BASE_BUNDLE_ASSET = "assets://" + BUNDLE_ASSET_FOLDER + File.separator + BASE_BUNDLE_NAME;
        private String bundleLocation;

        BaseBundleLoader(String location) {
            bundleLocation = location;
        }

        private boolean existAssetBaseBundle() {
            try {
                String[] assets = getAssets().list(BUNDLE_ASSET_FOLDER);
                for (String asset : assets) {
                    if (BASE_BUNDLE_NAME.equals(asset)) {
                        return true;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        public String loadScript(CatalystInstanceImpl instance) {
            File bundle = new File(bundleLocation);
            File base = new File(bundle.getParent(), BASE_BUNDLE_NAME);
            if (base.exists()) {
                JSBundleLoader.createFileLoader(base.getPath()).loadScript(instance);
            } else if (existAssetBaseBundle()) {
                JSBundleLoader.createAssetLoader(getApplication(), BASE_BUNDLE_ASSET, false).loadScript(instance);
            }
            JSBundleLoader.createFileLoader(bundleLocation).loadScript(instance);
            return bundleLocation;
        }


    }
}