package org.smartregister.chw.hf.provider;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.joda.time.Years;
import org.smartregister.chw.anc.fragment.BaseAncRegisterFragment;
import org.smartregister.chw.anc.provider.AncRegisterProvider;
import org.smartregister.chw.anc.util.DBConstants;
import org.smartregister.chw.core.provider.ChwAncRegisterProvider;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.utils.HfReferralUtils;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.util.Utils;
import org.smartregister.view.contract.SmartRegisterClient;

import java.text.MessageFormat;
import java.util.Set;

public class HfAncReferralListRegisterProvider extends ChwAncRegisterProvider {
    private final LayoutInflater inflater;
    private View.OnClickListener onClickListener;
    private Context context;
    private Set<org.smartregister.configurableviews.model.View> visibleColumns;
    private CommonRepository commonRepository;

    public HfAncReferralListRegisterProvider(Context context, CommonRepository commonRepository, Set visibleColumns, View.OnClickListener onClickListener, View.OnClickListener paginationClickListener) {
        super(context, commonRepository, visibleColumns, onClickListener, paginationClickListener);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.onClickListener = onClickListener;
        this.context = context;
        this.visibleColumns = visibleColumns;

        this.commonRepository = commonRepository;
    }

    @Override
    protected void populatePatientColumn(@NotNull CommonPersonObjectClient pc, SmartRegisterClient client, @NotNull final AncRegisterProvider.RegisterViewHolder viewHolder) {
        String fname = Utils.getName(
                Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.FIRST_NAME, true),
                Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.MIDDLE_NAME, true)
        );

        String patientName = Utils.getName(fname, Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.LAST_NAME, true));

        // calculate LMP
        String dobString = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.DOB, false);
        if (StringUtils.isNotBlank(dobString)) {
            int age = Years.yearsBetween(new DateTime(dobString), new DateTime()).getYears();
            String gaLocation = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.VILLAGE_TOWN, true);

            viewHolder.patientAge.setText(gaLocation);

            String patientNameAge = MessageFormat.format("{0}, {1}", patientName, age);
            viewHolder.patientName.setText(patientNameAge);

        }

        // add patient listener
        viewHolder.patientColumn.setOnClickListener(onClickListener);
        viewHolder.patientColumn.setTag(client);
        viewHolder.patientColumn.setTag(R.id.VIEW_ID, BaseAncRegisterFragment.CLICK_VIEW_NORMAL);


        // add due listener
        viewHolder.dueButton.setOnClickListener(onClickListener);
        viewHolder.dueButton.setTag(client);
        viewHolder.dueButton.setTag(R.id.VIEW_ID, BaseAncRegisterFragment.CLICK_VIEW_DOSAGE_STATUS);

        viewHolder.registerColumns.setOnClickListener(v -> viewHolder.patientColumn.performClick());
        viewHolder.dueWrapper.setOnClickListener(v -> viewHolder.dueButton.performClick());
    }

    @Override
    public RegisterViewHolder createViewHolder(ViewGroup parent) {
        View view = inflater.inflate(R.layout.anc_register_list_row, parent, false);
        return new HfRegisterViewHolder(view);
    }

    @Override
    public void getView(Cursor cursor, SmartRegisterClient client, RegisterViewHolder viewHolder) {
        CommonPersonObjectClient pc = (CommonPersonObjectClient) client;
        if (visibleColumns.isEmpty()) {
            populatePatientColumn(pc, client, viewHolder);
            populateLastColumn(pc, viewHolder);
        }

        showLatestAncReferralDay((CommonPersonObjectClient) client, (HfRegisterViewHolder) viewHolder);
    }

    private void showLatestAncReferralDay(CommonPersonObjectClient client, HfRegisterViewHolder viewHolder) {
        HfReferralUtils.displayReferralDay(client, CoreConstants.TASKS_FOCUS.ANC_DANGER_SIGNS, viewHolder.textViewReferralDay);
    }

    public class HfRegisterViewHolder extends RegisterViewHolder {

        public TextView textViewReferralDay;

        public HfRegisterViewHolder(View itemView) {
            super(itemView);
            textViewReferralDay = itemView.findViewById(R.id.text_view_referral_day);
        }
    }

    private void populateLastColumn(CommonPersonObjectClient pc, RegisterViewHolder viewHolder) {
        if (commonRepository != null) {
            CommonPersonObject commonPersonObject = commonRepository.findByBaseEntityId(pc.entityId());
            if (commonPersonObject != null) {
                viewHolder.dueButton.setVisibility(View.VISIBLE);
                viewHolder.dueButton.setText(context.getString(org.smartregister.chw.opensrp_chw_anc.R.string.anc_home_visit));
                viewHolder.dueButton.setAllCaps(true);
            } else {
                viewHolder.dueButton.setVisibility(View.GONE);
            }
        }
    }
}
