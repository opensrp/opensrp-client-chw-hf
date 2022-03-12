package org.smartregister.chw.hf.dao;

import org.smartregister.chw.core.dao.CorePmtctDao;
import org.smartregister.chw.hf.utils.Constants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class HfPmtctDao extends CorePmtctDao {
    public static boolean isEligibleForEac(String baseEntityID) {
        String sql = "SELECT hvl_collection_date\n" +
                "FROM (SELECT *\n" +
                "      FROM ec_pmtct_followup\n" +
                "      WHERE entity_id = '" + baseEntityID + "'\n" +
                "        AND hvl_sample_id IS NOT NULL\n" +
                "        AND hvl_collection_date IS NOT NULL\n" +
                "      ORDER BY visit_number DESC\n" +
                "      LIMIT 1) pm\n" +
                "         INNER JOIN ec_pmtct_hvl_results ephr on pm.base_entity_id = ephr.hvl_pmtct_followup_form_submission_id\n" +
                "WHERE ephr.hvl_result > 1000 AND ephr.hvl_result IS NOT NULL";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "hvl_collection_date");

        List<String> res = readData(sql, dataMap);

        if (res.size() > 0 && res.get(0) != null) {
            return getElapsedTimeInMonths(res.get(0)) < 3;
        }
        return false;
    }

    public static boolean isEligibleForHlvTest(String baseEntityID) {
        Boolean eligible = isEligibleForHlvTestForNewlyRegisteredClients(baseEntityID);
        if (eligible != null) {
            return eligible;
        }

        eligible = isEligibleForHlvTestForNewlyRegisteredClientsWithVisitsButNotTestedViralLoad(baseEntityID);
        if (eligible != null) {
            return eligible;
        }


        eligible = isEligibleForHlvTestForClientsWithPreviousLackOfSuppression(baseEntityID);
        if (eligible != null) {
            return eligible;
        }

        eligible = isEligibleForHlvTestForClientsWithPreviousHvlTests(baseEntityID);
        if (eligible != null) {
            return eligible;
        }

        return false;
    }

    public static Boolean isEligibleForHlvTestForNewlyRegisteredClients(String baseEntityID) {
        //Checking eligibility for newly registered PMTCT Clients
        String sql = "SELECT known_on_art FROM ec_pmtct_registration p WHERE p.base_entity_id = '" + baseEntityID + "'  AND p.base_entity_id NOT IN (SELECT entity_id FROM ec_pmtct_followup)";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "known_on_art");
        List<String> res = readData(sql, dataMap);

        if (res != null && res.size() > 0 && res.get(0) != null) {
            return res.get(0).equalsIgnoreCase("yes");
        }
        return null;
    }

    public static Boolean isEligibleForHlvTestForNewlyRegisteredClientsWithVisitsButNotTestedViralLoad(String baseEntityID) {
        //Checking eligibility for newly registered PMTCT clients with visits but who have not tested Viral Load
        String sql = "SELECT pmtct_register_date FROM ec_pmtct_registration p WHERE p.base_entity_id = '" + baseEntityID + "' AND known_on_art = 'no' AND p.base_entity_id NOT IN (SELECT entity_id FROM ec_pmtct_followup WHERE hvl_sample_id IS NOT NULL)";

        DataMap<String> registrationDateMap = cursor -> getCursorValue(cursor, "pmtct_register_date");
        List<String> res = readData(sql, registrationDateMap);
        if (res != null && res.size() > 0 && res.get(0) != null) {
            return getElapsedTimeInMonths(res.get(0)) >= 3;
        }
        return null;
    }

    public static Boolean isEligibleForHlvTestForClientsWithPreviousLackOfSuppression(String baseEntityID) {
        //Checking eligibility for  PMTCT clients with lack of suppression after EAC visits
        String sql = "SELECT hvl_collection_date\n" +
                "FROM (SELECT *\n" +
                "      FROM ec_pmtct_followup\n" +
                "      WHERE entity_id = '" + baseEntityID + "'\n" +
                "        AND hvl_sample_id IS NOT NULL\n" +
                "        AND hvl_collection_date IS NOT NULL\n" +
                "      ORDER BY visit_number DESC\n" +
                "      LIMIT 1) pm\n" +
                "         INNER JOIN ec_pmtct_hvl_results ephr on pm.base_entity_id = ephr.hvl_pmtct_followup_form_submission_id\n" +
                "WHERE ephr.hvl_result > 1000 AND ephr.hvl_result IS NOT NULL";


        DataMap<String> hvlCollectionDateMap = cursor -> getCursorValue(cursor, "hvl_collection_date");
        List<String> res = readData(sql, hvlCollectionDateMap);
        if (res != null && res.size() > 0 && res.get(0) != null) {
            return getElapsedTimeInMonths(res.get(0)) >= 3;
        }
        return null;
    }

    public static Boolean isEligibleForHlvTestForClientsWithPreviousHvlTests(String baseEntityID) {
        //Checking eligibility for  registered PMTCT clients with previous Viral Load tests
        String sql =
                "SELECT  hvl_collection_date " +
                        "FROM ec_pmtct_followup epf  " +
                        "WHERE epf.hvl_sample_id IS NOT NULL AND epf.entity_id = '" + baseEntityID + "' " +
                        "ORDER BY epf.visit_number DESC " +
                        "LIMIT 1";


        DataMap<String> hvlCollectionDateMap = cursor -> getCursorValue(cursor, "hvl_collection_date");
        List<String> res = readData(sql, hvlCollectionDateMap);
        if (res != null && res.size() > 0 && res.get(0) != null) {
            return getElapsedTimeInMonths(res.get(0)) >= 6;
        }
        return null;
    }

    public static boolean isEligibleForCD4Retest(String baseEntityID) {
        String sql = "SELECT cd4_collection_date\n" +
                "FROM (SELECT *\n" +
                "      FROM ec_pmtct_followup f\n" +
                "      WHERE f.entity_id = '" + baseEntityID + "'\n" +
                "        AND cd4_sample_id IS NOT NULL\n" +
                "        AND cd4_collection_date IS NOT NULL\n" +
                "      ORDER BY visit_number DESC\n" +
                "      LIMIT 1) f\n" +
                "         LEFT JOIN ec_pmtct_cd4_results epc4r on f.base_entity_id = epc4r.cd4_pmtct_followup_form_submission_id\n" +
                "WHERE epc4r.cd4_result IS NULL\n" +
                "   OR epc4r.cd4_result < 350\n" +
                "ORDER BY visit_number DESC\n" +
                "LIMIT 1";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "cd4_collection_date");
        List<String> res = readData(sql, dataMap);

        if (res != null && res.size() > 0 && res.get(0) != null) {
            return getElapsedTimeInMonths(res.get(0)) >= 6;
        }
        return false;
    }

    public static boolean isEligibleForCD4Test(String baseEntityID) {
        String sql = "SELECT known_on_art FROM ec_pmtct_registration WHERE base_entity_id NOT IN (SELECT entity_id FROM ec_pmtct_followup WHERE cd4_sample_id IS NOT NULL) AND ec_pmtct_registration.base_entity_id = '" + baseEntityID + "'";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "known_on_art");
        List<String> res = readData(sql, dataMap);

        if (res != null && res.size() > 0 && res.get(0) != null) {
            return !res.get(0).equals("yes");
        }
        return false;
    }

    public static boolean isEligibleForBaselineInvestigation(String baseEntityID) {
        String sql = "SELECT known_on_art FROM ec_pmtct_registration WHERE known_on_art = 'no' AND base_entity_id NOT IN (SELECT entity_id FROM ec_pmtct_followup) AND base_entity_id = '" + baseEntityID + "'";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "known_on_art");
        List<String> res = readData(sql, dataMap);

        return res != null && res.size() > 0 && res.get(0) != null;
    }

    public static boolean isEligibleForBaselineInvestigationOnFollowupVisit(String baseEntityID) {
        String sql = "SELECT p.base_entity_id FROM ec_pmtct_registration as p INNER JOIN (SELECT * FROM ec_pmtct_followup ORDER BY visit_number DESC LIMIT 1) as pf on pf.entity_id = p.base_entity_id WHERE (pf.liver_function_test_conducted = 'test_not_conducted' OR pf.receive_liver_function_test_results='no' OR  pf.renal_function_test_conducted = 'test_not_conducted' OR pf.receive_renal_function_test_results='no') AND p.base_entity_id = '" + baseEntityID + "'";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "base_entity_id");
        List<String> res = readData(sql, dataMap);

        return res != null && res.size() > 0 && res.get(0) != null;
    }

    public static int getVisitNumber(String baseEntityID) {
        String sql = "SELECT base_entity_id FROM ec_pmtct_followup WHERE entity_id = '" + baseEntityID + "'";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "base_entity_id");
        List<String> res = readData(sql, dataMap);

        if (res != null && res.size() > 0 && res.get(0) != null) {
            return res.size();
        }

        return 0;
    }

    private static int getElapsedTimeInMonths(String startDateString) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        Date startDate = null;
        try {
            startDate = simpleDateFormat.parse(startDateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(startDate.getTime());
        cal.set(Calendar.DAY_OF_MONTH, 1);
        startDate = cal.getTime();

        Date now = new Date(System.currentTimeMillis());

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(now.getTime() - startDate.getTime());
        return c.get(Calendar.MONTH);
    }

    public static boolean hasHvlResults(String baseEntityId) {
        String sql = "SELECT hvl_sample_id from ec_pmtct_followup\n" +
                "       WHERE entity_id = '" + baseEntityId + "'" +
                "       AND hvl_sample_id IS NOT NULL";


        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "hvl_sample_id");
        List<String> res = readData(sql, dataMap);

        return res != null && res.size() > 0;
    }

    public static boolean hasCd4Results(String baseEntityId) {
        String sql = "SELECT cd4_sample_id from ec_pmtct_followup\n" +
                "       WHERE entity_id = '" + baseEntityId + "'" +
                "       AND cd4_sample_id IS NOT NULL";


        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "cd4_sample_id");
        List<String> res = readData(sql, dataMap);

        return res != null && res.size() > 0;
    }

    public static String getEacVisitType(String baseEntityID) {
        String sql = "SELECT hvl_collection_date\n" +
                "FROM (SELECT *\n" +
                "      FROM ec_pmtct_followup\n" +
                "      WHERE entity_id = '" + baseEntityID + "'\n" +
                "        AND hvl_sample_id IS NOT NULL\n" +
                "        AND hvl_collection_date IS NOT NULL\n" +
                "      ORDER BY visit_number DESC\n" +
                "      LIMIT 1 OFFSET 2) pm\n" +
                "         INNER JOIN ec_pmtct_hvl_results ephr on pm.base_entity_id = ephr.hvl_pmtct_followup_form_submission_id\n" +
                "WHERE ephr.hvl_result > 1000 AND ephr.hvl_result IS NOT NULL";

        DataMap<Integer> dataMap = cursor -> getCursorIntValue(cursor, "hvl_collection_date");

        List<Integer> res = readData(sql, dataMap);

        if (res.size() > 0 && res.get(0) != null) {
            if (res.size() == 1)
                return Constants.EacVisitTypes.EAC_SECOND_VISIT;
            else
                return Constants.EacVisitTypes.EAC_FIRST_VISIT;
        }
        return Constants.EacVisitTypes.EAC_FIRST_VISIT;
    }

    public static boolean isLiverFunctionTestConducted(String baseEntityID) {
        String sql = "SELECT p.base_entity_id FROM ec_pmtct_registration as p INNER JOIN (SELECT * FROM ec_pmtct_followup ORDER BY visit_number DESC LIMIT 1) as pf on pf.entity_id = p.base_entity_id WHERE (pf.liver_function_test_conducted = 'test_conducted') AND p.base_entity_id = '" + baseEntityID + "'";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "base_entity_id");
        List<String> res = readData(sql, dataMap);

        return res != null && res.size() > 0 && res.get(0) != null;
    }

    public static boolean isLiverFunctionTestResultsFilled(String baseEntityID) {
        String sql = "SELECT p.base_entity_id FROM ec_pmtct_registration as p INNER JOIN (SELECT * FROM ec_pmtct_followup ORDER BY visit_number DESC LIMIT 1) as pf on pf.entity_id = p.base_entity_id WHERE (pf.receive_liver_function_test_results='yes') AND p.base_entity_id = '" + baseEntityID + "'";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "base_entity_id");
        List<String> res = readData(sql, dataMap);

        return res != null && res.size() > 0 && res.get(0) != null;
    }

    public static boolean isRenalFunctionTestConducted(String baseEntityID) {
        String sql = "SELECT p.base_entity_id FROM ec_pmtct_registration as p INNER JOIN (SELECT * FROM ec_pmtct_followup ORDER BY visit_number DESC LIMIT 1) as pf on pf.entity_id = p.base_entity_id WHERE (pf.renal_function_test_conducted = 'test_conducted') AND p.base_entity_id = '" + baseEntityID + "'";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "base_entity_id");
        List<String> res = readData(sql, dataMap);

        return res != null && res.size() > 0 && res.get(0) != null;
    }

    public static boolean isRenalFunctionTestResultsFilled(String baseEntityID) {
        String sql = "SELECT p.base_entity_id FROM ec_pmtct_registration as p INNER JOIN (SELECT * FROM ec_pmtct_followup ORDER BY visit_number DESC LIMIT 1) as pf on pf.entity_id = p.base_entity_id WHERE (pf.receive_renal_function_test_results='yes') AND p.base_entity_id = '" + baseEntityID + "'";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "base_entity_id");
        List<String> res = readData(sql, dataMap);

        return res != null && res.size() > 0 && res.get(0) != null;
    }

    public static boolean isEligibleForSecondEac(String baseEntityID) {
//        String sql = "SELECT hvl_suppression_after_eac_1 FROM ec_pmtct_followup p " +
//                "WHERE p.base_entity_id = '" + baseEntityID + "'";
//
//        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "hvl_suppression_after_eac_1");
//
//        List<String> res = readData(sql, dataMap);
//
//        if (res.size() > 0 && res.get(0) != null) {
//            try {
//                int viralLoad = Integer.parseInt(res.get(0));
//                return viralLoad >= 1000;
//            } catch (Exception e) {
//                return false;
//            }
//        }
        return false;
    }

    public static boolean isSecondEacDone(String baseEntityID) {
//        String sql = "SELECT count(" +
//                "               CASE WHEN eac_month_1 IS NOT NULL AND eac_month_2 IS NOT NULL AND eac_month_3 IS NOT NULL " +
//                "                    AND   eac_month_1 <> 'NULL' AND eac_month_2 <> 'NULL' AND eac_month_3 <> 'NULL' " +
//                "               THEN 0 END)  as count_eac  " +
//                "                   FROM ec_pmtct_followup p " +
//                "                   WHERE p.base_entity_id = '" + baseEntityID + "'";
//
//        DataMap<Integer> dataMap = cursor -> getCursorIntValue(cursor, "count_eac");
//
//        List<Integer> res = readData(sql, dataMap);

        return false;
    }
}
