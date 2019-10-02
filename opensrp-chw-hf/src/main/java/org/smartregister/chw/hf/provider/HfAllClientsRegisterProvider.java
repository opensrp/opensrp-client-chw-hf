package org.smartregister.chw.hf.provider;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.opd.configuration.OpdRegisterQueryProviderContract;

public class HfAllClientsRegisterProvider extends OpdRegisterQueryProviderContract {

    @NonNull
    @Override
    public String getObjectIdsQuery(@Nullable String filters) {
        if (TextUtils.isEmpty(filters)) {
            return "SELECT object_id, last_interacted_with FROM (SELECT object_id, last_interacted_with FROM ec_family_member_search WHERE date_removed IS NULL) ORDER BY last_interacted_with DESC ";
        } else {
            return "SELECT object_id, last_interacted_with FROM (SELECT object_id, last_interacted_with FROM ec_family_member_search WHERE date_removed IS NULL) ORDER BY last_interacted_with DESC ";
        }
    }

    @NonNull
    @Override
    public String[] countExecuteQueries(@Nullable String filters) {
        SmartRegisterQueryBuilder sqb = new SmartRegisterQueryBuilder();

        return new String[]{
                sqb.countQueryFts("ec_family_member", null, null, filters),
        };
    }

    @NonNull
    @Override
    public String mainSelectWhereIDsIn() {
        return "";
    }

    @NonNull
    public String getChildRegisterQuery() {
        return "SELECT ec_child.id                      as _id,\n" +
                "       ec_child.relational_id          as relationalid,\n" +
                "       ec_child.first_name,\n" +
                "       ec_child.last_name,\n" +
                "       ec_child.middle_name,\n" +
                "       ec_child.gender,\n" +
                "       ec_family.village_town          as home_address,\n" +
                "       ec_child.dob,\n" +
                "       cast(strftime('%Y-%m-%d %H:%M:%S', 'now') - strftime('%Y-%m-%d %H:%M:%S', ec_child.dob) as int) as age,\n" +
                "       'Child'      AS register_type\n" +
                "FROM ec_child\n" +
                "         inner join ec_family_member on ec_family_member.base_entity_id = ec_child.base_entity_id\n" +
                "         inner join ec_family on ec_family.base_entity_id = ec_family_member.relational_id\n" +
                "WHERE ec_family_member.is_closed = '0' AND ec_family_member.date_removed is null\n" +
                "  AND age > 0\n";
    }


    @NonNull
    public String getANCRegisterQuery() {
        return "SELECT ec_family_member.first_name,\n" +
                "       ec_family_member.middle_name,\n" +
                "       ec_family_member.last_name,\n" +
                "       ec_family_member.gender,\n" +
                "        ec_family_member.dob,\n" +
                "       ec_family_member.base_entity_id,\n" +
                "       ec_family_member.id            as _id,\n" +
                "       'ANC'                          AS register_type,\n" +
                "       ec_family_member.relational_id as relationalid,\n" +
                "       ec_family.village_town         as home_address\n" +
                "FROM ec_anc_register\n" +
                "         inner join ec_family_member on ec_family_member.base_entity_id = ec_anc_register.base_entity_id\n" +
                "         inner join ec_family on ec_family.base_entity_id = ec_family_member.relational_id\n" +
                "where ec_family_member.date_removed is null\n" +
                "  and ec_anc_register.is_closed is 0\n" +
                "\n";
    }

    @NonNull
    public String getPNCRegisterQuery() {
        return "SELECT ec_family_member.first_name,\n" +
                "       ec_family_member.middle_name,\n" +
                "       ec_family_member.last_name,\n" +
                "       ec_family_member.gender,\n" +
                "       ec_family_member.dob,\n" +
                "       ec_family_member.base_entity_id,\n" +
                "       ec_family_member.id            as _id,\n" +
                "       'PNC'                          AS register_type,\n" +
                "       ec_family_member.relational_id as relationalid,\n" +
                "       ec_family.village_town         as home_address\n" +
                "FROM ec_pregnancy_outcome\n" +
                "         inner join ec_family_member on ec_family_member.base_entity_id = ec_pregnancy_outcome.base_entity_id\n" +
                "         inner join ec_family on ec_family.base_entity_id = ec_family_member.relational_id\n" +
                "where ec_family_member.date_removed is null\n" +
                "  and ec_pregnancy_outcome.is_closed is 0 AND ec_pregnancy_outcome.base_entity_id NOT IN (SELECT base_entity_id FROM ec_anc_register WHERE ec_anc_register.is_closed IS 0)\n" +
                "\n";
    }

    @NonNull
    public String getFamilyPlanningRegisterQuery() {
        return "";
    }

    @NonNull
    public String getMalariaRegisterQuery() {
        return "";
    }
}
