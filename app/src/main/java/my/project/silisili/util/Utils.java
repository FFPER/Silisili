package my.project.silisili.util;

import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.ArrayRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.cardview.widget.CardView;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.bumptech.glide.request.transition.Transition;
import com.fanchen.sniffing.SniffingVideo;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import my.project.silisili.R;
import my.project.silisili.application.Silisili;
import my.project.silisili.net.GlideApp;

public class Utils {
    private static Context context;

    private Utils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    /**
     * 初始化工具类
     *
     * @param context 上下文
     */
    public static void init(Context context) {
        Utils.context = context.getApplicationContext();
    }

    /**
     * 获取ApplicationContext
     *
     * @return ApplicationContext
     */
    public static Context getContext() {
        if (context != null) return context;
        throw new NullPointerException("u should init first");
    }

    public static void creatFile() {
        File dataDir = new File(Environment.getExternalStorageDirectory() + "/SilisiliAnime/Database");
        if (!dataDir.exists())
            dataDir.mkdirs();
    }

    // 两次点击按钮之间的点击间隔不能少于500毫秒
    private static final int MIN_CLICK_DELAY_TIME = 500;
    private static long lastClickTime;

    public static boolean isFastClick() {
        boolean flag = false;
        long curClickTime = System.currentTimeMillis();
        if ((curClickTime - lastClickTime) >= MIN_CLICK_DELAY_TIME) {
            flag = true;
        }
        lastClickTime = curClickTime;
        return flag;
    }

