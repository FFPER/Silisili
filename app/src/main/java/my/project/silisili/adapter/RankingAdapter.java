package my.project.silisili.adapter;

import android.content.Context;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import my.project.silisili.bean.HomeWekBean;

public class RankingAdapter extends BaseQuickAdapter<HomeWekBean, BaseViewHolder> {
    private Context context;

    public RankingAdapter(Context context, int layoutResId, @Nullable List<HomeWekBean> data) {
        super(layoutResId, data);
        this.context = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, HomeWekBean item) {

    }
}