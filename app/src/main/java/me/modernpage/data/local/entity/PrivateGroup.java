package me.modernpage.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Ignore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import me.modernpage.data.local.entity.model.Link;

@Entity(tableName = "PRIVATE_GROUP", inheritSuperIndices = true)
public class PrivateGroup extends Group implements Serializable {

    @Ignore
    @Embedded
    private Profile owner;

    @Ignore
    @Embedded
    private List<Profile> members = new ArrayList<>();

    @ColumnInfo(name = "OWNER_ID")
    private long ownerId;

    public PrivateGroup() {
    }

    public PrivateGroup(long id, String name, Image image, List<Link> links, Profile owner, List<Profile> members) {
        super(id, name, image, links);
        this.owner = owner;
        this.members = members;
    }

    public Profile getOwner() {
        return owner;
    }

    public void setOwner(Profile owner) {
        this.owner = owner;
    }

    public List<Profile> getMembers() {
        return members;
    }

    public void setMembers(List<Profile> members) {
        this.members = members;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(long ownerId) {
        this.ownerId = ownerId;
    }

    public void process() {
        super.process();
        ownerId = owner.getId();
    }

    public String _members() {
        for (Link link : getLinks()) {
            if ("members".equals(link.getRel()))
                return link.getHref();
        }
        return null;
    }

    @Override
    public String toString() {
        return getName();
    }
}
