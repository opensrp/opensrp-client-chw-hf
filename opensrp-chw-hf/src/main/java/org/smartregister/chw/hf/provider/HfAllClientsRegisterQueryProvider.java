package org.smartregister.chw.hf.provider;

import org.smartregister.chw.core.provider.CoreAllClientsRegisterQueryProvider;
import org.smartregister.chw.hf.utils.HfQueryConstant;

import androidx.annotation.NonNull;

public class HfAllClientsRegisterQueryProvider extends CoreAllClientsRegisterQueryProvider {
    @NonNull
    @Override
    public String mainSelectWhereIDsIn() {
        return HfQueryConstant.ALL_CLIENTS_SELECT_QUERY;
    }
}
