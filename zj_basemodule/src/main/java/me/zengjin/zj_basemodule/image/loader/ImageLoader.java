package me.zengjin.zj_basemodule.image.loader;

import android.net.Uri;

import java.io.File;

/**
 * 图片加载类
 * 策略或者静态代理模式，开发者只需要关心ImageLoader + LoaderOptions
 *
 * ※※※※流程:
 * imageloader的整一个流程就是getinstance->load(url),然后得到一个loaderoption对象，然后设置这个对象的信息，
 * 最后 在into（imageview）的时候，调用真正的loader的loadimage策略去加载图片
 * ※※※※依赖关系:
 * 1.realloader impliment ILoaderStrategy策略
 * 2.loaderoption是单独的，用来规定加载图片的一些固有参数
 * 3.imageloader依赖ILoaderStrategy和loaderoption,主要是用来构造一个loaderoption对象
 * 2-3总结，imageloader通过构造loaderoption对象进行设置参数，最后option的into方法又调用
 * imager中的ILoaderStrategy，把option传给它，间接使用realloader加载图片
 */

public class ImageLoader{
	private static ILoaderStrategy sLoader;
	private static volatile ImageLoader mInstance;

	private ImageLoader() {
	}

	//单例模式
	public static ImageLoader getInstance() {
		if (mInstance == null) {
			synchronized (ImageLoader.class) {
				if (mInstance == null) {
					mInstance = new ImageLoader();
				}
			}
		}
		return mInstance;
	}

	public static ILoaderStrategy getRealLoader() {
		return sLoader;
	}

	/**
	 * 提供全局替换图片加载框架的接口，若切换其它框架，可以实现一键全局替换
	 */
	public void setGlobalImageLoader(ILoaderStrategy loader) {
		sLoader = loader;
	}

	public LoaderOptions load(String url) {
		return new LoaderOptions(url);
	}
	public LoaderOptions load(int drawable) {
		return new LoaderOptions(drawable);
	}

	public LoaderOptions load(File file) {
		return new LoaderOptions(file);
	}

	public LoaderOptions load(Uri uri) {
		return new LoaderOptions(uri);
	}



	public void clearMemoryCache() {
		checkNotNull();
		sLoader.clearMemoryCache();
	}

	public void clearDiskCache() {
		checkNotNull();
		sLoader.clearDiskCache();
	}

	private void checkNotNull() {
		if (sLoader == null) {
			throw new NullPointerException("you must be set your imageLoader at first!");
		}
	}
}
