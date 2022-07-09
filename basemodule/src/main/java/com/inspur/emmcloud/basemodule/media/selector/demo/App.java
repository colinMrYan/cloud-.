package com.inspur.emmcloud.basemodule.media.selector.demo;

import android.app.Application;
import android.content.Context;

import com.inspur.emmcloud.basemodule.media.selector.app.IApp;
import com.inspur.emmcloud.basemodule.media.selector.app.PictureAppMaster;
import com.inspur.emmcloud.basemodule.media.selector.engine.PictureSelectorEngine;

//import androidx.annotation.NonNull;
//import androidx.camera.camera2.Camera2Config;
//import androidx.camera.core.CameraXConfig;

//import coil.ComponentRegistry;
//import coil.ImageLoader;
//import coil.ImageLoaderFactory;
//import coil.decode.GifDecoder;
//import coil.decode.ImageDecoderDecoder;
//import coil.decode.VideoFrameDecoder;
//import coil.util.CoilUtils;
//import okhttp3.OkHttpClient;


/**
 * @author：luck
 * @date：2019-12-03 22:53
 * @describe：Application
 */
//public class App extends Application implements IApp, ImageLoaderFactory {
public class App extends Application implements IApp {
    private static final String TAG = App.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        PictureAppMaster.getInstance().setApp(this);
    }

    @Override
    public Context getAppContext() {
        return this;
    }

    @Override
    public PictureSelectorEngine getPictureSelectorEngine() {
        return new PictureSelectorEngineImp();
    }

//    @NonNull
//    @Override
//    public ImageLoader newImageLoader() {
//        ComponentRegistry registry = new ComponentRegistry.Builder()
//                .add(SDK_INT >= 28 ? new ImageDecoderDecoder(getAppContext()) : new GifDecoder())
//                .add(new VideoFrameDecoder(getAppContext()))
//                .build();
//        return new ImageLoader.Builder(getAppContext())
//                .componentRegistry(registry)
//                .crossfade(true)
//                .okHttpClient(new OkHttpClient.Builder()
//                        .cache(CoilUtils.createDefaultCache(getAppContext())).build())
//                .availableMemoryPercentage(0.5)
//                .allowHardware(false)
//                .allowRgb565(true)
//                .build();
//    }
}
