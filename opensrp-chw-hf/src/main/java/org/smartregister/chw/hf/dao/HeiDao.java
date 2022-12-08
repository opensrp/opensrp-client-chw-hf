package org.smartregister.chw.hf.dao;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.joda.time.Weeks;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.pmtct.domain.MemberObject;
import org.smartregister.dao.AbstractDao;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

public class HeiDao extends AbstractDao {
    public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

    public static MemberObject getMember(String baseEntityID) {
        String sql = "select m.base_entity_id , m.unique_id , m.relational_id , m.dob , m.first_name , m.middle_name , m.last_name , m.gender , m.phone_number , m.other_phone_number , f.first_name family_name ,f.primary_caregiver , f.family_head , f.village_town ,fh.first_name family_head_first_name , fh.middle_name family_head_middle_name , fh.last_name family_head_last_name, fh.phone_number family_head_phone_number , ancr.is_closed anc_is_closed, pncr.is_closed pnc_is_closed, pcg.first_name pcg_first_name , pcg.last_name pcg_last_name , pcg.middle_name pcg_middle_name , pcg.phone_number  pcg_phone_number , mr.* from ec_family_member m inner join ec_family f on m.relational_id = f.base_entity_id inner join ec_hei mr on mr.base_entity_id = m.base_entity_id left join ec_family_member fh on fh.base_entity_id = f.family_head left join ec_family_member pcg on pcg.base_entity_id = f.primary_caregiver left join ec_anc_register ancr on ancr.base_entity_id = m.base_entity_id left join ec_pregnancy_outcome pncr on pncr.base_entity_id = m.base_entity_id where m.base_entity_id ='" + baseEntityID + "' ";


        DataMap<MemberObject> dataMap = cursor -> {
            MemberObject memberObject = new MemberObject();

            memberObject.setFirstName(getCursorValue(cursor, "first_name", ""));
            memberObject.setMiddleName(getCursorValue(cursor, "middle_name", ""));
            memberObject.setLastName(getCursorValue(cursor, "last_name", ""));
            memberObject.setAddress(getCursorValue(cursor, "village_town"));
            memberObject.setGender(getCursorValue(cursor, "gender"));
            memberObject.setUniqueId(getCursorValue(cursor, "unique_id", ""));
            memberObject.setAge(getCursorValue(cursor, "dob"));
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
        if (res == null || res.size() != 1)
            return null;

        return res.get(0);
    }

    public static boolean isEligibleForDnaCprHivTest(String baseEntityID) {
        try {
            String riskLevel = getRiskLevel(baseEntityID);
            DateTime dobDateTime = new DateTime(getMember(baseEntityID).getDob());
            int months = getElapsedTimeInMonths(simpleDateFormat.format(dobDateTime.toDate()));
            if (months >= 15) {
                return false;
            }
            int weeks = getElapsedTimeInWeeks(simpleDateFormat.format(dobDateTime.toDate()));
            if (riskLevel != null && riskLevel.equalsIgnoreCase("high_risk")) {
                return true;
            } else return riskLevel != null && riskLevel.equalsIgnoreCase("low_risk") && weeks >= 4;

        } catch (Exception e) {
            Timber.e(e);
            return true;
        }
    }

    public static boolean isEligibleForArvPrescriptionForHighRisk(String baseEntityID) {
        String sql = "SELECT * FROM ec_hei hei\n" +
                "         LEFT JOIN (SELECT * FROM ec_hei_followup WHERE prophylaxis_arv_for_high_risk_given IS NOT NULL AND ec_hei_followup.entity_id ='" + baseEntityID + "'" +
                "           ORDER BY visit_number DESC LIMIT 1) heif\n" +
                "                   on hei.base_entity_id = heif.entity_id\n" +
                "WHERE hei.base_entity_id='" + baseEntityID + "'";

        DataMap<String> dobMap = cursor -> getCursorValue(cursor, "dob");
        DataMap<String> prophylaxisArvForHighRiskMap = cursor -> getCursorValue(cursor, "prophylaxis_arv_for_high_risk_given");
        DataMap<String> riskCategoryMap = cursor -> getCursorValue(cursor, "risk_category");

        List<String> dobRes = readData(sql, dobMap);
        List<String> prophylaxisArvForHighRiskRes = readData(sql, prophylaxisArvForHighRiskMap);
        List<String> riskCategoryRes = readData(sql, riskCategoryMap);

        DateTime dobDateTime = new DateTime(dobRes.get(0));
        Date dob = dobDateTime.toDate();

        int weeks = getElapsedTimeInWeeks(simpleDateFormat.format(dob));

        if (weeks < 4 && getNextHivTestAge(baseEntityID).equals(Constants.HeiHIVTestAtAge.AT_BIRTH) && riskCategoryRes != null && riskCategoryRes.get(0) != null && riskCategoryRes.get(0).equals("high")) {
            return prophylaxisArvForHighRiskRes == null || prophylaxisArvForHighRiskRes.get(0) == null;
        } else return false;
    }


    public static boolean isArvPrescriptionForHighRiskGiven(String baseEntityID) {
        String sql = "SELECT * FROM ec_hei hei\n" +
                "         LEFT JOIN (SELECT * FROM ec_hei_followup WHERE prophylaxis_arv_for_high_risk_given IS NOT NULL AND ec_hei_followup.entity_id ='" + baseEntityID + "'" +
                "           ORDER BY visit_number DESC LIMIT 1) heif\n" +
                "                   on hei.base_entity_id = heif.entity_id\n" +
                "WHERE hei.base_entity_id='" + baseEntityID + "'";

        DataMap<String> prophylaxisArvForHighRiskMap = cursor -> getCursorValue(cursor, "prophylaxis_arv_for_high_risk_given");
        DataMap<String> riskCategoryMap = cursor -> getCursorValue(cursor, "risk_category");

        List<String> prophylaxisArvForHighRiskRes = readData(sql, prophylaxisArvForHighRiskMap);
        List<String> riskCategoryRes = readData(sql, riskCategoryMap);

        if (riskCategoryRes != null && riskCategoryRes.get(0) != null && riskCategoryRes.get(0).equals("high") && prophylaxisArvForHighRiskRes != null && prophylaxisArvForHighRiskRes.get(0) != null) {
            return prophylaxisArvForHighRiskRes.get(0).equalsIgnoreCase("yes");
        } else return false;
    }

    public static boolean isEligibleForArvPrescriptionForHighAndLowRisk(String baseEntityID) {
        String sql = "SELECT * FROM ec_hei hei\n" +
                "         LEFT JOIN (SELECT * FROM ec_hei_followup WHERE prophylaxis_arv_for_high_and_low_risk_given IS NOT NULL AND ec_hei_followup.entity_id ='" + baseEntityID + "'" +
                "           ORDER BY visit_number DESC LIMIT 1) heif\n" +
                "                   on hei.base_entity_id = heif.entity_id\n" +
                "WHERE hei.base_entity_id='" + baseEntityID + "'";

        DataMap<String> dobMap = cursor -> getCursorValue(cursor, "dob");
        DataMap<String> prophylaxisArvForHighAndLowRiskMap = cursor -> getCursorValue(cursor, "prophylaxis_arv_for_high_and_low_risk_given");
        DataMap<String> riskCategoryMap = cursor -> getCursorValue(cursor, "risk_category");
        List<String> riskCategoryRes = readData(sql, riskCategoryMap);

        List<String> dobRes = readData(sql, dobMap);
        List<String> prophylaxisArvForHighAndLowRiskRes = readData(sql, prophylaxisArvForHighAndLowRiskMap);

        DateTime dobDateTime = new DateTime(dobRes.get(0));
        Date dob = dobDateTime.toDate();

        int weeks = getElapsedTimeInWeeks(simpleDateFormat.format(dob));

        if (weeks < 9 && weeks >= 4 && getNextHivTestAge(baseEntityID).equals(Constants.HeiHIVTestAtAge.AT_6_WEEKS) && riskCategoryRes != null && riskCategoryRes.get(0) != null && riskCategoryRes.get(0).equals("high") && isArvPrescriptionForHighRiskGiven(baseEntityID)) {
            return prophylaxisArvForHighAndLowRiskRes == null || prophylaxisArvForHighAndLowRiskRes.get(0) == null;
        } else if (weeks < 4 && getNextHivTestAge(baseEntityID).equals(Constants.HeiHIVTestAtAge.AT_BIRTH) && riskCategoryRes != null && riskCategoryRes.get(0) != null && riskCategoryRes.get(0).equals("low")) {
            return prophylaxisArvForHighAndLowRiskRes == null || prophylaxisArvForHighAndLowRiskRes.get(0) == null;
        } else return false;
    }

    public static boolean isEligibleForAntiBodiesHivTest(String baseEntityID) {
        String sql = "SELECT * FROM ec_hei hei\n" +
                "         LEFT JOIN (SELECT * FROM ec_hei_followup WHERE sample_id IS NOT NULL AND ec_hei_followup.entity_id ='" + baseEntityID + "'" +
                "ORDER BY visit_number DESC LIMIT 1) heif\n" +
                "                   on hei.base_entity_id = heif.entity_id\n" +
                "WHERE hei.base_entity_id='" + baseEntityID + "'";

        DataMap<String> map = cursor -> getCursorValue(cursor, "dob");
        List<String> res = readData(sql, map);

        DateTime dobDateTime = new DateTime(res.get(0));
        Date dob = dobDateTime.toDate();

        int months = getElapsedTimeInMonths(simpleDateFormat.format(dob));
        return months >= 15;
    }

    public static boolean isEligibleForCtx(String baseEntityID) {
        String sql = "SELECT * FROM ec_hei hei\n" +
                "WHERE hei.base_entity_id='" + baseEntityID + "'";

        DataMap<String> dobMap = cursor -> getCursorValue(cursor, "dob");

        List<String> dobRes = readData(sql, dobMap);

        DateTime dobDateTime = new DateTime(dobRes.get(0));
        Date dob = dobDateTime.toDate();

        int weeks = getElapsedTimeInWeeks(simpleDateFormat.format(dob));
        return weeks >= 4;
    }

    public static int getElapsedTimeInMonths(String startDateString) {
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

    private static int getElapsedTimeInWeeks(String startDateString) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        Date startDate = null;
        try {
            startDate = simpleDateFormat.parse(startDateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar startDateCal = Calendar.getInstance();
        startDateCal.setTimeInMillis(startDate.getTime());

        LocalDate startLocalDate = new LocalDate(startDateCal.get(Calendar.YEAR), startDateCal.get(Calendar.MONTH) + 1, startDateCal.get(Calendar.DAY_OF_MONTH));
        LocalDate now = new LocalDate();

        return Weeks.weeksBetween(startLocalDate, now).getWeeks();
    }

    public static String getNextHivTestAge(String baseEntityID) {
        String sql = "SELECT * FROM ec_hei  WHERE base_entity_id='" + baseEntityID + "'";

        DataMap<String> dobMap = cursor -> getCursorValue(cursor, "dob");
        List<String> dobRes = readData(sql, dobMap);

        if (dobRes != null && dobRes.size() > 0 && dobRes.get(0) != null) {
            DateTime dobDateTime = new DateTime(dobRes.get(0));
            Date dob = dobDateTime.toDate();

            int weeks = getElapsedTimeInWeeks(simpleDateFormat.format(dob));
            int months = getElapsedTimeInMonths(simpleDateFormat.format(dob));

            if (months >= 18)
                return Constants.HeiHIVTestAtAge.AT_18_MONTHS;
            else if (months >= 15)
                return Constants.HeiHIVTestAtAge.AT_15_MONTHS;
            else if (months >= 9)
                return Constants.HeiHIVTestAtAge.AT_9_MONTHS;
            else if (weeks >= 4)
                return Constants.HeiHIVTestAtAge.AT_6_WEEKS;
            else return Constants.HeiHIVTestAtAge.AT_BIRTH;
        } else return Constants.HeiHIVTestAtAge.AT_BIRTH;

    }

    public static int getVisitNumber(String baseEntityID) {
        String sql = "SELECT visit_number  FROM ec_hei_followup WHERE entity_id='" + baseEntityID + "' ORDER BY visit_number DESC LIMIT 1";
        DataMap<Integer> map = cursor -> getCursorIntValue(cursor, "visit_number");
        List<Integer> res = readData(sql, map);

        if (res != null && res.size() > 0 && res.get(0) != null) {
            return res.get(0) + 1;
        } else
            return 0;

    }

    public static String getRiskLevel(String baseEntityID) {
        String sql = "SELECT risk_category FROM ec_hei hei\n" +
                "       WHERE hei.base_entity_id='" + baseEntityID + "'" +
                "       AND risk_category IS NOT NULL";

        DataMap<String> riskCategoryMap = cursor -> getCursorValue(cursor, "risk_category");

        List<String> riskCategoryRes = readData(sql, riskCategoryMap);
        if (riskCategoryRes.get(0).equals("high")) {
            return org.smartregister.chw.pmtct.util.Constants.RISK_LEVELS.RISK_HIGH;
        }
        if (riskCategoryRes.get(0).equals("low")) {
            return org.smartregister.chw.pmtct.util.Constants.RISK_LEVELS.RISK_LOW;
        }
        return "";
    }

    public static String getMotherBaseEntityId(String baseEntityID) {
        String sql = "SELECT mother_entity_id FROM ec_hei hei\n" +
                "       WHERE hei.base_entity_id='" + baseEntityID + "'";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "mother_entity_id");

        List<String> res = readData(sql, dataMap);

        return (res != null && res.get(0) != null) ? res.get(0) : "";
    }

    public static String getTestAtAgeForFollowupVisit(String baseEntityID) {
        String sql = "SELECT test_at_age FROM ec_hei_followup \n" +
                "       WHERE base_entity_id='" + baseEntityID + "'";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "test_at_age");

        List<String> res = readData(sql, dataMap);

        return (res != null && res.get(0) != null) ? res.get(0) : null;
    }

    public static String getLatestTestAtAge(String baseEntityID) {
        String sql = "SELECT test_at_age FROM ec_hei_followup \n" +
                "       WHERE entity_id='" + baseEntityID + "'" +
                "           AND test_at_age IS NOT NULL" +
                "           ORDER BY visit_number DESC " +
                "           LIMIT 1";
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "test_at_age");

        List<String> res = readData(sql, dataMap);
        //for low risk will return at birth for test_at_age
        return (res != null && res.size() > 0 && res.get(0) != null) ? res.get(0) : Constants.HeiHIVTestAtAge.AT_BIRTH;
    }

