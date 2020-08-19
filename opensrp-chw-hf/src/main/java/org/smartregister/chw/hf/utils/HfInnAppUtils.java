package org.smartregister.chw.hf.utils;

import android.content.Context;

import org.smartregister.chw.hf.R;

import java.lang.reflect.Field;

public class HfInnAppUtils {
    public static int getResId(String resName, Class<?> c) {
        try {
            Field idField = c.getDeclaredField(resName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static String getStringResource(Context context, String resName) {
        int res = getResId(resName, R.string.class);
        if (res != -1)
            return context.getString(res);

        return null;
    }

    public static String getYearMonth(String month, String year) {
        return year.concat("-").concat(month);
    }

}


