package com.lambz.lingo_chat.models;

import java.io.Serializable;

public class Contact implements Serializable
{
    String name, image, uid;

    public Contact(String name, String image, String uid)
    {
        this.name = name;
        this.image = image;
        this.uid = uid;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getImage()
    {
        return image;
    }

    public void setImage(String image)
    {
        this.image = image;
    }

    public String getUid()
    {
        return uid;
    }

    public void setUid(String uid)
    {
        this.uid = uid;
    }
}