    public static Date getHeiRegisterDate(String baseEntityID) {
        //basically returns back the date of birth of the child
        String sql = "select dob from ec_hei where base_entity_id = '" + baseEntityID + "'";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "dob");

        List<String> res = readData(sql, dataMap);
        if (res == null || res.size() != 1)
            return null;
        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(res.get(0));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static Date getHeiFollowUpVisitDate(String baseEntityID) {
        //for latest followup visit dates
        String sql = "SELECT next_visit_date FROM ec_hei_followup WHERE followup_status <> 'lost_to_followup' AND followup_status <> 'transfer_out' AND entity_id = '" + baseEntityID + "'"
                + "ORDER BY visit_number DESC "
                + "LIMIT 1";
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "next_visit_date");

        List<String> res = readData(sql, dataMap);
        if (res == null || res.size() != 1 || res.get(0) == null)
            return null;
        Date date = null;
        try {
            date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(res.get(0));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date;
    }


    public static boolean hasTheChildTransferedOut(String baseEntityID) {
        String sql = "SELECT p.base_entity_id\n" +
                "FROM ec_hei as p\n" +
                "         INNER JOIN (SELECT *\n" +
                "                     FROM ec_hei_followup\n" +
                "                     WHERE ec_hei_followup.entity_id ='" + baseEntityID + "'" +
                "                     ORDER BY visit_number DESC\n" +
                "                     LIMIT 1) as pf on pf.entity_id = p.base_entity_id\n" +
                "WHERE (pf.followup_status = 'transfer_out')\n" +
                "AND p.base_entity_id = '" + baseEntityID + "'";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "base_entity_id");
        List<String> res = readData(sql, dataMap);

