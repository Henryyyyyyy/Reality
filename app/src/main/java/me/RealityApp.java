package me;

import android.app.Application;

import me.zengjin.imgframework.PicassoLoader;
import me.zengjin.zj_basemodule.image.loader.ImageLoader;

public class RealityApp extends Application{
    public static RealityApp gApp;
    @Override
    public void onCreate() {
        super.onCreate();
        gApp = this;
        //初始化图片库
        ImageLoader.getInstance().setGlobalImageLoader(new PicassoLoader());
    }
}
