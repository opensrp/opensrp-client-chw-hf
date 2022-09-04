package org.smartregister.chw.hf.activity;

import static org.smartregister.chw.core.utils.Utils.passToolbarTitle;
import static org.smartregister.chw.hiv.util.Constants.Tables.HIV_COMMUNITY_FEEDBACK;
import static org.smartregister.chw.tb.util.Constants.Tables.TB_COMMUNITY_FEEDBACK;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;

import org.json.JSONObject;
import org.smartregister.chw.core.activity.BaseReferralTaskViewActivity;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.BuildConfig;
import org.smartregister.chw.hf.HealthFacilityApplication;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.model.ChwFollowupFeedbackDetailsModel;
import org.smartregister.chw.hf.utils.AllClientsUtils;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.clientandeventmodel.Obs;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.FamilyLibrary;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.Utils;
import org.smartregister.opd.utils.OpdDbConstants;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.BaseRepository;
import org.smartregister.sync.helper.ECSyncHelper;
import org.smartregister.util.JsonFormUtils;
import org.smartregister.view.customcontrols.CustomFontTextView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

import timber.log.Timber;

/**
 * Created by cozej4 on 6/21/20.
 *
 * @author cozej4 https://github.com/cozej4
 */
public class CommunityFollowupFeedbackViewActivity extends BaseReferralTaskViewActivity implements View.OnClickListener {
    public ChwFollowupFeedbackDetailsModel followupFeedbackDetailsModel;

