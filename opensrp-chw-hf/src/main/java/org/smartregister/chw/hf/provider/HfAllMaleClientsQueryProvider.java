package org.smartregister.chw.hf.provider;

import androidx.annotation.NonNull;

import org.smartregister.chw.core.provider.CoreAllClientsRegisterQueryProvider;
import org.smartregister.chw.hf.utils.HfQueryForMaleClients;

public class HfAllMaleClientsQueryProvider extends CoreAllClientsRegisterQueryProvider {
    @NonNull
    @Override
    public String mainSelectWhereIDsIn() {
        return HfQueryForMaleClients.ALL_MALE_CLIENTS_SELECT_QUERY;
    }
}
