package me.modernpage.data.remote.model.response;

import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class LoadResponse<T> {
    @SerializedName("content")
    private List<T> contents;
    private Integer nextPage;
    @SerializedName("totalElements")
    private int total;

    public List<T> getContents() {
        return contents;
    }

    public void setContents(List<T> contents) {
        this.contents = contents;
    }

    public Integer getNextPage() {
        return nextPage;
    }

    public void setNextPage(Integer nextPage) {
        this.nextPage = nextPage;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    @NotNull
    public abstract List<Long> getIds();
}
