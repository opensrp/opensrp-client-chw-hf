package org.smartregister.chw.hf.utils;

import android.content.Context;
import android.webkit.JavascriptInterface;

import static org.smartregister.util.Utils.getAllSharedPreferences;

public class HfWebAppInterface {
    Context mContext;

    public HfWebAppInterface(Context c) {
        mContext = c;
    }

    @JavascriptInterface
    public String getData(String key) {
        return "";
    }

    @JavascriptInterface
    public String getDataPeriod() {
        return "";
    }

    @JavascriptInterface
    public String getReportingFacility() {
        return getAllSharedPreferences().fetchCurrentLocality();
    }
}
