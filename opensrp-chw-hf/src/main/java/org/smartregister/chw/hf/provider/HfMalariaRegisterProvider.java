package org.smartregister.chw.hf.provider;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.smartregister.chw.core.provider.ChwMalariaRegisterProvider;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.utils.HfReferralUtils;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.provider.MalariaRegisterProvider;
import org.smartregister.view.contract.SmartRegisterClient;

import java.util.Set;

public class HfMalariaRegisterProvider extends ChwMalariaRegisterProvider {
    private final LayoutInflater inflater;

    public HfMalariaRegisterProvider(Context context, View.OnClickListener paginationClickListener, View.OnClickListener onClickListener, Set visibleColumns) {
        super(context, paginationClickListener, onClickListener, visibleColumns);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public void getView(Cursor cursor, SmartRegisterClient client, RegisterViewHolder viewHolder) {
        super.getView(cursor, client, viewHolder);
        showLatestMalariaReferralDay((CommonPersonObjectClient) client, (HfMalariaRegisterViewHolder) viewHolder);
    }

    private void showLatestMalariaReferralDay(CommonPersonObjectClient client, HfMalariaRegisterViewHolder viewHolder) {
        HfReferralUtils.displayReferralDay(client, CoreConstants.TASKS_FOCUS.SUSPECTED_MALARIA, viewHolder.textViewReferralDay);
    }

    public class HfMalariaRegisterViewHolder extends MalariaRegisterProvider.RegisterViewHolder {

        public TextView textViewReferralDay;

        public HfMalariaRegisterViewHolder(View itemView) {
            super(itemView);
            textViewReferralDay = itemView.findViewById(R.id.text_view_referral_day);
        }
    }

    @Override
    public RegisterViewHolder createViewHolder(ViewGroup parent) {
        View view = inflater.inflate(R.layout.malaria_register_list_row, parent, false);
        return new HfMalariaRegisterProvider.HfMalariaRegisterViewHolder(view);
    }
}
