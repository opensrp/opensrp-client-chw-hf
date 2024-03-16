package org.smartregister.chw.hf.dao;

import org.smartregister.chw.core.dao.AncDao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;

public class HfAncDao extends AncDao {

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
        DataMap<String> partnerHivDataMap = cursor -> getCursorValue(cursor, "partner_hiv");
        DataMap<String> reasonForNotConductingPartnerHivTestDataMap = cursor -> getCursorValue(cursor, "reason_for_not_conducting_partner_hiv_test");

        String sql = String.format(
                "SELECT partner_hiv, reason_for_not_conducting_partner_hiv_test FROM %s WHERE base_entity_id = '%s' " +
                        "AND partner_hiv is not null " +
                        "AND is_closed = 0",
                "ec_anc_register",
                baseEntityId
        );

        List<String> partnerHivRes = readData(sql, partnerHivDataMap);
        List<String> reasonForNotConductingPartnerHivTestRes = readData(sql, reasonForNotConductingPartnerHivTestDataMap);

        try {
            if (partnerHivRes.size() == 1) {
                if (!partnerHivRes.get(0).equalsIgnoreCase("test_not_conducted"))
                    return !partnerHivRes.get(0).equalsIgnoreCase("test_not_conducted");
                else if (reasonForNotConductingPartnerHivTestRes.size() == 1) {
                    return reasonForNotConductingPartnerHivTestRes.get(0).equalsIgnoreCase("known_on_art");
                }
            }
        } catch (Exception e) {
            Timber.e(e);
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
            if (res.get(0).equalsIgnoreCase("test_not_conducted")) {
                return Integer.parseInt(testNumberRes.get(0)) - 1;
            }
            return Integer.parseInt(testNumberRes.get(0));
        }
        return 0;
    }

    public static int getNextPartnerHivTestNumber(String baseEntityId) {
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
        if (res != null && res.size() > 0 && res.get(0) != null) {
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

    public static String getPartnerOtherStdsStatus(String baseEntityId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "partner_other_stds");

        String sql = String.format(
                "SELECT partner_other_stds FROM %s WHERE base_entity_id = '%s' " +
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
        if (res != null && res.size() > 0 && res.get(0) != null)
            return res.get(0).equalsIgnoreCase("positive") || res.get(0).equalsIgnoreCase("negative");
        return false;
    }

    public static boolean isDewormingGiven(String baseEntityId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "deworming");
        String sql = String.format(
                "SELECT deworming FROM %s WHERE entity_id = '%s' " +
                        "AND is_closed = 0 AND deworming is not null AND deworming <> 'medication_not_given'",
                "ec_anc_followup",
                baseEntityId
        );

        List<String> res = readData(sql, dataMap);
        return res != null && res.size() > 0 && res.get(0) != null;
    }

    //This method is used to obtain previous values during Editing Visits.
    public static boolean wasDewormingGivenPreviously(String baseEntityId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "deworming");
        String sql = String.format(
                "SELECT deworming FROM %s WHERE entity_id = '%s' " +
                        "AND is_closed = 0 AND deworming is not null AND deworming <> 'medication_not_given' ORDER BY visit_date DESC Limit 1,1",
                "ec_anc_followup",
                baseEntityId
        );

        List<String> res = readData(sql, dataMap);
        return res != null && res.size() > 0 && res.get(0) != null;
    }

    //This method is used to obtain previous values during Editing Visits.
    public static boolean isDewormingGivenPreviously(String baseEntityId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "deworming");
        String sql = String.format(
                "SELECT deworming FROM %s WHERE entity_id = '%s' " +
                        "AND is_closed = 0 AND deworming is not null AND deworming <> 'medication_not_given' ORDER BY visit_date DESC Limit 1",
                "ec_anc_followup",
                baseEntityId
        );

        List<String> res = readData(sql, dataMap);
        return res != null && res.size() > 0 && res.get(0) != null;
    }

    public static String malariaLastIptDose(String baseEntityId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "malaria_preventive_therapy");

        String sql = String.format(
                "SELECT malaria_preventive_therapy FROM %s WHERE base_entity_id = '%s' " +
                        "AND is_closed = 0",
                "ec_anc_register",
                baseEntityId
        );

        List<String> res = readData(sql, dataMap);
        if (res != null && res.size() > 0 && res.get(0) != null) {
            return res.get(0);
        }
        return "null";
    }

    public static String malariaIptDosage(String iptDosage, String baseEntityId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, iptDosage);

        String sql = String.format(
                "SELECT %s FROM %s WHERE entity_id = '%s' " +
                        "AND is_closed = 0 AND %s IS NOT NULL ORDER BY visit_date DESC Limit 1",
                iptDosage,
                "ec_anc_followup",
                baseEntityId,
                iptDosage
        );

        List<String> res = readData(sql, dataMap);
        if (res != null && res.size() > 0 && res.get(0) != null) {
            return res.get(0);
        }
        return "null";
    }

    //This method is used to obtain previous Malaria IPT Dosage during Editing Visits.
    public static String previousMalariaIptDosage(String iptDosage, String baseEntityId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, iptDosage);

        String sql = String.format(
                "SELECT %s FROM %s WHERE entity_id = '%s' " +
                        "AND is_closed = 0 AND %s IS NOT NULL ORDER BY visit_date DESC Limit 1,1",
                iptDosage,
                "ec_anc_followup",
                baseEntityId,
                iptDosage
        );

        List<String> res = readData(sql, dataMap);
        if (res != null && res.size() > 0 && res.get(0) != null) {
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
            if (res.get(0).equalsIgnoreCase("test_not_conducted")) {
                return Integer.parseInt(testNumberRes.get(0)) - 1;
            }
            return Integer.parseInt(testNumberRes.get(0));
        }
        return 0;
    }

    public static int getNextHivTestNumber(String baseEntityId) {
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
        if (res != null && res.size() > 0 && res.get(0) != null) {
            return res.get(0);
        }
        return "null";
    }

    public static boolean isClientKnownOnArt(String baseEntityId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "known_on_art");

        String sql = String.format(
                "SELECT  known_on_art\n" +
                        "FROM %s WHERE base_entity_id = '%s' " +
                        "AND known_on_art IS NOT NULL " +
                        "AND is_closed = 0",
                "ec_anc_register",
                baseEntityId
        );

        List<String> res = readData(sql, dataMap);
        return res.size() > 0 && !res.get(0).equalsIgnoreCase("null") && !res.get(0).equalsIgnoreCase("0");
    }

    public static String getClientCtcNumber(String baseEntityId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "ctc_number");

        String sql = String.format(
                "SELECT ctc_number FROM %s WHERE base_entity_id = '%s' " +
                        " AND ctc_number IS NOT NULL" +
                        " AND is_closed = 0",
                "ec_anc_register",
                baseEntityId
        );

        List<String> res = readData(sql, dataMap);
        if (res != null && res.size() > 0 && res.get(0) != null) {
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
        if (res != null && res.size() > 0 && res.get(0) != null) {
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

    public static boolean isLLINProvided(String baseEntityId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "llin_provision");

        String sql = String.format(
                "SELECT llin_provision FROM %s WHERE base_entity_id = '%s' " +
                        "AND is_closed = 0",
                "ec_anc_register",
                baseEntityId
        );

        List<String> res = readData(sql, dataMap);
        if (res.get(0) != null) {
            return res.get(0).equalsIgnoreCase("yes");
        }
        return false;
    }

    public static boolean isEligibleForTtVaccination(String baseEntityId) {
        DataMap<List<String>> dataMap = cursor -> Collections.singletonList(getCursorValue(cursor, "tt_vaccination_type"));

        String sql = "SELECT tt_vaccination_type FROM ec_anc_register WHERE base_entity_id = '" + baseEntityId + "' AND tt_vaccination_type like '%tt5%'";

        List<List<String>> res = readData(sql, dataMap);
        return res == null || res.size() <= 0;
    }

    public static boolean isTransferInClient(String baseEntityId) {
        DataMap<List<String>> dataMap = cursor -> Collections.singletonList(getCursorValue(cursor, "is_transfer_in"));

        String sql = "SELECT is_transfer_in FROM ec_anc_register WHERE base_entity_id = '" + baseEntityId + "' AND is_transfer_in = true";

        List<List<String>> res = readData(sql, dataMap);
        return res != null && res.size() > 0;
    }

    public static String getIptDoses(String baseEntityId) {
        int iptDoses = 0;
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "malaria_preventive_therapy");
        String sql = "SELECT malaria_preventive_therapy FROM ec_anc_register WHERE base_entity_id = '" + baseEntityId + "' AND malaria_preventive_therapy IS NOT NULL AND malaria_preventive_therapy <> '0' ";
        List<String> res = readData(sql, dataMap);
        try {
            if (res != null && res.size() > 0) {
                return res.get(0).split("ipt")[1];
            }
        } catch (Exception e) {
            Timber.e(e);
        }

        return String.valueOf(iptDoses);
    }

    public static String getMalariaTestResults(String baseEntityId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "mRDT_for_malaria");
        String sql = "SELECT mRDT_for_malaria FROM ec_anc_register WHERE base_entity_id = '" + baseEntityId + "' AND mRDT_for_malaria IS NOT NULL";
        List<String> res = readData(sql, dataMap);
        if (res != null && res.size() > 0) {
            return res.get(0);
        }

        return "test_not_conducted";
    }

    public static String getTTDoses(String baseEntityId) {
        DataMap<List<String>> dataMap = cursor -> Collections.singletonList(getCursorValue(cursor, "tt_vaccination_type"));

        String sql = "SELECT tt_vaccination_type FROM ec_anc_register WHERE base_entity_id = '" + baseEntityId + "'";

        List<List<String>> res = readData(sql, dataMap);
        if (res != null && res.size() > 0) {
            return String.valueOf(res.get(0).toString().split(",").length);
        }
        return "";
    }

    public static String getLastMeasuredHB(String baseEntityId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "hb_level");

        String sql = String.format(
                "SELECT hb_level FROM %s WHERE entity_id = '%s' " +
                        "AND is_closed = 0 " +
                        "AND hb_level IS NOT NULL " +
                        "ORDER BY visit_date DESC LIMIT 1 ",
                "ec_anc_followup",
                baseEntityId
        );

        List<String> res = readData(sql, dataMap);
        if (res != null && res.size() > 0) {
            return res.get(0);
        }
        return "";
    }

    public static String getLastMeasuredHBDate(String baseEntityId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "measured_date");

        String sql = "SELECT strftime('%d-%m-%Y', datetime(visit_date / 1000, 'unixepoch')) measured_date " +
                " FROM ec_anc_followup " +
                " WHERE entity_id = '" + baseEntityId + "' " +
                "  AND hb_level IS NOT NULL" +
                "  AND is_closed = 0" +
                " ORDER BY visit_date desc" +
                " LIMIT 1";

        List<String> res = readData(sql, dataMap);
        if (res != null && res.size() > 0) {
            return res.get(0);
        }
        return "";
    }

    public static String getSyphilisTestResult(String baseEntityId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "syphilis");

        String sql = String.format(
                "SELECT syphilis FROM %s WHERE base_entity_id = '%s' " +
                        "AND is_closed = 0 " +
                        "AND syphilis IS NOT NULL ",
                "ec_anc_register",
                baseEntityId);
        List<String> res = readData(sql, dataMap);
        if (res != null && res.size() > 0) {
            return res.get(0);
        }
        return "";
    }


    public static boolean getSyphilisTreatment(String baseEntityId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "syphilis_treatment");

        String sql = String.format(
                "SELECT syphilis_treatment FROM %s WHERE entity_id = '%s' " +
                        "AND is_closed = 0 " +
                        "AND syphilis_treatment IS NOT NULL " +
                        "ORDER BY visit_date DESC LIMIT 1 ",
                "ec_anc_followup",
                baseEntityId
        );

        List<String> res = readData(sql, dataMap);
        if (res != null && res.size() > 0) {
            return res.get(0).equalsIgnoreCase("yes");
        }
        return false;
    }

    public static String getBloodGroup(String baseEntityId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "blood_group");
        String sql = String.format(
                "SELECT blood_group FROM %s WHERE base_entity_id = '%s' " +
                        "AND is_closed = 0 " +
                        "AND blood_group IS NOT NULL ",
                "ec_anc_register",
                baseEntityId);
        List<String> res = readData(sql, dataMap);
        if (res != null && res.size() > 0) {
            return res.get(0);
        }
        return "";
    }

    public static String getRhFactor(String baseEntityId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "rh_factor");
        String sql = String.format(
                "SELECT rh_factor FROM %s WHERE base_entity_id = '%s' " +
                        "AND is_closed = 0 " +
                        "AND rh_factor IS NOT NULL ",
                "ec_anc_register",
                baseEntityId);
        List<String> res = readData(sql, dataMap);
        if (res != null && res.size() > 0) {
            return res.get(0);
        }
        return "";
    }

    public static String getHivTestDate(String baseEntityId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "date_anc_hiv_test");
        String sql = String.format(
                "SELECT date_anc_hiv_test FROM %s WHERE base_entity_id = '%s' " +
                        "AND is_closed = 0 " +
                        "AND date_anc_hiv_test IS NOT NULL ",
                "ec_anc_register",
                baseEntityId);
        List<String> res = readData(sql, dataMap);
        if (res != null && res.size() > 0) {
            return res.get(0);
        }
        return "";
    }

    public static String getParity(String baseEntityId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "parity");
        String sql = String.format(
                "SELECT parity FROM %s WHERE base_entity_id = '%s' " +
                        "AND is_closed = 0 " +
                        "AND parity IS NOT NULL ",
                "ec_anc_register",
                baseEntityId);
        List<String> res = readData(sql, dataMap);
        if (res != null && res.size() > 0) {
            return res.get(0);
        }
        return "";
    }

    public static String getMedicalAndSurgicalHistory(String baseEntityId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "medical_surgical_history");
        String sql = String.format(
                "SELECT medical_surgical_history FROM %s WHERE base_entity_id = '%s' " +
                        "AND is_closed = 0 " +
                        "AND medical_surgical_history IS NOT NULL ",
                "ec_anc_register",
                baseEntityId);
        List<String> res = readData(sql, dataMap);
        if (res != null && res.size() > 0) {
            return res.get(0);
        }
        return "";
    }

    public static String getOtherMedicalAndSurgicalHistory(String baseEntityId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "other_medical_surgical_history");
        String sql = String.format(
                "SELECT other_medical_surgical_history FROM %s WHERE base_entity_id = '%s' " +
                        "AND is_closed = 0 " +
                        "AND other_medical_surgical_history IS NOT NULL ",
                "ec_anc_register",
                baseEntityId);
        List<String> res = readData(sql, dataMap);
        if (res != null && res.size() > 0) {
            return res.get(0);
        }
        return "";
    }

    public static String getNumberOfSurvivingChildren(String baseEntityId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "no_surv_children");
        String sql = String.format(
                "SELECT no_surv_children FROM %s WHERE base_entity_id = '%s' " +
                        "AND is_closed = 0 " +
                        "AND no_surv_children IS NOT NULL ",
                "ec_anc_register",
                baseEntityId);
        List<String> res = readData(sql, dataMap);
        if (res != null && res.size() > 0) {
            return res.get(0);
        }
        return "";
    }

    public static boolean hasNoFollowups(String baseEntityId) {
        DataMap<Integer> dataMap = cursor -> getCursorIntValue(cursor, "count");
        String sql = String.format(
                "SELECT count(*) as count FROM %s WHERE entity_id = '%s' " +
                        "AND is_closed = 0 ",
                "ec_anc_followup",
                baseEntityId);
        List<Integer> res = readData(sql, dataMap);
        return res != null && res.size() > 0;
    }

    public static boolean isBloodGroupTestConducted(String baseEntityId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "blood_group");
        String sql = String.format(
                "SELECT blood_group FROM %s WHERE base_entity_id = '%s' " +
                        "AND is_closed = 0 " +
                        "AND blood_group IS NOT NULL ",
                "ec_anc_register",
                baseEntityId);
        List<String> res = readData(sql, dataMap);
        if (res != null && res.size() > 0) {
            return !res.get(0).equalsIgnoreCase("test_not_conducted");
        }
        return false;
    }

    public static boolean hasReferredForPartnerCommunityFollowup(String baseEntityId) {
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "base_entity_id");
        String sql = String.format(
                "SELECT base_entity_id FROM %s WHERE entity_id = '%s' " +
                        "AND is_closed = 0 ",
                "ec_anc_partner_community_followup",
                baseEntityId);
        List<String> res = readData(sql, dataMap);
        if (res != null && res.size() > 0) {
            return res.size() > 0;
        }
        return false;
    }
}
