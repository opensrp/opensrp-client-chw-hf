package org.smartregister.chw.hf.provider;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.smartregister.chw.core.provider.CoreTbProvider;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.utils.HfReferralUtils;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.view.contract.SmartRegisterClient;

import java.util.Set;

public class HfTbRegisterProvider extends CoreTbProvider {

    private final LayoutInflater inflater;

    public HfTbRegisterProvider(Context context, Set visibleColumns, View.OnClickListener onClickListener, View.OnClickListener paginationClickListener) {
        super(context, visibleColumns, onClickListener, paginationClickListener);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public void getView(Cursor cursor, SmartRegisterClient smartRegisterClient, RegisterViewHolder registerViewHolder) {
        super.getView(cursor, smartRegisterClient, registerViewHolder);
        registerViewHolder.getDueWrapper().setVisibility(View.GONE);

        showLatestTbReferralDay((CommonPersonObjectClient) smartRegisterClient, (HfRegisterViewHolder) registerViewHolder);
    }

    @Override
    public RegisterViewHolder createViewHolder(ViewGroup parent) {
        View view = inflater.inflate(R.layout.hf_tb_register_list_row_item, parent, false);
        return new HfRegisterViewHolder(view);
    }

    private void showLatestTbReferralDay(CommonPersonObjectClient client, HfTbRegisterProvider.HfRegisterViewHolder viewHolder) {
        HfReferralUtils.displayReferralDay(client, CoreConstants.TASKS_FOCUS.SUSPECTED_TB, viewHolder.textViewReferralDay);
    }

    public class HfRegisterViewHolder extends RegisterViewHolder {
        public TextView textViewReferralDay;

        public HfRegisterViewHolder(View itemView) {
            super(itemView);
            textViewReferralDay = itemView.findViewById(R.id.text_view_referral_day);
        }
    }
}
