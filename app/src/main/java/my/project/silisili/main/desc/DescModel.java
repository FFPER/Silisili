package my.project.silisili.main.desc;

import android.text.TextUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import my.project.silisili.R;
import my.project.silisili.application.Silisili;
import my.project.silisili.bean.AnimeDescBean;
import my.project.silisili.bean.AnimeDescDetailsBean;
import my.project.silisili.bean.AnimeDescHeaderBean;
import my.project.silisili.bean.AnimeDescRecommendBean;
import my.project.silisili.bean.DownBean;
import my.project.silisili.database.DatabaseUtil;
import my.project.silisili.net.HttpGet;
import my.project.silisili.util.Utils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class DescModel implements DescContract.Model {
    private String fid;
    private String dramaStr = "";
    private AnimeDescBean animeDescBean = new AnimeDescBean();

    @Override
    public void getData(String url, DescContract.LoadDataCallback callback) {
        new HttpGet(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.error(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    Document doc = Jsoup.parse(response.body().string());
                    Element detail = doc.getElementById("main");
                    //新版解析方案
                    if (detail != null) {
                        AnimeDescHeaderBean bean = new AnimeDescHeaderBean();
                        String animeName = detail.select("h1").text();
                        bean.setImg(detail.select("img").attr("src").contains("http") ? detail.select("img").attr("src") : Silisili.DOMAIN + detail.select("img").attr("src"));
                        bean.setName(animeName);
                        //创建index
                        DatabaseUtil.addAnime(animeName);
                        fid = DatabaseUtil.getAnimeID(animeName);
                        dramaStr = DatabaseUtil.queryAllIndex(fid);
                        Elements tags = detail.select("div.v_sd_r > p:first-child > a");
                        bean.setUrl(url);
                        setTags(tags, bean);
                        //简介
                        bean.setDesc(detail.select("div.v_cont").get(0).childNode(2) == null ? "略" : detail.select("div.v_cont").get(0).childNode(2).toString().replaceAll(".*()：", "")
                                .replaceAll("&nbsp;"," "));
                        callback.successDesc(bean);
// 播放类型/下载
                        Elements playDesc = doc.select("div.play-pannel-box div.widget-title");
                        Elements play = doc.getElementsByClass("play-pannel-list");
                        if (play.size() > 0) {// 有数据
                            for (int i = 0; i < play.size(); i++) {
                                String typeTitle = playDesc.get(i).text().toUpperCase(Locale.CHINA);
                                if (!typeTitle.contains("下载")) {
                                    //分集
                                    Elements play_list = play.get(i).select("ul > li");
                                    setPlayData(typeTitle, play_list);
                                } else {
                                    //下载
                                    Elements down = play.get(i).select("ul > li > a");
                                    if (down.size() > 0) {
                                        List<DownBean> downList = new ArrayList<>();
                                        for (int j = 0; j < down.size(); j++) {
                                            if (!down.get(j).text().isEmpty()) {
                                                downList.add(
                                                        new DownBean(
                                                                down.get(j).text(),
                                                                down.get(j).attr("href")
                                                        )
                                                );
                                            }
                                        }
                                        callback.hasDown(downList);
                                    }
                                }
                            }
//                            //分集
//                            Elements play_list = doc.getElementsByClass("time_pic").get(0).getElementsByClass("swiper-slide").select("ul.clear >li");
                            //下载
//                            Elements down = doc.getElementsByClass("time_pic").get(0).getElementsByClass("xfswiper3").select("ul.clear >li >a");
                            //推荐
                            Elements recommend = doc.getElementsByClass("sliderlist").select("div.vod_hl_list a");
                            if (recommend.size() > 0) {
                                setRecommendData(recommend);
                            }
                            callback.isFavorite(DatabaseUtil.checkFavorite(animeName));
                            callback.successMain(animeDescBean);
                        } else {
                            callback.error(Utils.getString(R.string.no_playlist_error));
                        }
                    } else {
                        //解析失败
                        callback.error(Utils.getString(R.string.parsing_error));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.error(e.getMessage());
                }
            }
        });
    }

    public void setTags(Elements tags, AnimeDescHeaderBean bean) {
        List<String> tagTitles = new ArrayList<>();
        List<String> tagUrls = new ArrayList<>();
        for (int i = 0; i < tags.size(); i++) {
            if (tags.get(i).attr("href").contains("/class/")) {
                tagTitles.add(tags.get(i).text().replaceAll(".*()：", ""));
                tagUrls.add(tags.get(i).attr("href"));
                tags.remove(i);
                i--;
            }
        }
//        Element bq = tags.get(0);// 类型
//        String tagStr = bq.text().replaceAll(".*()：", "");
//        if (!tagStr.isEmpty()) {
//            if (tagStr.contains("|"))
//                for (String str : tagStr.split("\\|")) {
//                    tagTitles.add(str);
//                    tagUrls.add("");
//                }
//            else if (tagStr.contains(","))
//                for (String str : tagStr.split(",")) {
//                    tagTitles.add(str);
//                    tagUrls.add("");
//                }
//            else if (tagStr.contains(" "))
//                for (String str : tagStr.split(" ")) {
//                    tagTitles.add(str);
//                    tagUrls.add("");
//                }
//        }
        //类型的数据已经被删除
        Element dq = tags.get(0);//地区
        tagTitles.add(dq.text().replaceAll(".*()：", ""));
        tagUrls.add(dq.select("a").attr("href"));
        Element nd = tags.get(1);//年度
        tagTitles.add(nd.text().replaceAll(".*()：", ""));
        tagUrls.add(nd.select("a").attr("href"));
        Element zt = tags.get(2);//语言
        tagTitles.add(zt.text().replaceAll(".*()：", ""));
        tagUrls.add(zt.select("a").attr("href"));
        bean.setTagTitles(tagTitles);
        bean.setTagUrls(tagUrls);
    }

    /**
     * 番剧列表
     *
     * @param type 在线播放源
     * @param els
     */
    public void setPlayData(String type, Elements els) {
        List<AnimeDescDetailsBean> animeDescDetailsBeans = new ArrayList<>();
        int k = 0;
        boolean select;
        for (int i = 0; i < els.size(); i++) {
            String name = els.get(i).select("a").text();
            String watchUrl = els.get(i).select("a").attr("href");
            if (!watchUrl.isEmpty()) {
                k++;
                if (dramaStr.contains(watchUrl.replaceAll(Silisili.DOMAIN, "")))
                    select = true;
                else
                    select = false;
                animeDescDetailsBeans.add(new AnimeDescDetailsBean(name, watchUrl, select));
            }
        }
        if (k == 0)
            animeDescDetailsBeans.add(new AnimeDescDetailsBean(Utils.getString(R.string.no_resources), "", false));
        animeDescBean.addAnimeDescDetailsBeans(type, animeDescDetailsBeans);
    }

    public void setRecommendData(Elements els) {
        List<AnimeDescRecommendBean> animeDescRecommendBeans = new ArrayList<>();
        for (int i = 0; i < els.size(); i++) {
            String str = els.get(i).text();
            String imagePath = "";
            if (!TextUtils.isEmpty(str)) {
                imagePath = els.get(i).selectFirst("i.thumb").attr("style").split("\\(")[1].split("\\)")[0];
            }
            animeDescRecommendBeans.add(new AnimeDescRecommendBean(str,
                    imagePath.contains("http") ? imagePath : Silisili.DOMAIN + imagePath,
                    Silisili.DOMAIN + els.get(i).select("a").attr("href"))
            );
        }
        animeDescBean.setAnimeDescRecommendBeans(animeDescRecommendBeans);
    }

}
