package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.apache.commons.lang3.StringUtils;
import org.jeasy.rules.api.Rules;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.util.Constants;
import org.smartregister.chw.anc.util.DBConstants;
import org.smartregister.chw.anc.util.NCUtils;
import org.smartregister.chw.core.activity.CoreAncMemberProfileActivity;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.interactor.CoreAncMemberProfileInteractor;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.core.utils.HomeVisitUtil;
import org.smartregister.chw.core.utils.VisitSummary;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.adapter.ReferralCardViewAdapter;
import org.smartregister.chw.hf.dao.HfAncDao;
import org.smartregister.chw.hf.model.FamilyProfileModel;
import org.smartregister.chw.hf.presenter.AncMemberProfilePresenter;
import org.smartregister.chw.hf.utils.VisitUtils;
import org.smartregister.chw.hiv.dao.HivDao;
import org.smartregister.chw.pmtct.dao.PmtctDao;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.commonregistry.AllCommonsRepository;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.AlertStatus;
import org.smartregister.domain.Task;
import org.smartregister.family.domain.FamilyEventClient;
import org.smartregister.family.interactor.FamilyProfileInteractor;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;
import org.smartregister.repository.AllSharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

import androidx.recyclerview.widget.RecyclerView;
import timber.log.Timber;

import static org.smartregister.chw.core.utils.Utils.passToolbarTitle;
import static org.smartregister.chw.core.utils.Utils.updateToolbarTitle;
import static org.smartregister.chw.hf.utils.Constants.Events.ANC_FIRST_FACILITY_VISIT;
import static org.smartregister.chw.hf.utils.Constants.Events.ANC_RECURRING_FACILITY_VISIT;

public class AncMemberProfileActivity extends CoreAncMemberProfileActivity {
    private CommonPersonObjectClient commonPersonObjectClient;
    private boolean hivPositive;
    public static void startMe(Activity activity, String baseEntityID) {
        Intent intent = new Intent(activity, AncMemberProfileActivity.class);
        passToolbarTitle(activity, intent);
        intent.putExtra(Constants.ANC_MEMBER_OBJECTS.BASE_ENTITY_ID, baseEntityID);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreation() {
        super.onCreation();
        findViewById(R.id.record_visit_panel).setVisibility(View.VISIBLE);
        setCommonPersonObjectClient(getClientDetailsByBaseEntityID(baseEntityID));
    }

    @Override
    public void initializeFloatingMenu() {
        super.initializeFloatingMenu();
        if (baseAncFloatingMenu != null) {
            FloatingActionButton floatingActionButton = baseAncFloatingMenu.findViewById(R.id.anc_fab);
            if (floatingActionButton != null)
                floatingActionButton.setImageResource(R.drawable.floating_call);
        }
    }

    @Override
    protected void registerPresenter() {
        presenter = new AncMemberProfilePresenter(this, new CoreAncMemberProfileInteractor(this),memberObject);
    }

    @Override
    public boolean usesPregnancyRiskProfileLayout() {
        return true;
    }

    @Override
    public void setUpComingServicesStatus(String service, AlertStatus status, Date date) {
        view_most_due_overdue_row.setVisibility(View.GONE);
        rlUpcomingServices.setVisibility(View.GONE);
    }

    @Override
    public void setFamilyStatus(AlertStatus status) {
        view_family_row.setVisibility(View.GONE);
        rlFamilyServicesDue.setVisibility(View.GONE);
    }

    @Override
    public void setFamilyLocation() {
        if (!StringUtils.isBlank(getMemberGPS())) {
            view_family_location_row.setVisibility(View.VISIBLE);
            rlFamilyLocation.setVisibility(View.VISIBLE);
        }
    }

    private String getMemberGPS() {
        return memberObject.getGps();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.anc_danger_signs_outcome).setVisible(true);
        menu.findItem(R.id.action_anc_registration).setVisible(false);
        menu.findItem(R.id.action_remove_member).setVisible(false);
        menu.findItem(R.id.action_pregnancy_out_come).setVisible(!HfAncDao.isClientClosed(baseEntityID));
        menu.findItem(R.id.action_malaria_diagnosis).setVisible(false);
        menu.findItem(R.id.action_pmtct_register).setVisible(!PmtctDao.isRegisteredForPmtct(baseEntityID) && (hivPositive || HivDao.isRegisteredForHiv(baseEntityID)));
        return true;
    }

