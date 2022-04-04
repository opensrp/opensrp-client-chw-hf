package org.smartregister.chw.hf.utils;

import android.content.Context;
import android.os.Build;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.webkit.WebView;

import java.util.Calendar;

import androidx.annotation.RequiresApi;

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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void printTheWebPage(WebView webView, Context context, String printJobName) {

        // Creating  PrintManager instance
        PrintManager printManager = (PrintManager) context.getSystemService(Context.PRINT_SERVICE);
        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter(printJobName);

        // Create a print job with name and adapter instance
        assert printManager != null;
        printManager.print(printJobName, printAdapter,
                new PrintAttributes.Builder().build());
    }


}
