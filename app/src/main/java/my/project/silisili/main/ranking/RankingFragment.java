package my.project.silisili.main.ranking;

import android.os.Bundle;
import android.view.ViewGroup;

import my.project.silisili.databinding.FragmentRankingBinding;
import my.project.silisili.main.base.LazyFragment2;

/**
 * 人气排行碎片页面
 */
public class RankingFragment extends LazyFragment2<FragmentRankingBinding> {
    private static final String PARAM_TITLE = "title";
    private String title;

    public RankingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param title 碎片标题
     * @return A new instance of fragment RankingFragment.
     */
    public static RankingFragment newInstance(String title) {
        RankingFragment fragment = new RankingFragment();
        Bundle args = new Bundle();
        args.putString(PARAM_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            title = getArguments().getString(PARAM_TITLE);
        }
    }

    @Override
    protected FragmentRankingBinding getViewBinding(ViewGroup container) {
        return FragmentRankingBinding.inflate(getLayoutInflater(), container, false);
    }

    @Override
    protected void initViews() {

    }

    @Override
    protected void initData() {

    }
}