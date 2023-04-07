package my.project.silisili.main.ranking;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter.base.BaseQuickAdapter;

import java.util.ArrayList;
import java.util.List;

import my.project.silisili.R;
import my.project.silisili.adapter.RankingAdapter;
import my.project.silisili.application.Silisili;
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
    private static final String PARAM_DATA = "rankingData";
    protected RankingAdapter adapter;
    private List<RankingBean> list;
    private Silisili application;
    private View view;
    private View errorView;
    private TextView errorTitle;

    public RankingFragment() {
        // Required empty public constructor
    }

    /**
     * @param list 数据
     * @return A new instance of fragment RankingFragment.
     */
    public static RankingFragment newInstance(ArrayList<RankingBean> list) {
        RankingFragment fragment = new RankingFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(PARAM_DATA, list);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                list = getArguments().getParcelableArrayList(PARAM_DATA, RankingBean.class);
            } else {
                list = getArguments().getParcelableArrayList(PARAM_DATA);
            }
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
        if (application == null) application = Silisili.getInstance();
        initAdapter();
    }

    @Override
    protected void initData() {
        initRankingData();
    }

    private void initAdapter() {
        if (adapter == null) {
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
        }
        viewBinding.rvRankingList.setAdapter(adapter);
        viewBinding.rvRankingList.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    /**
     * 初始化排行榜数据
     */
    private void initRankingData() {
        if (adapter.getData().isEmpty()) {
            if (list.size() == 0) {
                if (!application.rankTypeError.isEmpty()) {
                    errorTitle.setText(application.rankTypeError);
                    adapter.setEmptyView(errorView);
                }
            } else
                adapter.setNewData(list);
        }
    }
}