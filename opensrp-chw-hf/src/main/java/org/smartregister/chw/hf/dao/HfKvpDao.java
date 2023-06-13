package org.smartregister.chw.hf.dao;

import org.smartregister.chw.kvp.dao.KvpDao;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

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

    public static String getClientEnrollmentDate(String baseEntityId) {
        String sql = "SELECT enrollment_date FROM ec_kvp_register p " +
                " WHERE p.base_entity_id = '" + baseEntityId + "'";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "enrollment_date");

        List<String> res = readData(sql, dataMap);
        if (res != null && res.size() != 0 && res.get(0) != null) {
            return res.get(0);
        }
        return "";
    }

    public static boolean hasPrepFollowup(String baseEntityId) {
        String sql = "SELECT visit_type FROM ec_prep_followup p " +
                " WHERE p.entity_id = '" + baseEntityId + "'";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "visit_type");

        List<String> res = readData(sql, dataMap);
        return res != null && res.size() != 0 && res.get(0) != null;
    }

    public static Date getHbvTestDate(String baseEntityId) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String sql = "SELECT hbv_test_date FROM ec_prep_register p " +
                " WHERE p.base_entity_id = '" + baseEntityId + "'";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "hbv_test_date");

        List<String> res = readData(sql, dataMap);
        if (res != null && res.size() != 0 && res.get(0) != null) {
            try {
                return simpleDateFormat.parse(res.get(0));
            } catch (ParseException e) {
                Timber.e(e);
                return null;
            }
        }
        return null;
    }

    public static Date getHcvTestDate(String baseEntityId) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String sql = "SELECT hcv_test_date FROM ec_prep_register p " +
                " WHERE p.base_entity_id = '" + baseEntityId + "'";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "hcv_test_date");

        List<String> res = readData(sql, dataMap);
        if (res != null && res.size() != 0 && res.get(0) != null) {
            try {
                return simpleDateFormat.parse(res.get(0));
            } catch (ParseException e) {
                Timber.e(e);
                return null;
            }
        }
        return null;
    }

    public static Date getCrclTestDate(String baseEntityId) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String sql = "SELECT crcl_test_date FROM ec_prep_register p " +
                " WHERE p.base_entity_id = '" + baseEntityId + "'";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "crcl_test_date");

        List<String> res = readData(sql, dataMap);
        if (res != null && res.size() != 0 && res.get(0) != null) {
            try {
                return simpleDateFormat.parse(res.get(0));
            } catch (ParseException e) {
                Timber.e(e);
                return null;
            }
        }
        return null;
    }

    public static String getCrclResults(String baseEntityId) {
        String sql = "SELECT crcl_results FROM ec_prep_register p " +
                " WHERE p.base_entity_id = '" + baseEntityId + "'";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "crcl_results");

        List<String> res = readData(sql, dataMap);
        if (res != null && res.size() != 0 && res.get(0) != null) {
            return res.get(0);
        }
        return null;
    }

    public static boolean wereSelfTestingKitsDistributed(String baseEntityId) {
        String sql = "SELECT kits_distributed FROM ec_kvp_bio_medical_services p " +
                " WHERE p.entity_id = '" + baseEntityId + "' ORDER BY kvp_visit_date DESC LIMIT 1";
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "kits_distributed");

        List<String> res = readData(sql, dataMap);
        if (res != null && res.size() > 0 && res.get(0) != null) {
            return res.get(0).equalsIgnoreCase("yes");
        }
        return false;
    }
}
