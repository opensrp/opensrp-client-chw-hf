package org.smartregister.chw.hf.provider;

import org.smartregister.chw.core.provider.CoreAllClientsRegisterQueryProvider;
import org.smartregister.chw.hf.utils.HfQueryForMaleClients;

import androidx.annotation.NonNull;

public class HfAllMaleClientsQueryProvider extends CoreAllClientsRegisterQueryProvider {
    @NonNull
    @Override
    public String mainSelectWhereIDsIn() {
        return HfQueryForMaleClients.ALL_MALE_CLIENTS_SELECT_QUERY;
    }
}
