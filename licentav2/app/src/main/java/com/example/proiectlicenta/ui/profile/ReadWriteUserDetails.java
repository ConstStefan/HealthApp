package com.example.proiectlicenta.ui.profile;

public class ReadWriteUserDetails {
    public String dob,gender,mobile;

    //Constructor
    public ReadWriteUserDetails(){};

    public ReadWriteUserDetails(String textDoB, String textGender, String textMobile){

        this.dob=textDoB;
        this.gender=textGender;
        this.mobile=textMobile;
    }
}
