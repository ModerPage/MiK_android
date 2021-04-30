package me.modernpage.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.modernpage.data.local.entity.model.Link;

@Entity(tableName = "PROFILE",
        indices = {@Index("PID")})
public class Profile implements Serializable {

    @PrimaryKey
    @ColumnInfo(name = "PID")
    private long id;

    @ColumnInfo(name = "FULLNAME")
    private String fullname;

    @ColumnInfo(name = "BIRTHDATE")
    private Date birthdate;

    @ColumnInfo(name = "CREATED")
    private Date created;

    @ColumnInfo(name = "_LINKS")
    private List<Link> links = new ArrayList<>();

    @Ignore
    public Profile() {
    }

    public Profile(long id, String fullname, Date birthdate, Date created, List<Link> links) {
        this.id = id;
        this.fullname = fullname;
        this.birthdate = birthdate;
        this.created = created;
        this.links = links;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public Date getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(Date birthdate) {
        this.birthdate = birthdate;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    public String _avatar() {
        for (Link link : links) {
            if ("avatar".equals(link.getRel()))
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

    public String _posts() {
        for (Link link : links) {
            if ("posts".equals(link.getRel()))
                return link.getHref();
        }
        return null;
    }

    public String _groups() {
        for (Link link : links) {
            if ("groups".equals(link.getRel()))
                return link.getHref();
        }
        return null;
    }

    public String _joined_groups() {
        for (Link link : links) {
            if ("joined_groups".equals(link.getRel()))
                return link.getHref();
        }
        return null;
    }

    public String _hidden_posts() {
        for (Link link : links) {
            if ("hidden_posts".equals(link.getRel()))
                return link.getHref();
        }
        return null;
    }

    public String _followers() {
        for (Link link : links) {
            if ("followers".equals(link.getRel()))
                return link.getHref();
        }
        return null;
    }

    public String _following() {
        for (Link link : links) {
            if ("following".equals(link.getRel()))
                return link.getHref();
        }
        return null;
    }

    @Override
    public String toString() {
        return "Profile{" +
                "id=" + id +
                ", fullname='" + fullname + '\'' +
                ", birthdate=" + birthdate +
                ", created=" + created +
                ", links=" + links +
                '}';
    }
}
