package my.project.silisili.main.ranking;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import my.project.silisili.R;
import my.project.silisili.databinding.ActivityRankingBinding;
import my.project.silisili.main.base.BaseActivity;

/**
 * 排行榜
 */
public class RankingActivity extends BaseActivity<RankingContract.View, RankingPresenter> {
    private ActivityRankingBinding viewBinding;

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

    }

    @Override
    protected void initBeforeView() {

    }
}