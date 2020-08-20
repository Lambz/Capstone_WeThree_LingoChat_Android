package com.lambz.lingo_chat.models;

public class Message
{
    String from, lang, text, type, link, to;

    public Message()
    {

    }

    public Message(String from, String lang, String text, String type, String link, String to)
    {
        this.from = from;
        this.lang = lang;
        this.text = text;
        this.type = type;
        this.link = link;
        this.to = to;
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
}
