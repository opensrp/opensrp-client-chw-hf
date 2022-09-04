package org.smartregister.chw.hf.provider;

import static org.smartregister.util.Utils.getName;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.smartregister.chw.anc.fragment.BaseAncRegisterFragment;
import org.smartregister.chw.anc.util.DBConstants;
import org.smartregister.chw.core.provider.ChwPncRegisterProvider;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.hf.utils.HfReferralUtils;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.util.Utils;
import org.smartregister.view.contract.SmartRegisterClient;

import java.text.MessageFormat;
import java.util.Set;

public class HfPncNoMotherRegisterProvider extends ChwPncRegisterProvider {
    private final LayoutInflater inflater;
    private final Context context;
    private final View.OnClickListener onClickListener;
    private final Set<org.smartregister.configurableviews.model.View> visibleColumns;

    public HfPncNoMotherRegisterProvider(Context context, CommonRepository commonRepository, Set visibleColumns, View.OnClickListener onClickListener, View.OnClickListener paginationClickListener) {
        super(context, commonRepository, visibleColumns, onClickListener, paginationClickListener);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.onClickListener = onClickListener;
        this.context = context;
        this.visibleColumns = visibleColumns;
    }

    @Override
    public void getView(Cursor cursor, SmartRegisterClient client, RegisterViewHolder viewHolder) {
        super.getView(cursor, client, viewHolder);
        viewHolder.dueWrapper.setVisibility(View.GONE);
        CommonPersonObjectClient pc = (CommonPersonObjectClient) client;
        showLatestPncReferralDay((CommonPersonObjectClient) client, (HfPncRegisterViewHolder) viewHolder);
        if (visibleColumns.isEmpty()) {
            populatePatientColumn(pc, client, viewHolder);
        }
    }


    @Override
    public RegisterViewHolder createViewHolder(ViewGroup parent) {
        View view = inflater.inflate(R.layout.pnc_no_mother_register_list_row, parent, false);
        return new HfPncRegisterViewHolder(view);
    }

    private void showLatestPncReferralDay(CommonPersonObjectClient client, HfPncNoMotherRegisterProvider.HfPncRegisterViewHolder viewHolder) {
        HfReferralUtils.displayReferralDay(client, CoreConstants.TASKS_FOCUS.PNC_DANGER_SIGNS, viewHolder.textViewReferralDay);
    }

    private void populatePatientColumn(CommonPersonObjectClient pc, SmartRegisterClient client, final RegisterViewHolder viewHolder) {

        viewHolder.villageTown.setText(Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.VILLAGE_TOWN, true));

        String fname = getName(
                Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.FIRST_NAME, true),
                Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.MIDDLE_NAME, true)
        );

        String patientName = getName(fname, Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.LAST_NAME, true));

        String dob = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.DOB, false);
        String dobString = org.smartregister.family.util.Utils.getDuration(dob);
        if (StringUtils.isNotBlank(dobString)) {
            String patientNameAge = MessageFormat.format("{0}, {1}",
                    patientName,
                    dobString
            );
            viewHolder.patientNameAndAge.setText(patientNameAge);
        } else {
            viewHolder.patientNameAndAge.setText(patientName);
        }

        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd-MM-yyyy");
        String dayPnc = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.DELIVERY_DATE, true);
        if (StringUtils.isNotBlank(dayPnc)) {
            int period = Days.daysBetween(new DateTime(formatter.parseDateTime(dayPnc)), new DateTime()).getDays();
            String pncDay = MessageFormat.format("{0} {1}",
                    context.getString(org.smartregister.chw.pnc.R.string.pnc_day),
                    period
            );
            viewHolder.pncDay.setText(pncDay);
        }

        TextView childCaregiverName = viewHolder.itemView.findViewById(R.id.child_caregiver);
        childCaregiverName.setText(context.getString(R.string.caregiver_name, Utils.getValue(pc.getColumnmaps(), Constants.DBConstants.CAREGIVER_NAME, false)));


        // add patient listener
        viewHolder.patientColumn.setOnClickListener(onClickListener);
        viewHolder.patientColumn.setTag(client);
        viewHolder.patientColumn.setTag(org.smartregister.chw.opensrp_chw_anc.R.id.VIEW_ID, BaseAncRegisterFragment.CLICK_VIEW_NORMAL);

        // add due listener
        viewHolder.dueButton.setOnClickListener(onClickListener);
        viewHolder.dueButton.setTag(client);
        viewHolder.dueButton.setTag(org.smartregister.chw.opensrp_chw_anc.R.id.VIEW_ID, BaseAncRegisterFragment.CLICK_VIEW_DOSAGE_STATUS);


        viewHolder.registerColumns.setOnClickListener(v -> viewHolder.patientColumn.performClick());

        viewHolder.dueWrapper.setOnClickListener(v -> viewHolder.dueButton.performClick());
    }

    public class HfPncRegisterViewHolder extends RegisterViewHolder {

        public TextView textViewReferralDay;

        public HfPncRegisterViewHolder(View itemView) {
            super(itemView);
            textViewReferralDay = itemView.findViewById(R.id.text_view_referral_day);
        }
    }
}
