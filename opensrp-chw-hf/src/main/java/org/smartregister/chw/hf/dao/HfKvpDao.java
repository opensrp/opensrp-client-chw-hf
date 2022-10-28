package org.smartregister.chw.hf.dao;

import org.smartregister.chw.kvp.dao.KvpDao;

import java.util.List;

public class HfKvpDao extends KvpDao {

    public static String getClientStatus(String baseEntityId) {
        String sql = "SELECT client_status FROM ec_kvp_bio_medical_services p " +
                " WHERE p.entity_id = '" + baseEntityId + "'";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "client_status");

        List<String> res = readData(sql, dataMap);
        if (res != null && res.size() != 0 && res.get(0) != null) {
            return res.get(0);
        }
        return "";
    }
}
