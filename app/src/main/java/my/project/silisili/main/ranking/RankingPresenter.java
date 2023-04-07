package my.project.silisili.main.ranking;

import java.util.ArrayList;
import java.util.Map;

import my.project.silisili.bean.RankingBean;
import my.project.silisili.main.base.BasePresenter;
import my.project.silisili.main.base.Presenter;

public class RankingPresenter extends Presenter<RankingContract.View> implements BasePresenter, RankingContract.LoadDataCallback {

    private final RankingContract.View view;
    private final RankingModel model;

    /**
     * 构造函数
     *
     * @param view 需要关联的View
     */
    public RankingPresenter(RankingContract.View view) {
        super(view);
        this.view = view;
        model = new RankingModel();
    }

    @Override
    public void error(String msg) {
        view.showLoadErrorView(msg);
    }

    @Override
    public void loadData(boolean isMain) {
        if (isMain)
            view.showLoadingView();
        model.getData(this);
    }

    @Override
    public void success(Map<String, ArrayList<RankingBean>> map) {
        view.showLoadSuccess(map);
    }
}
