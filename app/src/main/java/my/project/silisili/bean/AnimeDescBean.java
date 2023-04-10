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
    private Map<String, ArrayList<AnimeDescDetailsBean>> animeDescDetailsBeans = new HashMap<>();
    // 番剧推荐集合
    private List<AnimeDescRecommendBean> animeDescRecommendBeans = new ArrayList<>();

//    public List<AnimeDescDetailsBean> getAnimeDescDetailsBeans() {
//        return animeDescDetailsBeans;
//    }
//
//    public void setAnimeDescDetailsBeans(List<AnimeDescDetailsBean> animeDescDetailsBeans) {
//        this.animeDescDetailsBeans = animeDescDetailsBeans;
//    }

    public Map<String, ArrayList<AnimeDescDetailsBean>> getAnimeDescDetailsBeans2() {
        return animeDescDetailsBeans;
    }

    public String[] getAnimeDescDetailsKey() {
        String[] keys = new String[this.animeDescDetailsBeans.size()];
        return animeDescDetailsBeans.keySet().toArray(new String[0]);
    }


    public ArrayList<AnimeDescDetailsBean> getAnimeDescDetailsBeans(String key) {
        return animeDescDetailsBeans.containsKey(key) ? animeDescDetailsBeans.get(key) : new ArrayList<>();
    }

    public void setAnimeDescDetailsBeans(Map<String, ArrayList<AnimeDescDetailsBean>> animeDescDetailsBeans) {
        this.animeDescDetailsBeans = animeDescDetailsBeans;
    }

    public void addAnimeDescDetailsBeans(String type, ArrayList<AnimeDescDetailsBean> animeDescDetailsBeans) {
        this.animeDescDetailsBeans.put(type, animeDescDetailsBeans);
    }

    public List<AnimeDescRecommendBean> getAnimeDescRecommendBeans() {
        return animeDescRecommendBeans;
    }

    public void setAnimeDescRecommendBeans(List<AnimeDescRecommendBean> animeDescRecommendBeans) {
        this.animeDescRecommendBeans = animeDescRecommendBeans;
    }

}
