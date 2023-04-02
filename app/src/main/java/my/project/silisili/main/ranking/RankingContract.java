package my.project.silisili.main.ranking;

import java.util.LinkedHashMap;

import my.project.silisili.main.base.BaseLoadDataCallback;
import my.project.silisili.main.base.BaseView;

/**
 * 排行榜
 */
public interface RankingContract {
    interface Model{
        void getData(RankingContract.LoadDataCallback callback);
    }

    interface View extends BaseView {
        void showLoadSuccess(LinkedHashMap<String,Object> map);
    }

    interface LoadDataCallback extends BaseLoadDataCallback {
        void success(LinkedHashMap<String,Object> map);
    }
}
