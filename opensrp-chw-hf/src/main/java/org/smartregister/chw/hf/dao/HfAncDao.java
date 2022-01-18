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

    public static boolean isTestConducted(String testName,String baseEntityId) {
        DataMap<String> dataMap =  cursor -> getCursorValue(cursor, testName);

        String sql = String.format(
                "SELECT %s FROM %s WHERE base_entity_id = '%s' " +
                        "AND is_closed = 0",
                testName,
                "ec_anc_register",
                baseEntityId
        );

        List<String> res = readData(sql,dataMap);
        return res.get(0).equalsIgnoreCase("positive") || res.get(0).equalsIgnoreCase("negative");
    }

    public static String malariaDosageGiven(String baseEntityId){
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "malaria_preventive_therapy");

        String sql = String.format(
                "SELECT malaria_preventive_therapy FROM %s WHERE base_entity_id = '%s' " +
                        "AND is_closed = 0",
                "ec_anc_register",
                baseEntityId
        );

        List<String> res = readData(sql, dataMap);
        if(res.get(0) != null){
            return res.get(0);
        }
        return "null";
    }

    public static boolean isHivTestConductedAtWk32(String baseEntityId){
        DataMap<String> dataMap =  cursor -> getCursorValue(cursor,"hiv_test_at_32");

        String sql = String.format(
                "SELECT hiv_test_at_32 FROM %s WHERE base_entity_id = '%s' " +
                        "AND is_closed = 0",
                "ec_anc_register",
                baseEntityId
        );

        List<String> res = readData(sql,dataMap);
        return res.get(0) != null;
    }
    public static String getHivStatus(String baseEntityId){
        DataMap<String> dataMap =  cursor -> getCursorValue(cursor,"hiv");

        String sql = String.format(
                "SELECT hiv FROM %s WHERE base_entity_id = '%s' " +
                        "AND is_closed = 0",
                "ec_anc_register",
                baseEntityId
        );

        List<String> res = readData(sql,dataMap);
        if(res.get(0) != null){
            return res.get(0);
        }
        return "null";
    }
    public static String getClientHeight(String baseEntityId){
        DataMap<String> dataMap =  cursor -> getCursorValue(cursor,"height");

        String sql = String.format(
                "SELECT height FROM %s WHERE base_entity_id = '%s' " +
                        "AND is_closed = 0",
                "ec_anc_register",
                baseEntityId
        );

        List<String> res = readData(sql,dataMap);
        if(res.get(0) != null){
            return res.get(0);
        }
        return "null";
    }

    public static int getVisitNumber(String baseEntityId){
        DataMap<String> dataMap =  cursor -> getCursorValue(cursor,"visit_number");

        String sql = String.format(
                "SELECT visit_number FROM %s WHERE base_entity_id = '%s' " +
                        "AND is_closed = 0",
                "ec_anc_register",
                baseEntityId
        );

        List<String> res = readData(sql,dataMap);
        if(res.get(0) != null){
            return Integer.parseInt(res.get(0));
        }
        return 0;
    }
}
