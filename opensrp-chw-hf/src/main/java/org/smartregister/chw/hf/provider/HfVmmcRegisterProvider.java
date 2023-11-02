package org.smartregister.chw.hf.provider;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.smartregister.chw.core.provider.ChwVmmcRegisterProvider;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.hf.utils.HfReferralUtils;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.view.contract.SmartRegisterClient;

import java.util.Set;

public class HfVmmcRegisterProvider extends ChwVmmcRegisterProvider {
    private final LayoutInflater inflater;

    public HfVmmcRegisterProvider(Context context, View.OnClickListener paginationClickListener, View.OnClickListener onClickListener, Set visibleColumns) {
        super(context, paginationClickListener, onClickListener, visibleColumns);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public void getView(Cursor cursor, SmartRegisterClient client, RegisterViewHolder viewHolder) {
        super.getView(cursor, client, viewHolder);
        showLatestVmmcReferralDay((CommonPersonObjectClient) client, (HfVmmcRegisterViewHolder) viewHolder);
    }

    private void showLatestVmmcReferralDay(CommonPersonObjectClient client, HfVmmcRegisterViewHolder viewHolder) {
        HfReferralUtils.displayReferralDay(client, Constants.FOCUS.VMMC_REFERRALS, viewHolder.textViewReferralDay);
    }

    @Override
    public RegisterViewHolder createViewHolder(ViewGroup parent) {
        View view = inflater.inflate(R.layout.malaria_register_list_row, parent, false);
        return new HfVmmcRegisterViewHolder(view);
    }

    public class HfVmmcRegisterViewHolder extends RegisterViewHolder {

        public TextView textViewReferralDay;

        public HfVmmcRegisterViewHolder(View itemView) {
            super(itemView);
            textViewReferralDay = itemView.findViewById(R.id.text_view_referral_day);
        }
    }
}
