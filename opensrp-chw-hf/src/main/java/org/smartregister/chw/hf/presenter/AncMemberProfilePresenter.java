package org.smartregister.chw.hf.presenter;

import static org.smartregister.AllConstants.LocationConstants.SPECIAL_TAG_FOR_OPENMRS_TEAM_MEMBERS;

import android.content.Context;

import com.vijay.jsonwizard.utils.FormUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.core.contract.AncMemberProfileContract;
import org.smartregister.chw.core.presenter.CoreAncMemberProfilePresenter;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.dao.HfAncDao;
import org.smartregister.chw.hf.interactor.AncMemberProfileInteractor;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.referral.util.JsonFormConstants;
import org.smartregister.dao.LocationsDao;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.repository.AllSharedPreferences;

import java.util.Collections;

import timber.log.Timber;

public class AncMemberProfilePresenter extends CoreAncMemberProfilePresenter {
    private AncMemberProfileInteractor ancMemberProfileInteractor;

    public AncMemberProfilePresenter(AncMemberProfileContract.View view, AncMemberProfileContract.Interactor interactor, MemberObject memberObject) {
        super(view, interactor, memberObject);
        ancMemberProfileInteractor = (AncMemberProfileInteractor) interactor;
    }

    @Override
    public void refreshProfileTopSection(MemberObject memberObject) {
        String entityType = memberObject.getBaseEntityId();
        getView().setMemberName(memberObject.getMemberName());
        getView().setMemberGA(String.valueOf(memberObject.getGestationAge()));
        getView().setMemberAddress(memberObject.getAddress());
        getView().setMemberChwMemberId(memberObject.getChwMemberId());
        getView().setProfileImage(memberObject.getBaseEntityId(), entityType);
        getView().setMemberGravida(memberObject.getGravida());
        if (HfAncDao.isClientClosed(memberObject.getBaseEntityId())) {
            getView().setPregnancyRiskLabel(Constants.Visits.TERMINATED);
        } else {
            getView().setPregnancyRiskLabel(memberObject.getPregnancyRiskLevel());
        }
    }

    @Override
    public void setPregnancyRiskTransportProfileDetails(MemberObject memberObject) {
        String entityType = memberObject.getBaseEntityId();
        getView().setMemberName(memberObject.getMemberName());
        getView().setMemberGA(String.valueOf(memberObject.getGestationAge()));
        getView().setMemberAddress(memberObject.getAddress());
        getView().setMemberChwMemberId(memberObject.getChwMemberId());
        getView().setProfileImage(memberObject.getBaseEntityId(), entityType);
        getView().setMemberGravida(memberObject.getGravida());
        if (HfAncDao.isClientClosed(memberObject.getBaseEntityId())) {
            getView().setPregnancyRiskLabel(Constants.Visits.TERMINATED);
        } else {
            getView().setPregnancyRiskLabel(memberObject.getPregnancyRiskLevel());
        }

    }

    public void startPartnerFollowupReferralForm(MemberObject memberObject) {
        try {
            JSONObject formJsonObject = (new FormUtils()).getFormJsonFromRepositoryOrAssets((Context) getView(), CoreConstants.JSON_FORM.getAncPartnerCommunityFollowupReferral());
            //adds the chw locations under the current facility
            JSONObject chwLocations = CoreJsonFormUtils.getJsonField(formJsonObject, org.smartregister.util.JsonFormUtils.STEP1, "chw_location");
            CoreJsonFormUtils.addLocationsToDropdownField(LocationsDao.getLocationsByTags(
                    Collections.singleton(SPECIAL_TAG_FOR_OPENMRS_TEAM_MEMBERS)), chwLocations);

            getView().startFormActivity(formJsonObject);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    public void startPartnerTestingForm(MemberObject memberObject) {
        JSONObject partnerTestingForm;
        try {
            partnerTestingForm = org.smartregister.chw.core.utils.FormUtils.getFormUtils().getFormJson(CoreConstants.JSON_FORM.PARTNER_TESTING);
            partnerTestingForm.getJSONObject("global").put("hiv_testing_done", HfAncDao.isPartnerTestedForHiv(memberObject.getBaseEntityId()));
            partnerTestingForm.getJSONObject("global").put("gestational_age", memberObject.getGestationAge());
            partnerTestingForm.getJSONObject("global").put("syphilis_testing_done", HfAncDao.isPartnerTestedForSyphilis(memberObject.getBaseEntityId()));
            partnerTestingForm.getJSONObject("global").put("hepatitis_testing_done", HfAncDao.isPartnerTestedForHepatitis(memberObject.getBaseEntityId()));
            partnerTestingForm.getJSONObject("global").put("partner_hiv_test_at_32_done", HfAncDao.isPartnerHivTestConductedAtWk32(memberObject.getBaseEntityId()));
            partnerTestingForm.getJSONObject("global").put("partner_hiv_status", HfAncDao.getPartnerHivStatus(memberObject.getBaseEntityId()));
            partnerTestingForm.getJSONObject("global").put("partner_other_stds", HfAncDao.getPartnerOtherStdsStatus(memberObject.getBaseEntityId()));

            JSONArray fields = partnerTestingForm.getJSONObject(Constants.JsonFormConstants.STEP1).getJSONArray(JsonFormConstants.FIELDS);
            JSONObject renamePartnerSecondHivAt32 = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "partner_hiv");
            JSONObject partnerHivTestNumberField = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "partner_hiv_test_number");
            JSONObject gest_ageField = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "gest_age");
            partnerHivTestNumberField.put(JsonFormUtils.VALUE, HfAncDao.getNextPartnerHivTestNumber(memberObject.getBaseEntityId()));
            gest_ageField.put(JsonFormUtils.VALUE, memberObject.getGestationAge());
            if (HfAncDao.getNextPartnerHivTestNumber(memberObject.getBaseEntityId()) == 2) {
                renamePartnerSecondHivAt32.put("label", getView().getContext().getString(R.string.second_hiv_test_results_partner));
            }

            getView().startFormActivity(partnerTestingForm);
        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    public void createPartnerFollowupReferralEvent(AllSharedPreferences allSharedPreferences, String jsonString, String entityID) throws Exception {
        ancMemberProfileInteractor.createPartnerFollowupReferralEvent(allSharedPreferences, jsonString, entityID);
    }

    public void savePartnerTestingEvent(AllSharedPreferences allSharedPreferences, String jsonString, String entityID) throws Exception {
        ancMemberProfileInteractor.createTestingEvent(allSharedPreferences, jsonString, entityID);
    }
}
