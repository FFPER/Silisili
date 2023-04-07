package my.project.silisili.adapter;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import my.project.silisili.R;
import my.project.silisili.bean.RankingBean;

public class RankingAdapter extends BaseQuickAdapter<RankingBean, BaseViewHolder> {

    public RankingAdapter(@Nullable List<RankingBean> data) {
        super(R.layout.item_ranking, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, RankingBean item) {
        // 修改序号的背景颜色
        if (item.getIndex() == 1) {
            helper.setBackgroundRes(R.id.tv_ranking_index, R.color.rank_first_color);
        } else if (item.getIndex() == 2) {
            helper.setBackgroundRes(R.id.tv_ranking_index, R.color.rank_second_color);
        } else if (item.getIndex() == 3) {
            helper.setBackgroundRes(R.id.tv_ranking_index, R.color.rank_third_color);
        } else {
            helper.setBackgroundRes(R.id.tv_ranking_index, R.color.rank_other_color);
        }
        // 设置序号的值
        helper.setText(R.id.tv_ranking_index, String.valueOf(item.getIndex()));
        // 设置标题的值
        helper.setText(R.id.tv_ranking_index, String.valueOf(item.getTitle()));
        // 设置热度的值
        helper.setText(R.id.tv_ranking_index, String.valueOf(item.getHeat()));
        // 设置分数的值
        helper.setText(R.id.tv_ranking_index, String.valueOf(item.getScore()));
    }
}