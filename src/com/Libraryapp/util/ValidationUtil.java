package com.Libraryapp.util;

import java.util.function.Predicate;

public class ValidationUtil {

    public static final Predicate<String> IS_NOT_EMPTY =
            s -> s != null && !s.trim().isEmpty();

    public static final Predicate<String> IS_VALID_EMAIL =
            s -> s != null && s.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    public static final Predicate<String> IS_VALID_MOBILE =
            s -> s != null && s.matches("\\d{10}");

    public static final Predicate<String> IS_VALID_PASSWORD =
            s -> s != null && s.length() >= 6;

    private ValidationUtil() {
    }
}
