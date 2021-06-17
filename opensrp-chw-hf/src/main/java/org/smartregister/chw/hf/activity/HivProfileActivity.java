package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.vijay.jsonwizard.utils.FormUtils;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.AllConstants;
import org.smartregister.chw.core.activity.CoreHivProfileActivity;
import org.smartregister.chw.core.activity.CoreHivUpcomingServicesActivity;
import org.smartregister.chw.core.listener.OnClickFloatingMenu;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.adapter.HivAndTbReferralCardViewAdapter;
import org.smartregister.chw.hf.contract.HivProfileContract;
import org.smartregister.chw.hf.custom_view.HivFloatingMenu;
import org.smartregister.chw.hf.interactor.HfHivProfileInteractor;
import org.smartregister.chw.hf.model.HivTbReferralTasksAndFollowupFeedbackModel;
import org.smartregister.chw.hf.presenter.HivProfilePresenter;
import org.smartregister.chw.hiv.activity.BaseHivRegistrationFormsActivity;
import org.smartregister.chw.hiv.domain.HivMemberObject;
import org.smartregister.chw.hiv.util.HivUtil;
import org.smartregister.chw.tb.util.Constants;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.opd.pojo.RegisterParams;
import org.smartregister.opd.utils.OpdConstants;
import org.smartregister.opd.utils.OpdJsonFormUtils;
import org.smartregister.opd.utils.OpdUtils;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import timber.log.Timber;

public class HivProfileActivity extends CoreHivProfileActivity implements HivProfileContract.View {

    private CommonPersonObjectClient commonPersonObjectClient;

    public static void startHivProfileActivity(Activity activity, HivMemberObject memberObject) {
        Intent intent = new Intent(activity, HivProfileActivity.class);
        intent.putExtra(Constants.ActivityPayload.MEMBER_OBJECT, memberObject);
        activity.startActivity(intent);
    }

    public static void startHivFollowupActivity(Activity activity, String baseEntityID) throws JSONException {
        Intent intent = new Intent(activity, BaseHivRegistrationFormsActivity.class);
        intent.putExtra(org.smartregister.chw.hiv.util.Constants.ActivityPayload.BASE_ENTITY_ID, baseEntityID);
        intent.putExtra(org.smartregister.chw.hiv.util.Constants.ActivityPayload.JSON_FORM, (new FormUtils()).getFormJsonFromRepositoryOrAssets(activity, CoreConstants.JSON_FORM.getHivFollowupVisit()).toString());
        intent.putExtra(org.smartregister.chw.hiv.util.Constants.ActivityPayload.ACTION, Constants.ActivityPayloadType.FOLLOW_UP_VISIT);
        intent.putExtra(org.smartregister.chw.hiv.util.Constants.ActivityPayload.USE_DEFAULT_NEAT_FORM_LAYOUT, false);

        activity.startActivityForResult(intent, org.smartregister.chw.anc.util.Constants.REQUEST_CODE_HOME_VISIT);
    }

