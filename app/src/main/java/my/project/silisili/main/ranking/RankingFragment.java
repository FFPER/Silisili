package my.project.silisili.main.ranking;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;

import com.chad.library.adapter.base.BaseQuickAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import my.project.silisili.R;
import my.project.silisili.adapter.FragmentAdapter;
import my.project.silisili.adapter.RankingAdapter;
import my.project.silisili.application.Silisili;
import my.project.silisili.bean.HomeWekBean;
import my.project.silisili.bean.RankingBean;
import my.project.silisili.databinding.FragmentRankingBinding;
import my.project.silisili.main.base.LazyFragment2;
import my.project.silisili.main.desc.DescActivity;
import my.project.silisili.util.Utils;
import my.project.silisili.util.VideoUtils;

/**
 * 排行碎片页面
 */
public class RankingFragment extends LazyFragment2<FragmentRankingBinding> {
    private static final String PARAM_TITLE = "rankingType";
    private String rankingType;
    protected RankingAdapter adapter;
    private List<RankingBean> list = new ArrayList<>();
    private Silisili application;
    private View view;
    private View errorView;
    private TextView errorTitle;

    public RankingFragment() {
        // Required empty public constructor
    }

    /**
     * @param rankingType 碎片标题
     * @return A new instance of fragment RankingFragment.
     */
    public static RankingFragment newInstance(String rankingType) {
        RankingFragment fragment = new RankingFragment();
        Bundle args = new Bundle();
        args.putString(PARAM_TITLE, rankingType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            rankingType = getArguments().getString(PARAM_TITLE);
        }
    }

    @Override
    protected FragmentRankingBinding getViewBinding(ViewGroup container) {
        return FragmentRankingBinding.inflate(getLayoutInflater(), container, false);
    }

    @Override
    protected void initViews() {
        errorView = getLayoutInflater().inflate(R.layout.base_error_view, (ViewGroup) viewBinding.rvRankingList.getParent(), false);
        errorTitle = errorView.findViewById(R.id.title);
        if (Utils.checkHasNavigationBar(requireActivity()))
            viewBinding.rvRankingList.setPadding(0, 0, 0, Utils.getNavigationBarHeight(requireActivity()));
        if (application == null) application = (Silisili) requireActivity().getApplication();
        initAdapter();
    }

    @Override
    protected void initData() {
        initRankingData();
    }

    private void initAdapter() {
        if (adapter == null) {
            viewBinding.rvRankingList.setLayoutManager(new GridLayoutManager(getActivity(), Utils.isPad() ? 5 : 3));
            adapter = new RankingAdapter(list);
            adapter.openLoadAnimation();
            adapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
            adapter.setOnItemClickListener((adapter, view, position) -> {
                if (!Utils.isFastClick()) return;
                RankingBean bean = (RankingBean) adapter.getItem(position);
                Bundle bundle = new Bundle();
                assert bean != null;
                bundle.putString("name", bean.getTitle());
                bundle.putString("url", VideoUtils.getSiliUrl(bean.getUrl()));
                startActivity(new Intent(getActivity(), DescActivity.class).putExtras(bundle));
            });
            viewBinding.rvRankingList.setAdapter(adapter);
        }
    }

    /**
     * 初始化排行榜数据
     */
    private void initRankingData() {
        viewBinding.pbRankingLoading.setVisibility(View.GONE);
        adapter.removeAllFooterView();
        if (adapter.getData().isEmpty()) {
            list = getList(rankingType);
            if (list.size() == 0) {
                if (!application.error.isEmpty()) {
                    errorTitle.setText(application.error);
                    adapter.setEmptyView(errorView);
                }
            } else
                adapter.setNewData(list);
        }
    }

    /**
     * 获取拆分list?
     *
     * @param rankingType 分类
     * @return 每一个分组的数据
     */
    private List<RankingBean> getList(String rankingType) {
        list = new ArrayList<>();
        if (application.rankType.length() > 0) {
            //try {
            // TODO: 2023/4/7 应该是从数组中get出来
            //JSONArray arr = new JSONArray(application.rankType.getString(rankingType));
            //for (int i = 0; i < arr.length(); i++) {
            //    JSONObject object = new JSONObject(arr.getString(i));
            //    list.add(new HomeWekBean(object.getString("rankingType"),
            //            object.getString("img"),
            //            object.getString("url"),
            //            object.getString("drama"),
            //            object.getBoolean("new"),
            //            object.getString("date")));
            //}
            //} catch (JSONException e) {
            //    e.printStackTrace();
            //}
        }
        return list;
    }
}