package com.meltemyilmaz.gelecegiyazanlar.notdefterim;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Meltem YILMAZ on 25.03.2017.
 */

@IgnoreExtraProperties
public class Note {

    public String uid;
    public String author;
    public String note;

    public Note(){
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public Note(String uid, String author, String note){
        this.uid=uid;
        this.author=author;
        this.note=note;
    }

    @Exclude
    public Map<String, Object> toMap() {

        HashMap<String, Object> result = new HashMap<>();

        result.put("uid", uid);
        result.put("author", author);
        result.put("note", note);

        return result;
    }


}