    @Override // to chw
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == JsonFormUtils.REQUEST_CODE_GET_JSON && resultCode == RESULT_OK) {
            try {
                String jsonString = data.getStringExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON);
                JSONObject form = new JSONObject(jsonString);
                String encounterType = form.getString(JsonFormUtils.ENCOUNTER_TYPE);
                if (encounterType.equals(Utils.metadata().familyMemberRegister.updateEventType)) {
                    FamilyEventClient familyEventClient =
                            new FamilyProfileModel(memberObject.getFamilyName()).processUpdateMemberRegistration(jsonString, memberObject.getBaseEntityId());
                    new FamilyProfileInteractor().saveRegistration(familyEventClient, jsonString, true, ancMemberProfilePresenter());
                } else if (encounterType.equals(CoreConstants.EventType.UPDATE_ANC_REGISTRATION)) {
                    AllSharedPreferences allSharedPreferences = org.smartregister.util.Utils.getAllSharedPreferences();
                    Event baseEvent = org.smartregister.chw.anc.util.JsonFormUtils.processJsonForm(allSharedPreferences, jsonString, Constants.TABLES.ANC_MEMBERS);
                    NCUtils.processEvent(baseEvent.getBaseEntityId(), new JSONObject(org.smartregister.chw.anc.util.JsonFormUtils.gson.toJson(baseEvent)));
                    AllCommonsRepository commonsRepository = CoreChwApplication.getInstance().getAllCommonsRepository(CoreConstants.TABLE_NAME.ANC_MEMBER);

                    JSONArray field = org.smartregister.util.JsonFormUtils.fields(form);
                    JSONObject phoneNumberObject = org.smartregister.util.JsonFormUtils.getFieldJSONObject(field, DBConstants.KEY.PHONE_NUMBER);
                    String phoneNumber = phoneNumberObject.getString(CoreJsonFormUtils.VALUE);
                    String baseEntityId = baseEvent.getBaseEntityId();
                    if (commonsRepository != null) {
                        ContentValues values = new ContentValues();
                        values.put(DBConstants.KEY.PHONE_NUMBER, phoneNumber);
                        CoreChwApplication.getInstance().getRepository().getWritableDatabase().update(CoreConstants.TABLE_NAME.ANC_MEMBER, values, DBConstants.KEY.BASE_ENTITY_ID + " = ?  ", new String[]{baseEntityId});
                    }
                } else if (encounterType.equals(CoreConstants.EventType.ANC_DANGER_SIGNS_OUTCOME)) {
                    ancMemberProfilePresenter().createAncDangerSignsOutcomeEvent(Utils.getAllSharedPreferences(), jsonString, baseEntityID);
                }
            } catch (Exception e) {
                Timber.e(e, "AncMemberProfileActivity -- > onActivityResult");
            }
        }
        invalidateOptionsMenu();
    }

    @Override
    public void startFormForEdit(Integer title_resource, String formName) {
        try {
            JSONObject form = CoreJsonFormUtils.getAncPncForm(title_resource, formName, memberObject, this);
            startActivityForResult(CoreJsonFormUtils.getAncPncStartFormIntent(form, this), JsonFormUtils.REQUEST_CODE_GET_JSON);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public void openMedicalHistory() {
        AncMedicalHistoryActivity.startMe(this, memberObject);
    }

    @Override
    public void openUpcomingService() {
        //// TODO: 29/08/19
    }

    @Override
    public void openFamilyDueServices() {
        Intent intent = new Intent(this, FamilyProfileActivity.class);

        intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID, memberObject.getFamilyBaseEntityId());
        intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.FAMILY_HEAD, memberObject.getFamilyHead());
        intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.PRIMARY_CAREGIVER, memberObject.getPrimaryCareGiver());
        intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.FAMILY_NAME, memberObject.getFamilyName());
        intent.putExtra(CoreConstants.INTENT_KEY.SERVICE_DUE, true);
        startActivity(intent);
    }

    @Override
    public void setupViews() {
        super.setupViews();
        updateToolbarTitle(this, org.smartregister.chw.core.R.id.toolbar_title, memberObject.getFamilyName());
        if(!HfAncDao.isClientClosed(baseEntityID)){
            getButtonStatus();
            try {
                VisitUtils.processVisits();
            } catch (Exception e) {
                Timber.e(e);
            }
            Visit firstVisit = getVisit(ANC_FIRST_FACILITY_VISIT);
            Visit lastVisit = getVisit(ANC_RECURRING_FACILITY_VISIT);
            setHivPositive(firstVisit);
            if(firstVisit == null){
                textview_record_anc_visit.setText(R.string.record_anc_first_visit);
            }else{
                textview_record_anc_visit.setText(R.string.record_anc_followup_visit);
            }

            if(lastVisit == null){
                if(firstVisit != null){
                  checkVisitStatus(firstVisit);
               }
            }else{
                checkVisitStatus(lastVisit);
            }
        }
        else{
            layoutRecordView.setVisibility(View.GONE);
            pregnancyRiskLabel.setTextSize(12);
        }

        if (baseAncFloatingMenu != null) {
            FloatingActionButton floatingActionButton = baseAncFloatingMenu.findViewById(R.id.anc_fab);
            if (floatingActionButton != null)
                floatingActionButton.setImageResource(R.drawable.floating_call);
        }
    }

    private boolean setHivPositive(Visit firstVisit) {
        hivPositive = false;
       if(firstVisit != null){
            try {
                JSONObject jsonObject = new JSONObject(firstVisit.getJson());
                JSONArray obs = jsonObject.getJSONArray("obs");
                int obsSize = obs.length();
                for(int i = 0; i < obsSize; i++){
                    JSONObject checkObj = obs.getJSONObject(i);
                    if(checkObj.getString("fieldCode").equalsIgnoreCase("hiv")){
                       JSONArray values = checkObj.getJSONArray("values");
                        if(values.getString(0).equalsIgnoreCase("positive")){
                            hivPositive = true;
                            break;
                        }
                    }
                }
            }catch (JSONException e){
                Timber.e(e);
            }
       }
        return hivPositive;
    }

    private void checkVisitStatus(Visit firstVisit) {
            boolean visitDone = firstVisit.getProcessed();
            if (!visitDone) {
                showVisitInProgress();
                textViewUndo.setVisibility(View.GONE);
                textViewAncVisitNot.setVisibility(View.GONE);
            }else{
                getButtonStatus();
            }
    }

    private void showVisitInProgress() {
       layoutRecordView.setVisibility(View.GONE);
       tvEdit.setVisibility(View.VISIBLE);
       layoutNotRecordView.setVisibility(View.VISIBLE);
       textViewNotVisitMonth.setText(getContext().getString(R.string.visit_in_progress,"ANC"));
       imageViewCross.setImageResource(org.smartregister.chw.core.R.drawable.activityrow_notvisited);
    }


    @Override
    public void setClientTasks(Set<Task> taskList) {
        if (notificationAndReferralRecyclerView != null && taskList.size() > 0) {
            RecyclerView.Adapter mAdapter = new ReferralCardViewAdapter(taskList, this, getCommonPersonObjectClient(), CoreConstants.REGISTERED_ACTIVITIES.ANC_REGISTER_ACTIVITY);
            notificationAndReferralRecyclerView.setAdapter(mAdapter);
            notificationAndReferralLayout.setVisibility(View.VISIBLE);
            findViewById(R.id.view_notification_and_referral_row).setVisibility(View.VISIBLE);
        }
    }

    public CommonPersonObjectClient getCommonPersonObjectClient() {
        return commonPersonObjectClient;
    }

    public void setCommonPersonObjectClient(CommonPersonObjectClient commonPersonObjectClient) {
        this.commonPersonObjectClient = commonPersonObjectClient;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        // implemented but not used.
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeNotificationReferralRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ancMemberProfilePresenter().fetchTasks();
        if (notificationAndReferralRecyclerView != null && notificationAndReferralRecyclerView.getAdapter() != null) {
            notificationAndReferralRecyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        int id = view.getId();
        if (id == R.id.textview_record_visit || id == R.id.textview_record_reccuring_visit) {
            Visit firstVisit = getVisit(ANC_FIRST_FACILITY_VISIT);
            if (firstVisit == null) {
                AncFirstFacilityVisitActivity.startMe(this, memberObject.getBaseEntityId(), false);
            } else {
                AncRecurringFacilityVisitActivity.startMe(this, memberObject.getBaseEntityId(), false);
            }
        } else if (id == R.id.textview_edit) {
            Visit lastVisit = getVisit(ANC_RECURRING_FACILITY_VISIT);
            if (lastVisit == null)
                AncFirstFacilityVisitActivity.startMe(this, memberObject.getBaseEntityId(), true);
            else
                AncRecurringFacilityVisitActivity.startMe(this, memberObject.getBaseEntityId(), true);
        }
    }

    private void getButtonStatus() {
        openVisitMonthView();
        textViewUndo.setVisibility(View.GONE);

        Rules rules = CoreChwApplication.getInstance().getRulesEngineHelper().rules(CoreConstants.RULE_FILE.ANC_HOME_VISIT);
        Visit lastNotDoneVisit = AncLibrary.getInstance().visitRepository().getLatestVisit(baseEntityID, org.smartregister.chw.anc.util.Constants.EVENT_TYPE.ANC_HOME_VISIT_NOT_DONE);
        if (lastNotDoneVisit != null) {
            Visit lastNotDoneVisitUndo = AncLibrary.getInstance().visitRepository().getLatestVisit(baseEntityID, org.smartregister.chw.anc.util.Constants.EVENT_TYPE.ANC_HOME_VISIT_NOT_DONE_UNDO);
            if (lastNotDoneVisitUndo != null
                    && lastNotDoneVisitUndo.getDate().after(lastNotDoneVisit.getDate())) {
                lastNotDoneVisit = null;
            }
        }
        Visit latestVisit = getVisit(ANC_FIRST_FACILITY_VISIT);
        String visitDate = latestVisit != null ? new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(latestVisit.getDate()) : null;
        String lastVisitNotDone = lastNotDoneVisit != null ? new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(lastNotDoneVisit.getDate()) : null;

        VisitSummary visitSummary = HomeVisitUtil.getAncVisitStatus(this, rules, visitDate, lastVisitNotDone, getDateCreated());

        String visitStatus = visitSummary.getVisitStatus();
        String monthString = null;
        if(latestVisit != null){
            Calendar cal = Calendar.getInstance();
            int offset = cal.getTimeZone().getOffset(cal.getTimeInMillis());

            Long longDate = latestVisit.getDate().getTime();
            Date date = new Date(longDate - (long) offset);
            monthString = (String) DateFormat.format("MMMM", date);
        }


        if (visitStatus.equalsIgnoreCase(CoreConstants.VISIT_STATE.OVERDUE)) {
            textview_record_anc_visit.setBackgroundResource(org.smartregister.chw.core.R.drawable.record_btn_selector_overdue);
            getLayoutVisibility();
        } else if (visitStatus.equalsIgnoreCase(CoreConstants.VISIT_STATE.DUE) || visitStatus.equalsIgnoreCase("VISIT_THIS_MONTH")) {
            textview_record_anc_visit.setBackgroundResource(org.smartregister.chw.core.R.drawable.record_btn_anc_selector);
            getLayoutVisibility();
        } else if (visitStatus.equalsIgnoreCase(CoreConstants.VISIT_STATE.NOT_VISIT_THIS_MONTH)) {
            textViewUndo.setVisibility(View.VISIBLE);
            textViewUndo.setText(getString(org.smartregister.chw.opensrp_chw_anc.R.string.undo));
            record_reccuringvisit_done_bar.setVisibility(View.GONE);
            openVisitMonthView();
        }else if(visitStatus.equalsIgnoreCase("LESS_TWENTY_FOUR")){
            layoutNotRecordView.setVisibility(View.VISIBLE);
            textViewNotVisitMonth.setText(getContext().getString(org.smartregister.chw.core.R.string.anc_visit_done, monthString));
            tvEdit.setVisibility(View.GONE);
            imageViewCross.setImageResource(org.smartregister.chw.core.R.drawable.activityrow_visited);
        }
    }

    @Override
    public void setPregnancyRiskLabel(String pregnancyRiskLevel) {
        if (pregnancyRiskLabel != null && StringUtils.isNotBlank(pregnancyRiskLevel)) {
            int labelTextColor;
            int background;
            String labelText;
            switch (pregnancyRiskLevel) {
                case Constants.HOME_VISIT.PREGNANCY_RISK_LOW:
                    labelTextColor = context().getColorResource(org.smartregister.chw.opensrp_chw_anc.R.color.low_risk_text_green);
                    background = org.smartregister.chw.opensrp_chw_anc.R.drawable.low_risk_label;
                    labelText = getContext().getString(org.smartregister.chw.opensrp_chw_anc.R.string.low_pregnancy_risk);
                    break;
                case Constants.HOME_VISIT.PREGNANCY_RISK_MEDIUM:
                    labelTextColor = context().getColorResource(org.smartregister.chw.opensrp_chw_anc.R.color.medium_risk_text_orange);
                    background = org.smartregister.chw.opensrp_chw_anc.R.drawable.medium_risk_label;
                    labelText = getContext().getString(org.smartregister.chw.opensrp_chw_anc.R.string.medium_pregnancy_risk);
                    break;
                case Constants.HOME_VISIT.PREGNANCY_RISK_HIGH:
                    labelTextColor = context().getColorResource(org.smartregister.chw.opensrp_chw_anc.R.color.high_risk_text_red);
                    background = org.smartregister.chw.opensrp_chw_anc.R.drawable.high_risk_label;
                    labelText = getContext().getString(org.smartregister.chw.opensrp_chw_anc.R.string.high_pregnancy_risk);
                    break;
                case org.smartregister.chw.hf.utils.Constants.Visits.TERMINATED:
                    labelTextColor = context().getColorResource(org.smartregister.chw.opensrp_chw_anc.R.color.high_risk_text_red);
                    background = org.smartregister.chw.opensrp_chw_anc.R.drawable.high_risk_label;
                    labelText = "Services Ended";
                    break;
                default:
                    labelTextColor = context().getColorResource(org.smartregister.chw.opensrp_chw_anc.R.color.default_risk_text_black);
                    background = org.smartregister.chw.opensrp_chw_anc.R.drawable.risk_label;
                    labelText = getContext().getString(org.smartregister.chw.opensrp_chw_anc.R.string.low_pregnancy_risk);
                    break;
            }
            pregnancyRiskLabel.setVisibility(View.VISIBLE);
            pregnancyRiskLabel.setText(labelText);
            pregnancyRiskLabel.setTextColor(labelTextColor);
            pregnancyRiskLabel.setBackgroundResource(background);
        }
    }

    @Override
    protected void displayView() {
        Visit lastAncHomeVisitNotDoneEvent = getVisit(Constants.EVENT_TYPE.ANC_HOME_VISIT_NOT_DONE);
        Visit lastAncHomeVisitNotDoneUndoEvent = getVisit(Constants.EVENT_TYPE.ANC_HOME_VISIT_NOT_DONE_UNDO);

        if (lastAncHomeVisitNotDoneEvent != null && lastAncHomeVisitNotDoneUndoEvent != null &&
                lastAncHomeVisitNotDoneUndoEvent.getDate().before(lastAncHomeVisitNotDoneEvent.getDate())
                && ancHomeVisitNotDoneEvent(lastAncHomeVisitNotDoneEvent)) {
            setVisitViews();
        } else if (lastAncHomeVisitNotDoneUndoEvent == null && lastAncHomeVisitNotDoneEvent != null && ancHomeVisitNotDoneEvent(lastAncHomeVisitNotDoneEvent)) {
            setVisitViews();
        }
        Visit firstVisit = getVisit(ANC_FIRST_FACILITY_VISIT);
        Visit lastVisit = getVisit(ANC_RECURRING_FACILITY_VISIT);
        setHivPositive(firstVisit);
        if(lastVisit == null){
            if(firstVisit != null){
                checkVisitStatus(firstVisit);
            }
        }else{
            checkVisitStatus(lastVisit);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == org.smartregister.chw.core.R.id.action_pmtct_register) {
            startPmtctRegistration();
            return true;
        }
        else if (itemId == R.id.action_pregnancy_out_come) {
            PncRegisterActivity.startPncRegistrationActivity(AncMemberProfileActivity.this, memberObject.getBaseEntityId(), null, CoreConstants.JSON_FORM.getPregnancyOutcome(), AncLibrary.getInstance().getUniqueIdRepository().getNextUniqueId().getOpenmrsId(), memberObject.getFamilyBaseEntityId(), memberObject.getFamilyName(), memberObject.getLastMenstrualPeriod());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void startPmtctRegistration() {
        try {
            PmtctRegisterActivity.startPmtctRegistrationActivity(this, baseEntityID);
        } catch (Exception e) {
            Timber.e(e);
        }
    }


}
