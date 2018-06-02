package me.zengjin.customvideo;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;

import me.zengjin.reality.R;
import me.zengjin.zj_video.video.XueErVideoView;

public class CustomVideoActivity extends AppCompatActivity {
private XueErVideoView xueer_video;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_custom_video);
        xueer_video=findViewById(R.id.xueer_video);
        xueer_video.onInitMediaPlayer();
        xueer_video.setDataSource("http://ovj1az9fj.bkt.clouddn.com/1513759159554.mp4");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        xueer_video.onDestroyMediaPlayer();
    }
}
