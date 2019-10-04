package org.smartregister.chw.hf.provider;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.view.View;

import org.smartregister.chw.hf.holder.AllClientsRegisterViewHolder;
import org.smartregister.chw.hf.utils.HfReferralUtils;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.opd.holders.OpdRegisterViewHolder;
import org.smartregister.opd.provider.OpdRegisterProvider;
import org.smartregister.view.contract.SmartRegisterClient;

public class HfAllClientsRegisterProvider extends OpdRegisterProvider {

    public HfAllClientsRegisterProvider(@NonNull Context context, @NonNull View.OnClickListener onClickListener, @NonNull View.OnClickListener paginationClickListener) {
        super(context, onClickListener, paginationClickListener);
    }

    @Override
    public void getView(Cursor cursor, SmartRegisterClient client, OpdRegisterViewHolder viewHolder) {
        super.getView(cursor, client, viewHolder);
        showLatestClientReferralDay((CommonPersonObjectClient) client, (AllClientsRegisterViewHolder) viewHolder);
    }

    private void showLatestClientReferralDay(CommonPersonObjectClient client, AllClientsRegisterViewHolder viewHolder) {
        String registerType = client.getDetails().get(HfReferralUtils.REGISTER_TYPE);
        HfReferralUtils.displayReferralDay(client, HfReferralUtils.getTaskFocus(registerType), viewHolder.textViewReferralDay);
    }
}
