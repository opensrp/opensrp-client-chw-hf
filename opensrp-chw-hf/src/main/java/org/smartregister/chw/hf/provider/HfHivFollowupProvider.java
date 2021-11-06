package org.smartregister.chw.hf.provider;

import android.content.Context;
import android.database.Cursor;
import android.view.View;

import org.smartregister.chw.hf.utils.HfHivTbFeedbackUtils;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.view.contract.SmartRegisterClient;

import java.util.Set;

public class HfHivFollowupProvider extends HfHivRegisterProvider {

    public HfHivFollowupProvider(Context context, Set visibleColumns, View.OnClickListener onClickListener, View.OnClickListener paginationClickListener) {
        super(context, visibleColumns, onClickListener, paginationClickListener);
    }

    @Override
    public void getView(Cursor cursor, SmartRegisterClient smartRegisterClient, RegisterViewHolder registerViewHolder) {
        super.getView(cursor, smartRegisterClient, registerViewHolder);
        registerViewHolder.getDueWrapper().setVisibility(View.GONE);

        showLatestHivReferralDay((CommonPersonObjectClient) smartRegisterClient, (HfRegisterViewHolder) registerViewHolder);
    }

    private void showLatestHivReferralDay(CommonPersonObjectClient client, HfHivFollowupProvider.HfRegisterViewHolder viewHolder) {
        HfHivTbFeedbackUtils.displayReferralDay(client, viewHolder.textViewReferralDay);
    }

}
