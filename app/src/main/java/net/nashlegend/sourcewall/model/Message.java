package net.nashlegend.sourcewall.model;

/**
 * Created by NashLegend on 2015/2/2 0002
 * 站内信
 */
public class Message extends AceModel {

    private String id = "";
    private String content = "";
    private String direction = "send";
    private boolean is_read = false;
    private String dateCreated = "";
    private String ukey = "";
    private String another_ukey = "";
    private int unread_count = 0;//获取全部列表的时候才有这个属性
    private int total = 0;//获取全部列表的时候才有这个属性

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * html格式
     *
     * @return
     */
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public boolean isIs_read() {
        return is_read;
    }

    public void setIs_read(boolean is_read) {
        this.is_read = is_read;
    }

    public int getUnread_count() {
        return unread_count;
    }

    public void setUnread_count(int unread_count) {
        this.unread_count = unread_count;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getAnother_ukey() {
        return another_ukey;
    }

    public void setAnother_ukey(String another_ukey) {
        this.another_ukey = another_ukey;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public String getUkey() {
        return ukey;
    }

    public void setUkey(String ukey) {
        this.ukey = ukey;
    }
}
