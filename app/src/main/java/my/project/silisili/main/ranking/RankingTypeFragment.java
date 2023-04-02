package my.project.silisili.main.ranking;

import android.os.Bundle;

import android.view.ViewGroup;

import my.project.silisili.databinding.FragmentTypeBinding;
import my.project.silisili.main.base.LazyFragment2;

/**
 * 类型碎片 电影、电视剧、经典动漫
 */
public class RankingTypeFragment extends LazyFragment2<FragmentTypeBinding> {

    private static final String PARAM_TITLE = "title";

    private String title;

    public RankingTypeFragment() {
        // Required empty public constructor
    }

    /**
     *
     * @param title 标题
     * @return A new instance of fragment RankingTypeFragment.
     */
    public static RankingTypeFragment newInstance(String title) {
        RankingTypeFragment fragment = new RankingTypeFragment();
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
    protected FragmentTypeBinding getViewBinding(ViewGroup container) {
        return FragmentTypeBinding.inflate(getLayoutInflater(), container, false);
    }


    @Override
    protected void initData() {

    }
}