package org.smartregister.chw.hf.dao;

import android.util.Pair;

import net.sqlcipher.database.SQLiteDatabase;

import org.json.JSONObject;
import org.smartregister.chw.hf.model.FamilyDetailsModel;
import org.smartregister.dao.AbstractDao;

import java.util.List;

import timber.log.Timber;

public class FamilyDao extends AbstractDao {

    public static FamilyDetailsModel getFamilyDetail(String baseEntityId) {
        String sql = String.format(
                "SELECT ec_family.base_entity_id,\n" +
                        "       ec_family.primary_caregiver,\n" +
                        "       ec_family.first_name as family_name,\n" +
                        "       ec_family.village_town as village_town,\n" +
                        "       ec_family.family_head\n" +
                        "FROM ec_family\n" +
                        "         INNER JOIN ec_family_member ON ec_family.base_entity_id = ec_family_member.relational_id\n" +
                        "WHERE ec_family_member.base_entity_id = '%s'", baseEntityId);

        DataMap<FamilyDetailsModel> dataMap = cursor -> {
            FamilyDetailsModel familyDetailsModel = new FamilyDetailsModel(
                    getCursorValue(cursor, "base_entity_id"),
                    getCursorValue(cursor, "family_head"),
                    getCursorValue(cursor, "primary_caregiver"),
                    getCursorValue(cursor, "family_name")
            );
            familyDetailsModel.setVillageTown(getCursorValue(cursor, "village_town"));
            return familyDetailsModel;
        };

        List<FamilyDetailsModel> familyProfileModels = readData(sql, dataMap);
        if (familyProfileModels == null || familyProfileModels.size() != 1)
            return null;

        return familyProfileModels.get(0);
    }

    public static void migrateAddLocationIdColSQLString(SQLiteDatabase db) {
        String sqlString = "ALTER TABLE ec_family_member ADD COLUMN sync_location_id";
        db.execSQL(sqlString);
    }

    public static void migrateInsertLocationIDs(SQLiteDatabase db) {
        String getLocationIdSQL = "select event.json, ec_family_member.base_entity_id \n" +
                "from event\n" +
                "inner join ec_family_member on event.baseEntityId = ec_family_member.base_entity_id\n" +
                "where event.eventType = 'Family Member Registration' and ec_family_member.is_closed = '0'";

        DataMap<Pair<String, String>> dataMap = cursor -> new Pair<>(getLocationId(getCursorValue(cursor, "json")), getCursorValue(cursor, "base_entity_id"));
        List<Pair<String, String>> jsonPairs = readData(getLocationIdSQL, dataMap, db);
        String locationId;
        String baseEntityId;
        String updateSQLString = "UPDATE ec_family_member SET sync_location_id = '%s' WHERE base_entity_id = '%s'";
        if (jsonPairs != null && !jsonPairs.isEmpty()) {
            for (Pair<String, String> locationIDEntityPair : jsonPairs) {
                locationId = locationIDEntityPair.first;
                baseEntityId = locationIDEntityPair.second;
                db.execSQL(String.format(updateSQLString, locationId, baseEntityId));
            }
        }
    }

    public static String getLocationId(String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            return jsonObject.getString("locationId");
        } catch (Exception ex) {
            Timber.e(ex);
            return "";
        }
    }
}
