package my.project.silisili.main.home;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import my.project.silisili.R;
import my.project.silisili.api.Api;
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
                    assert response.body() != null;
                    Document body = Jsoup.parse(Objects.requireNonNull(response.body()).string());
                    // 2022/12/10 这里对应的侧滑菜单的第一项，需要先整一个标签放上就行，放上专题的第一个吧  AnimeListModel.java
                    Element titleEle = body.select("div.row.wall > article:first-child").get(0);
                    String title = titleEle.select("div.entry-meta").get(0).childNode(0).toString().replaceAll("\n", "");
                    map.put("url", String.format("%s%s/", Api.TITLE_API, URLEncoder.encode(title, "utf-8")));
                    map.put("title", title);
                    @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
                    String date = sdf.format(Calendar.getInstance().getTime());
                    int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);//当前日期 周日1 ... 周六7
                    // 不需要最近更新tab  setDataToJson(TABS[7], date, body.select("div#mytabweek > li").get(0).select("ul.tab-content > li"), weekObj);
                    Elements weekLiEls = body.select("div.tab-item > li");// 番剧时间表的父容器
                    for (int index = 0; index < TABS.length; index++) {
                        if (index != TABS.length - 1) {
                            setDataToJson(TABS[index], date, weekLiEls.get(index + 1).select("ul.tab-content > li"), weekObj);
                            Log.e("index", "index=" + index);
                        } else {
                            setDataToJson(TABS[TABS.length - 1], date, weekLiEls.get(0).select("ul.tab-content > li"), weekObj);
                        }
                    }
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
        Pattern pattern = Pattern.compile("http(.*).[jpg|jpeg|png]", Pattern.DOTALL);
        for (int i = 0, size = els.size(); i < size; i++) {
            JSONObject object = new JSONObject();
            object.put("title", els.get(i).select("a.item-cover").attr("title"));
            Matcher m = pattern.matcher(els.get(i).select("a.item-cover > span").attr("style"));
            if (m.find()) {
                object.put("img", m.group());
            } else {
                object.put("img", "");
            }
            // 番剧信息
            Elements elements = els.get(i).select("div.item-info");
            object.put("url", elements.select("a").attr("href"));
            object.put("drama", String.format("更新至%s", elements.select("p.num > a").text()));
            object.put("date", elements.select("p.num").get(0).childNode(0).toString());
            object.put("new", date.equals(object.getString("date")));
            arr.put(object);
        }
        jsonObject.put(title, arr);
    }
}
