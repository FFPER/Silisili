package my.project.silisili.main.video;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import my.project.silisili.application.Silisili;
import my.project.silisili.database.DatabaseUtil;
import my.project.silisili.net.HttpGet;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class VideoModel implements VideoContract.Model {
    private final static Pattern PLAY_URL_PATTERN = Pattern.compile("(https?|ftp|file):\\/\\/[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]");
    private final static Pattern PLAY_DATA_OBJECT = Pattern.compile("\\{(.+?)\\}");//截取之间的字符穿

    @Override
    public void getData(String title, String HTML_url, VideoContract.LoadDataCallback callback) {
        Log.e("playHtml", HTML_url);
        new HttpGet(HTML_url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.error();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    Document doc = Jsoup.parse(response.body().string());
                    String fid = DatabaseUtil.getAnimeID(title);
                    DatabaseUtil.addIndex(fid, HTML_url.replaceAll(Silisili.DOMAIN, ""));
//                    String iframeUrl = doc.getElementById("iframe").attr("src");
//                    Log.e("iframeUrl",iframeUrl);
//                    if (iframeUrl.isEmpty() ||  !URLUtil.isValidUrl(iframeUrl)) callback.empty();
//                    else {
//                        // 解析
//                        String host = iframeUrl;
//                        java.net.URL urlHost;
//                        try {
//                            urlHost = new java.net.URL(iframeUrl);
//                            host = urlHost.getHost();
//                        } catch (MalformedURLException e) {
//                            e.printStackTrace();
//                        }
//                        new HttpGet(iframeUrl, host, new Callback() {
//                            @Override
//                            public void onFailure(Call call, IOException e) {
//                                callback.error();
//                            }
//
//                            @Override
//                            public void onResponse(Call call, Response response) throws IOException {
//                                Document doc = Jsoup.parse(response.body().string());
                    String source = doc.select("source").attr("src");
                    if (source.isEmpty()) {
                        Elements scripts = doc.select("script");
                        for (Element element : scripts) {
                            String html = element.html();
                            if (html.contains("var player_data")) {
                                //  2022/12/10 这里不能使用正则表达式获取了
//                                Matcher m = PLAY_URL_PATTERN.matcher(URLDecoder.decode(element.html(), "UTF-8"));
//                                if (m.find()) {
//                                    source = m.group();
//                                    break;
//                                }
                                // 需要先base64转义再decode
                                Matcher m = PLAY_DATA_OBJECT.matcher(URLDecoder.decode(html, "UTF-8"));
                                if (m.find()) {
                                    source = m.group();
                                }
                                try {
                                    JSONObject jsonObject = new JSONObject(source);
                                    source = jsonObject.getString("url");
                                    if (TextUtils.isEmpty(source)) {
                                        break;
                                    }
//                                  需要判断url是否是base64编码，1 否 0 是
                                    String encrypt = jsonObject.getString("encrypt");
                                    if ("1".equals(encrypt)) {
                                    } else if ("2".equals(encrypt)) {
                                        source = new String(Base64.decode(source.getBytes(), Base64.NO_WRAP));
                                    }
                                    source = URLDecoder.decode(source, "UTF-8");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    break;
                                }
                            }
                        }
                    }
                    if (source.isEmpty()) callback.sendIframeUrl(HTML_url);// iframeUrl
                    else callback.success(source);
//                            }
//                        });
//                    }
                } catch (SocketTimeoutException e) {
                    callback.error();
                }
            }
        });
    }
}
