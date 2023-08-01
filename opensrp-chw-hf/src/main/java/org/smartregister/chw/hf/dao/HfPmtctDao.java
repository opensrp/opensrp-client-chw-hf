package org.smartregister.chw.hf.dao;

import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.smartregister.chw.core.dao.CorePmtctDao;
import org.smartregister.chw.hf.utils.TimeUtils;
import org.smartregister.chw.pmtct.domain.MemberObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;


public class HfPmtctDao extends CorePmtctDao {
    public static boolean isEligibleForEac(String baseEntityID) {
        boolean enrollToEac = isToBeEnrolledToEac(baseEntityID);
        boolean hasEacVisits = hasEacVisits(baseEntityID);
        boolean isEacCompleted = isEacCompleted(baseEntityID);


        if (enrollToEac) {
            if (hasEacVisits) {
                return !isEacCompleted;
            }
            return true;
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
                "WHERE ephr.enroll_to_eac IS NOT NULL AND ephr.enroll_to_eac = 'yes' ";

        String completionSql = "SELECT strftime('%d-%m-%Y', form_submission_timestamp) as completion_date " +
                " FROM ec_pmtct_eac_visit " +
                " WHERE  entity_id = '" + baseEntityID + "'" +
                " AND eac_completion_status = 'complete' " +
                " ORDER BY  form_submission_timestamp DESC " +
                " LIMIT  1";

        SimpleDateFormat dt = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        DataMap<String> hvlCollectionDateMap = cursor -> getCursorValue(cursor, "hvl_collection_date");
        DataMap<String> completionDateMap = cursor -> getCursorValue(cursor, "completion_date");
        List<String> hvlCollectionDateRes = readData(sql, hvlCollectionDateMap);
        List<String> completionDateRes = readData(completionSql, completionDateMap);

        if (hvlCollectionDateRes != null && hvlCollectionDateRes.size() > 0 && hvlCollectionDateRes.get(0) != null) {
            if (completionDateRes != null && completionDateRes.size() > 0 && completionDateRes.get(0) != null) {
                Date completionDate = null;
                Date hvlCollectionDate = null;
                try {
                    completionDate = dt.parse(completionDateRes.get(0));
                    hvlCollectionDate = dt.parse(hvlCollectionDateRes.get(0));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return completionDate != null && completionDate.after(hvlCollectionDate);
            }
            return null;
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
                "   OR CAST(epc4r.cd4_result as INT) < 350\n" +
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
        String sql = "SELECT p.base_entity_id FROM ec_pmtct_registration as p INNER JOIN (SELECT * FROM ec_pmtct_followup WHERE followup_status <> 'lost_to_followup' AND followup_status <> 'transfer_out' AND entity_id = '" + baseEntityID + "' ORDER BY visit_number DESC LIMIT 1) as pf on pf.entity_id = p.base_entity_id WHERE (pf.liver_function_test_conducted = 'test_not_conducted' OR pf.receive_liver_function_test_results='no' OR  pf.renal_function_test_conducted = 'test_not_conducted' OR pf.receive_renal_function_test_results='no')";

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

        Calendar startDateCal = Calendar.getInstance();
        startDateCal.setTimeInMillis(startDate.getTime());
        startDateCal.set(Calendar.DAY_OF_MONTH, 1);


        LocalDate startLocalDate = new LocalDate(startDateCal.get(Calendar.YEAR), startDateCal.get(Calendar.MONTH) + 1, startDateCal.get(Calendar.DAY_OF_MONTH));


        LocalDate now = new LocalDate();

        return Months.monthsBetween(startLocalDate, now).getMonths();
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


    public static boolean isLiverFunctionTestConducted(String baseEntityID) {
        String sql = "SELECT p.base_entity_id FROM ec_pmtct_registration as p INNER JOIN (SELECT * FROM ec_pmtct_followup WHERE followup_status <> 'lost_to_followup' AND followup_status <> 'transfer_out' AND ec_pmtct_followup.entity_id = " + "'" + baseEntityID + "'" + " ORDER BY visit_number DESC LIMIT 1) as pf on pf.entity_id = p.base_entity_id WHERE (pf.liver_function_test_conducted = 'test_conducted') AND p.base_entity_id = '" + baseEntityID + "'";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "base_entity_id");
        List<String> res = readData(sql, dataMap);

        return res != null && res.size() > 0 && res.get(0) != null;
    }

    public static boolean isLiverFunctionTestResultsFilled(String baseEntityID) {
        String sql = "SELECT p.base_entity_id FROM ec_pmtct_registration as p INNER JOIN (SELECT * FROM ec_pmtct_followup WHERE followup_status <> 'lost_to_followup' AND followup_status <> 'transfer_out' AND ec_pmtct_followup.entity_id = " + "'" + baseEntityID + "'" + "  ORDER BY visit_number DESC LIMIT 1) as pf on pf.entity_id = p.base_entity_id WHERE (pf.receive_liver_function_test_results='yes') AND p.base_entity_id = '" + baseEntityID + "'";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "base_entity_id");
        List<String> res = readData(sql, dataMap);

        return res != null && res.size() > 0 && res.get(0) != null;
    }

    public static boolean isRenalFunctionTestConducted(String baseEntityID) {
        String sql = "SELECT p.base_entity_id FROM ec_pmtct_registration as p INNER JOIN (SELECT * FROM ec_pmtct_followup WHERE followup_status <> 'lost_to_followup' AND followup_status <> 'transfer_out' AND ec_pmtct_followup.entity_id = " + "'" + baseEntityID + "'" + "  ORDER BY visit_number DESC LIMIT 1) as pf on pf.entity_id = p.base_entity_id WHERE (pf.renal_function_test_conducted = 'test_conducted') AND p.base_entity_id = '" + baseEntityID + "'";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "base_entity_id");
        List<String> res = readData(sql, dataMap);

        return res != null && res.size() > 0 && res.get(0) != null;
    }

    public static boolean isRenalFunctionTestResultsFilled(String baseEntityID) {
        String sql = "SELECT p.base_entity_id FROM ec_pmtct_registration as p INNER JOIN (SELECT * FROM ec_pmtct_followup WHERE followup_status <> 'lost_to_followup' AND followup_status <> 'transfer_out' AND ec_pmtct_followup.entity_id = " + "'" + baseEntityID + "'" + "  ORDER BY visit_number DESC LIMIT 1) as pf on pf.entity_id = p.base_entity_id WHERE (pf.receive_renal_function_test_results='yes') AND p.base_entity_id = '" + baseEntityID + "'";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "base_entity_id");
        List<String> res = readData(sql, dataMap);

        return res != null && res.size() > 0 && res.get(0) != null;
    }

