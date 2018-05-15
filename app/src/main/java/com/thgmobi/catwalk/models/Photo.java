package com.thgmobi.catwalk.models;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.thgmobi.catwalk.util.Common;

public class Photo {

    private String id;
    private String userId;
    private String urlDownload;
    private String temp;

    public Photo() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUrlDownload() {
        return urlDownload;
    }

    public void setUrlDownload(String urlDownload) {
        this.urlDownload = urlDownload;
    }

    public String getTemp() {
        return temp;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    public void savePhotoInFirebaseDB(String idUser){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        setId(databaseReference.push().getKey());
        databaseReference.child(Common.CHILD_DB_PHOTO).child(idUser).child(getId()).setValue(this);
    }

}
