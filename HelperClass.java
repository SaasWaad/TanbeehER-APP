package com.example.tanbeeherapp;

public class HelperClass {
    String name, email, password, phone, userType, plate1, fellowUsername;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUserType() {
        return userType;
    }

    public String getPlate1() {
        return plate1;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getFellowUsername() {
        return fellowUsername;
    }

    // Constructor for "Emergency Contact"
    public HelperClass(String name, String email, String phone, String userType, String plate1, String fellowUsername) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.userType = userType;
        this.plate1 = plate1;
        this.fellowUsername = fellowUsername;
    }

    // Constructor for "Fellow"
    public HelperClass(String name, String email, String phone, String userType) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.userType = userType;
    }

    public HelperClass() {
    }
}