        return res != null && res.size() > 0 && res.get(0) != null;
    }


    public static boolean isTheChildLostToFollowup(String baseEntityID) {
        String sql = "SELECT p.base_entity_id\n" +
                "FROM ec_hei as p\n" +
                "         INNER JOIN (SELECT *\n" +
                "                     FROM ec_hei_followup\n" +
                "                     WHERE ec_hei_followup.entity_id ='" + baseEntityID + "'" +
                "                     ORDER BY visit_number DESC\n" +
                "                     LIMIT 1) as pf on pf.entity_id = p.base_entity_id\n" +
                "WHERE (pf.followup_status = 'lost_to_followup')\n" +
                "AND p.base_entity_id = '" + baseEntityID + "'";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "base_entity_id");
        List<String> res = readData(sql, dataMap);

        return res != null && res.size() > 0 && res.get(0) != null;
    }

    public static boolean hasHeiNumber(String baseEntityId) {
        String sql = "SELECT hei_number FROM ec_hei WHERE base_entity_id = '" + baseEntityId + "'";
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "hei_number");
        List<String> res = readData(sql, dataMap);
        return res != null && res.size() > 0 && res.get(0) != null;
    }

    public static String getHeiNumber(String baseEntityId) {
        String sql = "SELECT hei_number FROM ec_hei WHERE base_entity_id = '" + baseEntityId + "'";
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "hei_number");
        List<String> res = readData(sql, dataMap);
        return res != null && res.size() > 0 && res.get(0) != null ? res.get(0) : null;
    }

    public static boolean hasHivResults(String baseEntityId) {
        String sql = "SELECT sample_id FROM ec_hei_followup WHERE sample_id IS NOT NULL AND entity_id = '" + baseEntityId + "'";
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "sample_id");
        List<String> res = readData(sql, dataMap);
        return res != null && res.size() > 0;
    }

    public static List<MemberObject> getMember() {
        String sql = "select m.base_entity_id , m.unique_id , m.relational_id , m.dob , m.first_name , m.middle_name , m.last_name , m.gender , m.phone_number , m.other_phone_number , f.first_name family_name ,f.primary_caregiver , f.family_head , f.village_town ,fh.first_name family_head_first_name , fh.middle_name family_head_middle_name , fh.last_name family_head_last_name, fh.phone_number family_head_phone_number , ancr.is_closed anc_is_closed, pncr.is_closed pnc_is_closed, pcg.first_name pcg_first_name , pcg.last_name pcg_last_name , pcg.middle_name pcg_middle_name , pcg.phone_number  pcg_phone_number , mr.* from ec_family_member m inner join ec_family f on m.relational_id = f.base_entity_id inner join ec_hei mr on mr.base_entity_id = m.base_entity_id left join ec_family_member fh on fh.base_entity_id = f.family_head left join ec_family_member pcg on pcg.base_entity_id = f.primary_caregiver left join ec_anc_register ancr on ancr.base_entity_id = m.base_entity_id left join ec_pregnancy_outcome pncr on pncr.base_entity_id = m.base_entity_id where mr.is_closed = 0 ";


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

    public static List<MemberObject> getMembersByMotherBaseEntityId(String motherBaseEntityId) {
        String sql = "select m.base_entity_id , m.unique_id , m.relational_id , m.dob , m.first_name , m.middle_name , m.last_name , m.gender , m.phone_number , m.other_phone_number , f.first_name family_name ,f.primary_caregiver , f.family_head , f.village_town ,fh.first_name family_head_first_name , fh.middle_name family_head_middle_name , fh.last_name family_head_last_name, fh.phone_number family_head_phone_number , ancr.is_closed anc_is_closed, pncr.is_closed pnc_is_closed, pcg.first_name pcg_first_name , pcg.last_name pcg_last_name , pcg.middle_name pcg_middle_name , pcg.phone_number  pcg_phone_number , mr.* from ec_family_member m inner join ec_family f on m.relational_id = f.base_entity_id inner join ec_hei mr on mr.base_entity_id = m.base_entity_id left join ec_family_member fh on fh.base_entity_id = f.family_head left join ec_family_member pcg on pcg.base_entity_id = f.primary_caregiver left join ec_anc_register ancr on ancr.base_entity_id = m.base_entity_id left join ec_pregnancy_outcome pncr on pncr.base_entity_id = m.base_entity_id where mr.is_closed = 0 AND mr.mother_entity_id = '" + motherBaseEntityId + "'";


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

    public static void saveAntiBodyTestResults(String baseEntityID, String formSubmissionId, String hivTestResults, String hivTestResultsDate, String ctcNumber) {
        String sql = String.format("INSERT INTO ec_hei_hiv_results (id, entity_id, base_entity_id, hei_followup_form_submission_id, hiv_test_result, hiv_test_result_date, ctc_number) VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s') ON CONFLICT (id) DO UPDATE SET hiv_test_result = '%s', hiv_test_result_date = '%s', ctc_number = '%s'", baseEntityID, baseEntityID, formSubmissionId, formSubmissionId, hivTestResults, hivTestResultsDate, ctcNumber, hivTestResults, hivTestResultsDate, ctcNumber);
        updateDB(sql);
    }
}
