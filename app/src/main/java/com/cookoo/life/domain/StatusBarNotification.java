package com.cookoo.life.domain;

/**
 * Created By: travis.roberson
 * Date: 12/16/13
 * Time: 3:47 PM
 */
public class StatusBarNotification {

    Long id;
    String packageName;
    int icon;
    String tag;
    String ticker;
    boolean seen;

    public StatusBarNotification(Long id, String packageName, int icon, String tag, String ticker, boolean seen) {
        this.id = id;
        this.packageName = packageName;
        this.icon = icon;
        this.tag = tag;
        this.ticker = ticker;
        this.seen = seen;
    }

    public StatusBarNotification() {
    }



    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }




}
