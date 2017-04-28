package com.meltemyilmaz.gelecegiyazanlar.notdefterim;


import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by Meltem YILMAZ on 25.03.2017.
 */
@IgnoreExtraProperties
public class User {

    public String username;
    public String email;

    public User(){
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }
    public User(String username, String email){
        this.username = username;
        this.email = email;
    }


}
