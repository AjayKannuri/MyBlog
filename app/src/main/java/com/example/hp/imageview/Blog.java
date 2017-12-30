package com.example.hp.imageview;

/**
 * Created by hp on 10/2/2017.
 */

public class Blog {
    private String title,desc,image,username;
    public Blog(String title,String desc,String image,String username)
    {
        this.title=title;
        this.desc=desc;
        this.image=image;
        this.username=username;
    }
    public Blog()
    {

    }
    public String getUsername(){
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDesc() {
        return desc;
    }

    public String getImage() {
        return image;
    }

    public String getTitle() {
        return title;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setTitle(String title) {
        this.title = title;
    }


}
