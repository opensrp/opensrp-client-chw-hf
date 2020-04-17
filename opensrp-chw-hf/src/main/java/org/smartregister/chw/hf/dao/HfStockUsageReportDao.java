package org.smartregister.chw.hf.dao;

import org.smartregister.dao.AbstractDao;

import java.util.ArrayList;
import java.util.List;

public class HfStockUsageReportDao extends AbstractDao {
    public List<String> getListOfProviders(String month, String year) {
        String sql = "SELECT DISTINCT provider_id FROM stock_usage_report " +
                "WHERE month= '" + month + "' " +
                "AND year= '" + year + "'" +
                "order by provider_id DESC";
        AbstractDao.DataMap<String> dataMap = cursor -> getCursorValue(cursor, "provider_id");
        List<String> res = readData(sql, dataMap);
        if (res == null)
            return new ArrayList<>();

        return res;
    }

}
