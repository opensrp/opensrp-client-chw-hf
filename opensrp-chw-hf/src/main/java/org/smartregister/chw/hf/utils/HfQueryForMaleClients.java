package org.smartregister.chw.hf.utils;

public interface HfQueryForMaleClients {
    String ALL_MALE_CLIENTS_SELECT_QUERY = "" +
            "/*INDEPENDENT MEMBERS*/\n" +
            "SELECT ec_family_member.first_name,\n" +
            "       ec_family_member.middle_name,\n" +
            "       ec_family_member.last_name,\n" +
            "       ec_family_member.gender,\n" +
            "       ec_family_member.dob,\n" +
            "       ec_family_member.base_entity_id,\n" +
            "       ec_family_member.id                   as _id,\n" +
            "       'Independent'                         AS register_type,\n" +
            "       ec_family_member.relational_id        as relationalid,\n" +
            "       ec_family.village_town                as home_address,\n" +
            "       NULL                                  AS mother_first_name,\n" +
            "       NULL                                  AS mother_last_name,\n" +
            "       NULL                                  AS mother_middle_name,\n" +
            "       ec_family_member.last_interacted_with AS last_interacted_with\n" +
            "FROM ec_family_member\n" +
            "         inner join ec_family on ec_family.base_entity_id = ec_family_member.relational_id\n" +
            "where ec_family_member.date_removed is null\n" +
            "  AND ec_family.entity_type = 'ec_independent_client'\n" +
            "  AND ec_family_member.gender = 'Male' " +
            "  AND (date(ec_family_member.dob, '+15 years') <= date('now')) \n" +
            "  AND ec_family_member.base_entity_id IN (%s)\n" +
            "ORDER BY last_interacted_with DESC;";
}
