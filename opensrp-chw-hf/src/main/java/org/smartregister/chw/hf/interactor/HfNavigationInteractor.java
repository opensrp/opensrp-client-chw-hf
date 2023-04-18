package org.smartregister.chw.hf.interactor;

import org.smartregister.chw.core.dao.NavigationDao;
import org.smartregister.chw.core.interactor.NavigationInteractor;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.Utils;
import org.smartregister.chw.referral.util.Constants;
import org.smartregister.repository.AllSharedPreferences;

public class HfNavigationInteractor extends NavigationInteractor {
    protected HfNavigationInteractor() {
        super();
    }

    @Override
    protected int getCount(String tableName) {
        if (CoreConstants.TABLE_NAME.FAMILY_MEMBER.equals(tableName.toLowerCase().trim())) {
            String allClients = " SELECT SUM(c) as count FROM (" +
                    "/*INDEPENDENT MEMBERS*/\n" +
                    "SELECT COUNT(ec_family_member.base_entity_id) AS c\n" +
                    "FROM ec_family_member\n" +
                    "         inner join ec_family on ec_family.base_entity_id = ec_family_member.relational_id\n" +
                    "where ec_family_member.date_removed is null\n" +
                    "  AND ec_family.entity_type = 'ec_independent_client'\n" +
                    "  AND ec_family_member.base_entity_id IN (%s)\n" +
                    "  AND ec_family_member.base_entity_id NOT IN (\n" +
                    "    SELECT ec_anc_register.base_entity_id AS base_entity_id\n" +
                    "    FROM ec_anc_register where ec_anc_register.is_closed is 0  and ec_anc_register.confirmation_status = 'Confirmed'\n" +
                    "    UNION ALL\n" +
                    "    SELECT ec_pregnancy_outcome.base_entity_id AS base_entity_id\n" +
                    "    FROM ec_pregnancy_outcome where  (ec_pregnancy_outcome.delivery_date is not null AND ec_pregnancy_outcome.is_closed is 0)\n" +
                    "    UNION ALL\n" +
                    "    SELECT ec_child.base_entity_id AS base_entity_id\n" +
                    "    FROM ec_child\n" +
                    "    UNION ALL\n" +
                    "    SELECT ec_malaria_confirmation.base_entity_id AS base_entity_id\n" +
                    "    FROM ec_malaria_confirmation\n" +
                    "    UNION ALL\n" +
                    "    SELECT ec_family_planning.base_entity_id AS base_entity_id\n" +
                    "    FROM ec_family_planning\n" +
                    "    UNION ALL\n" +
                    "    SELECT ec_tb_register.base_entity_id AS base_entity_id\n" +
                    "    FROM ec_tb_register\n" +
                    "    WHERE ec_tb_register.tb_case_closure_date is null\n" +
                    "    UNION ALL\n" +
                    "    SELECT ec_hiv_index_hf.base_entity_id AS base_entity_id\n" +
                    "    FROM ec_hiv_index_hf\n" +
                    "    UNION ALL\n" +
                    "    SELECT ec_hiv_register.base_entity_id AS base_entity_id\n" +
                    "    FROM ec_hiv_register \n" +
                    "    UNION ALL\n" +
                    "    SELECT ec_hts_register.base_entity_id AS base_entity_id\n" +
                    "    FROM ec_hts_register \n"+
                    "    WHERE ec_hts_register.is_closed is 0\n" +
                    "    AND ec_hts_register.ctc_number is null \n " +
                    "    AND ec_hts_register.chw_referral_service = 'Conventional HIV Test' \n"+
                    "    AND (ec_hts_register.client_hiv_status_after_testing IS NULL) \n"+
                    "    UNION ALL\n" +
                    "    SELECT ec_ld_confirmation.base_entity_id AS base_entity_id\n" +
                    "    FROM ec_ld_confirmation \n" +
                    "    WHERE labour_confirmation = 'true' AND is_closed is 0\n" +
                    ")" +
                    "UNION ALL" +
                    "/* HIV REGISTER */\n" +
                    "\n" +
                    "SELECT COUNT(ec_family_member.base_entity_id) AS c\n" +
                    "FROM ec_hiv_register\n" +
                    "         inner join ec_family_member on ec_family_member.base_entity_id = ec_hiv_register.base_entity_id\n" +
                    "         inner join ec_family on ec_family.base_entity_id = ec_family_member.relational_id\n" +
                    "where ec_family_member.date_removed is null\n" +
                    "  and ec_hiv_register.is_closed is 0\n" +
                    " AND (UPPER (ec_hiv_register.client_hiv_status_after_testing) LIKE UPPER('Positive')) \n"+
                    "  AND ec_hiv_register.base_entity_id IN (%s)\n" +
                    "  AND ec_family_member.base_entity_id NOT IN (\n" +
                    "    SELECT ec_anc_register.base_entity_id AS base_entity_id\n" +
                    "    FROM ec_anc_register where ec_anc_register.is_closed is 0\n" +
                    "    UNION ALL\n" +
                    "    SELECT ec_pregnancy_outcome.base_entity_id AS base_entity_id\n" +
                    "    FROM ec_pregnancy_outcome where  (ec_pregnancy_outcome.delivery_date is not null AND ec_pregnancy_outcome.is_closed is 0)\n" +
                    "    UNION ALL\n" +
                    "    SELECT ec_child.base_entity_id AS base_entity_id\n" +
                    "    FROM ec_child\n" +
                    "    UNION ALL\n" +
                    "    SELECT ec_tb_register.base_entity_id AS base_entity_id\n" +
                    "    FROM ec_tb_register\n" +
                    "    WHERE ec_tb_register.tb_case_closure_date is null\n" +
                    ")\n" +
                    "\n" +
                    "UNION ALL" +
                    "/* HTS REGISTER */\n" +
                    "\n" +
                    "SELECT COUNT( ec_family_member.base_entity_id) AS c\n" +
                    "FROM ec_hts_register\n" +
                    "         inner join ec_family_member on ec_family_member.base_entity_id = ec_hts_register.base_entity_id\n" +
                    "         inner join ec_family on ec_family.base_entity_id = ec_family_member.relational_id\n" +
                    "where ec_family_member.date_removed is null\n" +
                    "  and ec_hts_register.is_closed is 0\n" +
                    " AND ec_hts_register.ctc_number is null \n " +
                    " AND ec_hts_register.chw_referral_service = 'Conventional HIV Test' \n"+
                    " AND (ec_hts_register.client_hiv_status_after_testing IS NULL) \n"+
                    "  AND ec_hts_register.base_entity_id IN (%s)\n" +
                    "  AND ec_family_member.base_entity_id NOT IN (\n" +
                    "    SELECT ec_anc_register.base_entity_id AS base_entity_id\n" +
                    "    FROM ec_anc_register\n" +
                    "    UNION ALL\n" +
                    "    SELECT ec_pregnancy_outcome.base_entity_id AS base_entity_id\n" +
                    "    FROM ec_pregnancy_outcome\n" +
                    "    UNION ALL   \n" +
                    "    SELECT ec_hiv_index_hf.base_entity_id AS base_entity_id\n" +
                    "    FROM ec_hiv_index_hf\n" +
                    "    UNION ALL\n" +
                    "    SELECT ec_child.base_entity_id AS base_entity_id\n" +
                    "    FROM ec_child\n" +
                    "    UNION ALL\n" +
                    "    SELECT ec_tb_register.base_entity_id AS base_entity_id\n" +
                    "    FROM ec_tb_register\n" +
                    "    WHERE ec_tb_register.tb_case_closure_date is null\n" +
                    ")\n" +
                    "\n" +
                    "UNION ALL\n" +
                    "/*OTHER FAMILY MEMBERS*/\n" +
                    "SELECT COUNT( ec_family_member.base_entity_id) AS c\n" +
                    "FROM ec_family_member\n" +
                    "         inner join ec_family on ec_family.base_entity_id = ec_family_member.relational_id\n" +
                    "where ec_family_member.date_removed is null\n" +
                    "  AND (ec_family.entity_type = 'ec_family' OR ec_family.entity_type is null)\n" +
                    "  AND ec_family_member.base_entity_id IN (%s)\n" +
                    "  AND ec_family_member.base_entity_id NOT IN (\n" +
                    "    SELECT ec_anc_register.base_entity_id AS base_entity_id\n" +
                    "    FROM ec_anc_register where ec_anc_register.is_closed is 0\n" +
                    "    UNION ALL\n" +
                    "    SELECT ec_pregnancy_outcome.base_entity_id AS base_entity_id\n" +
                    "    FROM ec_pregnancy_outcome where ec_pregnancy_outcome.delivery_date is not null\n" +
                    "    UNION ALL\n" +
                    "    SELECT ec_child.base_entity_id AS base_entity_id\n" +
                    "    FROM ec_child\n" +
                    "    UNION ALL\n" +
                    "    SELECT ec_malaria_confirmation.base_entity_id AS base_entity_id\n" +
                    "    FROM ec_malaria_confirmation\n" +
                    "    UNION ALL\n" +
                    "    SELECT ec_family_planning.base_entity_id AS base_entity_id\n" +
                    "    FROM ec_family_planning\n" +
                    "    UNION ALL\n" +
                    "    SELECT ec_tb_register.base_entity_id AS base_entity_id\n" +
                    "    FROM ec_tb_register\n" +
                    "    WHERE ec_tb_register.tb_case_closure_date is null\n" +
                    "    UNION ALL\n" +
                    "    SELECT ec_hiv_register.base_entity_id AS base_entity_id\n" +
                    "    FROM ec_hiv_register\n" +
                    ")\n" +
                    "UNION ALL\n" +
                    "/* ANC REGISTER */\n" +
                    "SELECT COUNT( ec_family_member.base_entity_id) AS c" +
                    "FROM ec_anc_register\n" +
                    "         inner join ec_family_member on ec_family_member.base_entity_id = ec_anc_register.base_entity_id\n" +
                    "         inner join ec_family on ec_family.base_entity_id = ec_family_member.relational_id\n" +
                    "where ec_family_member.date_removed is null\n" +
                    "  and ec_anc_register.is_closed is 0\n" +
                    "  and ec_anc_register.confirmation_status = 'Confirmed'\n" +
                    "  and ec_anc_register.base_entity_id IN (%s)\n" +
                    "\n" +
                    "UNION ALL\n" +
                    "\n" +
                    "/* PNC REGISTER */\n" +
                    "\n" +
                    "SELECT ec_family_member.first_name               AS first_name,\n" +
                    "       ec_family_member.middle_name              AS middle_name,\n" +
                    "       ec_family_member.last_name                AS last_name,\n" +
                    "       ec_family_member.gender                   AS gender,\n" +
                    "       ec_family_member.dob                      AS dob,\n" +
                    "       ec_family_member.base_entity_id           AS base_entity_id,\n" +
                    "       ec_family_member.id                       as _id,\n" +
                    "       'PNC'                                     AS register_type,\n" +
                    "       ec_family_member.relational_id            as relationalid,\n" +
                    "       ec_family.village_town                    as home_address,\n" +
                    "       ec_pregnancy_outcome.last_interacted_with AS last_interacted_with,\n" +
                    "       NULL                                      AS mother_first_name,\n" +
                    "       NULL                                      AS mother_last_name,\n" +
                    "       NULL                                      AS mother_middle_name\n" +
                    "FROM ec_pregnancy_outcome\n" +
                    "         inner join ec_family_member on ec_family_member.base_entity_id = ec_pregnancy_outcome.base_entity_id\n" +
                    "         inner join ec_family on ec_family.base_entity_id = ec_family_member.relational_id\n" +
                    "where ec_family_member.date_removed is null\n" +
                    "  and ec_pregnancy_outcome.is_closed is 0\n" +
                    "  and ec_pregnancy_outcome.delivery_date is not null\n" +
                    "  AND ec_pregnancy_outcome.base_entity_id NOT IN\n" +
                    "      (SELECT base_entity_id FROM ec_anc_register WHERE ec_anc_register.is_closed IS 0)\n" +
                    "  AND ec_pregnancy_outcome.base_entity_id IN (%s)\n" +
                    "\n" +
                    "UNION ALL\n" +
                    "/* CHILD REGISTER */\n" +
                    "\n" +
                    "SELECT COUNT( ec_family_member.base_entity_id) AS c\n" +
                    "FROM (SELECT ec_child.*,\n" +
                    "             mother.first_name  AS mother_first_name,\n" +
                    "             mother.last_name   AS mother_last_name,\n" +
                    "             mother.middle_name AS mother_middle_name\n" +
                    "      FROM ec_child\n" +
                    "               inner join ec_family on ec_family.base_entity_id = ec_child.relational_id\n" +
                    "               INNER JOIN ec_family_member AS mother ON ec_family.primary_caregiver = mother.base_entity_id\n" +
                    "     ) ec_child\n" +
                    "         inner join ec_family_member on ec_family_member.base_entity_id = ec_child.base_entity_id\n" +
                    "         inner join ec_family on ec_family.base_entity_id = ec_family_member.relational_id\n" +
                    "WHERE ec_family_member.is_closed = '0'\n" +
                    "  AND ec_family_member.date_removed is null\n" +
                    "  AND cast(strftime('%Y-%m-%d %H:%M:%S', 'now') - strftime('%Y-%m-%d %H:%M:%S', ec_child.dob) as int) > 0\n" +
                    "  AND ec_child.base_entity_id IN (%s)\n" +
                    "\n" +
                    "UNION ALL\n" +
                    "/* TB REGISTER */\n" +
                    "\n" +
                    "SELECT COUNT( ec_family_member.base_entity_id) AS c\n" +
                    "FROM ec_tb_register\n" +
                    "         inner join ec_family_member on ec_family_member.base_entity_id = ec_tb_register.base_entity_id\n" +
                    "         inner join ec_family on ec_family.base_entity_id = ec_family_member.relational_id\n" +
                    "where ec_family_member.date_removed is null\n" +
                    "  and ec_tb_register.is_closed is 0\n" +
                    "  and ec_tb_register.tb_case_closure_date is null\n" +
                    "  AND ec_tb_register.base_entity_id IN (%s)\n" +
                    "  AND ec_family_member.base_entity_id NOT IN (\n" +
                    "    SELECT ec_anc_register.base_entity_id AS base_entity_id\n" +
                    "    FROM ec_anc_register\n" +
                    "    UNION ALL\n" +
                    "    SELECT ec_pregnancy_outcome.base_entity_id AS base_entity_id\n" +
                    "    FROM ec_pregnancy_outcome\n" +
                    "    UNION ALL\n" +
                    "    SELECT ec_child.base_entity_id AS base_entity_id\n" +
                    "    FROM ec_child\n" +
                    ")\n" +
                    "\n" +
                    "UNION ALL\n" +
                    "/*ONLY MALARIA PATIENTS*/\n" +
                    "SELECT COUNT( ec_family_member.base_entity_id) AS c\n" +
                    "FROM ec_family_member\n" +
                    "         inner join ec_family on ec_family.base_entity_id = ec_family_member.relational_id\n" +
                    "         inner join ec_malaria_confirmation\n" +
                    "                    on ec_family_member.base_entity_id = ec_malaria_confirmation.base_entity_id\n" +
                    "where ec_family_member.date_removed is null\n" +
                    "  AND ec_family_member.base_entity_id IN (%s)\n" +
                    "  AND ec_family_member.base_entity_id NOT IN (\n" +
                    "    SELECT ec_anc_register.base_entity_id AS base_entity_id\n" +
                    "    FROM ec_anc_register\n" +
                    "    UNION ALL\n" +
                    "    SELECT ec_pregnancy_outcome.base_entity_id AS base_entity_id\n" +
                    "    FROM ec_pregnancy_outcome\n" +
                    "    UNION ALL\n" +
                    "    SELECT ec_child.base_entity_id AS base_entity_id\n" +
                    "    FROM ec_child\n" +
                    "    UNION ALL\n" +
                    "    SELECT ec_family_planning.base_entity_id AS base_entity_id\n" +
                    "    FROM ec_family_planning\n" +
                    "    UNION ALL\n" +
                    "    SELECT ec_tb_register.base_entity_id AS base_entity_id\n" +
                    "    FROM ec_tb_register\n" +
                    "    WHERE ec_tb_register.tb_case_closure_date is null\n" +
                    "    UNION ALL\n" +
                    "    SELECT ec_hiv_register.base_entity_id AS base_entity_id\n" +
                    "    FROM ec_hiv_register\n" +
                    ")\n" +
                    "UNION ALL\n" +
                    "\n" +
                    "/*ONLY FAMILY PLANNING PATIENTS*/\n" +
                    "SELECT COUNT( ec_family_member.base_entity_id) AS c\n" +
                    "FROM ec_family_member\n" +
                    "         inner join ec_family on ec_family.base_entity_id = ec_family_member.relational_id\n" +
                    "         inner join ec_family_planning\n" +
                    "                    on ec_family_member.base_entity_id = ec_family_planning.base_entity_id\n" +
                    "where ec_family_member.date_removed is null\n" +
                    "  AND ec_family_planning.is_closed is 0\n" +
                    "  AND ec_family_member.base_entity_id IN (%s)\n" +
                    "  AND ec_family_member.base_entity_id NOT IN (\n" +
                    "    SELECT ec_anc_register.base_entity_id AS base_entity_id\n" +
                    "    FROM ec_anc_register\n" +
                    "    UNION ALL\n" +
                    "    SELECT ec_pregnancy_outcome.base_entity_id AS base_entity_id\n" +
                    "    FROM ec_pregnancy_outcome\n" +
                    "    UNION ALL\n" +
                    "    SELECT ec_child.base_entity_id AS base_entity_id\n" +
                    "    FROM ec_child\n" +
                    "    UNION ALL\n" +
                    "    SELECT ec_malaria_confirmation.base_entity_id AS base_entity_id\n" +
                    "    FROM ec_malaria_confirmation\n" +
                    "    UNION ALL\n" +
                    "    SELECT ec_tb_register.base_entity_id AS base_entity_id\n" +
                    "    FROM ec_tb_register\n" +
                    "    WHERE ec_tb_register.tb_case_closure_date is null\n" +
                    "    UNION ALL\n" +
                    "    SELECT ec_hiv_register.base_entity_id AS base_entity_id\n" +
                    "    FROM ec_hiv_register\n" +
                    "UNION ALL\n" +
                    "\n" +
                    "/*ONLY LABOUR AND DELIVERY PATIENTS*/\n" +
                    "SELECT ec_family_member.first_name,\n" +
                    "       ec_family_member.middle_name,\n" +
                    "       ec_family_member.last_name,\n" +
                    "       ec_family_member.gender,\n" +
                    "       ec_family_member.dob,\n" +
                    "       ec_family_member.base_entity_id,\n" +
                    "       ec_family_member.id                          as _id,\n" +
                    "       'Labour & Delivery'                        AS register_type,\n" +
                    "       ec_family_member.relational_id               as relationalid,\n" +
                    "       ec_family.village_town                       as home_address,\n" +
                    "       NULL                                         AS mother_first_name,\n" +
                    "       NULL                                         AS mother_last_name,\n" +
                    "       NULL                                         AS mother_middle_name,\n" +
                    "       ec_ld_confirmation.last_interacted_with AS last_interacted_with\n" +
                    "FROM ec_family_member\n" +
                    "         inner join ec_family on ec_family.base_entity_id = ec_family_member.relational_id\n" +
                    "         inner join ec_ld_confirmation\n" +
                    "                    on ec_family_member.base_entity_id = ec_ld_confirmation.base_entity_id\n" +
                    "where ec_family_member.date_removed is null\n" +
                    "  AND ec_ld_confirmation.is_closed is 0\n" +
                    "  AND ec_family_member.base_entity_id IN (%s)\n" +
                    "  AND ec_family_member.base_entity_id NOT IN (\n" +
                    "    SELECT ec_anc_register.base_entity_id AS base_entity_id\n" +
                    "    FROM ec_anc_register where ec_anc_register.is_closed is 0\n" +
                    "    UNION ALL\n" +
                    "    SELECT ec_pregnancy_outcome.base_entity_id AS base_entity_id\n" +
                    "    FROM ec_pregnancy_outcome\n" +
                    "    UNION ALL\n" +
                    "    SELECT ec_child.base_entity_id AS base_entity_id\n" +
                    "    FROM ec_child\n" +
                    "    UNION ALL\n" +
                    "    SELECT ec_malaria_confirmation.base_entity_id AS base_entity_id\n" +
                    "    FROM ec_malaria_confirmation\n" +
                    "    UNION ALL\n" +
                    "    SELECT ec_tb_register.base_entity_id AS base_entity_id\n" +
                    "    FROM ec_tb_register\n" +
                    "    WHERE ec_tb_register.tb_case_closure_date is null)\n" +
                    ")\n" +
                    "ORDER BY last_interacted_with DESC)";

            return NavigationDao.getQueryCount(allClients);
        } else if (CoreConstants.TABLE_NAME.REFERRAL.equals(tableName.toLowerCase().trim())) {
            AllSharedPreferences allSharedPreferences = Utils.getAllSharedPreferences();
            String anm = allSharedPreferences.fetchRegisteredANM();
            String currentLoaction = allSharedPreferences.fetchUserLocalityId(anm);
            String sqlReferral = "select count(*) " +
                    "from " + Constants.Tables.REFERRAL + " p " +
                    "inner join ec_family_member m on p.entity_id = m.base_entity_id COLLATE NOCASE " +
                    "inner join ec_family f on f.base_entity_id = m.relational_id COLLATE NOCASE " +
                    "inner join task t on p.id = t.reason_reference COLLATE NOCASE " +
                    "where m.date_removed is null and t.business_status = '" + CoreConstants.BUSINESS_STATUS.REFERRED + "' " +
                    "AND t.location <> '" + currentLoaction + "' COLLATE NOCASE " +
                    "AND p.chw_referral_service <> 'LTFU' COLLATE NOCASE ";
            return NavigationDao.getQueryCount(sqlReferral);
        }
        return super.getCount(tableName);

    }
}
