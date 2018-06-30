package me.zengjin.imgframework;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import me.zengjin.reality.R;
import me.zengjin.zj_basemodule.image.loader.ImageLoader;

public class LoadImageActivity extends AppCompatActivity {
ImageView iv_image;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_image);
        iv_image=findViewById(R.id.iv_image);
        String url = "http://ww2.sinaimg.cn/large/7a8aed7bgw1eutsd0pgiwj20go0p0djn.jpg";
        ImageLoader.getInstance()
                .load(url)
                .skipLocalCache(true)
                .into(iv_image);

    }
}
