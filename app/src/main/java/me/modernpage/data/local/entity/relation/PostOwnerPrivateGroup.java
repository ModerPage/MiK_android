package me.modernpage.data.local.entity.relation;

import androidx.room.Embedded;
import androidx.room.Relation;

import me.modernpage.data.local.entity.Location;
import me.modernpage.data.local.entity.Post;
import me.modernpage.data.local.entity.PrivateGroup;
import me.modernpage.data.local.entity.Profile;

public class PostOwnerPrivateGroup {
    @Embedded
    private Post post;

    @Relation(
            parentColumn = "OWNER_ID",
            entityColumn = "UID"
    )
    private Profile owner;

    @Relation(
            parentColumn = "GROUP_ID",
            entityColumn = "GID"
    )
    private PrivateGroup group;

    @Relation(
            parentColumn = "LOCATION_ID",
            entityColumn = "LID"
    )
    private Location location;


    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public Profile getOwner() {
        return owner;
    }

    public void setOwner(Profile owner) {
        this.owner = owner;
    }

    public PrivateGroup getGroup() {
        return group;
    }

    public void setGroup(PrivateGroup group) {
        this.group = group;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }


}
