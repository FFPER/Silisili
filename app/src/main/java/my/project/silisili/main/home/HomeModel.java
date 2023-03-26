package my.project.silisili.main.home;

import android.annotation.SuppressLint;
import android.text.format.DateUtils;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import my.project.silisili.R;
import my.project.silisili.net.HttpGet;
import my.project.silisili.util.Utils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class HomeModel implements HomeContract.Model {
    private static final String[] TABS = Utils.getArray(R.array.week_array);

    @Override
    public void getData(final HomeContract.LoadDataCallback callback) {
        new HttpGet(my.project.silisili.application.Silisili.DOMAIN, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.error(e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try {
                    LinkedHashMap<String, Object> map = new LinkedHashMap<>();
                    JSONObject weekObj = new JSONObject();
                    Document body = Jsoup.parse(response.body().string());
//                        map.put("url", body.select("ul.nav_lef > li").get(1).select("a").get(0).attr("href"));
//                        map.put("title",  body.select("ul.nav_lef > li").get(1).select("a").get(0).text());
//                        setDataToJson(TABS[0], body.select("div.xfswiper0 >div.swiper-wrapper >div.swiper-slide >ul.clear > li"), weekObj);
//                        setDataToJson(TABS[1], body.select("div.xfswiper1 >div.swiper-wrapper >div.swiper-slide >ul.clear > li"), weekObj);
//                        setDataToJson(TABS[2], body.select("div.xfswiper2 >div.swiper-wrapper >div.swiper-slide >ul.clear > li"), weekObj);
//                        setDataToJson(TABS[3], body.select("div.xfswiper3 >div.swiper-wrapper >div.swiper-slide >ul.clear > li"), weekObj);
//                        setDataToJson(TABS[4], body.select("div.xfswiper4 >div.swiper-wrapper >div.swiper-slide >ul.clear > li"), weekObj);
//                        setDataToJson(TABS[5], body.select("div.xfswiper5 >div.swiper-wrapper >div.swiper-slide >ul.clear > li"), weekObj);
//                        setDataToJson(TABS[6], body.select("div.xfswiper6 >div.swiper-wrapper >div.swiper-slide >ul.clear > li"), weekObj);
                    // 2022/12/10 这里对应的侧滑菜单的第一项，需要先整一个标签放上就行，放上番剧的第一个吧  AnimeListModel.java
                    Element titleEle = body.select("div.swiper-slide").get(0);
                    String url = String.format("%1$s%2$s", my.project.silisili.application.Silisili.DOMAIN, titleEle.select("a").get(0).attr("href"));
                    map.put("url", url);
                    map.put("title", titleEle.select("div.swiper-slide-votitle").get(0).childNode(1));
                    @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
                    String date = sdf.format(Calendar.getInstance().getTime());

                    // 不需要最近更新tab  setDataToJson(TABS[7], date, body.select("div#mytabweek > li").get(0).select("ul.tab-content > li"), weekObj);
                    setDataToJson(TABS[0], date, body.select("div#mytabweek > li").get(1).select("ul.tab-content > li"), weekObj);
                    setDataToJson(TABS[1], date, body.select("div#mytabweek > li").get(2).select("ul.tab-content > li"), weekObj);
                    setDataToJson(TABS[2], date, body.select("div#mytabweek > li").get(3).select("ul.tab-content > li"), weekObj);
                    setDataToJson(TABS[3], date, body.select("div#mytabweek > li").get(4).select("ul.tab-content > li"), weekObj);
                    setDataToJson(TABS[4], date, body.select("div#mytabweek > li").get(5).select("ul.tab-content > li"), weekObj);
                    setDataToJson(TABS[5], date, body.select("div#mytabweek > li").get(6).select("ul.tab-content > li"), weekObj);
                    setDataToJson(TABS[6], date, body.select("div#mytabweek > li").get(7).select("ul.tab-content > li"), weekObj);
                    map.put("week", weekObj);
                    callback.success(map);
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.error(Utils.getString(R.string.parsing_error));
                }
            }
        });
    }

    /**
     * 新番时间表
     *
     * @param title
     * @param els
     * @param date       现在的日期
     * @param jsonObject
     * @throws JSONException
     */
    public static void setDataToJson(String title, String date, Elements els, JSONObject jsonObject) throws JSONException {
        JSONArray arr = new JSONArray();
        for (int i = 0, size = els.size(); i < size; i++) {
            JSONObject object = new JSONObject();
            object.put("title", els.get(i).select("img").attr("alt"));
            object.put("img", els.get(i).select("img").attr("src"));
            object.put("url", els.get(i).select("a").attr("href"));
            object.put("drama", els.get(i).select("p.num > a") == null ? "" : String.format("更新至%1$s", els.get(i).select("p.num > a").text()));
            object.put("date", els.get(i).select("p.num").get(0).childNode(1).toString());
            object.put("new", date.equals(object.getString("date")));
            arr.put(object);
        }
        jsonObject.put(title, arr);
    }
}
