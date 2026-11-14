package com.disasterrelief.utility;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtils {

    public static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter FILE_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    public static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    private DateTimeUtils() {}

    public static String formatDateTime(LocalDateTime dt) {
        if (dt == null) return "N/A";
        return dt.format(ISO_FORMATTER);
    }

    public static String formatDisplay(LocalDateTime dt) {
        if (dt == null) return "N/A";
        return dt.format(DISPLAY_FORMATTER);
    }

    public static String fileTimestamp() {
        return LocalDateTime.now().format(FILE_TIMESTAMP_FORMATTER);
    }
}

