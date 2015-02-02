package net.nashlegend.sourcewall.model;

/**
 * Created by NashLegend on 2015/2/2 0002
 */
public class Message extends AceModel {

    private String content = "";
    private long date_last_updated = 0l;
    private String id = "";
    private boolean is_read = false;
    private String ukey = "";//永远是用户自己
    private String url = "";

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getDate_last_updated() {
        return date_last_updated;
    }

    public void setDate_last_updated(long date_last_updated) {
        this.date_last_updated = date_last_updated;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isIs_read() {
        return is_read;
    }

    public void setIs_read(boolean is_read) {
        this.is_read = is_read;
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


}
