package me.modernpage.entity;

import java.util.ArrayList;
import java.util.Collection;

public class Location {

    private long locationId;
    private double longitude;
    private double latitude;
    private String addressLine;
    private String city;
    private String country;

    public Location(double longitude, double latitude, String addressLine, String city, String country) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.addressLine = addressLine;
        this.city = city;
        this.country = country;
    }

    private Collection<Post> posts = new ArrayList<>();

    public long getLocationId() {
        return locationId;
    }

    public void setLocationId(long locationId) {
        this.locationId = locationId;
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

    public Collection<Post> getPosts() {
        return posts;
    }

    public void setPosts(Collection<Post> posts) {
        this.posts = posts;
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


}

