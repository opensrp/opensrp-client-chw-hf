package org.smartregister.chw.hf.provider;

import androidx.annotation.NonNull;

import org.smartregister.chw.core.provider.CoreAllClientsRegisterQueryProvider;
import org.smartregister.chw.hf.utils.HfQueryConstant;

public class HfAllClientsRegisterQueryProvider extends CoreAllClientsRegisterQueryProvider {
    @NonNull
    @Override
    public String mainSelectWhereIDsIn() {
        return HfQueryConstant.ALL_CLIENTS_SELECT_QUERY;
    }
}
