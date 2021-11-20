//Class for structuring tasks data for simple read/write operations
package com.example.ugotthis;

import android.util.Log;

public class Task {
    private String name;
    private String descp;
    private String photoURL;
    private String photoLoc;
    private boolean comp;

    public Task()
    {

    }

    public Task(String user, String Descrip)
    {
        this.name = user;
        this.descp = Descrip;
    }

    public Task(String user, String Descrip, boolean check)
    {
        this.name = user;
        this.descp = Descrip;
        this.comp = check;
    }

    public Task(String user, String descrip, String photo, boolean check)
    {
        this.name = user;
        this.descp = descrip;
        this.comp = check;
        this.photoURL = photo;
    }

    public Task(String user, String descrip, String photo,String loc, boolean check)
    {
        this.name = user;
        this.descp = descrip;
        this.comp = check;
        this.photoLoc = loc;
        this.photoURL = photo;
    }

    public String getName(){return name;
    }

    public String getDescp()
    { return descp;
    }

    public boolean getComp() {return comp;}

    public void setComp(boolean done) {this.comp = done;}

    public String getPhotoURL(){return  photoURL;}

    public String getPhotoLoc(){return photoLoc;}
}