    public static void startCommunityFollowupFeedbackViewActivity(Activity activity, CommonPersonObjectClient personObjectClient, ChwFollowupFeedbackDetailsModel followupFeedbackDetailsModel, String startingActivity) {
        Intent intent = new Intent(activity, CommunityFollowupFeedbackViewActivity.class);
        CommunityFollowupFeedbackViewActivity.personObjectClient = personObjectClient;
        intent.putExtra(CoreConstants.INTENT_KEY.USERS_TASKS, followupFeedbackDetailsModel);
        intent.putExtra(CoreConstants.INTENT_KEY.STARTING_ACTIVITY, startingActivity);
        passToolbarTitle(activity, intent);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.referrals_tasks_view_layout);
        if (getIntent().getExtras() != null) {
            extraFollowupFeedback();
            extraDetails();
            setStartingActivity((String) getIntent().getSerializableExtra(CoreConstants.INTENT_KEY.STARTING_ACTIVITY));
            inflateToolbar();
            setUpViews();
        }
    }

    protected void extraFollowupFeedback() {
        followupFeedbackDetailsModel = (ChwFollowupFeedbackDetailsModel) getIntent().getSerializableExtra(CoreConstants.INTENT_KEY.USERS_TASKS);
        if (followupFeedbackDetailsModel == null) {
            Timber.e("CommunityFollowupFeedbackViewActivity --> The followup feedback object is null");
            finish();
        }
    }

    @Override
    protected void extraDetails() {
        setClientName();
        baseEntityId = getPersonObjectClient().getCaseId();
        setFamilyHeadName((String) getIntent().getSerializableExtra(CoreConstants.INTENT_KEY.FAMILY_HEAD_NAME));
        setFamilyHeadPhoneNumber((String) getIntent().getSerializableExtra(CoreConstants.INTENT_KEY.FAMILY_HEAD_PHONE_NUMBER));
    }


    @Override
    protected void onCreation() {
        //overridden
    }

    @Override
    protected void onResumption() {
        //Overridden
    }

    public void setUpViews() {
        clientName = findViewById(R.id.client_name);
        careGiverName = findViewById(R.id.care_giver_name);
        childName = findViewById(R.id.child_name);
        careGiverPhone = findViewById(R.id.care_giver_phone);
        clientReferralProblem = findViewById(R.id.client_referral_problem);
        chwDetailsNames = findViewById(R.id.chw_details_names);
        referralDate = findViewById(R.id.referral_date);

        womanGaLayout = findViewById(R.id.woman_ga_layout);
        careGiverLayout = findViewById(R.id.care_giver_name_layout);
        childNameLayout = findViewById(R.id.child_name_layout);

        CustomFontTextView referralProblemLabel = findViewById(R.id.cclient_referral_problem_label);
        referralProblemLabel.setText(getString(R.string.followup_feedback));

        CustomFontTextView referralDateLabel = findViewById(R.id.referral_date_label);
        referralDateLabel.setText(getString(R.string.followup_date));

        womanGa = findViewById(R.id.woman_ga);
        CustomFontTextView viewProfile = findViewById(R.id.view_profile);

        CustomFontTextView markAskDone = findViewById(R.id.mark_ask_done);
        markAskDone.setOnClickListener(this);

        if (getStartingActivity().equals(CoreConstants.REGISTERED_ACTIVITIES.REFERRALS_REGISTER_ACTIVITY)) {
            viewProfile.setOnClickListener(this);
        } else {
            viewProfile.setVisibility(View.INVISIBLE);
        }
        getReferralDetails();
    }

    @Override
    protected void getReferralDetails() {
        if (getPersonObjectClient() != null) {
            String clientAge = (Utils.getTranslatedDate(Utils.getDuration(Utils.getValue(getPersonObjectClient().getColumnmaps(), DBConstants.KEY.DOB, false)), getBaseContext()));
            clientName.setText(getString(org.smartregister.chw.core.R.string.client_name_age_suffix, name, clientAge));
            referralDate.setText(org.smartregister.chw.core.utils.Utils.dd_MMM_yyyy.format(new Date(new BigDecimal(followupFeedbackDetailsModel.getFollowupFeedbackDate()).longValue())));
            clientReferralProblem.setText(followupFeedbackDetailsModel.getFollowupFeedback());
            String familyMemberContacts = getFamilyMemberContacts();
            careGiverPhone.setText(familyMemberContacts.isEmpty() ? getString(org.smartregister.chw.core.R.string.phone_not_provided) : familyMemberContacts);
            chwDetailsNames.setText(followupFeedbackDetailsModel.getChwName());
        }
    }

    public void setStartingActivity(String startingActivity) {
        this.startingActivity = startingActivity;
    }

    public void closeReferral() {
        closeReferralDialog();
    }

    private void closeReferralDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.mark_as_done_title));
        builder.setMessage(getString(R.string.mark_feedback_as_done_message));
        builder.setCancelable(true);

        builder.setPositiveButton(this.getString(R.string.mark_done), (dialog, id) -> {
            try {
                saveCloseReferralEvent();
                finish();
            } catch (Exception e) {
                Timber.e(e, "CommunityFollowupFeedbackViewActivity --> closeFeedbackDialog");
            }
        });
        builder.setNegativeButton(this.getString(R.string.cancel), ((dialog, id) -> dialog.cancel()));

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void saveCloseReferralEvent() {
        try {
            AllSharedPreferences sharedPreferences = Utils.getAllSharedPreferences();
            ECSyncHelper syncHelper = FamilyLibrary.getInstance().getEcSyncHelper();
            Event baseEvent = (Event) new Event()
                    .withBaseEntityId(getBaseEntityId())
                    .withEventDate(new Date())
                    .withFormSubmissionId(followupFeedbackDetailsModel.getFeedbackFormSubmissionId())
                    .withProviderId(sharedPreferences.fetchRegisteredANM())
                    .withTeamId(sharedPreferences.fetchDefaultTeamId(sharedPreferences.fetchRegisteredANM()))
                    .withTeam(sharedPreferences.fetchDefaultTeam(sharedPreferences.fetchRegisteredANM()))
                    .withClientDatabaseVersion(BuildConfig.DATABASE_VERSION)
                    .withClientApplicationVersion(BuildConfig.VERSION_CODE)
                    .withDateCreated(new Date());

            if (followupFeedbackDetailsModel.getFeedbackType().equals("HIV")) {
                baseEvent.setEventType(CoreConstants.EventType.CLOSE_HIV_FEEDBACK);
                baseEvent.setEntityType(HIV_COMMUNITY_FEEDBACK);
            }else if (followupFeedbackDetailsModel.getFeedbackType().equals("PMTCT")) {
                baseEvent.setEventType(CoreConstants.EventType.CLOSE_PMTCT_FEEDBACK);
                baseEvent.setEntityType(CoreConstants.TABLE_NAME.PMTCT_COMMUNITY_FEEDBACK);
            }  else {
                baseEvent.setEventType(CoreConstants.EventType.CLOSE_TB_FEEDBACK);
                baseEvent.setEntityType(TB_COMMUNITY_FEEDBACK);
            }

            baseEvent.addObs((new Obs()).withFormSubmissionField("community_feedback_id").withValue(followupFeedbackDetailsModel.getFeedbackFormSubmissionId())
                    .withFieldCode("community_feedback_id").withFieldType("formsubmissionField").withFieldDataType("text").withParentCode("").withHumanReadableValues(new ArrayList<>()));


            baseEvent.addObs((new Obs()).withFormSubmissionField("mark_as_done").withValue("1")
                    .withFieldCode("mark_as_done").withFieldType("formsubmissionField").withFieldDataType("text").withParentCode("").withHumanReadableValues(new ArrayList<>()));

            org.smartregister.chw.hf.utils.JsonFormUtils.tagSyncMetadata(Utils.context().allSharedPreferences(), baseEvent);// tag docs


            JSONObject eventJson = new JSONObject(JsonFormUtils.gson.toJson(baseEvent));
            syncHelper.addEvent(getBaseEntityId(), eventJson);
            long lastSyncTimeStamp = HealthFacilityApplication.getInstance().getContext().allSharedPreferences().fetchLastUpdatedAtDate(0);
            Date lastSyncDate = new Date(lastSyncTimeStamp);
            HealthFacilityApplication.getClientProcessor(HealthFacilityApplication.getInstance().getContext().applicationContext()).processClient(syncHelper.getEvents(lastSyncDate, BaseRepository.TYPE_Unprocessed));
            HealthFacilityApplication.getInstance().getContext().allSharedPreferences().saveLastUpdatedAtDate(lastSyncDate.getTime());
        } catch (Exception e) {
            Timber.e(e, "CommunityFollowupFeedbackViewActivity --> saveCloseFeedbackEvent");
        }

    }

    public String getBaseEntityId() {
        return baseEntityId;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.view_profile) {
            personObjectClient.getDetails().put(OpdDbConstants.KEY.REGISTER_TYPE, mapTaskFocusToRegisterType());
            AllClientsUtils.goToClientProfile(this, personObjectClient);
        } else if (view.getId() == R.id.mark_ask_done) {
            closeReferral();
        }
    }

    @NonNull
    private String mapTaskFocusToRegisterType() {
        switch (task.getFocus()) {
            case CoreConstants.TASKS_FOCUS.SICK_CHILD:
                return CoreConstants.REGISTER_TYPE.CHILD;
            case CoreConstants.TASKS_FOCUS.SUSPECTED_MALARIA:
                return CoreConstants.REGISTER_TYPE.MALARIA;
            case CoreConstants.TASKS_FOCUS.ANC_DANGER_SIGNS:
                return CoreConstants.REGISTER_TYPE.ANC;
            case CoreConstants.TASKS_FOCUS.PNC_DANGER_SIGNS:
                return CoreConstants.REGISTER_TYPE.PNC;
            case CoreConstants.TASKS_FOCUS.FP_SIDE_EFFECTS:
                return CoreConstants.REGISTER_TYPE.FAMILY_PLANNING;
            default:
                return "";
        }
    }
}