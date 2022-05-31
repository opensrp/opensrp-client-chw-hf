package org.smartregister.chw.hf.dao;

import org.smartregister.dao.AbstractDao;

import java.util.List;

public class HfHivDao extends AbstractDao {
    public static boolean isHivMember(String baseEntityId) {
        String sql = "select count(*) count from ec_hiv_register where base_entity_id = '" + baseEntityId + "'" + " AND (UPPER (ec_hiv_register.client_hiv_status_after_testing) LIKE UPPER('Positive')) ";

        DataMap<Integer> dataMap = cursor -> getCursorIntValue(cursor, "count");

        List<Integer> res = readData(sql, dataMap);

        if (res == null || res.size() < 1)
            return false;
        return res.get(0) == 1;
    }
}
