package org.smartregister.chw.hf.dao;

import org.smartregister.dao.AbstractDao;

import java.util.List;

public class HfAncBirthEmergencyPlanDao extends AbstractDao {
    private static final String TABLE_NAME = "ec_anc_birth_emergency_plan";

    public static boolean isAllFilled(String baseEntityId){
       return isDeliveryPlaceIdentified(baseEntityId)
               && isBloodDonorIdentified(baseEntityId)
               && isHouseholdSupportIdentified(baseEntityId)
               && isTransportMethodIdentified(baseEntityId)
               && isBirthCompanionIdentified(baseEntityId)
               && areEmergencyFundsPrepared(baseEntityId);
    }

    public static boolean isDeliveryPlaceIdentified(String baseEntityId) {
        //adding a check for name_of_hf because question for delivery place didn't exist prior
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "name_of_hf");
        String sql = String.format(
                "SELECT name_of_hf FROM %s WHERE entity_id = '%s'" +
                        " AND is_closed = 0 AND name_of_hf IS NOT NULL",
                TABLE_NAME, baseEntityId);
        List<String> res =readData(sql, dataMap);
        return res != null && res.size() > 0 && res.get(0) != null;
    }

    public static boolean isBloodDonorIdentified(String baseEntityId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "blood_donor");
        String sql = String.format(
                "SELECT blood_donor FROM %s WHERE entity_id = '%s' " +
                        "AND is_closed = 0 AND blood_donor is not null AND blood_donor <> 'not_prepared'",
                TABLE_NAME,
                baseEntityId
        );
        List<String> res = readData(sql, dataMap);
        return res != null && res.size() > 0 && res.get(0) != null;
    }

    public static boolean isHouseholdSupportIdentified(String baseEntityId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "household_support");
        String sql = String.format(
                "SELECT household_support FROM %s WHERE entity_id = '%s' " +
                        "AND is_closed = 0 AND household_support is not null AND household_support <> 'not_prepared'",
                TABLE_NAME,
                baseEntityId
        );
        List<String> res = readData(sql, dataMap);
        return res != null && res.size() > 0 && res.get(0) != null;
    }

    public static boolean isTransportMethodIdentified(String baseEntityId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "transport");
        String sql = String.format(
                "SELECT transport FROM %s WHERE entity_id = '%s' " +
                        "AND is_closed = 0 AND transport is not null AND transport <> 'not_prepared'",
                TABLE_NAME,
                baseEntityId
        );
        List<String> res = readData(sql, dataMap);
        return res != null && res.size() > 0 && res.get(0) != null;
    }

    public static boolean isBirthCompanionIdentified(String baseEntityId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "birth_companion");
        String sql = String.format(
                "SELECT birth_companion FROM %s WHERE entity_id = '%s' " +
                        "AND is_closed = 0 AND birth_companion is not null AND birth_companion <> 'not_prepared'",
                TABLE_NAME,
                baseEntityId
        );
        List<String> res = readData(sql, dataMap);
        return res != null && res.size() > 0 && res.get(0) != null;
    }

    public static boolean areEmergencyFundsPrepared(String baseEntityId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "emergency_funds");
        String sql = String.format(
                "SELECT emergency_funds FROM %s WHERE entity_id = '%s' " +
                        "AND is_closed = 0 AND emergency_funds is not null AND emergency_funds <> 'not_prepared'",
                TABLE_NAME,
                baseEntityId
        );
        List<String> res = readData(sql, dataMap);
        return res != null && res.size() > 0 && res.get(0) != null;
    }
}
