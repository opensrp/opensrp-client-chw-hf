package org.smartregister.chw.hf.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.smartregister.chw.hf.domain.anc_reports.AncMonthlyReportObject;
import org.smartregister.chw.hf.domain.cbhs_reports.CbhsMonthlyReportObject;
import org.smartregister.chw.hf.domain.ld_reports.LdMonthlyReportObject;
import org.smartregister.chw.hf.domain.ltfu_summary.LTFUSummaryObject;
import org.smartregister.chw.hf.domain.mother_champion_repots.MotherChampionReportObject;
import org.smartregister.chw.hf.domain.pmtct_reports.Pmtct12MonthsReportObject;
import org.smartregister.chw.hf.domain.pmtct_reports.Pmtct24MonthsReportObject;
import org.smartregister.chw.hf.domain.pmtct_reports.Pmtct3MonthsReportObject;
import org.smartregister.chw.hf.domain.pmtct_reports.PmtctEIDMonthlyReportObject;
import org.smartregister.chw.hf.domain.pnc_reports.PncMonthlyReportObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.RequiresApi;
import androidx.webkit.WebViewAssetLoader;
import timber.log.Timber;

public class ReportUtils {
    private static final int year = Calendar.getInstance().get(Calendar.YEAR);
    private static final int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
    public static String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sept", "Oct", "Nov", "Dec"};
    private static String printJobName;
    private static String reportPeriod;

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

    public static String getPrintJobName() {
        return printJobName;
    }

    public static void setPrintJobName(String printJobName) {
        ReportUtils.printJobName = printJobName;
    }

    public static Date getReportDate() {
        if (StringUtils.isNotBlank(reportPeriod)) {

            try {
                return new SimpleDateFormat("MM-yyyy", Locale.getDefault()).parse(reportPeriod);
            } catch (ParseException e) {
                Timber.e(e);
            }
        }

        return new Date();
    }

    public static String getReportPeriod() {
        return reportPeriod;
    }

    public static void setReportPeriod(String reportPeriod) {
        ReportUtils.reportPeriod = reportPeriod;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void printTheWebPage(WebView webView, Context context) {

        // Creating  PrintManager instance
        PrintManager printManager = (PrintManager) context.getSystemService(Context.PRINT_SERVICE);
        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter(getPrintJobName());

        // Create a print job with name and adapter instance
        assert printManager != null;
        printManager.print(getPrintJobName(), printAdapter,
                new PrintAttributes.Builder().build());
    }

    @SuppressLint("SetJavaScriptEnabled")
    public static void loadReportView(String reportPath, WebView mWebView, Context context, String reportType) {

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        final WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(context))
                .build();
        mWebView.setWebViewClient(new LocalContentWebViewClient(assetLoader));
        mWebView.addJavascriptInterface(new HfWebAppInterface(context, reportType), "Android");
        mWebView.loadUrl("https://appassets.androidplatform.net/assets/reports/" + reportPath + ".html");
    }

    public static class PMTCTReports {
        public static String computeThreeMonths(Date startDate) {
            Pmtct3MonthsReportObject pmtct3MonthsReportObject = new Pmtct3MonthsReportObject(startDate);
            try {
                return pmtct3MonthsReportObject.getIndicatorDataAsGson(pmtct3MonthsReportObject.getIndicatorData());
            } catch (JSONException e) {
                Timber.e(e);
            }
            return "";
        }

        public static String computeTwelveMonths(Date startDate) {
            Pmtct12MonthsReportObject pmtct12MonthsReportObject = new Pmtct12MonthsReportObject(startDate);
            try {
                return pmtct12MonthsReportObject.getIndicatorDataAsGson(pmtct12MonthsReportObject.getIndicatorData());
            } catch (JSONException e) {
                Timber.e(e);
            }
            return "";
        }

        public static String computeTwentyFourMonths(Date startDate) {
            Pmtct24MonthsReportObject pmtct24MonthsReportObject = new Pmtct24MonthsReportObject(startDate);
            try {
                return pmtct24MonthsReportObject.getIndicatorDataAsGson(pmtct24MonthsReportObject.getIndicatorData());
            } catch (JSONException e) {
                Timber.e(e);
            }
            return "";
        }

        public static String computeEIDMonthly(Date startDate) {
            PmtctEIDMonthlyReportObject pmtctEIDMonthlyReportObject = new PmtctEIDMonthlyReportObject(startDate);
            try {
                return pmtctEIDMonthlyReportObject.getIndicatorDataAsGson(pmtctEIDMonthlyReportObject.getIndicatorData());
            } catch (JSONException e) {
                Timber.e(e);
            }
            return "";
        }
    }

    public static class PNCReports {
        public static String computePncReport(Date now) {
            String report = "";
            PncMonthlyReportObject pncMonthlyReportObject = new PncMonthlyReportObject(now);
            try {
                report = pncMonthlyReportObject.getIndicatorDataAsGson(pncMonthlyReportObject.getIndicatorData());
            } catch (Exception e) {
                Timber.e(e);
            }
            return report;
        }
    }

    public static class ANCReports {
        public static String computeAncReport(Date now) {
            String report = "";
            AncMonthlyReportObject ancMonthlyReportObject = new AncMonthlyReportObject(now);
            try {
                report = ancMonthlyReportObject.getIndicatorDataAsGson(ancMonthlyReportObject.getIndicatorData());
            } catch (Exception e) {
                Timber.e(e);
            }
            return report;
        }
    }

    public static class CBHSReports {
        public static String computeCbhsReport(Date now) {
            String report = "";
            CbhsMonthlyReportObject cbhsMonthlyReportObject = new CbhsMonthlyReportObject(now);
            try {
                report = cbhsMonthlyReportObject.getIndicatorDataAsGson(cbhsMonthlyReportObject.getIndicatorData());
            } catch (Exception e) {
                Timber.e(e);
            }
            return report;
        }
    }

    public static class LDReports {
        public static String computeLdReport(Date now) {
            String report = "";
            LdMonthlyReportObject ldMonthlyReportObject = new LdMonthlyReportObject(now);
            try {
                report = ldMonthlyReportObject.getIndicatorDataAsGson(ldMonthlyReportObject.getIndicatorData());
            } catch (Exception e) {
                Timber.e(e);
            }
            return report;
        }
    }

    public static class MotherChampionReports {
        public static String computeMotherChampionReport(Date now) {
            String report = "";
            MotherChampionReportObject motherChampionReportObject = new MotherChampionReportObject(now);
            try {
                report = motherChampionReportObject.getIndicatorDataAsGson(motherChampionReportObject.getIndicatorData());
            } catch (Exception e) {
                Timber.e(e);
            }
            return report;
        }
    }

    public static class LTFUReports {
        public static String computeLTFUReport(Date now) {
            String report = "";
            LTFUSummaryObject ltfuSummaryObject = new LTFUSummaryObject(now);
            try {
                report = ltfuSummaryObject.getIndicatorDataAsGson(ltfuSummaryObject.getIndicatorData());
            } catch (Exception e) {
                Timber.e(e);
            }
            return report;
        }
    }


}
