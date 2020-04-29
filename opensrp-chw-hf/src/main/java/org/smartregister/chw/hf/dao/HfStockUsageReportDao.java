package org.smartregister.chw.hf.dao;

import org.smartregister.chw.hf.model.InAppUsages;
import org.smartregister.dao.AbstractDao;

import java.util.ArrayList;
import java.util.List;

public class HfStockUsageReportDao extends AbstractDao {

    public static String getProvidersMonthsValue(String month, String indicatorCode, String providerName) {
        String sql = "SELECT value " +
                "FROM monthly_tallies " +
                "WHERE indicator_code = '" + indicatorCode + "'" +
                "AND month = '" + month + "'" +
                "AND provider_id = '" + providerName + "'" +
                "GROUP by indicator_code";

        DataMap<InAppUsages> dataMap = cursor -> {
            InAppUsages inAppUsages = new InAppUsages();
            inAppUsages.setMonth(month);
            inAppUsages.setIndicatorCode(indicatorCode);
            inAppUsages.setProviderId(providerName);
            inAppUsages.setStockValues(getCursorValue(cursor, "value"));
            return inAppUsages;
        };

        List<InAppUsages> res = readData(sql, dataMap);
        return (res == null || res.size() == 0) ? "0" : res.get(0).getStockValues();
    }

    public static String getAllProvidersMonthsValue(String month, String indicatorCode) {
        String sql = "SELECT value " +
                "FROM monthly_tallies " +
                "WHERE indicator_code = '" + indicatorCode + "'" +
                "AND month = '" + month + "'" +
                "GROUP by indicator_code";

        DataMap<InAppUsages> dataMap = cursor -> {
            InAppUsages inAppUsages = new InAppUsages();
            inAppUsages.setMonth(month);
            inAppUsages.setIndicatorCode(indicatorCode);
            inAppUsages.setStockValues(getCursorValue(cursor, "value"));
            return inAppUsages;
        };
        List<InAppUsages> res = readData(sql, dataMap);
        return (res == null || res.size() == 0) ? "0" : res.get(0).getStockValues();
    }

    public static String getIndicatorLabels(String indicatorCode) {
        String sql = "SELECT description " +
                "FROM indicators " +
                "WHERE indicator_code = '" + indicatorCode + "'";
        AbstractDao.DataMap<String> dataMap = cursor -> getCursorValue(cursor, "description");
        List<String> res = readData(sql, dataMap);
        if (res == null)
            return "";

        return res.get(0);
    }

    public List<String> getListOfProviders(String month, String year, String tableName) {
        String sql = "SELECT DISTINCT provider_id FROM '" + tableName + "' " +
                "WHERE month= '" + month + "' " +
                "AND year= '" + year + "'" +
                "order by provider_id DESC";
        AbstractDao.DataMap<String> dataMap = cursor -> getCursorValue(cursor, "provider_id");
        List<String> res = readData(sql, dataMap);
        if (res == null)
            return new ArrayList<>();

        return res;
    }

    public List<String> getHFListOfProviders(String month, String tableName) {
        String sql = "SELECT DISTINCT provider_id FROM '" + tableName + "' " +
                "WHERE month= '" + month + "' " +
                "order by provider_id DESC";
        AbstractDao.DataMap<String> dataMap = cursor -> getCursorValue(cursor, "provider_id");
        List<String> res = readData(sql, dataMap);
        if (res == null)
            return new ArrayList<>();

        return res;
    }

}
