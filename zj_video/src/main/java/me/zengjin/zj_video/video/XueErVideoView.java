package me.zengjin.zj_video.video;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import me.zengjin.zj_video.R;
import me.zengjin.zj_video.utils.DimenUtil;


/**
 * Created by henry on 2017/12/27.
 */

public class XueErVideoView extends RelativeLayout implements TextureView.SurfaceTextureListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnInfoListener, MediaPlayer.OnErrorListener, MediaPlayer.OnBufferingUpdateListener {
    private static final String TAG = "XueErVideoView";

    public int viewWidth, viewHeigh;
    private TextureView texture_video;
    private ProgressBar pb_loading;
   // private ImageView iv_cover;
    private ImageView iv_playorstop;
    private Context mContext;
    //---
    private static final int LOAD_TOTAL_COUNT = 3;
    private static final int STATE_ERROR = -1;
    private static final int STATE_IDLE = 0;//闲置状态，相当于停止，重新开始？
    private static final int STATE_PLAYING = 1;
    private static final int STATE_PAUSING = 2;
    private static final int STATE_LOADING = 3;
    private Surface mVideoSurface;
    public String mVideoUrl;
    private boolean isMute = false;
    private int mCurrentCount = 0;
    private int playerState = STATE_IDLE;
    private ScreenEventReceiver mScreenReceiver;
    private MediaPlayer mediaPlayer;
    public static float HEIGHT_DEVIDE_WIDTH_PRESENTAGE = 9 / 16.0f;
    private int mScreenWidth;
    private int mScreenHeight;

    public XueErVideoView(Context context) {
        this(context, null);
    }

