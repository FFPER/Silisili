package my.project.silisili.main.player;

import android.app.PictureInPictureParams;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fanchen.sniffing.SniffingUICallback;
import com.fanchen.sniffing.SniffingVideo;
import com.fanchen.sniffing.web.SniffingUtil;
import com.google.android.material.button.MaterialButton;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;
import my.project.silisili.R;
import my.project.silisili.adapter.AnimeDescDramaAdapter;
import my.project.silisili.application.Silisili;
import my.project.silisili.bean.AnimeDescDetailsBean;
import my.project.silisili.bean.Event;
import my.project.silisili.main.base.BaseActivity;
import my.project.silisili.main.base.Presenter;
import my.project.silisili.main.video.VideoContract;
import my.project.silisili.main.video.VideoPresenter;
import my.project.silisili.util.SharedPreferencesUtils;
import my.project.silisili.util.StatusBarUtil;
import my.project.silisili.util.Utils;
import my.project.silisili.util.VideoUtils;

/**
 * 播放页面
 */
public class PlayerActivity extends BaseActivity implements VideoContract.View, JZPlayer.CompleteListener, JZPlayer.TouchListener, JZPlayer.ShowOrHideChangeViewListener, SniffingUICallback {
    @BindView(R.id.player)
    JZPlayer player;
    private String witchTitle, url, siliUrl;
    @BindView(R.id.rv_list)
    RecyclerView recyclerView;
    private List<AnimeDescDetailsBean> list;
    private AnimeDescDramaAdapter dramaAdapter;
    private AlertDialog alertDialog;
    private String animeTitle;
    @BindView(value = R.id.nav_view)
    LinearLayout linearLayout;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.pic_config)
    RelativeLayout picConfig;
    private VideoPresenter presenter;
    private boolean isPip = false;

    @BindView(R.id.nav_config_view)
    LinearLayout navConfigView;
    @BindView(R.id.speed)
    TextView speedTextView;
    private String[] speeds = Utils.getArray(R.array.speed_item);
    private int userSpeed = 2;
    @BindView(R.id.hide_progress)
    SwitchCompat switchCompat;
    private int clickIndex;
    private boolean hasPreVideo = false;
    private boolean hasNextVideo = false;
    protected static String PREVIDEOSTR = "上一集：%s";
    protected static String NEXTVIDEOSTR = "下一集：%s";

    @Override
    protected Presenter createPresenter() {
        return null;
    }

    @Override
    protected void loadData() {
    }

    @Override
    protected int setLayoutRes() {
        return R.layout.activity_play;
    }

    @Override
    protected void init() {
        Silisili.addDestoryActivity(this, "player");
        hideGap();
        Bundle bundle = getIntent().getExtras();
        init(bundle);
        initAdapter();
        initUserConfig();
    }

    @Override
    protected void initBeforeView() {
        StatusBarUtil.setTranslucent(this, 0);
    }

    private void init(Bundle bundle) {
        //播放地址
        url = bundle.getString("url");
        //集数名称
        witchTitle = bundle.getString("title");
        //番剧名称
        animeTitle = bundle.getString("animeTitle");
//        titleView.setText(animeTitle);
        //源地址
        siliUrl = bundle.getString("sili");
        //剧集list
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            list = bundle.getParcelableArrayList("list", AnimeDescDetailsBean.class);
        } else {
            list = bundle.getParcelableArrayList("list");
        }
        //当前播放剧集下标
        clickIndex = bundle.getInt("clickIndex");
        //禁止冒泡
        linearLayout.setOnClickListener(view -> {

        });
        navConfigView.setOnClickListener(view -> {

        });
        setPlayerPreNextTag();
        linearLayout.getBackground().mutate().setAlpha(150);
        navConfigView.getBackground().mutate().setAlpha(150);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                if (drawerLayout.isDrawerOpen(GravityCompat.START))
                    Jzvd.goOnPlayOnPause();
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                if (!drawerLayout.isDrawerOpen(GravityCompat.START))
                    Jzvd.goOnPlayOnResume();
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
        player.config.setOnClickListener(v -> {
            if (!Utils.isFastClick()) return;
            if (drawerLayout.isDrawerOpen(GravityCompat.START))
                drawerLayout.closeDrawer(GravityCompat.START);
            else drawerLayout.openDrawer(GravityCompat.START);
        });
        player.setListener(this, this, this, this);
        player.backButton.setOnClickListener(v -> finish());
        player.preVideo.setOnClickListener(v -> {
            clickIndex--;
            changePlayUrl(clickIndex);
        });
        player.nextVideo.setOnClickListener(v -> {
            clickIndex++;
            changePlayUrl(clickIndex);
        });
        // 加载视频失败，嗅探视频
        player.snifferBtn.setOnClickListener(v -> snifferPlayUrl(url));
        picConfig.setVisibility(View.VISIBLE);
        if (gtSdk23()) player.spinnerSpeed.setVisibility(View.VISIBLE);
        else player.spinnerSpeed.setVisibility(View.GONE);
        player.setUp(url, witchTitle, Jzvd.SCREEN_FULLSCREEN, JZExoPlayer.class);
        player.fullscreenButton.setOnClickListener(view -> {
            if (!Utils.isFastClick()) return;
            if (drawerLayout.isDrawerOpen(GravityCompat.END))
                drawerLayout.closeDrawer(GravityCompat.END);
            else drawerLayout.openDrawer(GravityCompat.END);
        });
        Jzvd.FULLSCREEN_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
        Jzvd.NORMAL_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
        player.playingShow();
        player.startButton.performClick();
        player.startVideo();
    }

    public void startPic() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            new Handler().postDelayed(this::enterPicInPic, 500);
        } else {
            Toast.makeText(this, R.string.picture_in_picture_title, Toast.LENGTH_SHORT).show();
        }
    }

    public void initAdapter() {
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        dramaAdapter = new AnimeDescDramaAdapter(this, list);
        recyclerView.setAdapter(dramaAdapter);
        dramaAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return;
//            setResult(0x20);
            drawerLayout.closeDrawer(GravityCompat.END);
            changePlayUrl(position);
        });
    }

    private void setPlayerPreNextTag() {
        hasPreVideo = clickIndex != 0;
        player.preVideo.setText(hasPreVideo ? String.format(PREVIDEOSTR, list.get(clickIndex - 1).getTitle()) : "");
        hasNextVideo = clickIndex != list.size() - 1;
        player.nextVideo.setText(hasNextVideo ? String.format(NEXTVIDEOSTR, list.get(clickIndex + 1).getTitle()) : "");
    }

    private void changePlayUrl(int position) {
        clickIndex = position;
        setPlayerPreNextTag();
        AnimeDescDetailsBean bean = dramaAdapter.getItem(position);
        Jzvd.releaseAllVideos();
        alertDialog = Utils.getProDialog(PlayerActivity.this, R.string.parsing);
        MaterialButton materialButton = (MaterialButton) dramaAdapter.getViewByPosition(recyclerView, position, R.id.tag_group);
        assert materialButton != null;
        materialButton.setTextColor(getResources().getColor(R.color.tabSelectedTextColor));
        assert bean != null;
        bean.setSelected(true);
        EventBus.getDefault().post(new Event(position));
        siliUrl = VideoUtils.getSiliUrl(bean.getUrl());
        witchTitle = animeTitle + " - " + bean.getTitle();
        player.playingShow();
        presenter = new VideoPresenter(animeTitle, siliUrl, PlayerActivity.this);
        presenter.loadData(true);
    }

    /**
     * 播放视频
     *
     * @param animeUrl 页面地址
     */
    private void playAnime(String animeUrl) {
        cancelDialog();
        url = animeUrl;
        /*switch ((Integer) SharedPreferencesUtils.getParam(getApplicationContext(), "player", 0)) {
            case 0:
                //调用播放器
                Jzvd.releaseAllVideos();
                player.currentSpeedIndex = 1;
                player.setUp(url, witchTitle, Jzvd.SCREEN_FULLSCREEN, JZExoPlayer.class);
                player.startVideo();
                break;
            case 1:
                Jzvd.releaseAllVideos();
                Utils.selectVideoPlayer(PlayerActivity.this, url);
                break;
        }*/
        Jzvd.releaseAllVideos();
        player.currentSpeedIndex = JZPlayer.DEFAULT_SPEED_INDEX;
        player.setUp(url, witchTitle, Jzvd.SCREEN_FULLSCREEN, JZExoPlayer.class);
        player.startVideo();
    }

    /**
     * 嗅探视频真实连接
     *
     * @param animeUrl 页面地址
     */
    private void snifferPlayUrl(String animeUrl) {
        alertDialog = Utils.getProDialog(PlayerActivity.this, R.string.should_be_used_web);
        SniffingUtil.get().activity(this).referer(animeUrl).callback(this).url(animeUrl).start();
    }

    private void initUserConfig() {
        switch ((Integer) SharedPreferencesUtils.getParam(this, "user_speed", 15)) {
            case 5:
                setUserSpeedConfig(speeds[0], 0);
                break;
            case 10:
                setUserSpeedConfig(speeds[1], 1);
                break;
            case 15:
                setUserSpeedConfig(speeds[2], 2);
                break;
            case 30:
                setUserSpeedConfig(speeds[3], 3);
                break;
        }
        switchCompat.setChecked((boolean) SharedPreferencesUtils.getParam(this, "hide_progress", false));
        switchCompat.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferencesUtils.setParam(this, "hide_progress", isChecked);
        });
    }

    private void setUserSpeedConfig(String text, int speed) {
        speedTextView.setText(text);
        userSpeed = speed;
    }

    private void setDefaultSpeed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(Utils.getString(R.string.set_user_speed));
        builder.setSingleChoiceItems(speeds, userSpeed, (dialog, which) -> {
            switch (which) {
                case 0:
                    SharedPreferencesUtils.setParam(getApplicationContext(), "user_speed", 5);
                    setUserSpeedConfig(speeds[0], which);
                    break;
                case 1:
                    SharedPreferencesUtils.setParam(getApplicationContext(), "user_speed", 10);
                    setUserSpeedConfig(speeds[1], which);
                    break;
                case 2:
                    SharedPreferencesUtils.setParam(getApplicationContext(), "user_speed", 15);
                    setUserSpeedConfig(speeds[2], which);
                    break;
                case 3:
                    SharedPreferencesUtils.setParam(getApplicationContext(), "user_speed", 30);
                    setUserSpeedConfig(speeds[3], which);
                    break;
            }
            dialog.dismiss();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @OnClick({R.id.speed_config, R.id.pic_config, R.id.player_config, R.id.browser_config})
    public void configBtnClick(RelativeLayout relativeLayout) {
        int id = relativeLayout.getId();
        if (id == R.id.speed_config) {
            setDefaultSpeed();
        } else if (id == R.id.pic_config) {
            if (gtSdk26()) startPic();
        } else if (id == R.id.player_config) {
            Utils.selectVideoPlayer(this, url);
        } else if (id == R.id.browser_config) {
            Utils.viewInChrome(this, siliUrl);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END))
            drawerLayout.closeDrawer(GravityCompat.END);
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        else finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Jzvd.goOnPlayOnPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideNavBar();
        if (!inMultiWindow()) Jzvd.goOnPlayOnResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isPip) finish();
    }

    /**
     * 是否为分屏模式
     *
     * @return 是否为分屏模式
     */
    public boolean inMultiWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) return this.isInMultiWindowMode();
        else return false;
    }

    /**
     * Android 8.0 画中画
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void enterPicInPic() {
//        PictureInPictureParams.Builder builder = new PictureInPictureParams.Builder();
        // 设置宽高比例值，第一个参数表示分子，第二个参数表示分母
        // 下面的10/5=2，表示画中画窗口的宽度是高度的两倍
//        Rational aspectRatio = new Rational(10,5);
        // 设置画中画窗口的宽高比例
//        builder.setAspectRatio(aspectRatio);
        // 进入画中画模式，注意enterPictureInPictureMode是Android8.0之后新增的方法
//        enterPictureInPictureMode(builder.build());
        PictureInPictureParams builder = new PictureInPictureParams.Builder().build();
        enterPictureInPictureMode(builder);
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, @NonNull Configuration newConfig) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        }
        if (isInPictureInPictureMode) {
            player.startPIP();
            isPip = true;
            Jzvd.goOnPlayOnResume();
        } else isPip = false;
    }

    @Override
    public void onMultiWindowModeChanged(boolean isInMultiWindowMode, @NonNull Configuration newConfig) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            super.onMultiWindowModeChanged(isInMultiWindowMode, newConfig);
        }
        if (isInMultiWindowMode)
            Jzvd.goOnPlayOnResume();
    }

    @Override
    public void cancelDialog() {
        Utils.cancelDialog(alertDialog);
    }

    @Override
    public void getVideoSuccess(String url) {
        runOnUiThread(() -> {
            hideNavBar();
            playAnime(url);
        });
    }

    @Override
    public void getIframeUrl(String iframeUrl) {
        runOnUiThread(() -> {
            application.showToastMsg(Utils.getString(R.string.should_be_used_web));
            SniffingUtil.get().activity(this).referer(iframeUrl).callback(this).url(iframeUrl).start();
//            application.showToastMsg(Utils.getString(R.string.should_be_used_web));
//            SniffingUtil.get().activity(this).referer(iframeUrl).callback(this).url(iframeUrl).start();
            url = iframeUrl;
            Jzvd.releaseAllVideos();
            player.currentSpeedIndex = JZPlayer.DEFAULT_SPEED_INDEX;
            player.setUp(url, witchTitle, Jzvd.SCREEN_FULLSCREEN, JZExoPlayer.class);
            player.startVideo();
        });
    }

    @Override
    public void getVideoEmpty() {
        runOnUiThread(() -> {
            application.showToastMsg(Utils.getString(R.string.open_web_view));
            VideoUtils.openDefaultWebview(this, siliUrl);
            finish();
        });
    }

    @Override
    public void getVideoError() {
        //网络出错
        runOnUiThread(() -> {
            hideNavBar();
            application.showErrorToastMsg(Utils.getString(R.string.error_700));
        });
    }

    @Override
    protected void onDestroy() {
        if (null != presenter) presenter.detachView();
        JzvdStd.releaseAllVideos();
        super.onDestroy();
    }

    @Override
    public void complete() {
        if (hasNextVideo) {
            application.showSuccessToastMsg("开始播放下一集");
            clickIndex++;
            changePlayUrl(clickIndex);
        } else {
            application.showSuccessToastMsg("全部播放完毕");
            if (!drawerLayout.isDrawerOpen(GravityCompat.END))
                drawerLayout.openDrawer(GravityCompat.END);
        }
    }

    @Override
    public void touch() {
        hideNavBar();
    }

    @Override
    public void onSniffingStart(View webView, String url) {

    }

    @Override
    public void onSniffingFinish(View webView, String url) {
        SniffingUtil.get().releaseWebView();
        cancelDialog();
        hideNavBar();
    }

    @Override
    public void onSniffingSuccess(View webView, String url, List<SniffingVideo> videos) {
        List<String> urls = Utils.ridRepeat(videos);
        if (urls.size() > 1)
            VideoUtils.showMultipleVideoSources(this,
                    urls,
                    (dialog, index) -> playAnime(urls.get(index)),
                    (dialog, which) -> dialog.dismiss(), 1);
        else playAnime(urls.get(0));
    }

    @Override
    public void onSniffingError(View webView, String url, int werrorCode) {
        application.showToastMsg(Utils.getString(R.string.open_web_view));
        VideoUtils.openDefaultWebview(this, siliUrl);
        finish();
    }

    @Override
    public void finish() {
        if (null != presenter) presenter.detachView();
        JzvdStd.releaseAllVideos();
        super.finish();
    }

    @Override
    public void showOrHideChangeView() {
        player.preVideo.setVisibility(hasPreVideo ? View.VISIBLE : View.GONE);
        player.nextVideo.setVisibility(hasNextVideo ? View.VISIBLE : View.GONE);
    }
}
