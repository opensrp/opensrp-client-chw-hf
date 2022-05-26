package org.smartregister.chw.hf.provider;

import static org.smartregister.util.Utils.getName;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.smartregister.chw.core.rule.HeiFollowupRule;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.FpUtil;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.dao.HeiDao;
import org.smartregister.chw.hf.rule.HfHeiFollowupRule;
import org.smartregister.chw.hf.utils.HfHomeVisitUtil;
import org.smartregister.chw.pmtct.fragment.BasePmtctRegisterFragment;
import org.smartregister.chw.pmtct.util.DBConstants;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.provider.PmtctRegisterProvider;
import org.smartregister.util.Utils;
import org.smartregister.view.contract.SmartRegisterClient;

import java.text.MessageFormat;
import java.util.Date;
import java.util.Set;

import timber.log.Timber;

public class HeiRegisterProvider extends PmtctRegisterProvider {

    private Context context;
    private Set<org.smartregister.configurableviews.model.View> visibleColumns;

    public HeiRegisterProvider(Context context, View.OnClickListener paginationClickListener, View.OnClickListener onClickListener, Set visibleColumns) {
        super(context, paginationClickListener, onClickListener, visibleColumns);
        this.onClickListener = onClickListener;
        this.visibleColumns = visibleColumns;
        this.context = context;
    }

    @Override
    public void getView(Cursor cursor, SmartRegisterClient smartRegisterClient, RegisterViewHolder registerViewHolder) {
        CommonPersonObjectClient pc = (CommonPersonObjectClient) smartRegisterClient;
        if (visibleColumns.isEmpty()) {
            populatePatientColumn(pc, registerViewHolder);
            registerViewHolder.dueButton.setVisibility(View.GONE);
            registerViewHolder.dueButton.setOnClickListener(null);
            String baseEntityId = smartRegisterClient.entityId();
            Utils.startAsyncTask(new UpdateHeiDueButtonStatusTask(registerViewHolder, baseEntityId), null);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void populatePatientColumn(CommonPersonObjectClient pc, final RegisterViewHolder viewHolder) {
        try {

            String firstName = getName(
                    Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.FIRST_NAME, true),
                    Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.MIDDLE_NAME, true));


            String dob = org.smartregister.family.util.Utils.getValue(pc.getColumnmaps(), org.smartregister.family.util.DBConstants.KEY.DOB, false);
            String dobString = org.smartregister.family.util.Utils.getDuration(dob);

            String dod = org.smartregister.family.util.Utils.getValue(pc.getColumnmaps(), org.smartregister.family.util.DBConstants.KEY.DOD, false);
            String patientName = getName(firstName, Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.LAST_NAME, true));


            if (StringUtils.isNotBlank(dod)) {
                dobString = org.smartregister.family.util.Utils.getDuration(dod, dob);
                dobString = dobString.contains("y") ? dobString.substring(0, dobString.indexOf("y")) : dobString;

                patientName = patientName + ", " + org.smartregister.family.util.Utils.getTranslatedDate(dobString, context) + " " + context.getString(R.string.deceased_brackets);
                viewHolder.patientName.setText(patientName);
                viewHolder.patientName.setTextColor(Color.GRAY);
                viewHolder.patientName.setTypeface(viewHolder.patientName.getTypeface(), Typeface.ITALIC);
                viewHolder.dueWrapper.setVisibility(View.GONE);
            } else {
                patientName = patientName + ", " + org.smartregister.family.util.Utils.getTranslatedDate(dobString, context);
                viewHolder.patientName.setText(patientName);
                viewHolder.patientName.setTextColor(Color.BLACK);
                viewHolder.patientName.setTypeface(viewHolder.patientName.getTypeface(), Typeface.NORMAL);
                viewHolder.patientColumn.setOnClickListener(onClickListener);
                viewHolder.dueButton.setOnClickListener(onClickListener);
                viewHolder.registerColumns.setOnClickListener(v -> viewHolder.patientColumn.performClick());
                viewHolder.registerColumns.setOnClickListener(v -> viewHolder.dueButton.performClick());
            }


            viewHolder.textViewGender.setText(updateMemberGender(pc));
            viewHolder.textViewVillage.setText(Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.VILLAGE_TOWN, true));

            viewHolder.patientColumn.setTag(pc);
            viewHolder.patientColumn.setTag(org.smartregister.pmtct.R.id.VIEW_ID, BasePmtctRegisterFragment.CLICK_VIEW_NORMAL);

