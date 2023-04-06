package my.project.silisili.adapter;

import android.content.Context;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import my.project.silisili.bean.HomeWekBean;

public class RankingAdapter extends BaseQuickAdapter<HomeWekBean, BaseViewHolder> {
    private Context context;

    // TODO: 2023/4/6 先将颜色读取出来 
    public RankingAdapter(Context context, int layoutResId, @Nullable List<HomeWekBean> data) {
        super(layoutResId, data);
        this.context = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, HomeWekBean item) {
// TODO: 2023/4/6 修改不就文件 
    }
}