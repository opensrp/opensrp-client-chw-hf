package org.smartregister.chw.hf.dao;

import org.smartregister.chw.vmmc.dao.VmmcDao;

import java.util.List;

public class HfVmmcDao extends VmmcDao {

    //trial ang
    public static int getVisitNumber(String baseEntityID) {
        String sql = "SELECT visit_number  FROM ec_vmmc_follow_up_visit WHERE entity_id='" + baseEntityID + "' ORDER BY visit_number DESC LIMIT 1";
        DataMap<Integer> map = cursor -> getCursorIntValue(cursor, "visit_number");
        List<Integer> res = readData(sql, map);

        if (res != null && res.size() > 0 && res.get(0) != null) {
            return res.get(0) + 1;
        } else
            return 0;
    }

    public static String getMcMethodUsed(String baseEntityId) {
        String sql = "SELECT male_circumcision_method FROM ec_vmmc_procedure p " +
                " WHERE p.entity_id = '" + baseEntityId + "' ORDER BY last_interacted_with DESC LIMIT 1";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "male_circumcision_method");

        List<String> res = readData(sql, dataMap);
        if (res != null && res.size() != 0 && res.get(0) != null) {
            return res.get(0);
        }
        return "";
    }

    public static String getMcDoneDate(String baseEntityId) {
        String sql = "SELECT mc_procedure_date FROM ec_vmmc_procedure p " +
                " WHERE p.entity_id = '" + baseEntityId + "' ORDER BY last_interacted_with DESC LIMIT 1";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "mc_procedure_date");

        List<String> res = readData(sql, dataMap);
        if (res != null && res.size() != 0 && res.get(0) != null) {
            return res.get(0);
        }
        return "";
    }

    public static String getDischargingDate(String baseEntityId) {
        String sql = "SELECT discharge_date FROM ec_vmmc_post_op_and_discharge p " +
                " WHERE p.entity_id = '" + baseEntityId + "' ORDER BY last_interacted_with DESC LIMIT 1";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "discharge_date");

        List<String> res = readData(sql, dataMap);
        if (res != null && res.size() != 0 && res.get(0) != null) {
            return res.get(0);
        }
        return "";
    }

    public static int getFollowUpVisitNumber(String baseEntityId) {
        String sql = "SELECT visit_number FROM ec_vmmc_follow_up_visit p " +
                " WHERE p.entity_id = '" + baseEntityId + "' ORDER BY last_interacted_with DESC LIMIT 1";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "visit_number");

        List<String> res = readData(sql, dataMap);
        if (res != null && res.size() != 0 && res.get(0) != null) {
            return Integer.parseInt(res.get(0));
        }
        return 0;
    }
}