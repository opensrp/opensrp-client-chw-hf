package org.smartregister.chw.hf.dao;

import org.smartregister.dao.AbstractDao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportDao extends AbstractDao {
    public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

    public static List<Map<String, String>> getMotherChampions(Date reportDate) {
        String sql = "SELECT chw_name, provider_id \n" +
                "from ec_mother_champion_followup \n" +
                "WHERE chw_name IS NOT NULL \n" +
                "  AND date((substr('%s', 1, 4) || '-' || substr('%s', 6, 2) || '-' || '01')) =\n" +
                "      date(substr(strftime('%Y-%m-%d', datetime(last_interacted_with / 1000, 'unixepoch', 'localtime')), 1, 4) ||\n" +
                "           '-' ||\n" +
                "           substr(strftime('%Y-%m-%d', datetime(last_interacted_with / 1000, 'unixepoch', 'localtime')), 6, 2) ||\n" +
                "           '-' || '01')\n" +
                "UNION \n" +
                "SELECT chw_name, provider_id \n" +
                "from ec_anc_partner_community_feedback \n" +
                "WHERE chw_name IS NOT NULL \n" +
                "  AND date((substr('%s', 1, 4) || '-' || substr('%s', 6, 2) || '-' || '01')) =\n" +
                "      date(substr(strftime('%Y-%m-%d', datetime(last_interacted_with / 1000, 'unixepoch', 'localtime')), 1, 4) ||\n" +
                "           '-' ||\n" +
                "           substr(strftime('%Y-%m-%d', datetime(last_interacted_with / 1000, 'unixepoch', 'localtime')), 6, 2) ||\n" +
                "           '-' || '01') \n" +
                "UNION \n" +
                "SELECT chw_name, provider_id \n" +
                "from ec_sbcc \n" +
                "WHERE chw_name IS NOT NULL \n" +
                "  AND date((substr('%s', 1, 4) || '-' || substr('%s', 6, 2) || '-' || '01')) =\n" +
                "      date(substr(sbcc_date, 7, 4) || '-' || substr(sbcc_date, 4, 2) || '-' || '01')\n" +
                "UNION\n" +
                "SELECT chw_name, provider_id\n" +
                "from ec_pmtct_community_feedback\n" +
                "WHERE chw_name IS NOT NULL\n" +
                "  AND date((substr('%s', 1, 4) || '-' || substr('%s', 6, 2) || '-' || '01')) =\n" +
                "      date(substr(strftime('%Y-%m-%d', datetime(pmtct_community_followup_visit_date / 1000, 'unixepoch', 'localtime')),\n" +
                "                  1, 4) ||\n" +
                "           '-' ||\n" +
                "           substr(strftime('%Y-%m-%d', datetime(pmtct_community_followup_visit_date / 1000, 'unixepoch', 'localtime')),\n" +
                "                  6, 2) ||\n" +
                "           '-' || '01')\n";

        String queryDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(reportDate);

        sql = sql.contains("%s") ? sql.replaceAll("%s", queryDate) : sql;

        DataMap<Map<String, String>> map = cursor -> {
            Map<String, String> data = new HashMap<>();
            data.put("chw_name", cursor.getString(cursor.getColumnIndex("chw_name")));
            data.put("provider_id", cursor.getString(cursor.getColumnIndex("provider_id")));
            return data;
        };

        List<Map<String, String>> res = readData(sql, map);


        if (res != null && res.size() > 0) {
            return res;
        } else
            return new ArrayList<>();
    }

    public static int getReportPerIndicatorCode(String indicatorCode, Date reportDate) {
        String reportDateString = simpleDateFormat.format(reportDate);
        String sql = "SELECT indicator_value\n" +
                "FROM indicator_daily_tally\n" +
                "WHERE indicator_code = '" + indicatorCode + "'\n" +
                "  AND date((substr('" + reportDateString + "', 7, 4) || '-' || substr('" + reportDateString + "', 4, 2) || '-' || '01')) = date((substr(day, 1, 4) || '-' || substr(day, 6, 2) || '-' || '01'))\n" +
                "ORDER BY day DESC LIMIT 1";

        DataMap<Integer> map = cursor -> getCursorIntValue(cursor, "indicator_value");

        List<Integer> res = readData(sql, map);


        if (res != null && res.size() > 0 && res.get(0) != null) {
            return res.get(0);
        } else
            return 0;
    }

}