    public XueErVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XueErVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = getContext();
        initView();
        setListeners();
    }


    LayoutParams params;

    private void initView() {
    View.inflate(mContext,R.layout.container_video,this);
    texture_video=findViewById(R.id.texture_video);
        //添加textureview,
         mScreenWidth = DimenUtil.getScreenWidth(mContext);
         mScreenHeight = DimenUtil.getScreenHeight(mContext);
        int height = (int) (mScreenWidth * HEIGHT_DEVIDE_WIDTH_PRESENTAGE);
        params = new LayoutParams(LayoutParams.MATCH_PARENT, height);
        params.addRule(CENTER_IN_PARENT);
        texture_video.setLayoutParams(params);//新加
//        texture_video = new TextureView(mContext);
//        addView(texture_video, params);
//        添加imageview  cover
//        iv_cover = new ImageView(mContext);
//        iv_cover.setScaleType(ImageView.ScaleType.CENTER_CROP);
//        addView(iv_cover, params);
//        添加progressbar
        pb_loading = new ProgressBar(mContext, null, android.R.attr.progressBarStyle);
        params = new LayoutParams(DimenUtil.dp2px(mContext, 30f), DimenUtil.dp2px(mContext, 30f));
        params.addRule(CENTER_IN_PARENT);
        pb_loading.setVisibility(GONE);
        addView(pb_loading, params);
        //添加播放暂停按钮
        iv_playorstop = new ImageView(mContext);
        params = new LayoutParams(DimenUtil.dp2px(mContext, 20f), DimenUtil.dp2px(mContext, 20f));
        params.topMargin = DimenUtil.dp2px(mContext, 15);
        params.leftMargin = DimenUtil.dp2px(mContext, 15);
        iv_playorstop.setImageResource(R.mipmap.icon_play);
        addView(iv_playorstop, params);


    }


    private void setListeners() {
        iv_playorstop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (playerState == STATE_PLAYING) {
                    pause();
                } else if (playerState == STATE_PAUSING) {
                    start();
                } else if (playerState == STATE_IDLE) {
                    load();
                }
            }
        });
    }

    /**
     * 在onChildViewAttachedToWindow调用
     */
    public void onInitMediaPlayer() {

        Log.d(TAG, "onInitMediaPlayer");
        if (texture_video != null) {
            texture_video.setSurfaceTextureListener(this);
        }
        registerBroadcastReceiver();
    }


    /**
     * 在onChildViewDetachedFromWindow调用
     * 停止，释放资源,重新加载
     */
    public void onDestroyMediaPlayer() {
        Log.e(TAG, "destroy-----------");
        if (this.mediaPlayer != null) {
            this.mediaPlayer.reset();
            this.mediaPlayer.setOnSeekCompleteListener(null);
            this.mediaPlayer.stop();
            this.mediaPlayer.release();
            this.mediaPlayer = null;
        }
        mVideoSurface = null;
        setCurrentPlayState(STATE_IDLE);
        unRegisterBroadcastReceiver();
        mScreenReceiver = null;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int w, int h) {
        mVideoSurface = new Surface(surfaceTexture);
        createMediaPlayer();
        if (mediaPlayer != null) {
            mediaPlayer.setSurface(mVideoSurface);
        }
    }

    private synchronized void createMediaPlayer() {

        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.reset();
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnInfoListener(this);
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            if (mVideoSurface != null && mVideoSurface.isValid()) {
                mediaPlayer.setSurface(mVideoSurface);
            }
        }
    }

    public void load() {
        Log.e(TAG, "load...");
        if (this.playerState != STATE_IDLE) {
            return;
        }
        setCurrentPlayState(STATE_LOADING);
        try {

            mute(false);
            mediaPlayer.setDataSource(mVideoUrl);
            mediaPlayer.prepareAsync(); //开始异步加载
        } catch (Exception e) {
            setCurrentPlayState(STATE_ERROR);
            Log.e(TAG, e.getMessage());
            // reload();
            //  stop(); //error以后重新调用stop加载
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mediaPlayer = mp;
        if (mediaPlayer != null) {
            Log.e(TAG, "width="+mediaPlayer.getVideoWidth()+"     height="+mediaPlayer.getVideoHeight());
            mediaPlayer.setOnBufferingUpdateListener(this);
            mCurrentCount = 0;
            //---刚初始化的时候改变textureview的大小
            int width=mediaPlayer.getVideoWidth();
            int height=mediaPlayer.getVideoHeight();
            HEIGHT_DEVIDE_WIDTH_PRESENTAGE=height*1.0f/width*1.0f;
            rejustSize();
            start();

        }
    }

    public void start() {
        if (!isPlaying()) {
            setCurrentPlayState(STATE_PLAYING);
            mediaPlayer.setOnSeekCompleteListener(null);
            mediaPlayer.start();
        } else {

        }
    }

    public void pause() {
        if (this.playerState != STATE_PLAYING) {
            return;
        }
        Log.d(TAG, "do pause");
        setCurrentPlayState(STATE_PAUSING);
        if (isPlaying()) {
            mediaPlayer.pause();

        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        playBack();
    }

    //播放完成后回到初始状态
    public void playBack() {
        Log.d(TAG, " do playBack");
        setCurrentPlayState(STATE_PAUSING);
        if (mediaPlayer != null) {
            mediaPlayer.setOnSeekCompleteListener(null);
            mediaPlayer.seekTo(0);
            mediaPlayer.pause();
        }
    }

@Deprecated
    public void setCover(String url) {
    //    if (iv_cover != null) {
     //       //todo 抽象成接口
//            Glide.with(mContext)
//                    .load(url)
//                    .into(iv_cover);

    //    }
    }

    public void setDataSource(String url) {
        mVideoUrl = url;

    }

    /**
     * true is no voice
     * 默认是false，有声音
     *
     * @param mute
     */
    public void mute(boolean mute) {
        isMute = mute;
        if (mediaPlayer != null) {
            float volume = isMute ? 0.0f : 1.0f;
            mediaPlayer.setVolume(volume, volume);
        }
    }

    private void registerBroadcastReceiver() {
        if (mScreenReceiver == null) {
            mScreenReceiver = new ScreenEventReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction(Intent.ACTION_USER_PRESENT);
            filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
            getContext().registerReceiver(mScreenReceiver, filter);
        }
    }

    private void unRegisterBroadcastReceiver() {
        if (mScreenReceiver != null) {
            getContext().unregisterReceiver(mScreenReceiver);
        }
    }


    /**
     * 监听锁屏事件的广播接收器
     */
    private class ScreenEventReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //主动锁屏时 pause, 主动解锁屏幕时，resume
            switch (intent.getAction()) {
                case Intent.ACTION_USER_PRESENT:
                    if (playerState == STATE_PAUSING) {
                        if (mediaPlayer != null)
                            if (mediaPlayer.getCurrentPosition() != 0)
                                start();
                    }
                    break;
                case Intent.ACTION_SCREEN_OFF:
                    if (playerState == STATE_PLAYING) {
                        pause();
                    }
                    break;

                case Intent.ACTION_CONFIGURATION_CHANGED:
                    rejustSize();
                    break;
            }
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int i1) {
        Log.e(TAG, "do error:" + what);
        this.playerState = STATE_ERROR;
        mediaPlayer = mp;
        if (mediaPlayer != null) {
            mediaPlayer.reset();
        }
        if (mCurrentCount >= LOAD_TOTAL_COUNT) {
            setCurrentPlayState(STATE_ERROR);
        }
        return true;
    }

    /**
     * 判断是否在播放
     *
     * @return
     */
    public boolean isPlaying() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }


    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int w, int h) {

    }
    public void rejustSize(){
        //横竖屏切换的时候要再获取一次
    mScreenWidth = DimenUtil.getScreenWidth(mContext);
    mScreenHeight = DimenUtil.getScreenHeight(mContext);

    Configuration mConfiguration = mContext.getResources().getConfiguration();
    if (mConfiguration.orientation==Configuration.ORIENTATION_PORTRAIT){//竖屏
        texture_video.getLayoutParams().width=mScreenWidth;
        texture_video.getLayoutParams().height= (int) (mScreenWidth*HEIGHT_DEVIDE_WIDTH_PRESENTAGE);
    }else {//横屏
        texture_video.getLayoutParams().width=(int) (mScreenHeight/HEIGHT_DEVIDE_WIDTH_PRESENTAGE);
        texture_video.getLayoutParams().height= mScreenHeight;
    }
    requestLayout();

}
    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {

    }

    private void setCurrentPlayState(int state) {
        playerState = state;
        switch (state) {
            case STATE_IDLE:
                iv_playorstop.setImageResource(R.mipmap.icon_play);
                //iv_cover.setVisibility(VISIBLE);
                pb_loading.setVisibility(GONE);
                break;
            case STATE_PAUSING:
                iv_playorstop.setImageResource(R.mipmap.icon_play);
               // iv_cover.setVisibility(GONE);
                pb_loading.setVisibility(GONE);
                break;

            case STATE_PLAYING:
                iv_playorstop.setImageResource(R.mipmap.icon_stop);
               // iv_cover.setVisibility(GONE);
                pb_loading.setVisibility(GONE);
                break;
            case STATE_LOADING:
                iv_playorstop.setImageResource(R.mipmap.icon_stop);
               // iv_cover.setVisibility(GONE);
                pb_loading.setVisibility(VISIBLE);
                break;
            case STATE_ERROR:
                iv_playorstop.setImageResource(R.mipmap.icon_play);
               // iv_cover.setVisibility(VISIBLE);
                pb_loading.setVisibility(GONE);
                break;
        }
    }
}
