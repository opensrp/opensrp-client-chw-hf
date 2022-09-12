package org.smartregister.chw.hf.provider;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.hf.utils.HfReferralUtils;
import org.smartregister.chw.ld.provider.LDRegisterProvider;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.view.contract.SmartRegisterClient;

import java.util.Set;

public class HfLDRegisterProvider extends LDRegisterProvider {
    public HfLDRegisterProvider(Context context, View.OnClickListener paginationClickListener, View.OnClickListener onClickListener, Set visibleColumns) {
        super(context, paginationClickListener, onClickListener, visibleColumns);
    }

    @Override
    public void getView(Cursor cursor, SmartRegisterClient smartRegisterClient, RegisterViewHolder registerViewHolder) {
        super.getView(cursor, smartRegisterClient, registerViewHolder);
        showLatestAncReferralDay((CommonPersonObjectClient) smartRegisterClient, (HfRegisterViewHolder) registerViewHolder);
    }

    private void showLatestAncReferralDay(CommonPersonObjectClient client, HfRegisterViewHolder viewHolder) {
        HfReferralUtils.displayReferralDay(client, Constants.FOCUS.LD_EMERGENCY, viewHolder.textViewReferralDay);
    }



    @Override
    public RegisterViewHolder createViewHolder(ViewGroup parent) {
        View view = inflater().inflate(org.smartregister.ld.R.layout.ld_register_list_row, parent, false);
        return new HfRegisterViewHolder(view);
    }

    public class HfRegisterViewHolder extends HfLDRegisterProvider.RegisterViewHolder {

        public TextView textViewReferralDay;

        public HfRegisterViewHolder(View itemView) {
            super(itemView);
            textViewReferralDay = itemView.findViewById(R.id.text_view_referral_day);
        }
    }
}
