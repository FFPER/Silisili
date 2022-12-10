package my.project.silisili.main.tag;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.r0adkll.slidr.Slidr;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import my.project.silisili.R;
import my.project.silisili.adapter.AnimeListAdapter;
import my.project.silisili.adapter.TagAdapter;
import my.project.silisili.bean.AnimeDescHeaderBean;
import my.project.silisili.bean.TagBean;
import my.project.silisili.custom.CustomLoadMoreView;
import my.project.silisili.main.animelist.AnimeListContract;
import my.project.silisili.main.animelist.AnimeListPresenter;
import my.project.silisili.main.base.BaseActivity;
import my.project.silisili.main.desc.DescActivity;
import my.project.silisili.util.SwipeBackLayoutUtil;
import my.project.silisili.util.Utils;
import my.project.silisili.util.VideoUtils;

public class TagActivity extends BaseActivity<TagContract.View, TagPresenter> implements TagContract.View, AnimeListContract.View {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rv_list)
    RecyclerView animeListRecyclerView;
    private AnimeListAdapter animeListAdapter;
    private List<AnimeDescHeaderBean> animeLists = new ArrayList<>();
    private TagAdapter tagAdapter;
    @BindView(R.id.mSwipe)
    SwipeRefreshLayout mSwipe;
    private List<MultiItemEntity> tagList = new ArrayList<>();
    private RecyclerView tagRecyclerView;
    private BottomSheetDialog mBottomSheetDialog;
    private Button ref;
    @BindView(R.id.tag_btn)
    FloatingActionButton tag_btn;
    private AnimeListPresenter animeListPresenter;
    private String animeUrl;
    private int nowPage = 1;
    private int pageCount = 0;
    private boolean isErr = true;

    @Override
    protected TagPresenter createPresenter() {
        return new TagPresenter(this);
    }

    @Override
    protected void loadData() {
        mPresenter.loadData(true);
    }

    @Override
    protected int setLayoutRes() {
        return R.layout.activity_tag;
    }

    @Override
    protected void init() {
        Slidr.attach(this, Utils.defaultInit());
        initToolbar();
        initFab();
        initSwipe();
        initAdapter();
    }

    @Override
    protected void initBeforeView() {
        SwipeBackLayoutUtil.convertActivityToTranslucent(this);
    }

    public void initToolbar() {
        toolbar.setTitle(Utils.getString(R.string.tag_title));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> finish());
    }

    public void initFab() {
        if (Utils.checkHasNavigationBar(this)) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) tag_btn.getLayoutParams();
            params.setMargins(Utils.dpToPx(this, 16),
                    Utils.dpToPx(this, 16),
                    Utils.dpToPx(this, 16),
                    Utils.getNavigationBarHeight(this) + 15);
            tag_btn.setLayoutParams(params);
        }
    }

    public void initSwipe() {
        mSwipe.setEnabled(false);
        mSwipe.setColorSchemeResources(R.color.pink500, R.color.blue500, R.color.purple500);
        mSwipe.setOnRefreshListener(() -> {
            animeLists.clear();
            animeListAdapter.setNewData(animeLists);
            nowPage = 1;
            animeListPresenter = new AnimeListPresenter(animeUrl, nowPage, this);
            animeListPresenter.loadData(true);
        });
    }

    public void initAdapter() {
        // 动漫列表数据
        animeListRecyclerView.setLayoutManager(new GridLayoutManager(this, Utils.isPad() ? 5 : 3));
        animeListAdapter = new AnimeListAdapter(this, animeLists);
        animeListAdapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        animeListAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return;
            AnimeDescHeaderBean bean = (AnimeDescHeaderBean) adapter.getItem(position);
            if (bean.getUrl().contains("nieta")) {
                application.showErrorToastMsg("抱歉，该内容无法解析");
                return;
            }
            Bundle bundle = new Bundle();
            bundle.putString("name", bean.getName());
            String diliUrl = VideoUtils.getSiliUrl(bean.getUrl());
            bundle.putString("url", diliUrl);
            startActivity(new Intent(TagActivity.this, DescActivity.class).putExtras(bundle));
        });
        if (Utils.checkHasNavigationBar(this)) animeListRecyclerView.setPadding(0,0,0, Utils.getNavigationBarHeight(this));
        animeListAdapter.setLoadMoreView(new CustomLoadMoreView());
        animeListAdapter.setOnLoadMoreListener(() -> animeListRecyclerView.postDelayed(() -> {
            if (nowPage >= pageCount) {
                //数据全部加载完毕
                animeListAdapter.loadMoreEnd();
                application.showSuccessToastMsg(Utils.getString(R.string.no_more));
            } else {
                if (isErr) {
                    //成功获取更多数据
                    nowPage++;
                    animeListPresenter = new AnimeListPresenter(animeUrl, nowPage, this);
                    animeListPresenter.loadData(false);
                } else {
                    //获取更多数据失败
                    isErr = true;
                    animeListAdapter.loadMoreFail();
                }
            }
        }, 500), animeListRecyclerView);
        animeListRecyclerView.setAdapter(animeListAdapter);
        // 分类数据
        View tagView = LayoutInflater.from(this).inflate(R.layout.dialog_tag, null);
        tagRecyclerView = tagView.findViewById(R.id.tag_list);
        ref = tagView.findViewById(R.id.ref);
        ref.setOnClickListener((view)-> {
            tagList.clear();
            tagAdapter.setNewData(tagList);
            mPresenter = createPresenter();
            mPresenter.loadData(true);
        });
        tagAdapter = new TagAdapter(this, tagList);
        tagAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (!Utils.isFastClick()) return;
            mBottomSheetDialog.dismiss();
            TextView textView = (TextView) adapter.getViewByPosition(tagRecyclerView, position, R.id.tag_group);
            textView.setTextColor(getResources().getColor(R.color.colorAccent));
            final TagBean bean = (TagBean) adapter.getItem(position);
            for (MultiItemEntity entity : tagList) {
                if (entity.getItemType()  == TagAdapter.TYPE_LEVEL_1) {
                    TagBean tagBean = (TagBean) entity;
                    tagBean.setSelected(false);
                }
            }
            bean.setSelected(true);
            adapter.setNewData(tagList);
            mSwipe.setEnabled(true);
            toolbar.setTitle(bean.getTitle());
            animeUrl = VideoUtils.getSiliUrl(bean.getUrl());
            animeListAdapter.setNewData(null);
            animeListAdapter.setEmptyView(getLayoutInflater().inflate(R.layout.base_emnty_view, null));
            nowPage = 1;
            animeListPresenter = new AnimeListPresenter(animeUrl, nowPage, this);
            animeListPresenter.loadData(true);
        });
        tagRecyclerView.setAdapter(tagAdapter);
        mBottomSheetDialog = new BottomSheetDialog(this);
        mBottomSheetDialog.setContentView(tagView);
    }

    public void setLoadState(boolean loadState) {
        isErr = loadState;
        animeListAdapter.loadMoreComplete();
    }

    @OnClick(R.id.tag_btn)
    public void tagBtnClick() {
        tagAdapter.setNewData(tagList);
        mBottomSheetDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        mBottomSheetDialog.show();
    }


    @Override
    public void showLoadingView() {
        mSwipe.setRefreshing(true);
    }

    @Override
    public void showSuccessView(List<MultiItemEntity> list) {
        runOnUiThread(() -> {
            if (!mActivityFinish) {
                ref.setVisibility(View.GONE);
                final GridLayoutManager manager = new GridLayoutManager(this, Utils.isPad() ? 12 : 8);
                manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        return tagAdapter.getItemViewType(position) == TagAdapter.TYPE_LEVEL_1 ? 1 : manager.getSpanCount();
                    }
                });
                // important! setLayoutManager should be called after setAdapter
                tagRecyclerView.setLayoutManager(manager);
                mSwipe.setRefreshing(false);
                tagList = list;
                tagAdapter.setNewData(tagList);
                tagAdapter.expandAll();
                mBottomSheetDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
                mBottomSheetDialog.show();
                tag_btn.show();
            }
        });
    }

    @Override
    public void showLoadErrorView(String msg) {
        runOnUiThread(() -> {
            if (!mActivityFinish) {
                ref.setVisibility(View.VISIBLE);
                tagRecyclerView.setLayoutManager(new LinearLayoutManager(TagActivity.this));
                mSwipe.setRefreshing(false);
                errorTitle.setText(msg);
                tagAdapter.setEmptyView(errorView);
                mBottomSheetDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
                mBottomSheetDialog.show();
                tag_btn.show();
            }
        });
    }

    @Override
    public void showEmptyVIew() {
        tagAdapter.setEmptyView(emptyView);
    }

    @Override
    public void showSuccessView(boolean isMain, List<AnimeDescHeaderBean> animeList) {
        runOnUiThread(() -> {
            if (!mActivityFinish) {
                if (isMain) {
                    mSwipe.setRefreshing(false);
                    animeLists = animeList;
                    animeListAdapter.setNewData(animeLists);
                } else {
                    animeListAdapter.addData(animeList);
                    setLoadState(true);
                }
            }
        });
    }

    @Override
    public void showErrorView(boolean isMain, String msg) {
        runOnUiThread(() -> {
            if (!mActivityFinish) {
                if (isMain) {
                    mSwipe.setRefreshing(false);
                    errorTitle.setText(msg);
                    animeListAdapter.setEmptyView(errorView);
                } else {
                    setLoadState(false);
                    application.showErrorToastMsg(msg);
                }
            }
        });
    }

    @Override
    public void getPageCountSuccessView(int count) {
        pageCount = count;
    }
}
