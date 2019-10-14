package org.smartregister.chw.hf.dao;

import org.smartregister.chw.core.dao.AbstractDao;
import org.smartregister.chw.hf.model.FamilyDetailsModel;

import java.util.List;

public class FamilyDao extends AbstractDao {

    public static FamilyDetailsModel getFamilyDetail(String baseEntityId) {
        String sql = String.format(
                "SELECT ec_family.base_entity_id,\n" +
                        "       ec_family.primary_caregiver,\n" +
                        "       ec_family.first_name as family_name,\n" +
                        "       ec_family.family_head\n" +
                        "FROM ec_family\n" +
                        "         INNER JOIN ec_family_member ON ec_family.base_entity_id = ec_family_member.relational_id\n" +
                        "WHERE ec_family_member.base_entity_id = '%s'", baseEntityId);

        DataMap<FamilyDetailsModel> dataMap = cursor -> new FamilyDetailsModel(
                getCursorValue(cursor, "base_entity_id"),
                getCursorValue(cursor, "family_head"),
                getCursorValue(cursor, "primary_caregiver"),
                getCursorValue(cursor, "family_name")
        );

        List<FamilyDetailsModel> familyProfileModels = readData(sql, dataMap);
        if (familyProfileModels == null || familyProfileModels.size() != 1)
            return null;

        return familyProfileModels.get(0);
    }
}
