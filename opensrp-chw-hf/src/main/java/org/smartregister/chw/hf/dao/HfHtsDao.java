package org.smartregister.chw.hf.dao;


import org.smartregister.chw.hiv.domain.HivMemberObject;
import org.smartregister.chw.hiv.util.DBConstants;
import org.smartregister.dao.AbstractDao;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import timber.log.Timber;

public class HfHtsDao extends AbstractDao {

    private static DataMap<HivMemberObject> dataMap;

    public static DataMap<HivMemberObject> getDataMap() {
        if (dataMap == null) {
            dataMap = cursor -> {
                HivMemberObject memberObject = new HivMemberObject(null);
                memberObject.setFirstName(getCursorValue(cursor, DBConstants.Key.FIRST_NAME, ""));
                memberObject.setMiddleName(getCursorValue(cursor, DBConstants.Key.MIDDLE_NAME, ""));
                memberObject.setLastName(getCursorValue(cursor, DBConstants.Key.LAST_NAME, ""));
                memberObject.setAddress(getCursorValue(cursor, DBConstants.Key.VILLAGE_TOWN, ""));
                memberObject.setGender(getCursorValue(cursor, DBConstants.Key.GENDER));
                memberObject.setUniqueId(getCursorValue(cursor, DBConstants.Key.UNIQUE_ID, ""));
                memberObject.setAge(getCursorValue(cursor, DBConstants.Key.DOB));
                memberObject.setFamilyBaseEntityId(getCursorValue(cursor, DBConstants.Key.FAMILY_BASE_ENTITY_ID, ""));
                memberObject.setRelationalId(getCursorValue(cursor, DBConstants.Key.FAMILY_BASE_ENTITY_ID, ""));
                memberObject.setPrimaryCareGiver(getCursorValue(cursor, DBConstants.Key.PRIMARY_CARE_GIVER));
                try {
                    memberObject.setCommunityReferralFormId(getCursorValue(cursor, DBConstants.Key.COMMUNITY_REFERRAL_FORM_ID, ""));
                } catch (Exception e) {
                    Timber.e(e);
                }
                memberObject.setFamilyName(getCursorValue(cursor, DBConstants.Key.FAMILY_NAME, ""));
                memberObject.setPhoneNumber(getCursorValue(cursor, DBConstants.Key.PHONE_NUMBER, ""));
                memberObject.setBaseEntityId(getCursorValue(cursor, DBConstants.Key.BASE_ENTITY_ID, ""));
                memberObject.setFamilyHead(getCursorValue(cursor, DBConstants.Key.FAMILY_HEAD, ""));
                memberObject.setFamilyHeadPhoneNumber(getCursorValue(cursor, DBConstants.Key.FAMILY_HEAD_PHONE_NUMBER, ""));
                memberObject.setCtcNumber(getCursorValue(cursor, DBConstants.Key.CTC_NUMBER, ""));
                memberObject.setCbhsNumber(getCursorValue(cursor, DBConstants.Key.CBHS_NUMBER, ""));
                memberObject.setTbNumber(getCursorValue(cursor, DBConstants.Key.TB_NUMBER, ""));
                memberObject.setMatNumber(getCursorValue(cursor, DBConstants.Key.MAT_NUMBER, ""));
                memberObject.setRchNumber(getCursorValue(cursor, DBConstants.Key.RCH_NUMBER, ""));
                memberObject.setClientHivStatusDuringRegistration(getCursorValue(
                        cursor,
                        DBConstants.Key.CLIENT_HIV_STATUS_DURING_REGISTRATION,
                        ""
                ));

                memberObject.setClientHivStatusAfterTesting(getCursorValue(cursor, DBConstants.Key.CLIENT_HIV_STATUS_AFTER_TESTING, ""));

                memberObject.setHivRegistrationDate(
                        new Date(
                                new BigDecimal(
                                        getCursorValue(
                                                cursor,
                                                DBConstants.Key.HIV_REGISTRATION_DATE, "0"
                                        )
                                ).longValue()
                        ));

                memberObject.setHivCommunityReferralDate(
                        new Date(
                                new BigDecimal(
                                        getCursorValue(cursor, DBConstants.Key.HIV_COMMUNITY_REFERRAL_DATE, "0")
                                ).longValue()
                        ));

                memberObject.setLastFacilityVisitDate(
                        new Date(
                                new BigDecimal(
                                        getCursorValue(cursor, DBConstants.Key.LAST_FACILITY_VISIT_DATE, "0")
                                ).longValue()
                        ));

                memberObject.setReasonsForIssuingCommunityFollowupReferral(getCursorValue(cursor, DBConstants.Key.REASONS_FOR_ISSUING_COMMUNITY_REFERRAL, ""));
                memberObject.setComment(getCursorValue(cursor, DBConstants.Key.COMMENTS, ""));
                memberObject.setClientFollowupStatus(getCursorValue(cursor, DBConstants.Key.CLIENT_FOLLOWUP_STATUS, ""));
                memberObject.setClosed(getCursorIntValue(cursor, DBConstants.Key.IS_CLOSED, 0) == 1);

                String familyHeadName = getCursorValue(cursor, "family_head_first_name", "") + " " + getCursorValue(cursor, "family_head_middle_name", "");

                familyHeadName = familyHeadName.trim() + getCursorValue(cursor, "family_head_last_name", "").trim();
                memberObject.setFamilyHead(familyHeadName);

                String entityType = getCursorValue(cursor, "entity_type", "");
                String familyPcgName = "";
                if (entityType.equals("ec_independent_client")) {
                    familyPcgName = getCursorValue(cursor, "primary_caregiver_name", "");
                    memberObject.setPrimaryCareGiverPhoneNumber(getCursorValue(cursor, DBConstants.Key.OTHER_PHONE_NUMBER, ""));
                } else {
                    familyPcgName = (getCursorValue(cursor, "pcg_first_name", "") + " "
                            + getCursorValue(cursor, "pcg_middle_name", ""));
                    familyPcgName =
                            (familyPcgName.trim() + " " + getCursorValue(
                                    cursor,
                                    "pcg_last_name",
                                    ""
                            )).trim();
                    memberObject.setPrimaryCareGiverPhoneNumber(
                            getCursorValue(cursor, DBConstants.Key.PRIMARY_CARE_GIVER_PHONE_NUMBER, ""));
                }

                memberObject.setPrimaryCareGiver(familyPcgName);
                memberObject.setFamilyMemberEntityType(getCursorValue(cursor, DBConstants.Key.FAMILY_MEMBER_ENTITY_TYPE, ""));
                return memberObject;
            };

        }
        return dataMap;
    }

