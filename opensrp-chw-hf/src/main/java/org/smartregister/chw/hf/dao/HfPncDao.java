package org.smartregister.chw.hf.dao;

import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.core.dao.PNCDao;
import org.smartregister.chw.core.model.ChildModel;

import java.util.List;

public class HfPncDao extends PNCDao {
    public static boolean isChildEligibleForBcg(String baseEntityId) {
        String sql = "SELECT child_bcg_vaccination FROM ec_child WHERE child_bcg_vaccination = 'yes' AND base_entity_id = '" + baseEntityId + "'";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "child_bcg_vaccination");
        List<String> res = readData(sql, dataMap);

        return res == null || res.size() == 0 || res.get(0) == null;
    }

    public static boolean isChildEligibleForOpv0(String baseEntityId) {
        String sql = "SELECT child_opv0_vaccination FROM ec_child WHERE child_opv0_vaccination = 'yes' AND base_entity_id = '" + baseEntityId + "'";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "child_opv0_vaccination");
        List<String> res = readData(sql, dataMap);

        return res == null || res.size() == 0 || res.get(0) == null;
    }

    public static boolean isChildEligibleForHepatitisB(String baseEntityId) {
        String sql = "SELECT child_hepatitis_b_vaccination FROM ec_child WHERE child_hepatitis_b_vaccination = 'yes' AND base_entity_id = '" + baseEntityId + "'";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "child_hepatitis_b_vaccination");
        List<String> res = readData(sql, dataMap);

        return res == null || res.size() == 0 || res.get(0) == null;
    }

    public static boolean isChildEligibleForVitaminK(String baseEntityId) {
        String sql = "SELECT child_vitamin_k_injection FROM ec_child WHERE child_vitamin_k_injection = 'yes' AND base_entity_id = '" + baseEntityId + "'";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "child_vitamin_k_injection");
        List<String> res = readData(sql, dataMap);

        return res == null || res.size() == 0 || res.get(0) == null;
    }

    public static boolean isMotherEligibleForHepB(String baseEntityId) {
        String sql = "SELECT hepatitis_b_vaccination FROM ec_pregnancy_outcome WHERE hepatitis_b_vaccination = 'yes' AND base_entity_id = '" + baseEntityId + "'";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "hepatitis_b_vaccination");
        List<String> res = readData(sql, dataMap);

        return res == null || res.size() == 0 || res.get(0) == null;
    }

    public static boolean isMotherEligibleForTetanus(String baseEntityId) {
        String sql = "SELECT tetanus_vaccination FROM ec_pregnancy_outcome WHERE tetanus_vaccination = 'yes' AND base_entity_id = '" + baseEntityId + "'";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "tetanus_vaccination");
        List<String> res = readData(sql, dataMap);

        return res == null || res.size() == 0 || res.get(0) == null;
    }

    public static boolean isMotherEligibleForHivTest(String baseEntityId) {
        String sql = "SELECT hiv FROM ec_pregnancy_outcome WHERE (hiv = 'test_not_conducted' OR hiv = 'unknown' OR hiv IS NULL) AND base_entity_id = '" + baseEntityId + "'";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "hiv");
        List<String> res = readData(sql, dataMap);

        return res != null && res.size() > 0;
    }

    public static boolean isMotherEligibleForPmtctRegistration(String baseEntityId) {
        String sql = "SELECT hiv FROM ec_pregnancy_outcome WHERE hiv = 'positive' AND base_entity_id NOT IN (SELECT base_entity_id FROM ec_pmtct_registration WHERE is_closed = 0) AND base_entity_id = '" + baseEntityId + "'";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "hiv");
        List<String> res = readData(sql, dataMap);

        return res != null && res.size() > 0 && res.get(0) != null;
    }

    public static List<ChildModel> childrenForPncWoman(String baseEntityId) {
        String sql = String.format("select c.first_name || ' ' || c.middle_name || ' ' || c.last_name as child_name, c.dob , c.first_name, c.base_entity_id " +
                "FROM ec_child c " +
                "INNER JOIN ec_family_member fm on fm.base_entity_id = c.base_entity_id " +
                "WHERE c.mother_entity_id = '" + baseEntityId + "' COLLATE NOCASE " +
                "AND (c.entry_point = 'PNC'  OR  c.entry_point = 'LD') " +
                "AND c.is_closed = 0 " +
                "AND fm.is_closed = 0 " +
                "AND ( date (c.dob, '+49 days') > date()) " +
                "ORDER by c.first_name ASC");

        DataMap<ChildModel> dataMap = cursor ->
                new ChildModel(getCursorValue(cursor, "child_name"), getCursorValue(cursor, "dob"), getCursorValue(cursor, "first_name"), getCursorValue(cursor, "base_entity_id"));

        return readData(sql, dataMap);
    }

    public static List<MemberObject> getPncMembersWithMoreThan42Days() {
        String sql = "select m.base_entity_id,\n" +
                "       m.unique_id,\n" +
                "       m.relational_id,\n" +
                "       m.dob,\n" +
                "       m.first_name,\n" +
                "       m.middle_name,\n" +
                "       m.last_name,\n" +
                "       m.gender,\n" +
                "       m.phone_number,\n" +
                "       m.other_phone_number,\n" +
                "       f.first_name    family_name,\n" +
                "       f.primary_caregiver,\n" +
                "       f.family_head,\n" +
                "       fh.first_name   family_head_first_name,\n" +
                "       fh.middle_name  family_head_middle_name,\n" +
                "       fh.last_name    family_head_last_name,\n" +
                "       fh.phone_number family_head_phone_number,\n" +
                "       f.village_town,\n" +
                "        epo.delivery_date\n" +
                "from ec_family_member m\n" +
                "         inner join ec_family f on m.relational_id = f.base_entity_id\n" +
                "        inner join ec_pregnancy_outcome epo on m.base_entity_id = epo.base_entity_id\n" +
                "         left join ec_family_member fh on fh.base_entity_id = f.family_head\n" +
                "where cast(julianday(datetime('now')) - julianday(datetime(substr(epo.delivery_date, 7,4)\n" +
                "                || '-' || substr(epo.delivery_date, 4,2) || '-' || substr(epo.delivery_date, 1,2))) as integer) >= 43\n" +
                "  AND epo.is_closed = 0 ";

        DataMap<MemberObject> dataMap = cursor -> {
            MemberObject memberObject = new MemberObject();
            memberObject.setLastMenstrualPeriod(getCursorValue(cursor, "last_menstrual_period"));
            memberObject.setChwMemberId(getCursorValue(cursor, "unique_id", ""));
            memberObject.setBaseEntityId(getCursorValue(cursor, "base_entity_id", ""));
            memberObject.setFamilyBaseEntityId(getCursorValue(cursor, "relational_id", ""));
            memberObject.setFamilyHead(getCursorValue(cursor, "family_head", ""));

            String familyHeadName = getCursorValue(cursor, "family_head_first_name", "") + " "
                    + getCursorValue(cursor, "family_head_middle_name", "");

            familyHeadName = (familyHeadName.trim() + " " + getCursorValue(cursor, "family_head_last_name", "")).trim();

            memberObject.setFamilyHeadName(familyHeadName);
            memberObject.setFamilyHeadPhoneNumber(getCursorValue(cursor, "family_head_phone_number", ""));
            memberObject.setPrimaryCareGiver(getCursorValue(cursor, "primary_caregiver"));
            memberObject.setFamilyName(getCursorValue(cursor, "family_name", ""));
            memberObject.setLastContactVisit(getCursorValue(cursor, "last_contact_visit"));
            memberObject.setLastInteractedWith(getCursorValue(cursor, "last_interacted_with"));
            memberObject.setFirstName(getCursorValue(cursor, "first_name", ""));
            memberObject.setMiddleName(getCursorValue(cursor, "middle_name", ""));
            memberObject.setLastName(getCursorValue(cursor, "last_name", ""));
            memberObject.setDob(getCursorValue(cursor, "dob"));
            memberObject.setPhoneNumber(getCursorValue(cursor, "phone_number", ""));
            memberObject.setConfirmedContacts(getCursorIntValue(cursor, "confirmed_visits", 0));
            memberObject.setDateCreated(getCursorValue(cursor, "date_created"));
            memberObject.setAddress(getCursorValue(cursor, "village_town"));
            memberObject.setHasAncCard(getCursorValue(cursor, "has_anc_card", ""));

            return memberObject;
        };
        List<MemberObject> res = readData(sql, dataMap);
        if (res == null || res.size() == 0)
            return null;

        return res;
    }

    public static boolean isChildEligibleForKangaroo(String baseEntityId, String motherBaseEntityId) {
        //child is eligible for kangaroo if weight at birth is less than 2.5kg and if its first visit
        String query = "SELECT weight FROM ec_child WHERE base_entity_id = '" + baseEntityId + "'";

        DataMap<Double> dataMap = cursor -> Double.parseDouble(getCursorValue(cursor, "weight", "0"));
        List<Double> res = readData(query, dataMap);

        if ((res != null && res.size() != 0)) {
            return (res.get(0) < 2.5 && getVisitNumber(motherBaseEntityId) == 0);
        }
        return false;
    }

    public static double getChildMinHeadCircumference(String baseEntityId) {
        //query from child table the head_circumference and return the value
        String query = "SELECT head_circumference FROM ec_child WHERE base_entity_id = '" + baseEntityId + "'";

        DataMap<Double> dataMap = cursor -> Double.parseDouble(getCursorValue(cursor, "head_circumference", "0"));
        List<Double> res = readData(query, dataMap);

        if ((res != null && res.size() != 0)) {
            return res.get(0);
        }
        return 0;
    }


    public static int getVisitNumber(String baseEntityID) {
        String sql = "SELECT visit_number  FROM ec_pnc_followup WHERE entity_id='" + baseEntityID + "' ORDER BY visit_number DESC LIMIT 1";
        DataMap<Integer> map = cursor -> getCursorIntValue(cursor, "visit_number");
        List<Integer> res = readData(sql, map);

        if (res != null && res.size() > 0 && res.get(0) != null) {
            return res.get(0) + 1;
        } else
            return 0;

    }

    public static boolean hasHivAntibodyTestBeenConducted(String baseEntityID) {
        String sql = "SELECT hiv_antibody_test  FROM ec_pnc_child_followup WHERE entity_id='" + baseEntityID + "' AND hiv_antibody_test IS NOT NULL  ORDER BY last_interacted_with DESC LIMIT 1";
        DataMap<String> map = cursor -> getCursorValue(cursor, "hiv_antibody_test");
        List<String> res = readData(sql, map);

        if (res != null && res.size() > 0 && res.get(0) != null) {
            return !res.get(0).equals("test_not_conducted");
        } else
            return false;
    }

    public static boolean isAChildWithoutMother(String baseEntityID) {
        String sql = "SELECT base_entity_id  FROM ec_no_mother_pnc WHERE base_entity_id='" + baseEntityID + "'";
        DataMap<String> map = cursor -> getCursorValue(cursor, "base_entity_id");
        List<String> res = readData(sql, map);

        return res != null && res.size() > 0 && res.get(0) != null;
    }


    public static boolean isMotherEligibleForVitaminA(String baseEntityId) {
        String sql = "SELECT vitamin_a FROM ec_pnc_followup WHERE vitamin_a = 'yes' AND entity_id = '" + baseEntityId + "'";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "vitamin_a");
        List<String> res = readData(sql, dataMap);

        return res == null || res.size() == 0 || res.get(0) == null || res.get(0).equals("no");
    }
}
