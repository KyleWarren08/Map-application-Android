package com.example.localguide.models;

public class User {
    private String fullname;
    private  String email;
    private String phone;
    private String password;
    private String distanceIn;
    private String searchQuery;

    public User() {

    }

    public User(String fullname, String email, String phone, String password, String distanceIn, String searchQuery) {
        this.fullname = fullname;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.distanceIn = distanceIn;
        this.searchQuery = searchQuery;
    }

    public User(String fullname, String phone, String distanceIn, String searchQuery) {
        this.fullname = fullname;
        this.phone = phone;
        this.distanceIn = distanceIn;
        this.searchQuery = searchQuery;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDistanceIn() {
        return distanceIn;
    }

    public void setDistanceIn(String distanceIn) {
        this.distanceIn = distanceIn;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }
}
