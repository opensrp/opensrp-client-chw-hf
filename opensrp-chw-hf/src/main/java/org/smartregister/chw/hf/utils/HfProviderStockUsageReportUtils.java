package org.smartregister.chw.hf.utils;

public class HfProviderStockUsageReportUtils {

    public String getAppendedMonthNumber(String month) {
        String valMonth = "";
        switch (month) {
            case "1":
                valMonth = "01";
                break;
            case "2":
                valMonth = "02";
                break;
            case "3":
                valMonth = "03";
                break;
            case "4":
                valMonth = "04";
                break;
            case "5":
                valMonth = "05";
                break;
            case "6":
                valMonth = "06";
                break;
            case "7":
                valMonth = "07";
                break;
            case "8":
                valMonth = "08";
                break;
            case "9":
                valMonth = "09";
                break;
            case "10":
                valMonth = "10";
                break;
            case "11":
                valMonth = "11";
                break;
            case "12":
                valMonth = "12";
                break;
            default:
                break;
        }
        return valMonth;
    }

}
