package com.lambz.lingo_chat.models;

public class Message
{
    String from, lang, text, type, link, to, fileName, id, lat, lng, locationtitle;

    public Message()
    {

    }

    public Message(String from, String lang, String text, String type, String link, String to, String fileName, String id, String lat, String lng, String locationtitle)
    {
        this.from = from;
        this.lang = lang;
        this.text = text;
        this.type = type;
        this.link = link;
        this.to = to;
        this.fileName = fileName;
        this.id = id;
        this.lat = lat;
        this.lng = lng;
        this.locationtitle = locationtitle;
    }

    public String getLat()
    {
        return lat;
    }

    public void setLat(String lat)
    {
        this.lat = lat;
    }

    public String getLng()
    {
        return lng;
    }

    public void setLng(String lng)
    {
        this.lng = lng;
    }

    public String getLocationtitle()
    {
        return locationtitle;
    }

    public void setLocationtitle(String locationtitle)
    {
        this.locationtitle = locationtitle;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getTo()
    {
        return to;
    }

    public void setTo(String to)
    {
        this.to = to;
    }

    public String getFrom()
    {
        return from;
    }

    public void setFrom(String from)
    {
        this.from = from;
    }

    public String getLang()
    {
        return lang;
    }

    public void setLang(String lang)
    {
        this.lang = lang;
    }

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getLink()
    {
        return link;
    }

    public void setLink(String link)
    {
        this.link = link;
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public boolean equals(Message message)
    {
        try
        {
            if(id.equals(message.getId()))
            {
                return true;
            }
        }
        catch (Exception e)
        {
        }
        return false;
    }
}
