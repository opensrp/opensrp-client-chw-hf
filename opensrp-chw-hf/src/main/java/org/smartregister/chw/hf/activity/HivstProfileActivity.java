package org.smartregister.chw.hf.activity;

import static org.smartregister.chw.hf.utils.Constants.JsonForm.HIV_REGISTRATION;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.View;

import androidx.annotation.NonNull;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.core.activity.CoreFamilyProfileActivity;
import org.smartregister.chw.core.activity.CoreHivstProfileActivity;
import org.smartregister.chw.core.interactor.CoreHivstProfileInteractor;
import org.smartregister.chw.core.presenter.CoreFamilyOtherMemberActivityPresenter;
import org.smartregister.chw.core.presenter.CoreHivstMemberProfilePresenter;
import org.smartregister.chw.core.utils.FormUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hiv.dao.HivDao;
import org.smartregister.chw.hivst.dao.HivstDao;
import org.smartregister.chw.hivst.util.Constants;
import org.smartregister.domain.AlertStatus;

import timber.log.Timber;

public class HivstProfileActivity extends CoreHivstProfileActivity {

    public static void startProfile(Activity activity, String baseEntityId, boolean openIssueSelfTestingKitsForm) {
        Intent intent = new Intent(activity, HivstProfileActivity.class);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityId);
        intent.putExtra(OPEN_ISSUE_SELF_TESTING_KITS_FORM, openIssueSelfTestingKitsForm);
        activity.startActivity(intent);
    }

    @Override
    protected void initializePresenter() {
        showProgressBar(true);
        String baseEntityId = getIntent().getStringExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID);
        memberObject = HivstDao.getMember(baseEntityId);
        profilePresenter = new CoreHivstMemberProfilePresenter(this, new CoreHivstProfileInteractor(), memberObject);
        fetchProfileData();
        profilePresenter.refreshProfileBottom();
    }

    @Override
    protected void startHivServicesRegistration() {
        try {
            HivRegisterActivity.startHIVFormActivity(this, memberObject.getBaseEntityId(), HIV_REGISTRATION, (new com.vijay.jsonwizard.utils.FormUtils()).getFormJsonFromRepositoryOrAssets(this, HIV_REGISTRATION).toString());
        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    @Override
    protected Class<? extends CoreFamilyProfileActivity> getFamilyProfileActivityClass() {
        return null;
    }

    @Override
    public void startIssueSelfTestingKitsForm(String baseEntityId) {
        JSONObject form = FormUtils.getFormUtils().getFormJson(Constants.FORMS.HIVST_ISSUE_KITS);
        try {
            form.put(org.smartregister.util.JsonFormUtils.ENTITY_ID, baseEntityId);
            JSONObject global = form.getJSONObject("global");
            boolean knownPositiveFromHIV = HivDao.isRegisteredForHiv(baseEntityId) && StringUtils.isNotBlank(HivDao.getMember(baseEntityId).getCtcNumber());
            global.put("known_positive", HivstDao.isTheClientKnownPositiveAtReg(baseEntityId) || knownPositiveFromHIV);
        } catch (JSONException e) {
            Timber.e(e);
        }
        startFormActivity(form);
    }

    @Override
    public void startResultViewActivity(Context context, String baseEntityId) {
        HivstResultViewActivity.startResultViewActivity(context, baseEntityId);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.rlSelfTestingResults) {
            startResultViewActivity(this, memberObject.getBaseEntityId());
        } else {
            super.onClick(view);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.findItem(org.smartregister.chw.core.R.id.action_hiv_registration).setVisible(!HivDao.isRegisteredForHiv(memberObject.getBaseEntityId()));
        return true;
    }

    @Override
    public void showReferralView() {
        baseHivstFloatingMenu.findViewById(R.id.refer_to_facility_layout).setVisibility(View.GONE);
    }

    @Override
    protected void removeMember() {
        //implement
    }


    @NonNull
    @Override
    public CoreFamilyOtherMemberActivityPresenter presenter() {
        return null;
    }

    @Override
    public void setProfileImage(String s, String s1) {
        //implement
    }

    @Override
    public void setProfileDetailThree(String s) {
        //implement
    }

    @Override
    public void toggleFamilyHead(boolean b) {
        //implement
    }

    @Override
    public void togglePrimaryCaregiver(boolean b) {
        //implement
    }

    @Override
    public void refreshList() {
        //implement
    }

    @Override
    public void updateHasPhone(boolean b) {
        //implement
    }

    @Override
    public void setFamilyServiceStatus(String s) {
        //implement
    }

    @Override
    public void verifyHasPhone() {
        //implement
    }

    @Override
    public void notifyHasPhone(boolean b) {
        //implement
    }

    @Override
    public void refreshMedicalHistory(boolean hasHistory) {
        rlLastVisit.setVisibility(View.GONE);
    }

    @Override
    public void refreshFamilyStatus(AlertStatus status) {
        super.refreshFamilyStatus(status);
        rlFamilyServicesDue.setVisibility(View.GONE);
    }
}
