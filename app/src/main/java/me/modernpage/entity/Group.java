package me.modernpage.entity;

import java.io.Serializable;

public class Group implements Serializable{

    private static final long serialVersionUID = 7691383037326270002L;

    private long groupId;
    private String groupName;
    private GroupType groupType;
    private String imageURI;

    public Group(long groupId, String groupName, GroupType groupType, String imageURI) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.groupType = groupType;
        this.imageURI = imageURI;
    }

    public Group() {}

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public GroupType getGroupType() {
        return groupType;
    }

    public void setGroupType(GroupType groupType) {
        this.groupType = groupType;
    }

    public String getImageURI() {
        return imageURI;
    }

    public void setImageURI(String imageURI) {
        this.imageURI = imageURI;
    }


}
