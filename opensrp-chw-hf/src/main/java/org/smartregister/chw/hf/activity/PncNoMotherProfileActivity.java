package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.util.Constants;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.dao.HfPncDao;
import org.smartregister.chw.hf.utils.PncVisitUtils;
import org.smartregister.chw.pmtct.dao.PmtctDao;
import org.smartregister.family.util.JsonFormUtils;

import timber.log.Timber;

import static org.smartregister.chw.core.utils.Utils.passToolbarTitle;

public class PncNoMotherProfileActivity extends PncMemberProfileActivity {
    public static void startMe(Activity activity, String baseEntityID, MemberObject memberObject) {
        Intent intent = new Intent(activity, PncNoMotherProfileActivity.class);
        intent.putExtra(Constants.ANC_MEMBER_OBJECTS.BASE_ENTITY_ID, baseEntityID);
        intent.putExtra(Constants.ANC_MEMBER_OBJECTS.MEMBER_PROFILE_OBJECT, memberObject);
        passToolbarTitle(activity, intent);
        activity.startActivity(intent);
    }

    public static void startMe(Activity activity, String baseEntityID) {
        Intent intent = new Intent(activity, PncNoMotherProfileActivity.class);
        intent.putExtra(Constants.ANC_MEMBER_OBJECTS.BASE_ENTITY_ID, baseEntityID);
        passToolbarTitle(activity, intent);
        activity.startActivity(intent);
    }

    public static void startChildForm(Activity activity, String childBaseEntityId) {
        JSONObject jsonForm = org.smartregister.chw.core.utils.FormUtils.getFormUtils().getFormJson(org.smartregister.chw.hf.utils.Constants.JsonForm.getPncChildGeneralExamination());
        try {
            jsonForm.getJSONObject("global").put("baseEntityId", childBaseEntityId);
            jsonForm.getJSONObject("global").put("is_eligible_for_bcg", HfPncDao.isChildEligibleForBcg(childBaseEntityId));
            jsonForm.getJSONObject("global").put("is_eligible_for_opv0", HfPncDao.isChildEligibleForOpv0(childBaseEntityId));
            activity.startActivityForResult(org.smartregister.chw.core.utils.FormUtils.getStartFormActivity(jsonForm, activity.getString(R.string.record_child_followup), activity), JsonFormUtils.REQUEST_CODE_GET_JSON);
        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    @Override
    public void setupViews() {
        super.setupViews();
        try {
            PncVisitUtils.processVisits();
        } catch (Exception e) {
            Timber.e(e);
        }
        textview_record_anc_visit.setVisibility(View.VISIBLE);
        textview_record_anc_visit.setOnClickListener(this);
        textViewAncVisitNot.setText(R.string.complete_pnc_visits);
        textViewAncVisitNot.setOnClickListener(v -> confirmRemovePncMember());


        if (HfPncDao.isMotherEligibleForPmtctRegistration(baseEntityID) && !PmtctDao.isRegisteredForPmtct(baseEntityID)) {
            textview_record_anc_visit.setVisibility(View.GONE);
            layoutNotRecordView.setVisibility(View.VISIBLE);
            textViewUndo.setVisibility(View.GONE);
            textViewNotVisitMonth.setText(getContext().getString(R.string.pmtct_pending_registration));
            tvEdit.setText(getContext().getString(R.string.register_button_text));
            tvEdit.setVisibility(View.VISIBLE);
            tvEdit.setOnClickListener(v -> startPmtctRegistration());
            imageViewCross.setImageResource(org.smartregister.chw.core.R.drawable.activityrow_notvisited);
        }

        Visit latestVisit = getVisit(org.smartregister.chw.hf.utils.Constants.Events.PNC_VISIT);
        if (latestVisit != null && !latestVisit.getProcessed()) {
            showVisitInProgress();
        }
    }

    @Override
    protected int getPncDay() {
        return 0;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.textview_record_visit) {
            startChildForm(this, baseEntityID);
        } else {
            super.onClick(v);
        }
    }
}
