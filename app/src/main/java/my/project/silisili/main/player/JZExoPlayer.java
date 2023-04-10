package my.project.silisili.main.player;

import static com.google.android.exoplayer2.Player.DISCONTINUITY_REASON_SEEK;
import static com.google.android.exoplayer2.Player.REPEAT_MODE_OFF;
import static com.google.android.exoplayer2.Player.REPEAT_MODE_ONE;
import static com.google.android.exoplayer2.Player.STATE_BUFFERING;
import static com.google.android.exoplayer2.Player.STATE_ENDED;
import static com.google.android.exoplayer2.Player.STATE_IDLE;
import static com.google.android.exoplayer2.Player.STATE_READY;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.ExoTrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoSize;

import cn.jzvd.JZMediaInterface;
import cn.jzvd.Jzvd;
import my.project.silisili.R;

/**
 * 饺子播放器的内核-使用ExoPlayer
 */
public class JZExoPlayer extends JZMediaInterface implements Player.Listener {
    private ExoPlayer exoPlayer;
    private Runnable callback;
    private final String TAG = "JZExoPlayer";
    private long previousSeek = 0;

    public JZExoPlayer(Jzvd jzvd) {
        super(jzvd);
    }

    @Override
    public void start() {
        exoPlayer.setPlayWhenReady(true);
    }

