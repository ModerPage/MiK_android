package me.modernpage.entity;

import java.io.Serializable;

public class GroupType implements Serializable {
    private static final long serialVersionUID = -449665726476731608L;

    private int groupTypeId;
    private String groupTypeName;

    public int getGroupTypeId() {
        return groupTypeId;
    }

    public void setGroupTypeId(int groupTypeId) {
        this.groupTypeId = groupTypeId;
    }

    public String getGroupTypeName() {
        return groupTypeName;
    }

    public void setGroupTypeName(String groupTypeName) {
        this.groupTypeName = groupTypeName;
    }

}
