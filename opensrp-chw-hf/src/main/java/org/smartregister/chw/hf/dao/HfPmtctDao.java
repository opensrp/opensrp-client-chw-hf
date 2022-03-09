package org.smartregister.chw.hf.dao;

import org.smartregister.chw.core.dao.CorePmtctDao;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class HfPmtctDao extends CorePmtctDao {
    public static boolean isEligibleForEac(String baseEntityID) {
//        String sql = "SELECT hvl_suppression FROM ec_pmtct_followup p " +
//                "WHERE p.base_entity_id = '" + baseEntityID + "'";
//
//        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "hvl_suppression");
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

    public static boolean isEligibleForHlvTest(String baseEntityID) {
        Boolean eligible = isEligibleForHlvTestForNewlyRegisteredClientsWithVisitsButNotTestedViralLoad(baseEntityID);
        if (eligible != null) {
            return eligible;
        }

        eligible = isEligibleForHlvTestForNewlyRegisteredClients(baseEntityID);
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
        String newClientsWithNoFollowupVisitsSql = "SELECT known_on_art FROM ec_pmtct_registration p WHERE p.base_entity_id = '" + baseEntityID + "'  AND p.base_entity_id NOT IN (SELECT entity_id FROM ec_pmtct_followup)";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "known_on_art");
        List<String> res = readData(newClientsWithNoFollowupVisitsSql, dataMap);

        if (res.size() > 0 && res.get(0) != null) {
            return res.get(0).equalsIgnoreCase("yes");
        }
        return null;
    }

    public static Boolean isEligibleForHlvTestForNewlyRegisteredClientsWithVisitsButNotTestedViralLoad(String baseEntityID) {
        //Checking eligibility for newly registered PMTCT clients with visits but who have not tested Viral Load
        String newClientsWithFollowupVisitsSql = "SELECT pmtct_register_date FROM ec_pmtct_registration p WHERE p.base_entity_id = '" + baseEntityID + "'  AND p.base_entity_id NOT IN (SELECT entity_id FROM ec_pmtct_followup WHERE hvl_sample_id IS NOT NULL)";

        DataMap<String> registrationDateMap = cursor -> getCursorValue(cursor, "pmtct_register_date");
        List<String> registrationDateRes = readData(newClientsWithFollowupVisitsSql, registrationDateMap);
        if (registrationDateRes.size() > 0 && registrationDateRes.get(0) != null) {
            return getElapsedTimeInMonths(registrationDateRes.get(0)) >= 3;
        }
        return null;
    }

    public static Boolean isEligibleForHlvTestForClientsWithPreviousHvlTests(String baseEntityID) {
        //Checking eligibility for  registered PMTCT clients with previous Viral Load tests
        String clientsWithFollowupVisitsSql =
                "SELECT  hvl_collection_date " +
                        "FROM ec_pmtct_followup epf  " +
                        "WHERE epf.hvl_sample_id IS NOT NULL AND p.base_entity_id = '" + baseEntityID + "' " +
                        "ORDER BY epf.visit_number DESC " +
                        "LIMIT 1";


        DataMap<String> hvlCollectionDateMap = cursor -> getCursorValue(cursor, "hvl_collection_date");
        List<String> hvlCollectionDateRes = readData(clientsWithFollowupVisitsSql, hvlCollectionDateMap);
        if (hvlCollectionDateRes.size() > 0 && hvlCollectionDateRes.get(0) != null) {
            return getElapsedTimeInMonths(hvlCollectionDateRes.get(0)) >= 6;
        }
        return null;
    }

    public static boolean isEligibleForCD4Retest(String baseEntityID) {
        String sql = "SELECT cd4_collection_date FROM ec_pmtct_followup WHERE cd4_collection_date IS NOT NULL AND p.base_entity_id = '" + baseEntityID + "'";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "cd4_collection_date");
        List<String> res = readData(sql, dataMap);

        if (res.size() > 0 && res.get(0) != null) {
            return getElapsedTimeInMonths(res.get(0)) >= 6;
        }
        return false;
    }

    private static int getElapsedTimeInMonths(String startDate) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        Date hvlCollectionDate = null;
        try {
            hvlCollectionDate = simpleDateFormat.parse(startDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Date now = new Date(System.currentTimeMillis());

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(now.getTime() - hvlCollectionDate.getTime());
        return c.get(Calendar.MONTH);
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

    public static boolean isEacFirstDone(String baseEntityID) {
        String sql = "SELECT count(" +
                "           CASE WHEN eac_day_1 IS NOT NULL AND eac_day_2 IS NOT NULL AND eac_day_3 IS NOT NULL " +
                "                  AND eac_day_1 <> 'NULL' AND eac_day_2 <> 'NULL' AND eac_day_3 <> 'NULL' " +
                "           THEN 0 END)  as count_eac  " +
                "               FROM ec_pmtct_followup p " +
                "               WHERE p.base_entity_id = '" + baseEntityID + "'";

        DataMap<Integer> dataMap = cursor -> getCursorIntValue(cursor, "count_eac");

        List<Integer> res = readData(sql, dataMap);

        return res.get(0) == 1;
    }

    public static boolean isSecondEacDone(String baseEntityID) {
        String sql = "SELECT count(" +
                "               CASE WHEN eac_month_1 IS NOT NULL AND eac_month_2 IS NOT NULL AND eac_month_3 IS NOT NULL " +
                "                    AND   eac_month_1 <> 'NULL' AND eac_month_2 <> 'NULL' AND eac_month_3 <> 'NULL' " +
                "               THEN 0 END)  as count_eac  " +
                "                   FROM ec_pmtct_followup p " +
                "                   WHERE p.base_entity_id = '" + baseEntityID + "'";

        DataMap<Integer> dataMap = cursor -> getCursorIntValue(cursor, "count_eac");

        List<Integer> res = readData(sql, dataMap);

        return res.get(0) == 1;
    }
}
