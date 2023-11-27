package org.smartregister.chw.hf.dao;

import org.smartregister.chw.fp.dao.FpDao;
import org.smartregister.chw.fp.domain.FpMemberObject;
import org.smartregister.chw.hf.utils.Constants;

import java.util.List;

public class HfFpDao extends FpDao {
    public static FpMemberObject getEcpMember(String baseEntityID) {
        String sql = "select m.base_entity_id , m.unique_id , m.relational_id , m.dob , m.first_name , m.middle_name , m.last_name , m.gender , m.phone_number , m.other_phone_number , f.first_name family_name ,f.primary_caregiver , f.family_head , f.village_town ,fh.first_name family_head_first_name , fh.middle_name family_head_middle_name , fh.last_name family_head_last_name, fh.phone_number family_head_phone_number ,  pcg.first_name pcg_first_name , pcg.last_name pcg_last_name , pcg.middle_name pcg_middle_name , pcg.phone_number  pcg_phone_number , mr.* from ec_family_member m inner join ec_family f on m.relational_id = f.base_entity_id left join " + Constants.TableName.FP_ECP_REGISTER + " mr on mr.base_entity_id = m.base_entity_id left join ec_family_member fh on fh.base_entity_id = f.family_head left join ec_family_member pcg on pcg.base_entity_id = f.primary_caregiver where m.base_entity_id ='" + baseEntityID + "' ";

        DataMap<FpMemberObject> dataMap = cursor -> {
            FpMemberObject fpMemberObject = new FpMemberObject();

            fpMemberObject.setFirstName(getCursorValue(cursor, "first_name", ""));
            fpMemberObject.setMiddleName(getCursorValue(cursor, "middle_name", ""));
            fpMemberObject.setLastName(getCursorValue(cursor, "last_name", ""));
            fpMemberObject.setAddress(getCursorValue(cursor, "village_town"));
            fpMemberObject.setGender(getCursorValue(cursor, "gender"));
            fpMemberObject.setUniqueId(getCursorValue(cursor, "unique_id", ""));
            fpMemberObject.setDob(getCursorValue(cursor, "dob"));
            fpMemberObject.setFamilyBaseEntityId(getCursorValue(cursor, "relational_id", ""));
            fpMemberObject.setRelationalId(getCursorValue(cursor, "relational_id", ""));
            fpMemberObject.setPrimaryCareGiver(getCursorValue(cursor, "primary_caregiver"));
            fpMemberObject.setFamilyName(getCursorValue(cursor, "family_name", ""));
            fpMemberObject.setPhoneNumber(getCursorValue(cursor, "phone_number", ""));
            fpMemberObject.setBaseEntityId(getCursorValue(cursor, "base_entity_id", ""));
            fpMemberObject.setFamilyHead(getCursorValue(cursor, "family_head", ""));
            fpMemberObject.setFamilyHeadPhoneNumber(getCursorValue(cursor, "pcg_phone_number", ""));
            fpMemberObject.setFamilyHeadPhoneNumber(getCursorValue(cursor, "family_head_phone_number", ""));

            String familyHeadName = getCursorValue(cursor, "family_head_first_name", "") + " " + getCursorValue(cursor, "family_head_middle_name", "");

            familyHeadName = (familyHeadName.trim() + " " + getCursorValue(cursor, "family_head_last_name", "")).trim();
            fpMemberObject.setFamilyHeadName(familyHeadName);

            String familyPcgName = getCursorValue(cursor, "pcg_first_name", "") + " " + getCursorValue(cursor, "pcg_middle_name", "");

            familyPcgName = (familyPcgName.trim() + " " + getCursorValue(cursor, "pcg_last_name", "")).trim();
            fpMemberObject.setPrimaryCareGiverName(familyPcgName);

            return fpMemberObject;
        };

        List<FpMemberObject> res = readData(sql, dataMap);
        if (res == null || res.size() != 1) return null;

        return res.get(0);
    }
}
