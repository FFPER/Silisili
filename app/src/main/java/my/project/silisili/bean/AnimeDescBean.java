package my.project.silisili.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnimeDescBean implements Serializable {
    // 播放列表集合
//    private List<AnimeDescDetailsBean> animeDescDetailsBeans = new ArrayList<>();
    // 播放列表集合
    private Map<String, List<AnimeDescDetailsBean>> animeDescDetailsBeans = new HashMap<>();
    // 番剧推荐集合
    private List<AnimeDescRecommendBean> animeDescRecommendBeans = new ArrayList<>();

//    public List<AnimeDescDetailsBean> getAnimeDescDetailsBeans() {
//        return animeDescDetailsBeans;
//    }
//
//    public void setAnimeDescDetailsBeans(List<AnimeDescDetailsBean> animeDescDetailsBeans) {
//        this.animeDescDetailsBeans = animeDescDetailsBeans;
//    }

    public Map<String, List<AnimeDescDetailsBean>> getAnimeDescDetailsBeans2() {
        return animeDescDetailsBeans;
    }

    public List<AnimeDescDetailsBean> getAnimeDescDetailsBeans() {
        return animeDescDetailsBeans.get("NO.T");
    }

    public void setAnimeDescDetailsBeans(Map<String, List<AnimeDescDetailsBean>> animeDescDetailsBeans) {
        this.animeDescDetailsBeans = animeDescDetailsBeans;
    }

    public void addAnimeDescDetailsBeans(String type, List<AnimeDescDetailsBean> animeDescDetailsBeans) {
        this.animeDescDetailsBeans.put(type, animeDescDetailsBeans);
    }

    public List<AnimeDescRecommendBean> getAnimeDescRecommendBeans() {
        return animeDescRecommendBeans;
    }

    public void setAnimeDescRecommendBeans(List<AnimeDescRecommendBean> animeDescRecommendBeans) {
        this.animeDescRecommendBeans = animeDescRecommendBeans;
    }

}
