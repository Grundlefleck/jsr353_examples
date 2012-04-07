package org.mutabilitydetector.jsr353;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class IsoDateHourMinuteFormatter {
    private static final DateTimeFormatter ISO_DATE_HOUR_MINUTE_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");

    public static DateTime parseDateTime(String isoDateHourMinuteString) {
        return ISO_DATE_HOUR_MINUTE_FORMATTER.parseDateTime(isoDateHourMinuteString);
    }
}
