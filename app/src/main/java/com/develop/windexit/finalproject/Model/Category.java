package com.develop.windexit.finalproject.Model;

/**
 * Created by WINDEX IT on 16-Feb-18.
 */

public class Category {
    private String Name;
    private String Image;
    //private String ActiveInactive;

    public Category() {
    }

    public Category(String name, String image) {
        Name = name;
        Image = image;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }
}
