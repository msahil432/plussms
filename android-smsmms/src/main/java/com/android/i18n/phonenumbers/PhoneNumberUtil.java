package com.android.i18n.phonenumbers;

public class PhoneNumberUtil {
    private static PhoneNumberUtil instance;

    public static PhoneNumberUtil getInstance() {
        return instance;
    }

    public String format(Phonenumber.PhoneNumber parsed, PhoneNumberFormat format) {
        return null;
    }

    public Phonenumber.PhoneNumber parse(String s, String s2) throws NumberParseException {
        return new Phonenumber.PhoneNumber();
    }

    public boolean isValidNumber(Phonenumber.PhoneNumber phoneNumber) {
        return true;
    }

    public enum PhoneNumberFormat {
        E164,
        INTERNATIONAL,
        NATIONAL,
        RFC3966
    }
}
