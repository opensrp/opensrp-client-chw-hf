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
            return "SELECT object_id FROM (SELECT object_id, last_interacted_with FROM ec_child_search WHERE date_removed IS NULL) ORDER BY last_interacted_with";
        } else {
            return "SELECT object_id FROM (SELECT object_id, last_interacted_with FROM ec_child_search WHERE date_removed IS NULL AND phrase MATCH '%s*') ORDER BY last_interacted_with";
        }
    }

    @NonNull
    @Override
    public String[] countExecuteQueries(@Nullable String filters) {
        SmartRegisterQueryBuilder sqb = new SmartRegisterQueryBuilder();

        return new String[]{
                sqb.countQueryFts("ec_child", null, null, filters),
        };
    }

    @NonNull
    @Override
    public String mainSelectWhereIDsIn() {
        return "SELECT ec_child.id as _id, ec_child.relational_id as relationalid, ec_child.first_name, ec_child.last_name, ec_child.middle_name, ec_child.gender, ec_child.dob, 'Child' AS register_type FROM ec_child  WHERE is_closed IS 0 AND base_entity_id IN (%s)";
    }
}
