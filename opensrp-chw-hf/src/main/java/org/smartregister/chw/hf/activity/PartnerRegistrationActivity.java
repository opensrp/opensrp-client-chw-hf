package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONObject;
import org.smartregister.chw.anc.util.NCUtils;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.BuildConfig;
import org.smartregister.chw.hf.R;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.clientandeventmodel.Obs;
import org.smartregister.family.util.Utils;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.util.JsonFormUtils;
import org.smartregister.view.activity.SecuredActivity;

import java.util.ArrayList;
import java.util.Date;

import androidx.constraintlayout.widget.ConstraintLayout;

import static org.smartregister.chw.hf.utils.Constants.Events.PARTNER_REGISTRATION_EVENT;
import static org.smartregister.chw.hf.utils.Constants.PartnerRegistrationConstants.BASE_ENTITY_ID;
import static org.smartregister.chw.hf.utils.Constants.PartnerRegistrationConstants.EXISTING_PARTNER_REQUEST_CODE;
import static org.smartregister.chw.hf.utils.Constants.PartnerRegistrationConstants.PARTNER_BASE_ENTITY_ID;

public class PartnerRegistrationActivity extends SecuredActivity implements View.OnClickListener {

    private String clientBaseEntityId;


    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_partner_registration);
        this.clientBaseEntityId = getIntent().getStringExtra(BASE_ENTITY_ID);
        setupView();
    }


    @Override
    protected void onResumption() {
        //overridden
    }

    public void setupView() {
        ImageView closeImageView = findViewById(R.id.close);
        ConstraintLayout newClientRegistrationView = findViewById(R.id.new_client_registration);
        ConstraintLayout existingClientRegistrationView = findViewById(R.id.existing_client_registration);

        newClientRegistrationView.setOnClickListener(this);
        existingClientRegistrationView.setOnClickListener(this);
        closeImageView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.close) {
            finish();
        } else if (id == R.id.new_client_registration) {
            startPartnerRegistration();
        } else if (id == R.id.existing_client_registration) {
            searchForPartner();
        }
    }

    private void startPartnerRegistration() {
        //implement
    }

    private void searchForPartner() {
        startActivityForResult(new Intent(this,AllMaleClientsActivity.class),EXISTING_PARTNER_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == EXISTING_PARTNER_REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK){
                String partner_id = data.getStringExtra(BASE_ENTITY_ID);
                savePartnerDetails(partner_id, clientBaseEntityId);
                Toast.makeText(this,partner_id,Toast.LENGTH_SHORT).show();
                finish();
            }
        }

    }

    protected void savePartnerDetails(String partnerBaseEntityId, String clientBaseEntityId) {
        AllSharedPreferences sharedPreferences = Utils.getAllSharedPreferences();
        Event baseEvent = (Event) new Event()
                .withBaseEntityId(clientBaseEntityId)
                .withEventDate(new Date())
                .withEventType(PARTNER_REGISTRATION_EVENT)
                .withFormSubmissionId(JsonFormUtils.generateRandomUUIDString())
                .withEntityType(CoreConstants.TABLE_NAME.ANC_MEMBER)
                .withProviderId(sharedPreferences.fetchRegisteredANM())
                .withLocationId(sharedPreferences.fetchDefaultLocalityId(sharedPreferences.fetchRegisteredANM()))
                .withTeamId(sharedPreferences.fetchDefaultTeamId(sharedPreferences.fetchRegisteredANM()))
                .withTeam(sharedPreferences.fetchDefaultTeam(sharedPreferences.fetchRegisteredANM()))
                .withClientDatabaseVersion(BuildConfig.DATABASE_VERSION)
                .withClientApplicationVersion(BuildConfig.VERSION_CODE)
                .withDateCreated(new Date());

        baseEvent.addObs(
                (new Obs())
                        .withFormSubmissionField(PARTNER_BASE_ENTITY_ID)
                        .withValue(partnerBaseEntityId)
                        .withFieldCode(PARTNER_BASE_ENTITY_ID)
                        .withFieldType("formsubmissionField")
                        .withFieldDataType("text")
                        .withParentCode("")
                        .withHumanReadableValues(new ArrayList<>()));
        // tag docs
        org.smartregister.chw.hf.utils.JsonFormUtils.tagSyncMetadata(Utils.context().allSharedPreferences(), baseEvent);
        try {
            NCUtils.processEvent(baseEvent.getBaseEntityId(), new JSONObject(org.smartregister.chw.anc.util.JsonFormUtils.gson.toJson(baseEvent)));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}