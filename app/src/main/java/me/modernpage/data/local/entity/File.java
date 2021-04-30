package me.modernpage.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "FILE")
public class File implements Serializable {
    @PrimaryKey
    @ColumnInfo(name = "FID")
    private long id;

    @ColumnInfo(name = "NAME")
    private String name;

    @ColumnInfo(name = "TYPE")
    private String type;

    public File(long id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    @Ignore
    public File() {
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
