package my.project.silisili.bean;

/**
 * 排行榜单条的bean
 */
public class RankingBean {
    private int index;//序号 从1开始
    private String title;
    private String url;// url
    private String heat;//热度
    private String score;//分数

    public RankingBean(int index, String title, String url, String heat, String score) {
        this.index = index;
        this.title = title;
        this.url = url;
        this.heat = heat;
        this.score = score;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
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
}