    /**
     * 加载框
     *
     * @return
     */
    public static AlertDialog getProDialog(Activity activity, @StringRes int id) {
        AlertDialog alertDialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.DialogStyle);
        View view = LayoutInflater.from(activity).inflate(R.layout.dialog_proress, null);
        RelativeLayout root = view.findViewById(R.id.root);
        TextView msg = view.findViewById(R.id.msg);
        root.setBackgroundColor(activity.getResources().getColor(R.color.window_bg));
        msg.setTextColor(activity.getResources().getColor(R.color.text_color_primary));
        msg.setText(getString(id));
        builder.setCancelable(false);
        alertDialog = builder.setView(view).create();
        alertDialog.show();
        return alertDialog;
    }

    /**
     * 关闭对话框
     *
     * @param alertDialog
     */
    public static void cancelDialog(AlertDialog alertDialog) {
        if (alertDialog != null)
            alertDialog.dismiss();
    }

    /**
     * 下载进度条
     *
     * @param context
     * @return
     */
    public static ProgressDialog showProgressDialog(Context context) {
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setCancelable(false);
        pd.setCanceledOnTouchOutside(false);
        pd.setMax(100);
        pd.setMessage(getString(R.string.download));
        return pd;
    }

    /**
     * 选择视频播放器
     *
     * @param url
     */
    public static void selectVideoPlayer(Context context, String url) {
        final Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.parse(url), "video/*");
        try {
            context.startActivity(Intent.createChooser(intent, "请选择视频播放器"));
        } catch (ActivityNotFoundException e) {
            Silisili.getInstance().showToastMsg("没有找到匹配的程序");
        }
    }

    /**
     * 通过浏览器打开
     *
     * @param context
     * @param url
     */
    public static void viewInChrome(Context context, String url) {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        //Sets the toolbar color.
        builder.setToolbarColor(context.getResources().getColor(R.color.night));
        Bitmap closeBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.baseline_close_white_48dp);
        builder.setCloseButtonIcon(closeBitmap);// 关闭按钮
        builder.setShowTitle(true); //显示网页标题
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(context, Uri.parse(url));
    }

    public static String getString(@StringRes int id) {
        return getContext().getResources().getString(id);
    }

    public static String[] getArray(@ArrayRes int id) {
        return getContext().getResources().getStringArray(id);
    }

    /**
     * 获取当前日期是星期几
     *
     * @param dt
     * @return + 1 当前日期是星期几
     */
    public static int getWeekOfDate(Date dt) {
        int[] weekDays = {6, 0, 1, 2, 3, 4, 5};
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0)
            w = 0;
        return weekDays[w];
    }

    /**
     * info
     */
    public static void showX5Info(Context context) {
        AlertDialog alertDialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.DialogStyle);
        builder.setPositiveButton(getString(R.string.x5_info_positive), null);
        builder.setMessage(getString(R.string.x5_info));
        builder.setTitle(getString(R.string.x5_info_title));
        builder.setCancelable(false);
        alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            SharedPreferencesUtils.setParam(getContext(), "new_version", false);
            alertDialog.dismiss();
        });
    }

    public static void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static ObjectAnimator tada(View view) {
        return tada(view, 2f);
    }

    public static ObjectAnimator tada(View view, float shakeFactor) {

        PropertyValuesHolder pvhScaleX = PropertyValuesHolder.ofKeyframe(View.SCALE_X,
                Keyframe.ofFloat(0f, 1f),
                Keyframe.ofFloat(.1f, .9f),
                Keyframe.ofFloat(.2f, .9f),
                Keyframe.ofFloat(.3f, 1.1f),
                Keyframe.ofFloat(.4f, 1.1f),
                Keyframe.ofFloat(.5f, 1.1f),
                Keyframe.ofFloat(.6f, 1.1f),
                Keyframe.ofFloat(.7f, 1.1f),
                Keyframe.ofFloat(.8f, 1.1f),
                Keyframe.ofFloat(.9f, 1.1f),
                Keyframe.ofFloat(1f, 1f)
        );

        PropertyValuesHolder pvhScaleY = PropertyValuesHolder.ofKeyframe(View.SCALE_Y,
                Keyframe.ofFloat(0f, 1f),
                Keyframe.ofFloat(.1f, .9f),
                Keyframe.ofFloat(.2f, .9f),
                Keyframe.ofFloat(.3f, 1.1f),
                Keyframe.ofFloat(.4f, 1.1f),
                Keyframe.ofFloat(.5f, 1.1f),
                Keyframe.ofFloat(.6f, 1.1f),
                Keyframe.ofFloat(.7f, 1.1f),
                Keyframe.ofFloat(.8f, 1.1f),
                Keyframe.ofFloat(.9f, 1.1f),
                Keyframe.ofFloat(1f, 1f)
        );

        PropertyValuesHolder pvhRotate = PropertyValuesHolder.ofKeyframe(View.ROTATION,
                Keyframe.ofFloat(0f, 0f),
                Keyframe.ofFloat(.1f, -3f * shakeFactor),
                Keyframe.ofFloat(.2f, -3f * shakeFactor),
                Keyframe.ofFloat(.3f, 3f * shakeFactor),
                Keyframe.ofFloat(.4f, -3f * shakeFactor),
                Keyframe.ofFloat(.5f, 3f * shakeFactor),
                Keyframe.ofFloat(.6f, -3f * shakeFactor),
                Keyframe.ofFloat(.7f, 3f * shakeFactor),
                Keyframe.ofFloat(.8f, -3f * shakeFactor),
                Keyframe.ofFloat(.9f, 3f * shakeFactor),
                Keyframe.ofFloat(1f, 0)
        );

        return ObjectAnimator.ofPropertyValuesHolder(view, pvhScaleX, pvhScaleY, pvhRotate).
                setDuration(1000);
    }

    public static ObjectAnimator nope(View view) {
        int delta = view.getResources().getDimensionPixelOffset(R.dimen.spacing_medium);
        PropertyValuesHolder pvhTranslateX = PropertyValuesHolder.ofKeyframe(View.TRANSLATION_X,
                Keyframe.ofFloat(0f, 0),
                Keyframe.ofFloat(.10f, -delta),
                Keyframe.ofFloat(.26f, delta),
                Keyframe.ofFloat(.42f, -delta),
                Keyframe.ofFloat(.58f, delta),
                Keyframe.ofFloat(.74f, -delta),
                Keyframe.ofFloat(.90f, delta),
                Keyframe.ofFloat(0f, 0)
        );
        return ObjectAnimator.ofPropertyValuesHolder(view, pvhTranslateX).
                setDuration(500);
    }

    /**
     * 隐藏动画&显示动画
     * type 0 隐藏  1 显示
     *
     * @return
     */
    public static ScaleAnimation animationOut(int type) {
        ScaleAnimation scaleAnimation = null;
        switch (type) {
            case 0:
                scaleAnimation = new ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f,
                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                scaleAnimation.setDuration(200);
                break;
            case 1:
                scaleAnimation = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f,
                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                scaleAnimation.setDuration(200);
                break;
        }
        return scaleAnimation;
    }

    /**
     * 复制提取码到剪切板
     */
    public static void putTextIntoClip(String string) {
        ClipboardManager clipboardManager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        //创建ClipData对象
        ClipData clipData = ClipData.newPlainText("string", string);
        //添加ClipData对象到剪切板中
        clipboardManager.setPrimaryClip(clipData);
    }

    /**
     * 滑动返回配置
     *
     * @return
     */
    public static SlidrConfig defaultInit() {
        SlidrConfig.Builder mBuilder;
        mBuilder = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .scrimStartAlpha(0.8f)
                .scrimEndAlpha(0f)
                .velocityThreshold(5f)
                .distanceThreshold(.35f)
                .edge(true | false)
                .edgeSize(0.18f);// The % of the screen that counts as the edge, default 18%;
        return mBuilder.build();
    }

    /**
     * 判断当前设备是手机还是平板，代码来自 Google I/O App for Android
     *
     * @return 平板返回 True，手机返回 False
     */
    public static boolean isPad() {
        return (getContext().getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    /**
     * 判断当前设备是否是竖屏
     *
     * @return 竖屏返回 True，横屏返回 False
     */
    public static boolean isPortrait() {
        return getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    /**
     * 设置默认图片
     *
     * @param context
     * @param url
     * @param imageView
     * @param setPalette
     * @param cardView
     * @param textView
     */
    public static void setDefaultImage(Context context, String url, ImageView imageView, boolean setPalette, CardView cardView, TextView textView) {
        DrawableCrossFadeFactory drawableCrossFadeFactory = new DrawableCrossFadeFactory.Builder(300).setCrossFadeEnabled(true).build();
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .format(DecodeFormat.PREFER_RGB_565)
                .placeholder((Boolean) SharedPreferencesUtils.getParam(getContext(), "darkTheme", false) ? R.drawable.loading_night : R.drawable.loading_light)
                .error(R.drawable.error);
        GlideApp.with(context)
                .load(url)
                .transition(DrawableTransitionOptions.withCrossFade(drawableCrossFadeFactory))
                .apply(options).addListener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        e.printStackTrace();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                })
                .into(imageView);
        if (setPalette) // 设置Palette
            GlideApp.with(context).asBitmap().load(url).into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    Palette.from(resource).generate(palette -> {
                        Palette.Swatch swatch = palette.getDarkVibrantSwatch();
                        if (swatch != null) {
                            cardView.setCardBackgroundColor(swatch.getRgb());
                            textView.setTextColor(swatch.getBodyTextColor());
                        }
                    });
                }
            });
    }

    public static void setCardDefaultBg(Context context, CardView cardView, TextView textView) {
        cardView.setCardBackgroundColor(context.getResources().getColor(R.color.window_bg));
        textView.setTextColor(context.getResources().getColor(R.color.text_color_primary));
    }

    public static String getASVersionName() {
        PackageManager manager = context.getPackageManager();
        String name = null;
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            name = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return name;
    }

    /**
     * 删除文件
     *
     * @param root
     */
    public static void deleteAllFiles(File root) {
        Log.e("删除文件", root.toString());
        File files[] = root.listFiles();
        if (files != null)
            for (File f : files) {
                if (f.isDirectory()) { // 判断是否为文件夹
                    deleteAllFiles(f);
                    try {
                        f.delete();
                    } catch (Exception e) {
                    }
                } else {
                    if (f.exists()) { // 判断是否存在
                        deleteAllFiles(f);
                        try {
                            f.delete();
                        } catch (Exception e) {
                        }
                    }
                }
            }
    }

    /**
     * 发现新版本弹窗
     *
     * @param context
     * @param version
     * @param body
     * @param posListener
     * @param negListener
     */
    public static void findNewVersion(Context context,
                                      String version,
                                      String body,
                                      DialogInterface.OnClickListener posListener,
                                      DialogInterface.OnClickListener negListener) {
        AlertDialog alertDialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.DialogStyle);
        builder.setMessage(body);
        builder.setTitle(getString(R.string.find_new_version) + version);
        builder.setPositiveButton(getString(R.string.update_now), posListener);
        builder.setNegativeButton(getString(R.string.update_after), negListener);
        builder.setCancelable(false);
        alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * 判断是否有NavigationBar
     *
     * @param activity
     * @return
     */
    public static boolean checkHasNavigationBar(Activity activity) {
        WindowManager windowManager = activity.getWindowManager();
        Display d = windowManager.getDefaultDisplay();

        DisplayMetrics realDisplayMetrics = new DisplayMetrics();
        d.getRealMetrics(realDisplayMetrics);

        int realHeight = realDisplayMetrics.heightPixels;
        int realWidth = realDisplayMetrics.widthPixels;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        d.getMetrics(displayMetrics);

        int displayHeight = displayMetrics.heightPixels;
        int displayWidth = displayMetrics.widthPixels;

        return (realWidth - displayWidth) > 0 || (realHeight - displayHeight) > 0;
    }

    /**
     * 获得NavigationBar的高度
     */
    public static int getNavigationBarHeight(Activity activity) {
        int result = 0;
        Resources resources = activity.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0 && checkHasNavigationBar(activity)) {
            result = resources.getDimensionPixelSize(resourceId);
        }
        Log.e("navigation_bar_height", result + "");
        return result + 15;
    }

    /**
     * dp转px
     *
     * @param context
     * @param dp
     * @return
     */
    public static int dpToPx(Context context, float dp) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) ((dp * scale) + 0.5f);
    }

    /**
     * 将px转换为dp
     *
     * @param px
     * @return
     */
    public static int pxTodp(Context context, float px) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5);
    }

    /**
     * 获取状态栏高度
     *
     * @return
     */
    public static int getStatusBarHeight() {
        int statusBarHeight = 20;
        int resourceId = getContext().getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getContext().getResources().getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }

    /**
     * 获取ActionBar 高度
     *
     * @return
     */
    public static int getActionBarHeight() {
        TypedValue tv = new TypedValue();
        if (getContext().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data,
                    getContext().getResources().getDisplayMetrics());
        }
        return 0;
    }

    /**
     * X5内核加载状态
     *
     * @return
     */
    public static boolean getX5State() {
        return (boolean) SharedPreferencesUtils.getParam(getContext(), "X5State", false);
    }

    /**
     * 是否启用x5内核
     *
     * @return
     */
    public static boolean loadX5() {
        return (boolean) SharedPreferencesUtils.getParam(getContext(), "loadX5", false);
    }

    /**
     * 嗅探视频地址集合
     *
     * @param list
     * @return
     */
    public static List<String> ridRepeat(List<SniffingVideo> list) {
        List<String> urls = new ArrayList<>();
        for (SniffingVideo sniffingVideo : list) {
            if (!urls.contains(sniffingVideo.getUrl()) && !sniffingVideo.getUrl().startsWith("mp4"))
                urls.add(sniffingVideo.getUrl());
        }
        return urls;
    }

    public static int screenWidth(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (wm != null) {
            wm.getDefaultDisplay().getMetrics(dm);
            return dm.widthPixels;
        }
        return 1080;
    }
}
