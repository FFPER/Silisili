package my.project.silisili.bean;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class AnimeDescDetailsBean implements Serializable, Parcelable {
    // 标题
    private String title;
    // 链接
    private String url;
    // 是否选中
    private boolean selected;

    public AnimeDescDetailsBean(String title, String url, boolean selected) {
        this.title = title;
        this.url = url;
        this.selected = selected;
    }

    protected AnimeDescDetailsBean(Parcel in) {
        title = in.readString();
        url = in.readString();
        selected = in.readByte() != 0;
    }

    public static final Creator<AnimeDescDetailsBean> CREATOR = new Creator<AnimeDescDetailsBean>() {
        @Override
        public AnimeDescDetailsBean createFromParcel(Parcel in) {
            return new AnimeDescDetailsBean(in);
        }

        @Override
        public AnimeDescDetailsBean[] newArray(int size) {
            return new AnimeDescDetailsBean[size];
        }
    };

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

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(url);
        dest.writeByte((byte) (selected ? 1 : 0));
    }
}