    @Override
    public void prepare() {
        Log.e(TAG, "prepare");
        Context context = jzvd.getContext();

        release();
        mMediaHandlerThread = new HandlerThread("JZVD");
        mMediaHandlerThread.start();
        mMediaHandler = new Handler(context.getMainLooper());//主线程还是非主线程，就在这里
        handler = new Handler();
        mMediaHandler.post(() -> {

            ExoTrackSelection.Factory videoTrackSelectionFactory =
                    new AdaptiveTrackSelection.Factory();
            TrackSelector trackSelector =
                    new DefaultTrackSelector(context, videoTrackSelectionFactory);

            LoadControl loadControl = new DefaultLoadControl.Builder()
                    .setAllocator(new DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE))
                    .setBufferDurationsMs(360000, 600000, 1000, 5000)
                    .setPrioritizeTimeOverSizeThresholds(false)
                    .setTargetBufferBytes(C.LENGTH_UNSET)
                    .build();


            BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter.Builder(context).build();
            // 2. Create the player

            RenderersFactory renderersFactory = new DefaultRenderersFactory(context);
            exoPlayer = new ExoPlayer.Builder(context, renderersFactory)
                    .setTrackSelector(trackSelector)
                    .setLoadControl(loadControl)
                    .setBandwidthMeter(bandwidthMeter)
                    .build();
            // Produces DataSource instances through which media data is loaded.
            DefaultHttpDataSource.Factory httpDataSource = new DefaultHttpDataSource.Factory();
            httpDataSource.setUserAgent(Util.getUserAgent(context, context.getResources().getString(R.string.app_name)));
            DataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(context, httpDataSource);

            String currUrl = jzvd.jzDataSource.getCurrentUrl().toString();
            MediaSource videoSource;
            if (currUrl.contains(".m3u8")) {
                videoSource = new HlsMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(MediaItem.fromUri(Uri.parse(currUrl)));
                //addEventListener 这里只有两个参数都要传入值才可以成功设置
                // 否者会被断言 Assertions.checkArgument(handler != null && eventListener != null);
                // 并且报错  IllegalArgumentException()  所以不需要添加监听器时 注释掉
                //      videoSource .addEventListener( handler, null);
            } else {
                videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(MediaItem.fromUri(Uri.parse(currUrl)));
            }

            Log.e(TAG, "URL Link = " + currUrl);

            exoPlayer.addListener(this);
            boolean isLoop = jzvd.jzDataSource.looping;
            if (isLoop) {
                exoPlayer.setRepeatMode(REPEAT_MODE_ONE);
            } else {
                exoPlayer.setRepeatMode(REPEAT_MODE_OFF);
            }

            exoPlayer.setMediaSource(videoSource);
            exoPlayer.prepare();
            exoPlayer.setPlayWhenReady(true);
            callback = new onBufferingUpdate();

            if (jzvd.textureView != null) {
                SurfaceTexture surfaceTexture = jzvd.textureView.getSurfaceTexture();
                if (surfaceTexture != null) {
                    exoPlayer.setVideoSurface(new Surface(surfaceTexture));
                }
            }
        });
    }

    @Override
    public void onVideoSizeChanged(@NonNull VideoSize videoSize) {
        Player.Listener.super.onVideoSizeChanged(videoSize);
        handler.post(() -> jzvd.onVideoSizeChanged((int) (videoSize.width * videoSize.pixelWidthHeightRatio), videoSize.height));
    }

    @Override
    public void onRenderedFirstFrame() {
        Log.e(TAG, "onRenderedFirstFrame");
    }

    @Override
    public void pause() {
        exoPlayer.setPlayWhenReady(false);
    }

    @Override
    public boolean isPlaying() {
        return exoPlayer.getPlayWhenReady();
    }

    @Override
    public void seekTo(long time) {
        if (exoPlayer == null) {
            return;
        }
        if (time != previousSeek) {
            if (time >= exoPlayer.getBufferedPosition()) {
                jzvd.onStatePreparingPlaying();
            }
            exoPlayer.seekTo(time);
            previousSeek = time;
            jzvd.seekToInAdvance = time;

        }
    }

    @Override
    public void release() {
        if (mMediaHandler != null && mMediaHandlerThread != null && exoPlayer != null) {//不知道有没有妖孽
            HandlerThread tmpHandlerThread = mMediaHandlerThread;
            ExoPlayer tmpMediaPlayer = exoPlayer;
            JZMediaInterface.SAVED_SURFACE = null;

            mMediaHandler.post(() -> {
                tmpMediaPlayer.release();//release就不能放到主线程里，界面会卡顿
                tmpHandlerThread.quit();
            });
            exoPlayer = null;
        }
    }

    @Override
    public long getCurrentPosition() {
        if (exoPlayer != null)
            return exoPlayer.getCurrentPosition();
        else return 0;
    }

    @Override
    public long getDuration() {
        if (exoPlayer != null)
            return exoPlayer.getDuration();
        else return 0;
    }

    @Override
    public void setVolume(float leftVolume, float rightVolume) {
        exoPlayer.setVolume(leftVolume);
        exoPlayer.setVolume(rightVolume);
    }

    @Override
    public void setSpeed(float speed) {
        PlaybackParameters playbackParameters = new PlaybackParameters(speed, 1.0F);
        exoPlayer.setPlaybackParameters(playbackParameters);
    }

    @Override
    public void onTimelineChanged(@NonNull Timeline timeline, int reason) {
        Player.Listener.super.onTimelineChanged(timeline, reason);
        Log.e(TAG, "onTimelineChanged");
        ///        JZMediaPlayer.instance().mainThreadHandler.post(() -> {
//                if (reason == 0) {
//
//                    JzvdMgr.getCurrentJzvd().onInfo(reason, timeline.getPeriodCount());
//                }
//        });
    }

    @Override
    public void onIsLoadingChanged(boolean isLoading) {
        Player.Listener.super.onIsLoadingChanged(isLoading);
        Log.e(TAG, "onLoadingChanged");
    }

    @Override
    public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
        Player.Listener.super.onPlayWhenReadyChanged(playWhenReady, reason);
        Log.e(TAG, "onPlayWhenReadyChanged：" + playWhenReady);
        if (playWhenReady) {
            jzvd.onStatePlaying();
        }
    }

    @Override
    public void onPlaybackStateChanged(int playbackState) {
        Player.Listener.super.onPlaybackStateChanged(playbackState);
        Log.e(TAG, "onPlaybackStateChanged：=" + playbackState);
        handler.post(() -> {
            switch (playbackState) {
                case STATE_IDLE: {
                }
                break;
                case STATE_BUFFERING: {
                    jzvd.onStatePreparingPlaying();
                    handler.post(callback);
                }
                break;
                case STATE_READY: {
                    jzvd.onStatePlaying();
                }
                break;
                case STATE_ENDED: {
                    jzvd.onCompletion();
                }
                break;
            }
        });
    }

    @Override
    public void onPlayerError(@NonNull PlaybackException error) {
        Player.Listener.super.onPlayerError(error);
        Log.e(TAG, "onPlayerError" + error);
        handler.post(() -> jzvd.onError(1000, 1000));
    }

    @Override
    public void onPlayerErrorChanged(@Nullable PlaybackException error) {
        Player.Listener.super.onPlayerErrorChanged(error);
        Log.e(TAG, "onPlayerErrorChanged" + error);
        handler.post(() -> jzvd.onError(1000, 1000));
    }

    @Override
    public void onPositionDiscontinuity(@NonNull Player.PositionInfo oldPosition, @NonNull Player.PositionInfo newPosition, int reason) {
        Player.Listener.super.onPositionDiscontinuity(oldPosition, newPosition, reason);
        if (reason == DISCONTINUITY_REASON_SEEK) {
            handler.post(() -> jzvd.onSeekComplete());
        }
    }

    @Override
    public void setSurface(Surface surface) {
        if (exoPlayer != null) {
            exoPlayer.setVideoSurface(surface);
        } else {
            Log.e("AGVideo", "simpleExoPlayer为空");
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (SAVED_SURFACE == null) {
            SAVED_SURFACE = surface;
            prepare();
        } else {
            jzvd.textureView.setSurfaceTexture(SAVED_SURFACE);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    private class onBufferingUpdate implements Runnable {
        @Override
        public void run() {
            if (exoPlayer != null) {
                final int percent = exoPlayer.getBufferedPercentage();
                handler.post(() -> jzvd.setBufferProgress(percent));
                if (percent < 100) {
                    handler.postDelayed(callback, 300);
                } else {
                    handler.removeCallbacks(callback);
                }
            }
        }
    }
}
