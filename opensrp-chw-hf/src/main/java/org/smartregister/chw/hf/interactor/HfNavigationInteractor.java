package org.smartregister.chw.hf.interactor;

import org.smartregister.chw.core.dao.NavigationDao;
import org.smartregister.chw.core.interactor.NavigationInteractor;
import org.smartregister.chw.core.utils.CoreConstants;

public class HfNavigationInteractor extends NavigationInteractor {
    protected HfNavigationInteractor() {
        super();
    }

    @Override
    protected int getCount(String tableName) {
        if (CoreConstants.TABLE_NAME.FAMILY_MEMBER.equals(tableName.toLowerCase().trim())) {
            String allClients = "SELECT count(*) FROM " +
                    " ec_family_member\n" +
                    "         inner join ec_family on ec_family.base_entity_id = ec_family_member.relational_id\n" +
                    "where ec_family_member.date_removed is null\n" +
                    "  AND ec_family.entity_type = 'ec_independent_client'\n" +
                    "  AND ec_family_member.base_entity_id IN (%s)\n" +
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
                    "    FROM ec_hts_register \n" +
                    "    WHERE ec_hts_register.is_closed is 0\n" +
                    "    AND ec_hts_register.ctc_number is null \n " +
                    "    AND ec_hts_register.chw_referral_service = 'Suspected HIV' \n" +
                    "    AND (ec_hts_register.client_hiv_status_after_testing IS NULL) \n" +
                    ")";
            return NavigationDao.getQueryCount(allClients);
        }
        return super.getCount(tableName);

    }
}