    public static boolean hasTheClientTransferedOut(String baseEntityID) {
        String sql = "SELECT p.base_entity_id\n" +
                "FROM ec_pmtct_registration as p\n" +
                "         INNER JOIN (SELECT *\n" +
                "                     FROM ec_pmtct_followup\n" +
                "                     WHERE entity_id ='" + baseEntityID + "'" +
                "                     ORDER BY visit_number DESC\n" +
                "                     LIMIT 1) as pf on pf.entity_id = p.base_entity_id\n" +
                "WHERE (pf.followup_status = 'transfer_out')\n" +
                "AND p.base_entity_id = '" + baseEntityID + "'";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "base_entity_id");
        List<String> res = readData(sql, dataMap);

        return res != null && res.size() > 0 && res.get(0) != null;
    }

    public static boolean isTheClientLostToFollowup(String baseEntityID) {
        String sql = "SELECT p.base_entity_id\n" +
                "FROM ec_pmtct_registration as p\n" +
                "         INNER JOIN (SELECT *\n" +
                "                     FROM ec_pmtct_followup\n" +
                "                     WHERE entity_id ='" + baseEntityID + "'" +
                "                     ORDER BY visit_number DESC\n" +
                "                     LIMIT 1) as pf on pf.entity_id = p.base_entity_id\n" +
                "WHERE (pf.followup_status = 'lost_to_followup')\n" +
                "AND p.base_entity_id = '" + baseEntityID + "'";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "base_entity_id");
        List<String> res = readData(sql, dataMap);

        return res != null && res.size() > 0 && res.get(0) != null;
    }

    public static String hasTheClientBeenProvidedWithTpt(String baseEntityID) {
        String sql = "SELECT has_been_provided_with_tpt_before FROM ec_pmtct_followup  WHERE has_been_provided_with_tpt_before IS NOT NULL  AND entity_id = '" + baseEntityID + "' ORDER BY visit_number DESC LIMIT 1";

        DataMap<String> hasBeenProvidedWithTptBeforeDataMap = cursor -> getCursorValue(cursor, "has_been_provided_with_tpt_before");
        List<String> hasBeenProvidedWithTptBeforeRes = readData(sql, hasBeenProvidedWithTptBeforeDataMap);


        if (hasBeenProvidedWithTptBeforeRes != null && hasBeenProvidedWithTptBeforeRes.size() > 0 && hasBeenProvidedWithTptBeforeRes.get(0) != null) {
            return hasBeenProvidedWithTptBeforeRes.get(0);
        } else {
            return null;
        }

    }


    public static boolean hasTheClientBeenProvidedWithTptInPreviousSessions(String baseEntityID) {
        String sql = "SELECT is_client_provided_with_tpt FROM ec_pmtct_followup  WHERE (is_client_provided_with_tpt IS NOT NULL)  AND entity_id = '" + baseEntityID + "' ORDER BY visit_number DESC LIMIT 1";

        DataMap<String> isClientProvidedWithTptDataMap = cursor -> getCursorValue(cursor, "is_client_provided_with_tpt");
        List<String> isClientProvidedWithTptRes = readData(sql, isClientProvidedWithTptDataMap);

        if (isClientProvidedWithTptRes != null && isClientProvidedWithTptRes.size() > 0 && isClientProvidedWithTptRes.get(0) != null) {
            return isClientProvidedWithTptRes.get(0).equals("yes");
        } else {
            return false;
        }
    }

    public static boolean hasTheClientCompletedTpt(String baseEntityID) {
        String sql = "SELECT completed_tpt FROM ec_pmtct_followup  WHERE (completed_tpt IS NOT NULL)  AND entity_id = '" + baseEntityID + "' ORDER BY visit_number DESC LIMIT 1";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "completed_tpt");
        List<String> res = readData(sql, dataMap);

        if (res != null && res.size() > 0 && res.get(0) != null) {
            return res.get(0).equals("yes");
        } else {
            return false;
        }

    }

    public static List<MemberObject> getMembers() {
        String sql = "select m.base_entity_id , m.unique_id , m.relational_id , m.dob , m.first_name , m.middle_name , m.last_name , m.gender , m.phone_number , m.other_phone_number , f.first_name family_name ,f.primary_caregiver , f.family_head , f.village_town ,fh.first_name family_head_first_name , fh.middle_name family_head_middle_name , fh.last_name family_head_last_name, fh.phone_number family_head_phone_number , ancr.is_closed anc_is_closed, pncr.is_closed pnc_is_closed, pcg.first_name pcg_first_name , pcg.last_name pcg_last_name , pcg.middle_name pcg_middle_name , pcg.phone_number  pcg_phone_number , mr.* from ec_family_member m inner join ec_family f on m.relational_id = f.base_entity_id inner join ec_pmtct_registration mr on mr.base_entity_id = m.base_entity_id left join ec_family_member fh on fh.base_entity_id = f.family_head left join ec_family_member pcg on pcg.base_entity_id = f.primary_caregiver left join ec_anc_register ancr on ancr.base_entity_id = m.base_entity_id left join ec_pregnancy_outcome pncr on pncr.base_entity_id = m.base_entity_id where mr.is_closed = 0 ";
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        DataMap<MemberObject> dataMap = cursor -> {
            MemberObject memberObject = new MemberObject();

            memberObject.setFirstName(getCursorValue(cursor, "first_name", ""));
            memberObject.setMiddleName(getCursorValue(cursor, "middle_name", ""));
            memberObject.setLastName(getCursorValue(cursor, "last_name", ""));
            memberObject.setAddress(getCursorValue(cursor, "village_town"));
            memberObject.setGender(getCursorValue(cursor, "gender"));
            memberObject.setUniqueId(getCursorValue(cursor, "unique_id", ""));
            memberObject.setAge(getCursorValue(cursor, "dob"));
            memberObject.setDod(getCursorValue(cursor, "dod", null));
            memberObject.setFamilyBaseEntityId(getCursorValue(cursor, "relational_id", ""));
            memberObject.setRelationalId(getCursorValue(cursor, "relational_id", ""));
            memberObject.setPrimaryCareGiver(getCursorValue(cursor, "primary_caregiver"));
            memberObject.setFamilyName(getCursorValue(cursor, "family_name", ""));
            memberObject.setPhoneNumber(getCursorValue(cursor, "phone_number", ""));
            memberObject.setBaseEntityId(getCursorValue(cursor, "base_entity_id", ""));
            memberObject.setFamilyHead(getCursorValue(cursor, "family_head", ""));
            memberObject.setFamilyHeadPhoneNumber(getCursorValue(cursor, "pcg_phone_number", ""));
            memberObject.setFamilyHeadPhoneNumber(getCursorValue(cursor, "family_head_phone_number", ""));
            memberObject.setAncMember(getCursorValue(cursor, "anc_is_closed", ""));
            memberObject.setPncMember(getCursorValue(cursor, "pnc_is_closed", ""));

            String familyHeadName = getCursorValue(cursor, "family_head_first_name", "") + " "
                    + getCursorValue(cursor, "family_head_middle_name", "");

            familyHeadName =
                    (familyHeadName.trim() + " " + getCursorValue(cursor, "family_head_last_name", "")).trim();
            memberObject.setFamilyHeadName(familyHeadName);

            String familyPcgName = getCursorValue(cursor, "pcg_first_name", "") + " "
                    + getCursorValue(cursor, "pcg_middle_name", "");

            familyPcgName =
                    (familyPcgName.trim() + " " + getCursorValue(cursor, "pcg_last_name", "")).trim();
            memberObject.setPrimaryCareGiverName(familyPcgName);

            return memberObject;
        };

        List<MemberObject> res = readData(sql, dataMap);
        if (res == null || res.size() == 0)
            return null;

        return res;
    }

    public static boolean isNewClient(String baseEntityId) {
        DataMap<String> knownOnArtDataMap = cursor -> getCursorValue(cursor, "known_on_art");
        DataMap<String> pmtctRegisterDateDataMap = cursor -> getCursorValue(cursor, "pmtct_register_date");

        String sql = "SELECT pmtct_register_date, known_on_art FROM ec_pmtct_registration WHERE base_entity_id = '" + baseEntityId + "' AND base_entity_id NOT IN (SELECT entity_id FROM ec_pmtct_followup)";

        List<String> resKnownOnArtRes = readData(sql, knownOnArtDataMap);
        List<String> pmtctRegisterDateRes = readData(sql, pmtctRegisterDateDataMap);
        if (resKnownOnArtRes != null && resKnownOnArtRes.size() > 0) {
            if (resKnownOnArtRes.get(0).equals("yes")) {
                if (pmtctRegisterDateRes != null && pmtctRegisterDateRes.size() > 0) {
                    try {
                        Date pmtctRegisterDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(pmtctRegisterDateRes.get(0));
                        int daysDiff = TimeUtils.getElapsedDays(pmtctRegisterDate);
                        if (daysDiff < 2) {
                            return true;
                        }

                    } catch (Exception e) {
                        Timber.e(e);
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                return true;
            }
        }

        return false;
    }

    public static Date getNextFacilityVisitDate(String baseEntityID) {
        //get next followup visit date
        String sql = "SELECT next_facility_visit_date FROM ec_pmtct_registration " +
                "     WHERE followup_status <> 'lost_to_followup' " +
                "     AND followup_status <> 'transfer_out' AND base_entity_id = '" + baseEntityID + "'";
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "next_facility_visit_date");

        List<String> res = readData(sql, dataMap);
        if (res == null || res.size() != 1 || res.get(0) == null)
            return null;
        Date date = null;
        try {
            date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(res.get(0));
        } catch (ParseException e) {
            e.printStackTrace();
            date = new Date();
        }

        return date;
    }

    public static int getEacSessionNumber(String baseEntityId) {
        String sql = "SELECT eac_visit_session from ec_pmtct_eac_visit" +
                "    WHERE  entity_id = '" + baseEntityId + "'" +
                "ORDER BY  form_submission_timestamp DESC " +
                "LIMIT  1";
        String completionSql = "SELECT eac_completion_status from ec_pmtct_eac_visit" +
                "    WHERE  entity_id = '" + baseEntityId + "'" +
                "ORDER BY  form_submission_timestamp DESC " +
                "LIMIT  1";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "eac_visit_session");
        DataMap<String> completionDataMap = cursor -> getCursorValue(cursor, "eac_completion_status");


        List<String> res = readData(sql, dataMap);
        List<String> completionRes = readData(completionSql, completionDataMap);

        if (completionRes != null && completionRes.size() > 0 && completionRes.get(0) != null && completionRes.get(0).equalsIgnoreCase("complete")) {
            return 1;
        }
        if (res != null && res.size() > 0 && res.get(0) != null) {
            return Integer.parseInt(res.get(0)) + 1;
        }
        return 1;
    }

    private static boolean isToBeEnrolledToEac(String baseEntityId) {
        String sql = "SELECT enroll_to_eac " +
                "FROM ec_pmtct_hvl_results " +
                "WHERE entity_id = '" + baseEntityId + "'" +
                " ORDER BY hvl_result_date DESC" +
                " LIMIT 1";
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "enroll_to_eac");
        List<String> res = readData(sql, dataMap);
        if (res != null && res.size() > 0 && res.get(0) != null) {
            return res.get(0).equalsIgnoreCase("yes");
        }
        return false;
    }

    private static boolean hasEacVisits(String baseEntityId) {
        String sql = "SELECT eac_visit_session " +
                " FROM ec_pmtct_eac_visit " +
                " WHERE entity_id = '" + baseEntityId + "'";
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "eac_visit_session");
        List<String> res = readData(sql, dataMap);
        return res != null && res.size() > 0;
    }

    private static boolean isEacCompleted(String baseEntityId) {
        String completionSql = "SELECT strftime('%d-%m-%Y', form_submission_timestamp) as completion_date " +
                " FROM ec_pmtct_eac_visit " +
                " WHERE  entity_id = '" + baseEntityId + "'" +
                " AND eac_completion_status = 'complete' " +
                " ORDER BY  form_submission_timestamp DESC " +
                " LIMIT  1";

        String hvlResultDateSql = "SELECT hvl_result_date " +
                " FROM ec_pmtct_hvl_results " +
                " WHERE  entity_id = '" + baseEntityId + "'" +
                " ORDER BY  hvl_result_date DESC " +
                " LIMIT  1";
        SimpleDateFormat dt = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        DataMap<String> completionDateDataMap = cursor -> getCursorValue(cursor, "completion_date");
        DataMap<String> hvlResultDateDataMap = cursor -> getCursorValue(cursor, "hvl_result_date");

        List<String> completionDateRes = readData(completionSql, completionDateDataMap);
        List<String> hvlResultDateRes = readData(hvlResultDateSql, hvlResultDateDataMap);

        if (completionDateRes != null && completionDateRes.size() > 0 && completionDateRes.get(0) != null) {
            Date completionDate = null;
            Date hvlResultDate = null;
            try {
                completionDate = dt.parse(completionDateRes.get(0));
                hvlResultDate = dt.parse(hvlResultDateRes.get(0));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return completionDate != null && completionDate.after(hvlResultDate);
        }
        return false;
    }

    public static boolean isAfterEAC(String baseEntityId) {
        String sql = "SELECT enroll_to_eac " +
                "FROM ec_pmtct_hvl_results " +
                "WHERE entity_id = '" + baseEntityId + "'" +
                " ORDER BY hvl_result_date DESC" +
                " LIMIT 1";
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "enroll_to_eac");
        List<String> res = readData(sql, dataMap);
        if (res != null && res.size() > 0 && res.get(0) != null) {
            return res.get(0).equalsIgnoreCase("yes");
        }
        return false;
    }

    public static boolean wasPreviousResultsAfterEAC(String baseEntityId) {
        String sql = "SELECT enroll_to_eac " +
                "FROM ec_pmtct_hvl_results " +
                "WHERE entity_id = '" + baseEntityId + "'" +
                " ORDER BY hvl_result_date DESC" +
                " LIMIT 1,1";
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "enroll_to_eac");
        List<String> res = readData(sql, dataMap);
        if (res != null && res.size() > 0 && res.get(0) != null) {
            return res.get(0).equalsIgnoreCase("yes");
        }
        return false;
    }

    public static Date getDateEACRecorded(String baseEntityId) {
        String sql = "SELECT strftime('%d-%m-%Y', form_submission_timestamp) as record_date " +
                " FROM ec_pmtct_eac_visit " +
                " WHERE entity_id = '" + baseEntityId + "'" +
                " ORDER BY form_submission_timestamp DESC " +
                " LIMIT  1";
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "record_date");

        SimpleDateFormat dt = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        List<String> res = readData(sql, dataMap);
        if (res != null && res.size() > 0 && res.get(0) != null) {
            try {
                return dt.parse(res.get(0));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static boolean isPrescribedArtRegimes(String baseEntityId) {
        String sql = "SELECT prescribed_regimes " +
                " FROM ec_pmtct_followup " +
                " WHERE entity_id = '" + baseEntityId + "'" +
                " ORDER BY visit_number DESC " +
                " LIMIT  1";
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "prescribed_regimes");


        List<String> res = readData(sql, dataMap);
        if (res != null && res.size() > 0 && res.get(0) != null) {
            try {
                return res.get(0).equalsIgnoreCase("yes");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean hasBeenReferredForMotherChampionServices(String baseEntityId) {
        String sql = "SELECT reasons_for_issuing_community_referral FROM ec_mother_champion WHERE reasons_for_issuing_community_referral = 'mother_champion_services' AND entity_id = '" + baseEntityId + "' ";
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "reasons_for_issuing_community_referral");

        List<String> res = readData(sql, dataMap);
        if (res != null && res.size() > 0 && res.get(0) != null) {
            try {
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static String getLinkedMotherChampionLocation(String baseEntityId) {
        String sql = "SELECT mother_champion_location FROM ec_mother_champion WHERE reasons_for_issuing_community_referral = 'mother_champion_services' AND entity_id = '" + baseEntityId + "' ORDER BY last_interacted_with DESC LIMIT 1 ";
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "mother_champion_location");

        List<String> res = readData(sql, dataMap);
        if (res != null && res.size() > 0 && res.get(0) != null) {
            return res.get(0);
        }
        return null;
    }

    public static void deleteEntryFromTableByFormSubmissionId(String tableName, String submissionId) {
        String sql = "delete from " + tableName + " where base_entity_id = '" + submissionId + "'";
        updateDB(sql);
    }
}
