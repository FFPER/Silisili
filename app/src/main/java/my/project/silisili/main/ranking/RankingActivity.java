package my.project.silisili.main.ranking;

import android.view.View;

import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Objects;

import my.project.silisili.R;
import my.project.silisili.adapter.RankingTypeAdapter;
import my.project.silisili.bean.Refresh;
import my.project.silisili.databinding.ActivityRankingBinding;
import my.project.silisili.main.base.BaseActivity;
import my.project.silisili.util.SwipeBackLayoutUtil;
import my.project.silisili.util.Utils;

/**
 * 排行榜
 */
public class RankingActivity extends BaseActivity<RankingContract.View, RankingPresenter> implements RankingContract.View {
    private ActivityRankingBinding viewBinding;
    private int rankingType = 0;// 排行类型的索引
    private final String[] tabs = Utils.getArray(R.array.ranking_type_array);
    private RankingTypeAdapter adapter;

    @Override
    protected RankingPresenter createPresenter() {
        return null;
    }

    @Override
    protected void loadData() {

    }

    @Override
    protected int setLayoutRes() {
        return 0;
    }

    @Override
    protected View setLayoutViewBinding() {
        viewBinding = ActivityRankingBinding.inflate(getLayoutInflater());
        return viewBinding.getRoot();
    }

    @Override
    protected void init() {
        EventBus.getDefault().register(this);
        initToolbar();
        initSwipe();
        initFragment();
    }

    @Override
    protected void initBeforeView() {
        SwipeBackLayoutUtil.convertActivityToTranslucent(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Refresh refresh) {
        if (refresh.getIndex() == 0) {
            viewBinding.vpRankingType.removeAllViews();
            removeFragmentTransaction();
            mPresenter.loadData(true);
        }
    }

    public void initToolbar() {
        viewBinding.include1.toolbar.setTitle(getResources().getString(R.string.rankingLongName));
        setSupportActionBar(viewBinding.include1.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        viewBinding.include1.toolbar.setNavigationOnClickListener(view -> finish());
    }

    public void initSwipe() {
        viewBinding.mSwipeRankingType.setColorSchemeResources(R.color.pink500, R.color.blue500, R.color.purple500);
        viewBinding.mSwipeRankingType.setOnRefreshListener(() -> {
            viewBinding.vpRankingType.removeAllViews();
            removeFragmentTransaction();
            mPresenter.loadData(true);
        });
    }

    public void initFragment() {
        for (int i = 0; i < tabs.length; i++) {
            viewBinding.tabRankingType.addTab(viewBinding.tabRankingType.newTab());
        }
        viewBinding.tabRankingType.setupWithViewPager(viewBinding.vpRankingType);
        //手动 添加标题必须在 setupwidthViewPager后
        for (int i = 0; i < tabs.length; i++) {
            Objects.requireNonNull(viewBinding.tabRankingType.getTabAt(i)).setText(tabs[i]);
        }
        Objects.requireNonNull(viewBinding.tabRankingType.getTabAt(rankingType)).select();
        viewBinding.tabRankingType.setSelectedTabIndicatorColor(getResources().getColor(R.color.tabSelectedTextColor));
    }

    public void removeFragmentTransaction() {
        try {//避免重启太快恢复
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            for (int i = 0; i < 3; i++) {
                fragmentTransaction.remove(adapter.getItem(i));
            }
            fragmentTransaction.commitAllowingStateLoss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化分类适配器
     *
     * @param pos 滑动的碎片位置-分类的索引
     */
    public void setRankingTypeAdapter(int pos) {
        adapter = new RankingTypeAdapter(getSupportFragmentManager(), viewBinding.tabRankingType.getTabCount());
        try {
            Field field = ViewPager.class.getDeclaredField("mRestoredCurItem");
            field.setAccessible(true);
            field.set(viewBinding.vpRankingType, rankingType);
        } catch (Exception e) {
            viewBinding.vpRankingType.setCurrentItem(pos);
            e.printStackTrace();
        }
        viewBinding.vpRankingType.setAdapter(adapter);
        for (int i = 0; i < tabs.length; i++) {
            Objects.requireNonNull(viewBinding.tabRankingType.getTabAt(i)).setText(tabs[i]);
        }
        viewBinding.vpRankingType.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                rankingType = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void showLoadingView() {
        viewBinding.mSwipeRankingType.setRefreshing(true);
        application.rankTypeError = "";
        application.rankType = new JSONObject();
    }

    @Override
    public void showLoadErrorView(String msg) {
        runOnUiThread(() -> {
            viewBinding.mSwipeRankingType.setRefreshing(false);
            application.showErrorToastMsg(msg);
            application.rankTypeError = msg;
            application.rankType = new JSONObject();
            setRankingTypeAdapter(rankingType);
        });
    }

    @Override
    public void showEmptyVIew() {

    }

    @Override
    public void showLoadSuccess(LinkedHashMap<String, Object> map) {
        runOnUiThread(() -> {
            if (!getSupportFragmentManager().isDestroyed()) {
                viewBinding.mSwipeRankingType.setRefreshing(false);
                application.rankTypeError = "";
                application.rankType = map.get("rankType") == null ? new JSONObject() : (JSONObject) map.get("rankType");
                setRankingTypeAdapter(rankingType);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

}