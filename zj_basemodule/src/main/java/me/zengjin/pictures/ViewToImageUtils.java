package me.zengjin.pictures;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.ColorInt;
import android.view.View;
import android.view.ViewGroup;

import java.io.FileOutputStream;
import java.io.IOException;

public class ViewToImageUtils {

    public static int DEFAULT_BACKGROUND_COLOR = Color.WHITE;

    public static void viewToImage(ViewGroup view, String path, onFinishCallback callback) {
        viewToImage(view, path, DEFAULT_BACKGROUND_COLOR, callback);
    }

    /**
     * 把View保存为图片
     * <p>
     * 注意：如果发现 WebView 绘制不完整，请添加下面代码关闭硬件加速
     * mWebview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
     */
    public static void viewToImage(ViewGroup view, String path, @ColorInt int backgroundColor, onFinishCallback callback) {
        final Bitmap bitmap = getViewGroupBitmap(view, backgroundColor);
        new Thread(() -> {
            FileOutputStream output = null;
            try {
                output = new FileOutputStream(path);
                boolean isSuccess = bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (callback != null) {
                        callback.onFinish(isSuccess);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (output != null) {
                    try {
                        output.close();
                    } catch (IOException e) {
                    }
                }
            }
            bitmap.recycle();
        }).start();
    }

    /**
     * 生成某个ViewGroup的图片
     */
    public static Bitmap getViewGroupBitmap(ViewGroup view, @ColorInt int backgroundColor) {
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        int w = view.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
//        int w = getWindowManager().getDefaultDisplay().getWidth();
        int h = 0;
        // 获取ViewGroup实际高度
        for (int i = 0; i < view.getChildCount(); i++) {
            View child = view.getChildAt(i);
//            child.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            lp = (ViewGroup.MarginLayoutParams) child.getLayoutParams();
            h += (child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
        }
//        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        // 创建对应大小的bitmap
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444);
        final Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(backgroundColor);
        view.draw(canvas);
        return bitmap;
    }

    public interface onFinishCallback {
        void onFinish(boolean isSuccess);
    }
    /**
     * demo:
     *
     */
    /*
    private void generateImage(ViewGroup view, int position) {
        mBaseView.showProgressDialog("正在生成图片");
        File file = new File(mActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_temp.png");
        ViewToImageUtils.viewToImage(view, file.getPath(), isSuccess -> {
            mBaseView.dismissDialog();
            if (!isSuccess) {
                mBaseView.showToastMessage("图片生成失败");
                return;
            }
            shareByImage(position, file.getPath());
        });
    }
     */

}