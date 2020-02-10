package org.smartregister.chw.hf.configs;

import android.database.Cursor;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.holder.AllClientsRegisterViewHolder;
import org.smartregister.chw.hf.utils.HfReferralUtils;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.opd.configuration.OpdRegisterRowOptions;
import org.smartregister.opd.holders.OpdRegisterViewHolder;
import org.smartregister.view.contract.SmartRegisterClient;

public class AllClientsRegisterRowOptions implements OpdRegisterRowOptions<AllClientsRegisterViewHolder> {

    @Override
    public boolean isDefaultPopulatePatientColumn() {
        return false;
    }

    @Override
    public void populateClientRow(@NonNull Cursor cursor, @NonNull CommonPersonObjectClient commonPersonObjectClient, @NonNull SmartRegisterClient smartRegisterClient, @NonNull OpdRegisterViewHolder opdRegisterViewHolder) {
        if (opdRegisterViewHolder instanceof AllClientsRegisterViewHolder) {
            AllClientsRegisterViewHolder allClientsRegisterViewHolder = (AllClientsRegisterViewHolder) opdRegisterViewHolder;
            String registerType = commonPersonObjectClient.getDetails().get(HfReferralUtils.REGISTER_TYPE);
            HfReferralUtils.displayReferralDay(commonPersonObjectClient, HfReferralUtils.getTaskFocus(registerType), allClientsRegisterViewHolder.textViewReferralDay);
        }
    }

    @Override
    public boolean isCustomViewHolder() {
        return true;
    }

    @Nullable
    @Override
    public AllClientsRegisterViewHolder createCustomViewHolder(@NonNull View parent) {
        return new AllClientsRegisterViewHolder(parent);
    }

    @Override
    public boolean useCustomViewLayout() {
        return true;
    }

    @Override
    public int getCustomViewLayoutId() {
        return R.layout.all_client_register_list_row;
    }
}