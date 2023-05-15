package org.smartregister.chw.hf.provider;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.smartregister.chw.core.provider.CoreHivIndexContactsProvider;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.dao.HivIndexFollowupFeedbackDao;
import org.smartregister.chw.hf.model.HivIndexFollowupFeedbackDetailsModel;
import org.smartregister.chw.hf.utils.HfReferralUtils;
import org.smartregister.chw.hiv.dao.HivIndexDao;
import org.smartregister.chw.hiv.domain.HivIndexContactObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.view.contract.SmartRegisterClient;

import java.util.List;
import java.util.Set;


public class HfHivIndexContactsRegisterProvider extends CoreHivIndexContactsProvider {

    private final LayoutInflater inflater;
    private Context context;

    public HfHivIndexContactsRegisterProvider(Context context, Set visibleColumns, View.OnClickListener onClickListener, View.OnClickListener paginationClickListener) {
        super(context, visibleColumns, onClickListener, paginationClickListener);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
    }

    @Override
    public void getView(Cursor cursor, SmartRegisterClient smartRegisterClient, RegisterViewHolder registerViewHolder) {
        super.getView(cursor, smartRegisterClient, registerViewHolder);
        registerViewHolder.getDueWrapper().setVisibility(View.GONE);
        ((HfRegisterViewHolder) registerViewHolder).goToProfileWrapper.setVisibility(View.VISIBLE);

        showLatestHivReferralDay((CommonPersonObjectClient) smartRegisterClient, (HfRegisterViewHolder) registerViewHolder);
    }

    @Override
    public RegisterViewHolder createViewHolder(ViewGroup parent) {
        View view = inflater.inflate(R.layout.hf_hiv_register_list_row_item, parent, false);
        return new HfRegisterViewHolder(view);
    }

    private void showLatestHivReferralDay(CommonPersonObjectClient client, HfHivIndexContactsRegisterProvider.HfRegisterViewHolder viewHolder) {
        HfReferralUtils.displayReferralDay(client, CoreConstants.TASKS_FOCUS.CONVENTIONAL_HIV_TEST, viewHolder.textViewReferralDay);
        displayReferralSent(client, viewHolder);
    }

    private void displayReferralSent(CommonPersonObjectClient client, HfHivIndexContactsRegisterProvider.HfRegisterViewHolder viewHolder) {
        String baseEntityId = client.entityId();
        HivIndexContactObject hivIndexContactObject = HivIndexDao.getMember(baseEntityId);

        List<HivIndexFollowupFeedbackDetailsModel> hivIndexFollowupFeedbackDetailsModels = HivIndexFollowupFeedbackDao.getHivIndexFollowupFeedback(baseEntityId);

        if (hivIndexFollowupFeedbackDetailsModels.size() > 0) {
            viewHolder.textViewReferralDay.setVisibility(View.VISIBLE);
            String referralDay = viewHolder.itemView.getContext().getString(R.string.index_contact_has_feedback_from_community);
            viewHolder.textViewReferralDay.setText(referralDay);
            viewHolder.textViewReferralDay.setTextColor(context.getResources().getColor(R.color.primary));
            viewHolder.textViewReferralDay.setBackgroundColor(context.getResources().getColor(R.color.green_overlay));
        } else if (HivIndexDao.isReferralSent(baseEntityId)) {
            viewHolder.textViewReferralDay.setVisibility(View.VISIBLE);
            String referralDay = viewHolder.itemView.getContext().getString(R.string.referral_sent);
            viewHolder.textViewReferralDay.setText(referralDay);
            viewHolder.textViewReferralDay.setTextColor(context.getResources().getColor(R.color.due_vaccine_red));
            viewHolder.textViewReferralDay.setBackgroundColor(context.getResources().getColor(R.color.referral_text_background));
        } else if (!hivIndexContactObject.getHasTheContactClientBeenTested().equals("") && !hivIndexContactObject.getHasTheContactClientBeenTested().equals("no") && hivIndexContactObject.getCtcNumber().equals("")) {
            viewHolder.textViewReferralDay.setVisibility(View.VISIBLE);
            String pendingCtcRegistration = viewHolder.itemView.getContext().getString(R.string.pending_ctc_registration);
            viewHolder.textViewReferralDay.setText(pendingCtcRegistration);
            viewHolder.textViewReferralDay.setTextColor(context.getResources().getColor(R.color.due_vaccine_red));
            viewHolder.textViewReferralDay.setBackgroundColor(context.getResources().getColor(R.color.referral_text_background));
        }else if (!hivIndexContactObject.getHasTheContactClientBeenTested().equals("") && hivIndexContactObject.getHasTheContactClientBeenTested().equals("no")) {
            viewHolder.textViewReferralDay.setVisibility(View.VISIBLE);
            String pendingCtcRegistration = viewHolder.itemView.getContext().getString(R.string.client_was_not_tested);
            viewHolder.textViewReferralDay.setText(pendingCtcRegistration);
            viewHolder.textViewReferralDay.setTextColor(context.getResources().getColor(R.color.due_vaccine_red));
            viewHolder.textViewReferralDay.setBackgroundColor(context.getResources().getColor(R.color.referral_text_background));
        } else {
            viewHolder.textViewReferralDay.setVisibility(View.GONE);
        }
    }

    public class HfRegisterViewHolder extends RegisterViewHolder {
        public TextView textViewReferralDay;
        public View goToProfileWrapper;

        public HfRegisterViewHolder(View itemView) {
            super(itemView);
            textViewReferralDay = itemView.findViewById(R.id.text_view_referral_day);
            goToProfileWrapper = itemView.findViewById(R.id.go_to_profile_wrapper);
        }
    }
}
