package net.nashlegend.sourcewall.model;

/**
 * Created by NashLegend on 2015/2/2 0002
 * 私信，貌似不仅仅是私信还有草稿，不知道还有多少
 * 私信是message，草稿是draft，草稿在这里就算了吧，我这里也不支持html编辑
 */
public class Reminder extends AceModel {

    private String content = "";
    private long date_created = 0l;
    private String id = "";
    private String group = "message";
    private String ukey = "";//永远是用户自己
    private String url = "";

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getDateCreated() {
        return date_created;
    }

    public void setDateCreated(long date_last_updated) {
        this.date_created = date_last_updated;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUkey() {
        return ukey;
    }

    public void setUkey(String ukey) {
        this.ukey = ukey;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}
