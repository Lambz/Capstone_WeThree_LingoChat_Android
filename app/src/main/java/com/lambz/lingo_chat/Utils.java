package com.lambz.lingo_chat;

import android.location.Address;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils
{
    private static HashMap<String, String> mUserData;

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    public static boolean validate(String emailStr)
    {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.find();
    }

//    public static String sha256(String base)
//    {
//        try
//        {
//            MessageDigest digest = MessageDigest.getInstance("SHA-256");
//            byte[] hash = digest.digest(base.getBytes("UTF-8"));
//            StringBuffer hexString = new StringBuffer();
//
//            for (int i = 0; i < hash.length; i++)
//            {
//                String hex = Integer.toHexString(0xff & hash[i]);
//                if (hex.length() == 1) hexString.append('0');
//                hexString.append(hex);
//            }
//
//            return hexString.toString();
//        } catch (Exception ex)
//        {
//            throw new RuntimeException(ex);
//        }
//    }

    public static String getLanguageCode(int code)
    {
        switch (code)
        {
            case 1:
                return "fr";
            case 2:
                return "de";
            case 3:
                return "es";
            case 4:
                return "hi";
            default:
                return "en";
        }
    }

    public static HashMap<String, String> getUserData()
    {
        return mUserData;
    }

    public static void setUserData(HashMap<String, String> userData)
    {
        Utils.mUserData = userData;
    }

    public static String getLanguageCode()
    {
        String lang = Utils.getUserData().get("lang");
        String code = "en";
        try
        {
            code = Utils.getLanguageCode(Integer.parseInt(lang));
        }
        catch (Exception e)
        {

        }
        return code;
    }

    public static String getIntLanguageCode()
    {
        return Utils.getUserData().get("lang");
    }

    public static String getFormattedAddress(Address address)
    {
        StringBuilder title = new StringBuilder();
        if (address.getSubThoroughfare() != null)
        {
            title.append(address.getSubThoroughfare());
        }
        if (address.getThoroughfare() != null)
        {
            if (!title.toString().isEmpty())
            {
                title.append(", ");
            }
            title.append(address.getThoroughfare());
        }
        if (address.getPostalCode() != null)
        {
            if (!title.toString().isEmpty())
            {
                title.append(", ");
            }
            title.append(address.getPostalCode());
        }
        return title.toString();
    }
}
