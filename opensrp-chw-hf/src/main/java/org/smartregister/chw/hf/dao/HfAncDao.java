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
}
