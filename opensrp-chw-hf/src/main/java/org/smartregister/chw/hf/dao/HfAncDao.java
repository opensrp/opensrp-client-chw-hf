package org.smartregister.chw.hf.dao;

import org.smartregister.dao.AbstractDao;

import java.util.List;

public class HfAncDao extends AbstractDao {

    public static boolean isReviewFormFilled(String baseEntityId){
        DataMap<String> dataMap = cursor -> getCursorValue(cursor,"name_of_hf");

        String sql = String.format(
                "SELECT name_of_hf FROM %s WHERE base_entity_id = '%s' " +
                        "AND name_of_hf is not null " +
                        "AND is_closed = 0",
                "ec_anc_register",
                baseEntityId
        );

        List<String> res = readData(sql,dataMap);

        return res.size() == 1;
    }

    public static boolean isClientClosed(String baseEntityId){
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "is_closed");

        String sql = String.format(
                "SELECT is_closed FROM %s WHERE base_entity_id = '%s' " +
                        "AND is_closed = 1",
                "ec_anc_register",
                baseEntityId
        );

        List<String> res = readData(sql,dataMap);

        return res.size() == 1;
    }
}
