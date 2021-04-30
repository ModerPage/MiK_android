package me.modernpage.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "PLOCATION", indices = {@Index(value = {"LID", "LONGITUDE", "LATITUDE"}, unique = true)})
public class Location implements Serializable {
    @PrimaryKey
    @ColumnInfo(name = "LID")
    private long id;
    @ColumnInfo(name = "LONGITUDE")
    private double longitude;
    @ColumnInfo(name = "LATITUDE")
    private double latitude;
    @ColumnInfo(name = "ADDRESS_LINE")
    private String addressLine;
    @ColumnInfo(name = "CITY")
    private String city;
    @ColumnInfo(name = "COUNTRY")
    private String country;

    public Location() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getAddressLine() {
        return addressLine;
    }

    public void setAddressLine(String addressLine) {
        this.addressLine = addressLine;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    public String toString() {
        return "Location{" +
                "id=" + id +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", addressLine='" + addressLine + '\'' +
                ", city='" + city + '\'' +
                ", country='" + country + '\'' +
                '}';
    }
}
