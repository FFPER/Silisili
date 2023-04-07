package my.project.silisili.main.ranking;

import android.view.View;

import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import my.project.silisili.R;
import my.project.silisili.adapter.RankingTypeAdapter;
import my.project.silisili.bean.RankingBean;
import my.project.silisili.bean.Refresh;
import my.project.silisili.databinding.ActivityRankingTypeBinding;
import my.project.silisili.main.base.BaseActivity;
import my.project.silisili.util.SwipeBackLayoutUtil;
import my.project.silisili.util.Utils;

/**
 * 排行榜
 */
public class RankingTypeActivity extends BaseActivity<RankingContract.View, RankingPresenter> implements RankingContract.View {
    private ActivityRankingTypeBinding viewBinding;
    private int rankingType = 0;// 排行类型的索引
    private final String[] tabs = Utils.getArray(R.array.my_ranking_array);
    private RankingTypeAdapter adapter;

    @Override
    protected RankingPresenter createPresenter() {
        return new RankingPresenter(this);
    }

    @Override
    protected void loadData() {
        mPresenter.loadData(true);
    }

    @Override
    protected int setLayoutRes() {
        return 0;
    }

    @Override
    protected View setLayoutViewBinding() {
        viewBinding = ActivityRankingTypeBinding.inflate(getLayoutInflater());
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
            removeViews();
            mPresenter.loadData(true);
        }
    }

    public void initToolbar() {
        viewBinding.toolbarRanking.setTitle(getResources().getString(R.string.rankingLongName));
        setSupportActionBar(viewBinding.toolbarRanking);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        viewBinding.toolbarRanking.setNavigationOnClickListener(view -> finish());
    }

    public void initSwipe() {
        viewBinding.mSwipeRankingType.setColorSchemeResources(R.color.pink500, R.color.blue500, R.color.purple500);
        viewBinding.mSwipeRankingType.setOnRefreshListener(() -> {
            removeViews();
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

    private void removeViews() {
        viewBinding.vpRankingType.removeAllViews();
        removeFragmentTransaction();
    }

    /**
     * 初始化分类适配器
     *
     * @param pos 滑动的碎片位置-分类的索引
     * @param map 集合对象
     */
    public void setRankingTypeAdapter(int pos, Map<String, ArrayList<RankingBean>> map) {
        adapter = new RankingTypeAdapter(getSupportFragmentManager(), viewBinding.tabRankingType.getTabCount(), map);
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
    }

    @Override
    public void showLoadErrorView(String msg) {
        runOnUiThread(() -> {
            viewBinding.mSwipeRankingType.setRefreshing(false);
            application.showErrorToastMsg(msg);
            application.rankTypeError = msg;
            //setRankingTypeAdapter(rankingType, map.get(tabs[rankingType]));
        });
    }

    @Override
    public void showEmptyVIew() {

    }

    @Override
    public void showLoadSuccess(Map<String, ArrayList<RankingBean>> map) {
        runOnUiThread(() -> {
            if (!getSupportFragmentManager().isDestroyed()) {
                viewBinding.mSwipeRankingType.setRefreshing(false);
                application.rankTypeError = "";
                setRankingTypeAdapter(rankingType, map);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

}