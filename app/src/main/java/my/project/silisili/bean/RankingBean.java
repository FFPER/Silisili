package my.project.silisili.bean;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

/**
 * 排行榜单条的bean
 */
public class RankingBean implements Parcelable {
    private String index;//序号 从1开始
    private String title;
    private String url;// url
    private String heat;//热度
    private String score;//分数

    public RankingBean() {
    }

    protected RankingBean(Parcel in) {
        index = in.readString();
        title = in.readString();
        url = in.readString();
        heat = in.readString();
        score = in.readString();
    }

    public static final Creator<RankingBean> CREATOR = new Creator<RankingBean>() {
        @Override
        public RankingBean createFromParcel(Parcel in) {
            return new RankingBean(in);
        }

        @Override
        public RankingBean[] newArray(int size) {
            return new RankingBean[size];
        }
    };

    // 解除页面时专用
    public void setRankingBeanInfo(String index, String title, String heat, String score) {
        this.index = index;
        this.title = title;
        this.heat = heat;
        this.score = score;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHeat() {
        return heat;
    }

    public void setHeat(String heat) {
        this.heat = heat;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(index);
        dest.writeString(title);
        dest.writeString(url);
        dest.writeString(heat);
        dest.writeString(score);
    }
}