    public static HivMemberObject getMember(String baseEntityId) {


        String sql = String.format(
                " select m.base_entity_id , m.unique_id , m.relational_id as family_base_entity_id , m.dob , m.first_name , " +
                        " m.middle_name , m.last_name , m.gender , m.phone_number , m.other_phone_number , m.entity_type, m.has_primary_caregiver, m.has_primary_caregiver, m.primary_caregiver_name, " +
                        " f.first_name family_name ,f.primary_caregiver , f.family_head , f.village_town , " +
                        " fh.first_name family_head_first_name , fh.middle_name family_head_middle_name , " +
                        " fh.last_name family_head_last_name, fh.phone_number family_head_phone_number, " +
                        " pcg.first_name pcg_first_name , pcg.last_name pcg_last_name , pcg.middle_name pcg_middle_name , " +
                        " pcg.phone_number  pcg_phone_number , mr.* " +
                        " from ec_family_member m " +
                        " inner join ec_family f on m.relational_id = f.base_entity_id " +
                        " left join ec_hts_register mr on mr.base_entity_id = m.base_entity_id " +
                        " left join ec_family_member fh on fh.base_entity_id = f.family_head " +
                        " left join ec_family_member pcg on pcg.base_entity_id = f.primary_caregiver " +
                        " where m.base_entity_id ='%s' ",
                baseEntityId
        );

        List<HivMemberObject> res = readData(sql, getDataMap());

        return (res == null || res.size() == 0) ? null : res.get(0);
    }
}
