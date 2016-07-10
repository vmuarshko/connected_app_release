package com.cookoo.life.user;


import com.stackmob.sdk.model.StackMobUser;


public class User extends StackMobUser {

    private String email;

    public User(String username, String password) {
        super(User.class, username, password);
    }

}