package com.vaishnavas.storyapp;

import android.widget.VideoView;

public class Member {

    private String Name;
    private String Mail;

    private Member(){}

    public Member(String name,String mail){
        if (name.trim().equals("")){
            name = " not available";
        }
        Name = name;
        Mail =mail;
    }

    public String getName() {
        return Name;
    }

    public void setVideoName(String name) {
        Name = name;
    }

    public String getMail() {
        return Mail;
    }

    public void setVideoUri(String mail) {
        Mail = mail;
    }
}