package org.smartregister.chw.hf.holder;

import android.view.View;
import android.widget.TextView;

import org.smartregister.chw.hf.R;
import org.smartregister.opd.holders.OpdRegisterViewHolder;

public class AllClientsRegisterViewHolder extends OpdRegisterViewHolder {
    public TextView textViewReferralDay;

    public AllClientsRegisterViewHolder(View itemView) {
        super(itemView);
        textViewReferralDay = itemView.findViewById(R.id.text_view_referral_day);
    }

}
