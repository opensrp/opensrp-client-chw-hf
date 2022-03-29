package org.smartregister.chw.hf.utils;

import java.util.Calendar;

public class ReportUtils {
    private static final int year = Calendar.getInstance().get(Calendar.YEAR);
    private static final int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
    public static String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sept", "Oct", "Nov", "Dec"};

    public static String getDefaultReportPeriod() {
        String monthString = String.valueOf(month);
        if (month < 10) {
            monthString = "0" + monthString;
        }
        return monthString + "-" + year;
    }

    public static int getMonth() {
        return month;
    }

    public static int getYear() {
        return year;
    }

    public static String displayMonthAndYear(int month, int year) {
        return monthNames[month] + ", " + year;
    }

    public static String displayMonthAndYear() {
        return monthNames[getMonth() - 1] + ", " + getYear();
    }


}
