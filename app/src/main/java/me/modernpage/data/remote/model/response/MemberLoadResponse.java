package me.modernpage.data.remote.model.response;

public class MemberLoadResponse extends UserLoadResponse {
    private Boolean joined;

    public Boolean getJoined() {
        return joined;
    }

    public void setJoined(Boolean joined) {
        this.joined = joined;
    }
}
