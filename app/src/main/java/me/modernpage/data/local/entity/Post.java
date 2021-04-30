package me.modernpage.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.modernpage.data.local.entity.model.Link;


@Entity(tableName = "POST", indices = {@Index(value = {"PID"}), @Index("OWNER_ID"), @
        Index("LOCATION_ID"), @Index("GROUP_ID"), @Index("FILE_ID")},
        foreignKeys = {
                @ForeignKey(
                        entity = Profile.class,
                        parentColumns = "PID",
                        childColumns = "OWNER_ID"),
                @ForeignKey(
                        entity = Group.class,
                        parentColumns = "GID",
                        childColumns = "GROUP_ID"),
                @ForeignKey(
                        entity = Location.class,
                        parentColumns = "LID",
                        childColumns = "LOCATION_ID",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(
                        entity = File.class,
                        parentColumns = "FID",
                        childColumns = "FILE_ID",
                        onDelete = ForeignKey.CASCADE)
        })
public class Post implements Serializable {

    @PrimaryKey
    @ColumnInfo(name = "PID")
    private long id;

    @ColumnInfo(name = "TEXT")
    private String text;

    @ColumnInfo(name = "CREATED")
    private Date created;

    @ColumnInfo(name = "OWNER_ID")
    private long ownerId;

    @ColumnInfo(name = "GROUP_ID")
    private long groupId;

    @ColumnInfo(name = "LOCATION_ID")
    private Long locationId;

    @ColumnInfo(name = "FILE_ID")
    private Long fileId;

    @Ignore
    private Profile owner;
    @Ignore
    private Location location;
    @Ignore
    private Group group;
    @Ignore
    private File file;

    @Ignore
    private List<Like> likes = new ArrayList<>();
    @Ignore
    private List<Comment> comments = new ArrayList<>();

    @ColumnInfo(name = "_LINKS")
    private List<Link> links = new ArrayList<>();

    public Post() {
    }

    public Post(long id,
                String text,
                Date created,
                Profile owner,
                Location location,
                Group group,
                File file, List<Like> likes,
                List<Comment> comments,
                List<Link> links) {
        this.id = id;
        this.text = text;
        this.created = created;
        this.owner = owner;
        this.location = location;
        this.group = group;
        this.file = file;
        this.likes = likes;
        this.comments = comments;
        this.links = links;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Profile getOwner() {
        return owner;
    }

    public void setOwner(Profile owner) {
        this.owner = owner;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public List<Like> getLikes() {
        return likes;
    }

    public void setLikes(List<Like> likes) {
        this.likes = likes;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(long ownerId) {
        this.ownerId = ownerId;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String _self() {
        for (Link link : links) {
            if ("self".equals(link.getRel()))
                return link.getHref();
        }
        return null;
    }

    public String _file() {
        for (Link link : links) {
            if ("file".equals(link.getRel()))
                return link.getHref();
        }
        return null;
    }

    public String _owner() {
        for (Link link : links) {
            if ("owner".equals(link.getRel()))
                return link.getHref();
        }
        return null;
    }

    public String _location() {
        for (Link link : links) {
            if ("location".equals(link.getRel()))
                return link.getHref();
        }
        return null;
    }

    public String _group() {
        for (Link link : links) {
            if ("group".equals(link.getRel()))
                return link.getHref();
        }
        return null;
    }

    public String _likes() {
        for (Link link : links) {
            if ("likes".equals(link.getRel()))
                return link.getHref();
        }
        return null;
    }

    public String _comments() {
        for (Link link : links) {
            if ("comments".equals(link.getRel()))
                return link.getHref();
        }
        return null;
    }

    public void process() {
        ownerId = owner.getId();
        groupId = group.getId();
        fileId = file.getId();
        if (location != null)
            locationId = location.getId();
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", created=" + created +
                ", ownerId=" + ownerId +
                ", groupId=" + groupId +
                ", locationId=" + locationId +
                ", owner=" + owner +
                ", location=" + location +
                ", group=" + group +
                ", likes=" + likes +
                ", comments=" + comments +
                ", links=" + links +
                '}';
    }
}
