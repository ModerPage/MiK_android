package me.modernpage.data.local.entity.relation;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.io.Serializable;

import me.modernpage.data.local.entity.File;
import me.modernpage.data.local.entity.Group;
import me.modernpage.data.local.entity.Location;
import me.modernpage.data.local.entity.Post;
import me.modernpage.data.local.entity.Profile;

public class PostRelation implements Serializable {
    @Embedded
    private Post post;

    @Relation(
            entity = Profile.class,
            parentColumn = "OWNER_ID",
            entityColumn = "PID"
    )
    private Profile owner;

    @Relation(
            entity = Group.class,
            parentColumn = "GROUP_ID",
            entityColumn = "GID"
    )
    private Group group;

    @Relation(
            entity = Location.class,
            parentColumn = "LOCATION_ID",
            entityColumn = "LID"
    )
    private Location location;

    @Relation(
            entity = File.class,
            parentColumn = "FILE_ID",
            entityColumn = "FID"
    )
    private File file;


    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }


    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Profile getOwner() {
        return owner;
    }

    public void setOwner(Profile owner) {
        this.owner = owner;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    @Override
    public String toString() {
        return "PostOwnerPublicGroup{" +
                "post=" + post +
                ", owner=" + owner +
                ", group=" + group +
                ", location=" + location +
                '}';
    }
}
