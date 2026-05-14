package com.veloura.controller;

public class CustomerModel {

    private int number;
    private String name;
    private String email;
    private String phone;
    private String city;
    private String actions;

    public CustomerModel(int number, String name, String email, String phone, String city, String actions) {
        this.number = number;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.city = city;
        this.actions = actions;
    }

    public int getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getCity() {
        return city;
    }
    

    public String getActions() {
        return actions;
    }
}