package com.lambz.lingo_chat.interfaces;

import com.lambz.lingo_chat.models.Contact;

@FunctionalInterface
public interface ContactClickedInterface
{
    void contactClicked(Contact contact);
}