            viewHolder.dueButton.setTag(pc);
            viewHolder.dueButton.setTag(org.smartregister.pmtct.R.id.VIEW_ID, BasePmtctRegisterFragment.FOLLOW_UP_VISIT);
            viewHolder.registerColumns.setOnClickListener(onClickListener);


        } catch (Exception e) {
            Timber.e(e);
        }
    }

    private class UpdateHeiDueButtonStatusTask extends AsyncTask<Void, Void, Void> {
        private final RegisterViewHolder viewHolder;
        private final String baseEntityId;
        private HfHeiFollowupRule heiFollowUpRule;


        private UpdateHeiDueButtonStatusTask(RegisterViewHolder viewHolder, String baseEntityId) {
            this.viewHolder = viewHolder;
            this.baseEntityId = baseEntityId;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Date startRegisterDate = HeiDao.getHeiRegisterDate(baseEntityId);
            Date followUpDate = HeiDao.getHeiFollowUpVisitDate(baseEntityId);
            heiFollowUpRule = HfHomeVisitUtil.getHeiVisitStatus(startRegisterDate, followUpDate, baseEntityId);
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            if (heiFollowUpRule != null && StringUtils.isNotBlank(heiFollowUpRule.getButtonStatus()) && !CoreConstants.VISIT_STATE.EXPIRED.equalsIgnoreCase(heiFollowUpRule.getButtonStatus())) {
                updateDueColumn(context, viewHolder, heiFollowUpRule);
            }
        }

        private void updateDueColumn(Context context, RegisterViewHolder viewHolder, HeiFollowupRule heiFollowupRule) {
            if (!HeiDao.hasTheChildTransferedOut(heiFollowupRule.getBaseEntityId()) && !HeiDao.isTheChildLostToFollowup(heiFollowupRule.getBaseEntityId())) {
                if (heiFollowupRule.getDueDate() != null) {
                    viewHolder.dueButton.setVisibility(View.VISIBLE);
                    if (heiFollowupRule.getButtonStatus().equalsIgnoreCase(CoreConstants.VISIT_STATE.NOT_DUE_YET)) {
                        setVisitButtonNextDueStatus(context, FpUtil.sdf.format(heiFollowupRule.getDueDate()), viewHolder.dueButton);
                    }
                    if (heiFollowupRule.getButtonStatus().equalsIgnoreCase(CoreConstants.VISIT_STATE.DUE)) {
                        setVisitButtonDueStatus(context, String.valueOf(Days.daysBetween(new DateTime(heiFollowupRule.getDueDate()), new DateTime()).getDays()), viewHolder.dueButton);
                    } else if (heiFollowupRule.getButtonStatus().equalsIgnoreCase(CoreConstants.VISIT_STATE.OVERDUE)) {
                        setVisitButtonOverdueStatus(context, String.valueOf(Days.daysBetween(new DateTime(heiFollowupRule.getOverDueDate()), new DateTime()).getDays()), viewHolder.dueButton);
                    } else if (heiFollowupRule.getButtonStatus().equalsIgnoreCase(CoreConstants.VISIT_STATE.VISIT_DONE)) {
                        setVisitDone(context, viewHolder.dueButton);
                    }
                }
            } else {
                int followupStatus;
                int followupStatusColor;
                if (HeiDao.hasTheChildTransferedOut(heiFollowupRule.getBaseEntityId())) {
                    followupStatus = R.string.transfer_out;
                    followupStatusColor = context.getResources().getColor(org.smartregister.pmtct.R.color.medium_risk_text_orange);
                } else {
                    followupStatus = R.string.lost_to_followup;
                    followupStatusColor = context.getResources().getColor(org.smartregister.pmtct.R.color.alert_urgent_red);
                }

                viewHolder.dueButton.setVisibility(View.VISIBLE);
                viewHolder.dueButton.setTextColor(followupStatusColor);
                viewHolder.dueButton.setText(followupStatus);
                viewHolder.dueButton.setBackgroundResource(org.smartregister.chw.core.R.drawable.colorless_btn_selector);
                viewHolder.dueButton.setOnClickListener(null);
            }

        }

        private void setVisitButtonNextDueStatus(Context context, String visitDue, Button dueButton) {
            dueButton.setTextColor(context.getResources().getColor(org.smartregister.chw.core.R.color.light_grey_text));
            dueButton.setText(MessageFormat.format(context.getString(R.string.next_visit_date), visitDue));
            dueButton.setBackgroundResource(org.smartregister.chw.core.R.drawable.colorless_btn_selector);
            dueButton.setOnClickListener(null);
        }

        private void setVisitButtonDueStatus(Context context, String visitDue, Button dueButton) {
            dueButton.setTextColor(context.getResources().getColor(org.smartregister.chw.core.R.color.alert_in_progress_blue));
            if (visitDue.equalsIgnoreCase("0")) {
                dueButton.setText(context.getString(org.smartregister.chw.core.R.string.hiv_visit_day_due_today));
            } else {
                dueButton.setText(context.getString(org.smartregister.chw.core.R.string.hiv_visit_day_due, visitDue));
            }
            dueButton.setBackgroundResource(org.smartregister.chw.core.R.drawable.blue_btn_selector);
            dueButton.setOnClickListener(onClickListener);
        }


        private void setVisitButtonOverdueStatus(Context context, String visitDue, Button dueButton) {
            dueButton.setTextColor(context.getResources().getColor(org.smartregister.chw.core.R.color.white));
            if (visitDue.equalsIgnoreCase("0")) {
                dueButton.setText(context.getString(org.smartregister.chw.core.R.string.hiv_visit_day_overdue_today));

            } else {
                dueButton.setText(context.getString(org.smartregister.chw.core.R.string.hiv_visit_day_overdue, visitDue));
            }
            dueButton.setBackgroundResource(org.smartregister.chw.core.R.drawable.overdue_red_btn_selector);
            dueButton.setOnClickListener(onClickListener);
        }

        private void setVisitDone(Context context, Button dueButton) {
            dueButton.setTextColor(context.getResources().getColor(org.smartregister.chw.core.R.color.alert_complete_green));
            dueButton.setText(context.getString(org.smartregister.chw.core.R.string.visit_done));
            dueButton.setBackgroundColor(context.getResources().getColor(org.smartregister.chw.core.R.color.transparent));
            dueButton.setOnClickListener(null);
        }
    }
}
