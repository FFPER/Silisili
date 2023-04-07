package my.project.silisili.main.ranking;

import androidx.annotation.NonNull;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import my.project.silisili.R;
import my.project.silisili.api.Api;
import my.project.silisili.bean.RankingBean;
import my.project.silisili.net.HttpGet;
import my.project.silisili.util.Utils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class RankingModel implements RankingContract.Model {
    private static final String[] TABS = Utils.getArray(R.array.ranking_array);

    @Override
    public void getData(RankingContract.LoadDataCallback callback) {
        new HttpGet(Api.RANKING_API, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.error(e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try {
                    assert response.body() != null;
                    Document body = Jsoup.parse(response.body().string());
                    Map<String, ArrayList<RankingBean>> result = new HashMap<>();
                    Elements rankingTypes = body.select("ul.top-list");
                    for (int i = 0; i < TABS.length; i++) {
                        result.put(TABS[i], getRankingsData(rankingTypes.get(i).select("li a")));
                    }
                    callback.success(result);
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.error(Utils.getString(R.string.parsing_error));
                }
            }
        });
    }

    /**
     * 排行榜
     *
     * @param els 元素
     */
    public ArrayList<RankingBean> getRankingsData(Elements els) {
        ArrayList<RankingBean> list = new ArrayList<>();
        //  示例结果： {电影:{总排行:[row1,row2,row3...],月排行:[],周排行:[]},电视剧:{},经典动漫:{}}
        for (int i = 0; i < els.size(); i++) {
            RankingBean bean = new RankingBean();
            bean.setUrl(els.get(i).attr("href"));
            for (int j = 0; j < els.get(i).childNodeSize(); j++) {
                Element element = els.get(i).child(j);
                if (j == 0) {
                    bean.setIndex(element.text());
                } else if (j == 1) {
                    bean.setTitle(element.text());
                } else if (j == 2) {
                    bean.setScore(element.text());
                } else if (j == 3) {
                    bean.setHeat(element.text());
                }
            }
            list.add(bean);
        }
        return list;
    }
}
