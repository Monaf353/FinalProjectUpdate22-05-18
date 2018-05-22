package com.develop.windexit.finalproject.Model;

/**
 * Created by WINDEX IT on 15-Feb-18.
 */

public class User {
    private String image;
    private String email;
    private String Name;
    private String Password;
    private String Phone;
    private String homeAddress;
    private Object balance;
    private String birthday;

    public User() {
    }

    public User(String name, String password) {
        Name = name;
        Password = password;
        //IsStaff = "false";
    }

    public User(String image, String email, String name, String password, String phone, String homeAddress, Object balance, String birthday) {
        this.image = image;
        this.email = email;
        Name = name;
        Password = password;
        Phone = phone;
        this.homeAddress = homeAddress;
        this.balance = balance;
        this.birthday = birthday;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getHomeAddress() {
        return homeAddress;
    }

    public void setHomeAddress(String homeAddress) {
        this.homeAddress = homeAddress;
    }

    public Object getBalance() {
        return balance;
    }

    public void setBalance(Object balance) {
        this.balance = balance;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
