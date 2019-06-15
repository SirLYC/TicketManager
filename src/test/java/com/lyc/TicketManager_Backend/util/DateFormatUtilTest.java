package com.lyc.TicketManager_Backend.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.Calendar;

public class DateFormatUtilTest {

    @Test
    public void testParse() {
        Calendar calendar = DateFormatUtil.getCalendar("2019-5-9 10:00:00");
        Assert.assertNotNull(calendar);
        Assert.assertEquals(calendar.get(Calendar.YEAR), 2019);
        Assert.assertEquals(calendar.get(Calendar.MONTH), 4);
        Assert.assertEquals(calendar.get(Calendar.DAY_OF_MONTH), 9);
        Assert.assertEquals(calendar.get(Calendar.HOUR_OF_DAY), 10);
        Assert.assertEquals(calendar.get(Calendar.MINUTE), 0);
        Assert.assertEquals(calendar.get(Calendar.SECOND), 0);

        calendar = DateFormatUtil.getCalendar("2019-5-9");
        Assert.assertNull(calendar);
    }
}
