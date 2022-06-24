package org.smartregister.chw.hf.utils;

import org.smartregister.chw.ld.domain.MemberObject;
import org.smartregister.chw.ld.util.Constants;
import org.smartregister.dao.AbstractDao;
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

    public static String getAdmissionDate(String baseEntityId) {
        String sql = "SELECT admission_date FROM " + org.smartregister.chw.ld.util.Constants.TABLES.LD_CONFIRMATION + " WHERE base_entity_id = '" + baseEntityId + "'";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "admission_date");

        List<String> res = readData(sql, dataMap);
        if (res != null && res.size() > 0)
            return res.get(0);
        return null;
    }

    public static String getAdmissionTime(String baseEntityId) {
        String sql = "SELECT admission_time FROM " + Constants.TABLES.LD_CONFIRMATION + " WHERE base_entity_id = '" + baseEntityId + "'";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "admission_time");

        List<String> res = readData(sql, dataMap);
        if (res != null && res.size() > 0)
            return res.get(0);
        return null;
    }

}
