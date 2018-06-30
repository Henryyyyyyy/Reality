package me.zengjin.zj_basemodule.image.loader;

import android.graphics.Bitmap;



public interface BitmapCallBack {

	void onBitmapLoaded(Bitmap bitmap);

	void onBitmapFailed(Exception e);

	public static class EmptyCallback implements BitmapCallBack {


		@Override
		public void onBitmapLoaded(Bitmap bitmap) {

		}

		@Override
		public void onBitmapFailed(Exception e) {

		}
	}
}
