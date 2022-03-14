package org.smartregister.chw.hf.dao;

import org.joda.time.DateTime;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.pmtct.domain.MemberObject;
import org.smartregister.dao.AbstractDao;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
        String sql = "SELECT * FROM ec_hei hei\n" +
                "         LEFT JOIN (SELECT * FROM ec_hei_followup WHERE sample_id IS NOT NULL ORDER BY visit_number DESC LIMIT 1) heif\n" +
                "                   on hei.base_entity_id = heif.entity_id\n" +
                "WHERE hei.base_entity_id='" + baseEntityID + "'";

        DataMap<String> dobMap = cursor -> getCursorValue(cursor, "dob");
        DataMap<String> riskCategoryMap = cursor -> getCursorValue(cursor, "risk_category");

        List<String> dobRes = readData(sql, dobMap);
        List<String> riskCategoryRes = readData(sql, riskCategoryMap);

        DateTime dobDateTime = new DateTime(dobRes.get(0));
        Date dob = dobDateTime.toDate();

        int weeks = getElapsedTimeInWeeks(simpleDateFormat.format(dob));
        int months = getElapsedTimeInMonths(simpleDateFormat.format(dob));

        if (months >= 9 && getNextHivTestAge(baseEntityID).equals(Constants.HeiHIVTestAtAge.AT_9_MONTHS)) {
            return true;
        } else if (weeks >= 6 && getNextHivTestAge(baseEntityID).equals(Constants.HeiHIVTestAtAge.AT_6_WEEKS)) {
            return true;
        } else
            return riskCategoryRes.get(0).equals("high") && getNextHivTestAge(baseEntityID).equals(Constants.HeiHIVTestAtAge.AT_BIRTH);
    }

    public static boolean isEligibleForArvPrescriptionForHighRisk(String baseEntityID) {
        String sql = "SELECT * FROM ec_hei hei\n" +
                "         LEFT JOIN (SELECT * FROM ec_hei_followup WHERE prophylaxis_arv_for_high_risk_given IS NOT NULL ORDER BY visit_number DESC LIMIT 1) heif\n" +
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

        if (weeks < 6 && getNextHivTestAge(baseEntityID).equals(Constants.HeiHIVTestAtAge.AT_BIRTH) && riskCategoryRes != null && riskCategoryRes.get(0) != null && riskCategoryRes.get(0).equals("high")) {
            return prophylaxisArvForHighRiskRes == null || prophylaxisArvForHighRiskRes.get(0) == null;
        } else return false;
    }

    public static boolean isEligibleForArvPrescriptionForHighAndLowRisk(String baseEntityID) {
        String sql = "SELECT * FROM ec_hei hei\n" +
                "         LEFT JOIN (SELECT * FROM ec_hei_followup WHERE prophylaxis_arv_for_high_and_low_risk_given IS NOT NULL ORDER BY visit_number DESC LIMIT 1) heif\n" +
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

        if (weeks >= 6 && getNextHivTestAge(baseEntityID).equals(Constants.HeiHIVTestAtAge.AT_6_WEEKS) && riskCategoryRes != null && riskCategoryRes.get(0) != null && riskCategoryRes.get(0).equals("high")) {
            return prophylaxisArvForHighAndLowRiskRes == null || prophylaxisArvForHighAndLowRiskRes.get(0) == null;
        } else
            return weeks < 6 && getNextHivTestAge(baseEntityID).equals(Constants.HeiHIVTestAtAge.AT_BIRTH) && riskCategoryRes != null && riskCategoryRes.get(0) != null && riskCategoryRes.get(0).equals("low");
    }

    public static boolean isEligibleForAntiBodiesHivTest(String baseEntityID) {
        String sql = "SELECT * FROM ec_hei hei\n" +
                "         LEFT JOIN (SELECT * FROM ec_hei_followup WHERE sample_id IS NOT NULL ORDER BY visit_number DESC LIMIT 1) heif\n" +
                "                   on hei.base_entity_id = heif.entity_id\n" +
                "WHERE hei.base_entity_id='" + baseEntityID + "'";

        DataMap<String> map = cursor -> getCursorValue(cursor, "dob");
        List<String> res = readData(sql, map);

        DateTime dobDateTime = new DateTime(res.get(0));
        Date dob = dobDateTime.toDate();

        int months = getElapsedTimeInMonths(simpleDateFormat.format(dob));
        if (months >= 15 && getNextHivTestAge(baseEntityID).equals(Constants.HeiHIVTestAtAge.AT_15_MONTHS)) {
            return true;
        } else
            return months >= 18 && getNextHivTestAge(baseEntityID).equals(Constants.HeiHIVTestAtAge.AT_18_MONTHS);
    }

    public static boolean isEligibleForCtx(String baseEntityID) {
        String sql = "SELECT * FROM ec_hei hei\n" +
                "WHERE hei.base_entity_id='" + baseEntityID + "'";

        DataMap<String> dobMap = cursor -> getCursorValue(cursor, "dob");

        List<String> dobRes = readData(sql, dobMap);

        DateTime dobDateTime = new DateTime(dobRes.get(0));
        Date dob = dobDateTime.toDate();

        int weeks = getElapsedTimeInWeeks(simpleDateFormat.format(dob));
        return weeks >= 6;
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

    private static int getElapsedTimeInWeeks(String startDateString) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        Date startDate = null;
        try {
            startDate = simpleDateFormat.parse(startDateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Date now = new Date(System.currentTimeMillis());

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(now.getTime() - startDate.getTime());
        return c.get(Calendar.WEEK_OF_YEAR);
    }

    public static String getNextHivTestAge(String baseEntityID) {
        String sql = "SELECT test_at_age FROM ec_hei_followup WHERE sample_id IS NOT NULL AND entity_id='" + baseEntityID + "' ORDER BY visit_number DESC LIMIT 1";
        DataMap<String> map = cursor -> getCursorValue(cursor, "test_at_age");
        List<String> res = readData(sql, map);

        if (res != null && res.size() > 0 && res.get(0) != null) {
            String testAt = null;
            switch (res.get(0)) {
                case Constants.HeiHIVTestAtAge.AT_BIRTH:
                    testAt = Constants.HeiHIVTestAtAge.AT_6_WEEKS;
                    break;
                case Constants.HeiHIVTestAtAge.AT_6_WEEKS:
                    testAt = Constants.HeiHIVTestAtAge.AT_9_MONTHS;
                    break;
                case Constants.HeiHIVTestAtAge.AT_9_MONTHS:
                    testAt = Constants.HeiHIVTestAtAge.AT_15_MONTHS;
                    break;
                case Constants.HeiHIVTestAtAge.AT_15_MONTHS:
                    testAt = Constants.HeiHIVTestAtAge.AT_18_MONTHS;
                    break;
            }
            return testAt;
        } else
            return Constants.HeiHIVTestAtAge.AT_BIRTH;

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
}
