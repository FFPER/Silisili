package my.project.silisili.main.search;

import android.util.Log;

import androidx.annotation.NonNull;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import my.project.silisili.api.Api;
import my.project.silisili.bean.AnimeDescHeaderBean;
import my.project.silisili.net.HttpGet;
import my.project.silisili.net.HttpPost;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class SearchModel implements SearchContract.Model {
    //    private final static Pattern SEARCHID_PATTERN = Pattern.compile("searchid=(.*)");
    private final static Pattern SEARCH_ID_PATTERN = Pattern.compile("vodsearch(.+?)\\/");//截取之间的字符穿

    @Override
    public void getData(String title, String searchId, int page, boolean isMain, SearchContract.LoadDataCallback callback) {
        Log.e("page", page + "");
        if (page != 1) {// 不是首页，就可以请求
            // 如果不是第一页 则使用GET方式
            Log.e("分页GET", String.format(Api.SEARCH_GET_API, searchId, page));
            new HttpGet(String.format(Api.SEARCH_GET_API, searchId, page), new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    callback.error(isMain, e.getMessage());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    getSearchData(response, isMain, callback);
                }
            });
        } else {
            // 第一次使用POST获取searchID
            Log.e("第一页POST", "GO" + title);
            String encodedUrl = title;
            try {
                encodedUrl = URLEncoder.encode(title, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            new HttpPost(Api.SEARCH_API, "wd=" + encodedUrl, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, IOException e) {
                    callback.error(isMain, e.getMessage());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    getSearchData(response, isMain, callback);
                }
            });
        }

    }

    public void getSearchData(Response response, boolean isMain, SearchContract.LoadDataCallback callback) {
        try {
            assert response.body() != null;
            Document body = Jsoup.parse(response.body().string());
            Elements animeList = body.getElementsByClass("post-list").select("div.entry-container");
            if (animeList.size() > 0) {
                Elements pages = body.select("ul.list-page > li > a");
                if (pages.size() > 0) {
                    // 现在无法知道总页码,所以应该每次请求的时候重新解析页码 最后一个是个香油的箭头
                    Element element = pages.get(pages.size() - 2);
                    // 获取页码
                    String page = element.text();
                    if (isMain) {
                        // 只有首次加载的时候才需要筛选出查询条件
                        Matcher m = SEARCH_ID_PATTERN.matcher(element.attr("href"));
                        String searchID = "";
                        if (m.find()) {
                            searchID = m.group().replace("vodsearch", "").replace("/", "");
                        }
                        callback.searchID(searchID);
                    }
                    callback.pageCount(Integer.parseInt(page));
                }
                // 第一页肯定走这个方法
                List<AnimeDescHeaderBean> list = new ArrayList<>();
                for (int i = 0; i < animeList.size(); i++) {
                    AnimeDescHeaderBean bean = new AnimeDescHeaderBean();
                    bean.setName(animeList.get(i).select("div.entry-title > a").text());
                    String imageSrc = animeList.get(i).select("div.search-image > a > img").attr("srcset");
                    bean.setImg(imageSrc.contains("http") ? imageSrc : my.project.silisili.application.Silisili.DOMAIN + imageSrc);
                    bean.setUrl(animeList.get(i).select("div.entry-title").select("a").attr("href"));
                    //设置年代-其实是阅读量
                    bean.setYear(animeList.get(i).select("time").text());
                    Node metaNode = animeList.get(i).select("div.entry-meta").get(0);
                    //设置标签
                    bean.setTag(metaNode.childNode(0).toString().replaceAll("\n", ""));
                    //设置简介
                    bean.setDesc(animeList.get(i).select("div.entry-summary").select("p").text().trim());
                    //设置状态
                    bean.setState(metaNode.childNode(metaNode.childNodeSize() - 1).toString());
                    // 这里只有一个标签字段，不再包含年份和地区等信息
//                    Elements label = animeList.get(i).getElementsByClass("entry-meta");
//                    for (int k = 0; k < label.size(); k++) {
//                        String str = label.get(k).text();
//                        if (str.contains("地区"))
//                            bean.setRegion(str);
//                        else if (str.contains("年代"))
//                            bean.setYear(str);
//                        else if (str.contains("标签"))
//                            bean.setTag(str);
//                    }
//                    Elements p = animeList.get(i).select("p");
//                    for (int j = 0; j < p.size(); j++) {
//                        String str = p.get(j).text();
//                        if (str.contains("看点"))
//                            bean.setShow(str);
//                        else if (str.contains("简介"))
//                            bean.setDesc(str);
//                        else if (str.contains("状态"))
//                            bean.setState(str);
//                    }
                    list.add(bean);
                }
                callback.success(isMain, list);
            } else {
                callback.error(isMain, "没有搜索到相关信息");
            }
        } catch (Exception e) {
            e.printStackTrace();
            callback.error(e.getMessage());
        }
    }

}
