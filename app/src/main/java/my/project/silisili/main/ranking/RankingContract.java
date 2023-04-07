package my.project.silisili.main.ranking;

import java.util.ArrayList;
import java.util.Map;

import my.project.silisili.bean.RankingBean;
import my.project.silisili.main.base.BaseLoadDataCallback;
import my.project.silisili.main.base.BaseView;

/**
 * 排行榜
 */
public interface RankingContract {
    interface Model {
        void getData(RankingContract.LoadDataCallback callback);
    }

    interface View extends BaseView {
        void showLoadSuccess(Map<String, ArrayList<RankingBean>> map);
    }

    interface LoadDataCallback extends BaseLoadDataCallback {
        void success(Map<String, ArrayList<RankingBean>> map);
    }
}
