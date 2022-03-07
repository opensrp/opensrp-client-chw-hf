package org.smartregister.chw.hf.dao;

import org.smartregister.dao.AbstractDao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HfAncDao extends AbstractDao {

    public static boolean isReviewFormFilled(String baseEntityId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "name_of_hf");

        String sql = String.format(
                "SELECT name_of_hf FROM %s WHERE base_entity_id = '%s' " +
                        "AND name_of_hf is not null " +
                        "AND is_closed = 0",
                "ec_anc_register",
                baseEntityId
        );

        List<String> res = readData(sql, dataMap);

        return res.size() == 1;
    }

    public static boolean isPartnerRegistered(String baseEntityId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "partner_base_entity_id");

        String sql = String.format(
                "SELECT partner_base_entity_id FROM %s WHERE base_entity_id = '%s' " +
                        "AND partner_base_entity_id is not null " +
                        "AND is_closed = 0",
                "ec_anc_register",
                baseEntityId
        );

        List<String> res = readData(sql, dataMap);

        return res.size() == 1;
    }

    public static String getPartnerBaseEntityId(String baseEntityId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "partner_base_entity_id");

        String sql = String.format(
                "SELECT partner_base_entity_id FROM %s WHERE base_entity_id = '%s' " +
                        "AND partner_base_entity_id is not null " +
                        "AND is_closed = 0",
                "ec_anc_register",
                baseEntityId
        );

        List<String> res = readData(sql, dataMap);

        if (res.size() == 1) {
            return res.get(0);
        }

        return "";
    }


    public static boolean isPartnerTestedForHiv(String baseEntityId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "partner_hiv");

        String sql = String.format(
                "SELECT partner_hiv FROM %s WHERE base_entity_id = '%s' " +
                        "AND partner_hiv is not null " +
                        "AND is_closed = 0",
                "ec_anc_register",
                baseEntityId
        );

        List<String> res = readData(sql, dataMap);

        if (res.size() == 1) {
            return !res.get(0).equalsIgnoreCase("test_not_conducted");
        }

        return false;
    }

    public static boolean isPartnerHivTestConductedAtWk32(String baseEntityId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "partner_hiv_test_at_32");

        String sql = String.format(
                "SELECT partner_hiv_test_at_32 FROM %s WHERE base_entity_id = '%s' " +
                        "AND is_closed = 0",
                "ec_anc_register",
                baseEntityId
        );

        List<String> res = readData(sql, dataMap);
        if (res.get(0) != null)
            return res.get(0).equalsIgnoreCase("true");

        return false;
    }

    public static int getPartnerHivTestNumber(String baseEntityId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "partner_hiv");
        DataMap<String> testNumberMap = cursor -> getCursorValue(cursor, "partner_hiv_test_number");
        String sql = String.format(
                "SELECT partner_hiv,partner_hiv_test_number FROM %s WHERE base_entity_id = '%s' " +
                        " AND partner_hiv IS NOT NULL" +
                        " AND is_closed = 0",
                "ec_anc_register",
                baseEntityId
        );

        List<String> res = readData(sql, dataMap);
        List<String> testNumberRes = readData(sql, testNumberMap);
        if (res.size() > 0) {
            if(res.get(0).equalsIgnoreCase("test_not_conducted")){
                return Integer.parseInt(testNumberRes.get(0)) - 1;
            }
            return Integer.parseInt(testNumberRes.get(0));
        }
        return 0;
    }

    public static int getNextPartnerHivTestNumber(String baseEntityId){
        return getPartnerHivTestNumber(baseEntityId) + 1;
    }

    public static String getPartnerHivStatus(String baseEntityId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "partner_hiv");

        String sql = String.format(
                "SELECT partner_hiv FROM %s WHERE base_entity_id = '%s' " +
                        "AND is_closed = 0",
                "ec_anc_register",
                baseEntityId
        );

        List<String> res = readData(sql, dataMap);
        if (res.get(0) != null) {
            return res.get(0);
        }
        return "null";
    }

    public static boolean isPartnerTestedForSyphilis(String baseEntityId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "partner_syphilis");

        String sql = String.format(
                "SELECT partner_syphilis FROM %s WHERE base_entity_id = '%s' " +
                        "AND partner_syphilis is not null " +
                        "AND is_closed = 0",
                "ec_anc_register",
                baseEntityId
        );

        List<String> res = readData(sql, dataMap);

        if (res.size() == 1) {
            return !res.get(0).equalsIgnoreCase("test_not_conducted");
        }

        return false;
    }

    public static boolean isPartnerTestedForHepatitis(String baseEntityId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "partner_hepatitis");

        String sql = String.format(
                "SELECT partner_hepatitis FROM %s WHERE base_entity_id = '%s' " +
                        "AND partner_hepatitis is not null " +
                        "AND is_closed = 0",
                "ec_anc_register",
                baseEntityId
        );

        List<String> res = readData(sql, dataMap);

        if (res.size() == 1) {
            return !res.get(0).equalsIgnoreCase("test_not_conducted");
        }

        return false;
    }

    public static boolean isClientClosed(String baseEntityId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "is_closed");

        String sql = String.format(
                "SELECT is_closed FROM %s WHERE base_entity_id = '%s' " +
                        "AND is_closed = 1",
                "ec_anc_register",
                baseEntityId
        );

        List<String> res = readData(sql, dataMap);

        return res.size() == 1;
    }

    public static boolean isTestConducted(String testName, String baseEntityId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, testName);

        String sql = String.format(
                "SELECT %s FROM %s WHERE base_entity_id = '%s' " +
                        "AND is_closed = 0",
                testName,
                "ec_anc_register",
                baseEntityId
        );

        List<String> res = readData(sql, dataMap);
        return res.get(0).equalsIgnoreCase("positive") || res.get(0).equalsIgnoreCase("negative");
    }

    public static String malariaDosageIpt1(String baseEntityId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "malaria_preventive_therapy_ipt1");

        String sql = String.format(
                "SELECT malaria_preventive_therapy_ipt1 FROM %s WHERE base_entity_id = '%s' " +
                        "AND is_closed = 0",
                "ec_anc_register",
                baseEntityId
        );

        List<String> res = readData(sql, dataMap);
        if (res.get(0) != null) {
            return res.get(0);
        }
        return "null";
    }

    public static String malariaDosageIpt2(String baseEntityId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "malaria_preventive_therapy_ipt2");

        String sql = String.format(
                "SELECT malaria_preventive_therapy_ipt2 FROM %s WHERE base_entity_id = '%s' " +
                        "AND is_closed = 0",
                "ec_anc_register",
                baseEntityId
        );

        List<String> res = readData(sql, dataMap);
        if (res.get(0) != null) {
            return res.get(0);
        }
        return "null";
    }

    public static String malariaDosageIpt3(String baseEntityId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "malaria_preventive_therapy_ipt3");

        String sql = String.format(
                "SELECT malaria_preventive_therapy_ipt3 FROM %s WHERE base_entity_id = '%s' " +
                        "AND is_closed = 0",
                "ec_anc_register",
                baseEntityId
        );

        List<String> res = readData(sql, dataMap);
        if (res.get(0) != null) {
            return res.get(0);
        }
        return "null";
    }

    public static String malariaDosageIpt4(String baseEntityId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "malaria_preventive_therapy_ipt4");

        String sql = String.format(
                "SELECT malaria_preventive_therapy_ipt4 FROM %s WHERE base_entity_id = '%s' " +
                        "AND is_closed = 0",
                "ec_anc_register",
                baseEntityId
        );

        List<String> res = readData(sql, dataMap);
        if (res.get(0) != null) {
            return res.get(0);
        }
        return "null";
    }

    public static boolean isHivTestConductedAtWk32(String baseEntityId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "hiv_test_at_32");

        String sql = String.format(
                "SELECT hiv_test_at_32 FROM %s WHERE base_entity_id = '%s' " +
                        "AND is_closed = 0",
                "ec_anc_register",
                baseEntityId
        );

        List<String> res = readData(sql, dataMap);
        if (res.get(0) != null)
            return res.get(0).equalsIgnoreCase("true");

        return false;
    }

    public static int getHivTestNumber(String baseEntityId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "hiv");
        DataMap<String> testNumberMap = cursor -> getCursorValue(cursor, "hiv_test_number");
        String sql = String.format(
                "SELECT hiv, hiv_test_number FROM %s WHERE base_entity_id = '%s' " +
                        " AND hiv IS NOT NULL" +
                        " AND is_closed = 0",
                "ec_anc_register",
                baseEntityId
        );

        List<String> res = readData(sql, dataMap);
        List<String> testNumberRes = readData(sql, testNumberMap);
        if (res.size() > 0) {
            if(res.get(0).equalsIgnoreCase("test_not_conducted")){
                return Integer.parseInt(testNumberRes.get(0)) - 1;
            }
            return Integer.parseInt(testNumberRes.get(0));
        }
        return 0;
    }

    public static int getNextHivTestNumber(String baseEntityId){
        return getHivTestNumber(baseEntityId) + 1;
    }

    public static String getHivStatus(String baseEntityId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "hiv");

        String sql = String.format(
                "SELECT CASE known_on_art\n" +
                "           WHEN 'true'\n" +
                "               THEN 'positive'\n" +
                "           ELSE hiv\n" +
                "           END\n" +
                "           as 'hiv'\n" +
                "FROM %s WHERE base_entity_id = '%s' " +
                "AND is_closed = 0",
                "ec_anc_register",
                baseEntityId
        );

        List<String> res = readData(sql, dataMap);
        if (res.get(0) != null) {
            return res.get(0);
        }
        return "null";
    }

    public static String getClientHeight(String baseEntityId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "height");

        String sql = String.format(
                "SELECT height FROM %s WHERE base_entity_id = '%s' " +
                        "AND is_closed = 0",
                "ec_anc_register",
                baseEntityId
        );

        List<String> res = readData(sql, dataMap);
        if (res.get(0) != null) {
            return res.get(0);
        }
        return "null";
    }

    public static int getVisitNumber(String baseEntityId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "visit_number");

        String sql = String.format(
                "SELECT visit_number FROM %s WHERE base_entity_id = '%s' " +
                        "AND is_closed = 0",
                "ec_anc_register",
                baseEntityId
        );

        List<String> res = readData(sql, dataMap);
        if (res.get(0) != null) {
            return Integer.parseInt(res.get(0));
        }
        return 0;
    }

    public static String getFundalHeight(String baseEntityId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "fundal_height");

        String sql = String.format(
                "SELECT fundal_height FROM %s WHERE base_entity_id = '%s' " +
                        "AND is_closed = 0",
                "ec_anc_register",
                baseEntityId
        );

        List<String> res = readData(sql, dataMap);
        if (res.get(0) != null) {
            return res.get(0);
        }
        return "12";
    }

    public static List<String> getPresentTaskIds(String baseEntityId) {
        DataMap<List<String>> dataMap = cursor -> Collections.singletonList(getCursorValue(cursor, "task_id"));

        String sql = String.format(
                "SELECT task_id FROM ec_anc_register WHERE base_entity_id = '%s' ", baseEntityId);

        List<List<String>> res = readData(sql, dataMap);
        if (res.size() > 0) {
            return res.get(0);
        }
        return new ArrayList<>();
    }
}
