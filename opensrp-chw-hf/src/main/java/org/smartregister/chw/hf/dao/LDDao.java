package org.smartregister.chw.hf.dao;

import static org.smartregister.chw.hf.utils.Constants.FOCUS.LD_EMERGENCY;

import org.smartregister.chw.ld.domain.MemberObject;
import org.smartregister.chw.ld.util.Constants;
import org.smartregister.dao.AbstractDao;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.util.Utils;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by Kassim Sheghembe on 2022-05-08
 */
public class LDDao extends org.smartregister.chw.ld.dao.LDDao {

    // This can be refactored to the LD Library, more DB definition needed to create a Member Object that reflects LD
    public static org.smartregister.chw.ld.domain.MemberObject getLDMember(String baseEntityId) {
        String sql = "select " +
                "m.base_entity_id , " +
                "m.unique_id , " +
                "m.relational_id , " +
                "m.dob , " +
                "m.first_name , " +
                "m.middle_name , " +
                "m.last_name , " +
                "m.gender , " +
                "m.phone_number , " +
                "m.other_phone_number , " +
                "f.first_name family_name ," +
                "f.primary_caregiver , " +
                "f.family_head , " +
                "f.village_town , " +
                "fh.first_name family_head_first_name , " +
                "fh.middle_name family_head_middle_name , " +
                "fh.last_name family_head_last_name, " +
                "fh.phone_number family_head_phone_number , " +
                "ancr.is_closed anc_is_closed, " +
                "pcg.first_name pcg_first_name , " +
                "pcg.last_name pcg_last_name , " +
                "pcg.middle_name pcg_middle_name , " +
                "pcg.phone_number " +
                "from ec_family_member m " +
                "inner join ec_family f on m.relational_id = f.base_entity_id " +
                "left join ec_family_member fh on fh.base_entity_id = f.family_head " +
                "left join ec_family_member pcg on pcg.base_entity_id = f.primary_caregiver " +
                "left join ec_anc_register ancr on ancr.base_entity_id = m.base_entity_id " +
                "where m.base_entity_id ='" + baseEntityId + "' ";

        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        AbstractDao.DataMap<org.smartregister.chw.ld.domain.MemberObject> dataMap = cursor -> {
            org.smartregister.chw.ld.domain.MemberObject memberObject = new org.smartregister.chw.ld.domain.MemberObject();

            memberObject.setFirstName(getCursorValue(cursor, "first_name", ""));
            memberObject.setMiddleName(getCursorValue(cursor, "middle_name", ""));
            memberObject.setLastName(getCursorValue(cursor, "last_name", ""));
            memberObject.setAddress(getCursorValue(cursor, "village_town"));
            memberObject.setGender(getCursorValue(cursor, "gender"));
            memberObject.setUniqueId(getCursorValue(cursor, "unique_id", ""));
            memberObject.setAge(String.valueOf(Utils.getAgeFromDate(getCursorValue(cursor, "dob"))));
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

    public static String getPmtctTestDate(String baseEntityId) {
        String sql = "SELECT pmtct_test_date FROM " + Constants.TABLES.LD_CONFIRMATION + " WHERE base_entity_id = '" + baseEntityId + "'";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "pmtct_test_date");

        List<String> res = readData(sql, dataMap);
        if (res != null && res.size() > 0)
            return res.get(0);
        return null;
    }

    public static String getHbTestDate(String baseEntityId) {
        String sql = "SELECT hb_test_date FROM " + Constants.TABLES.LD_CONFIRMATION + " WHERE base_entity_id = '" + baseEntityId + "'";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "hb_test_date");

        List<String> res = readData(sql, dataMap);
        if (res != null && res.size() > 0)
            return res.get(0);
        return null;
    }

    public static String getHealthcareProviderNameWhoConductedLastPartographSession(String baseEntityId) {
        String sql = "SELECT name_of_the_health_care_provider FROM " + Constants.TABLES.EC_LD_PARTOGRAPH + " WHERE entity_id = '" + baseEntityId + "' ORDER BY last_interacted_with DESC LIMIT 1";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "name_of_the_health_care_provider");

        List<String> res = readData(sql, dataMap);
        if (res != null && res.size() > 0)
            return res.get(0);
        return null;
    }

    public static Boolean isTheClientReferred(String baseEntityId) {
        AllSharedPreferences allSharedPreferences = org.smartregister.chw.core.utils.Utils.getAllSharedPreferences();
        String anm = allSharedPreferences.fetchRegisteredANM();
        String currentLoaction = allSharedPreferences.fetchUserLocalityId(anm);

        String sql = "SELECT base_entity_id FROM " + Constants.TABLES.LD_CONFIRMATION + " elc " +
                " INNER JOIN task t on elc.base_entity_id = t.for " +
                " WHERE base_entity_id = '" + baseEntityId + "' " +
                " AND t.location = '" + currentLoaction + "' COLLATE NOCASE " +
                " AND t.focus = '" + LD_EMERGENCY + "' COLLATE NOCASE ";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "base_entity_id");

        List<String> res = readData(sql, dataMap);
        if (res != null && res.size() > 0)
            return true;
        return null;
    }

    public static String getSyphilisTest(String baseEntityId) {
        String sql = "SELECT syphilis FROM " + Constants.TABLES.LD_CONFIRMATION + " WHERE base_entity_id = '" + baseEntityId + "'";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "syphilis");

        List<String> res = readData(sql, dataMap);
        if (res != null && res.size() > 0)
            return res.get(0);
        return null;
    }

    public static String getMalariaTest(String baseEntityId) {
        String sql = "SELECT malaria FROM " + Constants.TABLES.LD_CONFIRMATION + " WHERE base_entity_id = '" + baseEntityId + "'";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "malaria");

        List<String> res = readData(sql, dataMap);
        if (res != null && res.size() > 0)
            return res.get(0);
        return null;
    }

    public static String getFundalHeight(String baseEntityId) {
        String sql = "SELECT fundal_height FROM " + Constants.TABLES.EC_LD_GENERAL_EXAMINATION + " WHERE base_entity_id = '" + baseEntityId + "'";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "fundal_height");

        List<String> res = readData(sql, dataMap);
        if (res != null && res.size() > 0)
            return res.get(0);
        return null;
    }

    public static String getFetalLie(String baseEntityId) {
        String sql = "SELECT lie FROM " + Constants.TABLES.EC_LD_GENERAL_EXAMINATION + " WHERE base_entity_id = '" + baseEntityId + "'";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "lie");

        List<String> res = readData(sql, dataMap);
        if (res != null && res.size() > 0)
            return res.get(0);
        return null;
    }

}
