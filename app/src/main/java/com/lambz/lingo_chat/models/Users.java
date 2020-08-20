package com.lambz.lingo_chat.models;

public class Users
{
    String email, first_name, last_name, image, lang;

    public Users()
    {

    }

    public Users(String email, String first_name, String last_name, String image, String lang)
    {
        this.email = email;
        this.first_name = first_name;
        this.last_name = last_name;
        this.image = image;
        this.lang = lang;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getFirst_name()
    {
        return first_name;
    }

    public void setFirst_name(String first_name)
    {
        this.first_name = first_name;
    }

    public String getLast_name()
    {
        return last_name;
    }

    public void setLast_name(String last_name)
    {
        this.last_name = last_name;
    }

    public String getImage()
    {
        return image;
    }

    public void setImage(String image)
    {
        this.image = image;
    }

    public String getLang()
    {
        return lang;
    }

    public void setLang(String lang)
    {
        this.lang = lang;
    }
}