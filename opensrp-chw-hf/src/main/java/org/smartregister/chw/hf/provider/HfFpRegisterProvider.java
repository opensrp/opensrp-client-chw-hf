package org.smartregister.chw.hf.provider;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.smartregister.chw.core.provider.CoreFpProvider;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.fp.provider.BaseFpRegisterProvider;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.utils.HfReferralUtils;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.view.contract.SmartRegisterClient;

import java.util.Set;

public class HfFpRegisterProvider extends CoreFpProvider {

    private final LayoutInflater inflater;

    public HfFpRegisterProvider(Context context, Set visibleColumns, View.OnClickListener onClickListener, View.OnClickListener paginationClickListener) {
        super(context, visibleColumns, onClickListener, paginationClickListener);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public void getView(Cursor cursor, SmartRegisterClient smartRegisterClient, RegisterViewHolder registerViewHolder) {
        super.getView(cursor, smartRegisterClient, registerViewHolder);
        registerViewHolder.dueWrapper.setVisibility(View.GONE);
        showLatestPncReferralDay((CommonPersonObjectClient) smartRegisterClient, (HfRegisterViewHolder) registerViewHolder);
    }

    @Override
    public RegisterViewHolder createViewHolder(ViewGroup parent) {
        View view = inflater.inflate(R.layout.family_planning_register_list_row, parent, false);
        return new HfRegisterViewHolder(view);
    }

    private void showLatestPncReferralDay(CommonPersonObjectClient client, HfFpRegisterProvider.HfRegisterViewHolder viewHolder) {
        HfReferralUtils.displayReferralDay(client, CoreConstants.TASKS_FOCUS.FP_SIDE_EFFECTS, viewHolder.textViewReferralDay);
    }

    public class HfRegisterViewHolder extends BaseFpRegisterProvider.RegisterViewHolder {

        TextView textViewReferralDay;

        public HfRegisterViewHolder(View itemView) {
            super(itemView);
            textViewReferralDay = itemView.findViewById(R.id.text_view_referral_day);
        }
    }
}
