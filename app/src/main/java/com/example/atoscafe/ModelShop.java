package com.example.atoscafe;

public class ModelShop {

    private String email, shopName, location, Description,shopIcon,timestamp,uid;

    public ModelShop() {
    }

    public ModelShop(String uid, String email, String shopName, String location, String description, String shopIcon, String timestamp) {
        this.uid = uid;
        this.email = email;
        this.shopName = shopName;
        this.location = location;
        Description = description;
        this.shopIcon = shopIcon;
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getShopIcon() {
        return shopIcon;
    }

    public void setShopIcon(String shopIcon) {
        this.shopIcon = shopIcon;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}