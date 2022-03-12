package org.smartregister.chw.hf.provider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;

import org.smartregister.chw.pmtct.fragment.BasePmtctRegisterFragment;
import org.smartregister.chw.pmtct.util.DBConstants;
import org.smartregister.chw.pmtct.util.PmtctUtil;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.provider.PmtctRegisterProvider;
import org.smartregister.util.Utils;
import org.smartregister.view.contract.SmartRegisterClient;

import java.util.Set;

import timber.log.Timber;

import static org.smartregister.chw.core.utils.Utils.getDuration;
import static org.smartregister.util.Utils.getName;

public class HeiRegisterProvider extends PmtctRegisterProvider {
    private final LayoutInflater inflater;

    private View.OnClickListener paginationClickListener;
    private Context context;
    private Set<org.smartregister.configurableviews.model.View> visibleColumns;

    public HeiRegisterProvider(Context context, View.OnClickListener paginationClickListener, View.OnClickListener onClickListener, Set visibleColumns) {
        super(context, paginationClickListener, onClickListener, visibleColumns);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.paginationClickListener = paginationClickListener;
        this.onClickListener = onClickListener;
        this.visibleColumns = visibleColumns;
        this.context = context;
    }

    @Override
    public void getView(Cursor cursor, SmartRegisterClient smartRegisterClient, RegisterViewHolder registerViewHolder) {
        CommonPersonObjectClient pc = (CommonPersonObjectClient) smartRegisterClient;
        if (visibleColumns.isEmpty()) {
            populatePatientColumn(pc, registerViewHolder);
        }
    }

    private String updateMemberGender(CommonPersonObjectClient commonPersonObjectClient) {
        if ("0".equals(Utils.getValue(commonPersonObjectClient.getColumnmaps(), DBConstants.KEY.IS_ANC_CLOSED, false))) {
            return context.getResources().getString(org.smartregister.pmtct.R.string.anc_string);
        } else if ("0".equals(Utils.getValue(commonPersonObjectClient.getColumnmaps(), DBConstants.KEY.IS_PNC_CLOSED, false))) {
            return context.getResources().getString(org.smartregister.pmtct.R.string.pnc_string);
        } else {
            String gender = Utils.getValue(commonPersonObjectClient.getColumnmaps(), DBConstants.KEY.GENDER, true);
            return PmtctUtil.getGenderTranslated(context, gender);
        }
    }

    @SuppressLint("SetTextI18n")
    private void populatePatientColumn(CommonPersonObjectClient pc, final RegisterViewHolder viewHolder) {
        try {

            String firstName = getName(
                    Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.FIRST_NAME, true),
                    Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.MIDDLE_NAME, true));

            String dobString = org.smartregister.family.util.Utils.getTranslatedDate(getDuration(org.smartregister.family.util.Utils.getValue(pc.getColumnmaps(), org.smartregister.family.util.DBConstants.KEY.DOB, false)), inflater().getContext());

            String patientName = getName(firstName, Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.LAST_NAME, true));
            viewHolder.patientName.setText(patientName + ", " + dobString);
            viewHolder.textViewGender.setText(updateMemberGender(pc));
            viewHolder.textViewVillage.setText(Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.VILLAGE_TOWN, true));
            viewHolder.patientColumn.setOnClickListener(onClickListener);
            viewHolder.patientColumn.setTag(pc);
            viewHolder.patientColumn.setTag(org.smartregister.pmtct.R.id.VIEW_ID, BasePmtctRegisterFragment.CLICK_VIEW_NORMAL);

            viewHolder.dueButton.setOnClickListener(onClickListener);
            viewHolder.dueButton.setTag(pc);
            viewHolder.dueButton.setTag(org.smartregister.pmtct.R.id.VIEW_ID, BasePmtctRegisterFragment.FOLLOW_UP_VISIT);
            viewHolder.registerColumns.setOnClickListener(onClickListener);
            viewHolder.dueWrapper.setVisibility(View.GONE);


            viewHolder.registerColumns.setOnClickListener(v -> viewHolder.patientColumn.performClick());
            viewHolder.registerColumns.setOnClickListener(v -> viewHolder.dueButton.performClick());

        } catch (Exception e) {
            Timber.e(e);
        }
    }
}
