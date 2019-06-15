package com.lyc.TicketManager_Backend.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateFormatUtil {
    public static final String FORMAT = "yyyy-MM-dd hh:mm:ss";
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat(FORMAT);

    public static Calendar getCalendar(String input) {

        try {
            Date parse = SIMPLE_DATE_FORMAT.parse(input);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(parse.getTime());
            return calendar;
        } catch (ParseException e) {
            return null;
        }
    }
}
