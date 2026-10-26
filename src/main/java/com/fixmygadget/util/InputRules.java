package com.fixmygadget.util;

import java.util.regex.Pattern;

/**
 * Rules for names and phone numbers.
 * The same rules are used by the input fields in the UI and by the validation in the services.
 */
public final class InputRules {
    /** One allowed character of a name: any letter, space, hyphen or apostrophe. */
    public static final String NAME_CHAR = "[\\p{L} '’-]";

    public static final String PHONE_PREFIX = "+380";
    public static final int PHONE_LENGTH = 13;   // +380 and 9 digits

    private static final Pattern NAME = Pattern.compile("\\p{L}" + NAME_CHAR + "*");
    private static final Pattern PHONE = Pattern.compile("\\+380\\d{9}");

    private InputRules() {
    }

    public static boolean isValidName(String name) {
        return name != null && NAME.matcher(name.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE.matcher(phone.trim()).matches();
    }
}