    public void setReferralTasksAndFollowupFeedback(List<HivTbReferralTasksAndFollowupFeedbackModel> tasksAndFollowupFeedbackModels) {
        if (notificationAndReferralRecyclerView != null && tasksAndFollowupFeedbackModels.size() > 0) {
            RecyclerView.Adapter mAdapter = new HivAndTbReferralCardViewAdapter(tasksAndFollowupFeedbackModels, this, getCommonPersonObjectClient(), CoreConstants.REGISTERED_ACTIVITIES.FP_REGISTER_ACTIVITY);
            notificationAndReferralRecyclerView.setAdapter(mAdapter);
            notificationAndReferralLayout.setVisibility(View.VISIBLE);
            findViewById(R.id.view_notification_and_referral_row).setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onCreation() {
        super.onCreation();
        setCommonPersonObjectClient(getClientDetailsByBaseEntityID(getHivMemberObject().getBaseEntityId()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((HivProfileContract.Presenter) getHivProfilePresenter()).fetchReferralTasks();
        if (notificationAndReferralRecyclerView != null && notificationAndReferralRecyclerView.getAdapter() != null) {
            notificationAndReferralRecyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    public CommonPersonObjectClient getCommonPersonObjectClient() {
        return commonPersonObjectClient;
    }

    public void setCommonPersonObjectClient(CommonPersonObjectClient commonPersonObjectClient) {
        this.commonPersonObjectClient = commonPersonObjectClient;
    }

    @Override
    protected void initializePresenter() {
        showProgressBar(true);
        setHivProfilePresenter(new HivProfilePresenter(this, new HfHivProfileInteractor(this), getHivMemberObject()));
        fetchProfileData();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        try {
            if (itemId == R.id.action_hiv_outcome) {
                HivRegisterActivity.startHIVFormActivity(this, getHivMemberObject().getBaseEntityId(), CoreConstants.JSON_FORM.getHivOutcome(), (new FormUtils()).getFormJsonFromRepositoryOrAssets(this, CoreConstants.JSON_FORM.getHivOutcome()).toString());
                return true;
            } else if (itemId == R.id.action_issue_hiv_community_followup_referral) {
                HivRegisterActivity.startHIVFormActivity(this, getHivMemberObject().getBaseEntityId(), CoreConstants.JSON_FORM.getHivCommunityFollowupReferral(), (new FormUtils()).getFormJsonFromRepositoryOrAssets(this, CoreConstants.JSON_FORM.getHivCommunityFollowupReferral()).toString());
                return true;
            }
        } catch (JSONException e) {
            Timber.e(e);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(org.smartregister.chw.core.R.menu.hiv_profile_menu, menu);
        menu.findItem(R.id.action_hiv_outcome).setVisible(true);
        menu.findItem(R.id.action_issue_hiv_community_followup_referral).setVisible(true);
        return true;
    }


    @Override
    public void updateLastVisitRow(Date lastVisitDate) {
        //overriding showing of last visit row
    }

    @Override
    public void setupFollowupVisitEditViews(boolean isWithin24Hours) {
        //overriding setupFollowupVisitEditViews row
    }


    @Override
    public void openHivRegistrationForm() {
        try {
            HivRegisterActivity.startHIVFormActivity(this, getHivMemberObject().getBaseEntityId(), CoreConstants.JSON_FORM.getHivRegistration(), (new FormUtils()).getFormJsonFromRepositoryOrAssets(this, CoreConstants.JSON_FORM.getHivRegistration()).toString());
        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    @Override
    public void openUpcomingServices() {
        CoreHivUpcomingServicesActivity.startMe(this, HivUtil.toMember(getHivMemberObject()));
    }

    @Override
    public void openIndexClientsList(HivMemberObject hivMemberObject) {
        IndexContactsListActivity.startHivClientIndexListActivity(this, Objects.requireNonNull(hivMemberObject));
    }

    @Override
    public void openFamilyDueServices() {
        Intent intent = new Intent(this, FamilyProfileActivity.class);

        intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID, getHivMemberObject().getFamilyBaseEntityId());
        intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.FAMILY_HEAD, getHivMemberObject().getFamilyHead());
        intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.PRIMARY_CAREGIVER, getHivMemberObject().getPrimaryCareGiver());
        intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.FAMILY_NAME, getHivMemberObject().getFamilyName());

        intent.putExtra(CoreConstants.INTENT_KEY.SERVICE_DUE, true);
        startActivity(intent);
    }

    @Override
    public void openFollowUpVisitForm(boolean isEdit) {
        if (!isEdit) {
            try {
                startHivFollowupActivity(this, getHivMemberObject().getBaseEntityId());
            } catch (JSONException e) {
                Timber.e(e);
            }
        }
    }

    @Override
    protected void removeMember() {
        // Not required for HF (as seen in other profile activities)?
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        int id = view.getId();
        if (id == R.id.record_hiv_followup_visit) {
            openFollowUpVisitForm(false);
        }
    }

    @Override
    public void initializeCallFAB() {
        setHivFloatingMenu(new HivFloatingMenu(this, getHivMemberObject()));

        OnClickFloatingMenu onClickFloatingMenu = viewId -> {
            switch (viewId) {
                case R.id.hiv_fab:
                    checkPhoneNumberProvided();
                    ((HivFloatingMenu) getHivFloatingMenu()).animateFAB();
                    break;
                case R.id.call_layout:
                    ((HivFloatingMenu) getHivFloatingMenu()).launchCallWidget();
                    ((HivFloatingMenu) getHivFloatingMenu()).animateFAB();
                    break;
                case R.id.register_index_clients_layout:
                    Timber.d("Register Index Clients FAB clicked");
                    startHivIndexClientsRegistration();
                    break;
                default:
                    Timber.d("Unknown fab action");
                    break;
            }
        };

        ((HivFloatingMenu) getHivFloatingMenu()).setFloatMenuClickListener(onClickFloatingMenu);
        getHivFloatingMenu().setGravity(Gravity.BOTTOM | Gravity.END);
        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        addContentView(getHivFloatingMenu(), linearLayoutParams);
    }

    private void checkPhoneNumberProvided() {
        boolean phoneNumberAvailable = (StringUtils.isNotBlank(getHivMemberObject().getPhoneNumber())
                || StringUtils.isNotBlank(getHivMemberObject().getPrimaryCareGiverPhoneNumber()));

        ((HivFloatingMenu) getHivFloatingMenu()).redraw(phoneNumberAvailable);
    }

    @Override
    public Context getContext() {
        return HivProfileActivity.this;
    }

    @Override
    public void verifyHasPhone() {
        // TODO -> Implement for HF
    }

    @Override
    public void notifyHasPhone(boolean b) {
        // TODO -> Implement for HF
    }

    @Override
    public void openMedicalHistory() {
        //TODO implement
    }

    private void startHivIndexClientsRegistration() {
        try {
            String locationId = org.smartregister.family.util.Utils.context().allSharedPreferences().getPreference(AllConstants.CURRENT_LOCATION_ID);

            ((HivProfilePresenter) getHivProfilePresenter()).startForm(CoreConstants.JSON_FORM.getHivIndexClientsRegistrationForm(), null, null, locationId);
        } catch (Exception e) {
            Timber.e(e);
            displayToast(org.smartregister.family.R.string.error_unable_to_start_form);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) return;
        try {
            String jsonString = data.getStringExtra(OpdConstants.JSON_FORM_EXTRA.JSON);
            Timber.d("JSONResult : %s", jsonString);

            JSONObject form = new JSONObject(jsonString);
            String encounterType = form.getString(OpdJsonFormUtils.ENCOUNTER_TYPE);
            if (encounterType.equals(CoreConstants.EventType.FAMILY_REGISTRATION)) {
                RegisterParams registerParam = new RegisterParams();
                registerParam.setEditMode(false);
                registerParam.setFormTag(OpdJsonFormUtils.formTag(OpdUtils.context().allSharedPreferences()));
                showProgressDialog(org.smartregister.chw.core.R.string.saving_dialog_title);
                ((HivProfilePresenter) getHivProfilePresenter()).saveForm(jsonString, registerParam);
            }
        } catch (Exception e) {
            Timber.e(e);
        }
    }

}

