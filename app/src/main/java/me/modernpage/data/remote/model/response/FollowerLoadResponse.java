package me.modernpage.data.remote.model.response;

public class FollowerLoadResponse extends UserLoadResponse {
    private Boolean followed;
//    private Boolean requested;

    public Boolean getFollowed() {
        return followed;
    }

    public void setFollowed(Boolean followed) {
        this.followed = followed;
    }
//
//    public Boolean getRequested() {
//        return requested;
//    }
//
//    public void setRequested(Boolean requested) {
//        this.requested = requested;
//    }

}
