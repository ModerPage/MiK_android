package me.modernpage.data.local.entity.model;

import java.io.Serializable;

public class Link implements Serializable {

    private String rel;
    private String href;

    public Link() {
    }

    public String getRel() {
        return rel;
    }

    public void setRel(String rel) {
        this.rel = rel;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    @Override
    public String toString() {
        return "Link{" +
                ", rel='" + rel + '\'' +
                ", href='" + href + '\'' +
                '}';
    }
}
