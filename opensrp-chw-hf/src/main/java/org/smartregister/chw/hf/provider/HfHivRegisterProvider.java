package org.smartregister.chw.hf.provider;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.smartregister.chw.core.provider.CoreHivProvider;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.utils.HfReferralUtils;
import org.smartregister.chw.hiv.dao.HivIndexDao;
import org.smartregister.chw.hiv.domain.HivIndexContactObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.view.contract.SmartRegisterClient;

import java.util.List;
import java.util.Set;

public class HfHivRegisterProvider extends CoreHivProvider {

    private final LayoutInflater inflater;

    public HfHivRegisterProvider(Context context, Set visibleColumns, View.OnClickListener onClickListener, View.OnClickListener paginationClickListener) {
        super(context, visibleColumns, onClickListener, paginationClickListener);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public void getView(Cursor cursor, SmartRegisterClient smartRegisterClient, RegisterViewHolder registerViewHolder) {
        super.getView(cursor, smartRegisterClient, registerViewHolder);
        registerViewHolder.getDueWrapper().setVisibility(View.GONE);

        CommonPersonObjectClient commonPersonObjectClient = ((CommonPersonObjectClient) smartRegisterClient);
        List<HivIndexContactObject> hivIndexContactObjects = HivIndexDao.getIndexContacts(commonPersonObjectClient.entityId());
        if (hivIndexContactObjects != null && hivIndexContactObjects.size() > 0) {
            boolean hasPositiveContacts = false;
            for (HivIndexContactObject hivIndexContactObject : hivIndexContactObjects) {
                if (!hasPositiveContacts && !hivIndexContactObject.getTestResults().toLowerCase().equals("negative") )
                    hasPositiveContacts = true;
            }

            if (!hasPositiveContacts)
                ((HfRegisterViewHolder) registerViewHolder).textViewReElicit.setVisibility(View.VISIBLE);
            else
                ((HfRegisterViewHolder) registerViewHolder).textViewReElicit.setVisibility(View.GONE);

        }
        showLatestHivReferralDay((CommonPersonObjectClient) smartRegisterClient, (HfRegisterViewHolder) registerViewHolder);
    }

    @Override
    public RegisterViewHolder createViewHolder(ViewGroup parent) {
        View view = inflater.inflate(R.layout.hf_hiv_register_list_row_item, parent, false);
        return new HfRegisterViewHolder(view);
    }

    private void showLatestHivReferralDay(CommonPersonObjectClient client, HfHivRegisterProvider.HfRegisterViewHolder viewHolder) {
        HfReferralUtils.displayReferralDay(client, CoreConstants.TASKS_FOCUS.SUSPECTED_HIV, viewHolder.textViewReferralDay);
    }

    public class HfRegisterViewHolder extends RegisterViewHolder {
        public TextView textViewReferralDay;
        public TextView textViewReElicit;

        public HfRegisterViewHolder(View itemView) {
            super(itemView);
            textViewReferralDay = itemView.findViewById(R.id.text_view_referral_day);
            textViewReElicit = itemView.findViewById(R.id.text_view_re_elicit);
        }
    }
}
