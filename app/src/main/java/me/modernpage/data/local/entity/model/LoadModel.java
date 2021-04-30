package me.modernpage.data.local.entity.model;

import java.util.List;

public class LoadModel<T> {
    private List<T> contents;
    private int total;

    public LoadModel(List<T> contents, int total) {
        this.contents = contents;
        this.total = total;
    }

    public List<T> getContents() {
        return contents;
    }

    public void setContents(List<T> contents) {
        this.contents = contents;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
