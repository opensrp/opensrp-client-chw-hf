package org.smartregister.chw.hf.dao;

import org.smartregister.chw.pmtct.dao.PmtctDao;

import java.util.List;

public class HfPmtctDao extends PmtctDao {
    public static boolean isEligibleForEac(String baseEntityID) {
        String sql = "SELECT hvl_suppression FROM ec_pmtct_followup p " +
                "WHERE p.base_entity_id = '" + baseEntityID + "'";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "hvl_suppression");

        List<String> res = readData(sql, dataMap);

        if(res.size() > 0 && res.get(0) != null){
            int viralLoad = Integer.parseInt(res.get(0));
            return viralLoad >= 1000;
        }
        return false;
    }
    public static boolean isEligibleForSecondEac(String baseEntityID) {
        String sql = "SELECT hvl_suppression_after_eac_1 FROM ec_pmtct_followup p " +
                "WHERE p.base_entity_id = '" + baseEntityID + "'";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "hvl_suppression_after_eac_1");

        List<String> res = readData(sql, dataMap);

        if(res.size() > 0 && res.get(0) != null){
            int viralLoad = Integer.parseInt(res.get(0));
            return viralLoad >= 1000;
        }
        return false;
    }
    public static boolean isEacFirstDone(String baseEntityID){
        String sql = "SELECT count(CASE WHEN eac_day_1 IS NOT NULL AND eac_day_2 IS NOT NULL AND eac_day_3 IS NOT NULL THEN 0 END)  as count_eac  " +
                    " FROM ec_pmtct_followup p " +
                    "WHERE p.base_entity_id = '" + baseEntityID + "'";

        DataMap<Integer> dataMap = cursor -> getCursorIntValue(cursor, "count_eac");

        List<Integer> res = readData(sql, dataMap);

        return res.get(0) == 1;
    }
    public static boolean isSecondEacDone(String baseEntityID){
        String sql = "SELECT count(CASE WHEN eac_month_1 IS NOT NULL AND eac_month_2 IS NOT NULL AND eac_month_3 IS NOT NULL THEN 0 END)  as count_eac  " +
                " FROM ec_pmtct_followup p " +
                "WHERE p.base_entity_id = '" + baseEntityID + "'";

        DataMap<Integer> dataMap = cursor -> getCursorIntValue(cursor, "count_eac");

        List<Integer> res = readData(sql, dataMap);

        return res.get(0) == 1;
    }
}
