package org.smartregister.chw.hf.activity;

import static org.smartregister.chw.core.utils.Utils.passToolbarTitle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import org.smartregister.chw.core.activity.BaseReferralTaskViewActivity;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.model.HivIndexFollowupFeedbackDetailsModel;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.Utils;
import org.smartregister.view.customcontrols.CustomFontTextView;

import timber.log.Timber;

public class CommunityIndexFollowupFeedbackViewActivity extends BaseReferralTaskViewActivity {
    public HivIndexFollowupFeedbackDetailsModel followupFeedbackDetailsModel;

    private LinearLayout testLocationLayout;
    private LinearLayout chwClientFoundLayout;
    private LinearLayout chwClientAgreeTestingLayout;

    private CustomFontTextView testLocation;

    private CustomFontTextView chwClientFound;

    private CustomFontTextView chwClientAgreeTesting;


    public static void startCommunityIndexFollowupFeedbackViewActivity(Activity activity, CommonPersonObjectClient personObjectClient, String startingActivity, HivIndexFollowupFeedbackDetailsModel followupFeedbackDetailsModel) {
        Intent intent = new Intent(activity, CommunityIndexFollowupFeedbackViewActivity.class);
        CommunityIndexFollowupFeedbackViewActivity.personObjectClient = personObjectClient;
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
            extraDetails();
            extraFollowupFeedback();
            setStartingActivity((String) getIntent().getSerializableExtra(CoreConstants.INTENT_KEY.STARTING_ACTIVITY));
            inflateToolbar();
            setUpViews();
        }
    }


    private void setUpViews() {
        clientName = findViewById(R.id.client_name);
        careGiverPhone = findViewById(R.id.care_giver_phone);

        chwClientFound = findViewById(R.id.woman_ga);
        chwClientFoundLayout = findViewById(R.id.woman_ga_layout);
        CustomFontTextView chwClientFoundLabel = findViewById(R.id.woman_ga_label);

        testLocationLayout = findViewById(R.id.referral_date_layout);
        CustomFontTextView testLocationLabel = findViewById(R.id.referral_date_label);
        testLocation = findViewById(R.id.referral_date);

        chwClientAgreeTestingLayout = findViewById(R.id.client_referral_problem_layout);
        chwClientAgreeTesting = findViewById(R.id.client_referral_problem);
        CustomFontTextView chwClientAgreeTestingLabel = findViewById(R.id.cclient_referral_problem_label);

        chwClientAgreeTestingLabel.setText(getString(R.string.chw_agreed_to_be_tested));
        testLocationLabel.setText(getString(R.string.chw_test_location));
        chwClientFoundLabel.setText(R.string.chw_client_found);

        findViewById(R.id.view_profile).setVisibility(View.GONE);
        findViewById(R.id.mark_ask_done).setVisibility(View.GONE);
        findViewById(R.id.chw_details_bottom_layout).setVisibility(View.GONE);
        getReferralDetails();
    }

    private void setStartingActivity(String startingActivity) {
        this.startingActivity = startingActivity;
    }

    protected void extraFollowupFeedback() {
        followupFeedbackDetailsModel = (HivIndexFollowupFeedbackDetailsModel) getIntent().getSerializableExtra(CoreConstants.INTENT_KEY.USERS_TASKS);
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
    protected void getReferralDetails() {
        if (getPersonObjectClient() != null) {
            String clientAge = (Utils.getTranslatedDate(Utils.getDuration(Utils.getValue(getPersonObjectClient().getColumnmaps(), DBConstants.KEY.DOB, false)), getBaseContext()));
            clientName.setText(getString(org.smartregister.chw.core.R.string.client_name_age_suffix, name, clientAge));

            if(followupFeedbackDetailsModel.getFollowedByChw().equalsIgnoreCase("true")){
                chwClientFoundLayout.setVisibility(View.VISIBLE);
                chwClientFound.setText(followupFeedbackDetailsModel.getClientFound());
                if(followupFeedbackDetailsModel.getClientFound().equalsIgnoreCase("Yes")){
                    chwClientAgreeTesting.setText(followupFeedbackDetailsModel.getAgreedToBeTested());
                    if(followupFeedbackDetailsModel.getAgreedToBeTested().equalsIgnoreCase("yes")){
                        testLocation.setText(followupFeedbackDetailsModel.getTestLocation());
                    }else{
                        testLocationLayout.setVisibility(View.GONE);
                    }
                }else{
                    chwClientAgreeTestingLayout.setVisibility(View.GONE);
                }
            }else{
                chwClientFoundLayout.setVisibility(View.GONE);
            }

            String familyMemberContacts = getFamilyMemberContacts();
            careGiverPhone.setText(familyMemberContacts.isEmpty() ? getString(org.smartregister.chw.core.R.string.phone_not_provided) : familyMemberContacts);
        }
    }

    @Override
    protected void onCreation() {
        //overridden
    }

    @Override
    protected void onResumption() {
        //overridden
    }
}
