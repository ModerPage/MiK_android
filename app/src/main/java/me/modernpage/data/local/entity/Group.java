package me.modernpage.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import me.modernpage.data.local.entity.model.Link;

@Entity(tableName = "PGROUP", indices = {@Index(value = {"GID"})})
public class Group implements Serializable {
    @PrimaryKey
    @ColumnInfo(name = "GID")
    private long id;
    @ColumnInfo(name = "NAME")
    private String name;

    @Ignore
    private Image image;

    @ColumnInfo(name = "IMAGE_ID")
    private long image_id;

    @ColumnInfo(name = "_LINKS")
    private List<Link> links = new ArrayList<>();


    public Group() {
    }

    public Group(long id, String name, Image image, List<Link> links) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.links = links;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    public String _image() {
        for (Link link : links) {
            if ("image".equals(link.getRel()))
                return link.getHref();
        }
        return null;
    }

    public String _posts() {
        for (Link link : links) {
            if ("posts".equals(link.getRel()))
                return link.getHref();
        }
        return null;
    }

    public String _self() {
        for (Link link : links) {
            if ("self".equals(link.getRel()))
                return link.getHref();
        }
        return null;
    }

    public void process() {
        image_id = image.getId();
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public long getImage_id() {
        return image_id;
    }

    public void setImage_id(long image_id) {
        this.image_id = image_id;
    }

    @Override
    public String toString() {
        return name;
    }
}
