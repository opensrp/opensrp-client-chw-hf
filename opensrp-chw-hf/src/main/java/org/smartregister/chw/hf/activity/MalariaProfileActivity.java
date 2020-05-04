package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.core.activity.CoreFamilyProfileActivity;
import org.smartregister.chw.core.activity.CoreMalariaProfileActivity;
import org.smartregister.chw.core.model.CoreMalariaRegisterFragmentModel;
import org.smartregister.chw.core.presenter.CoreFamilyOtherMemberActivityPresenter;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.CoreReferralUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.adapter.ReferralCardViewAdapter;
import org.smartregister.chw.hf.contract.MalariaProfileContract;
import org.smartregister.chw.hf.presenter.FamilyOtherMemberActivityPresenter;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Task;
import org.smartregister.family.model.BaseFamilyOtherMemberProfileActivityModel;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;
import org.smartregister.view.customcontrols.CustomFontTextView;

import java.util.ArrayList;
import java.util.Set;

import timber.log.Timber;

import static org.smartregister.chw.core.utils.CoreReferralUtils.getCommonRepository;
import static org.smartregister.chw.malaria.util.Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID;

public class MalariaProfileActivity extends CoreMalariaProfileActivity implements MalariaProfileContract.InteractorCallback {

    private static boolean isStartedFromReferrals;
    private static String baseEntityId;
    public RelativeLayout referralRow;
    public RecyclerView referralRecyclerView;
    private CommonPersonObjectClient commonPersonObjectClient;
    private Set<Task> taskList;

    public static void startMalariaActivity(Activity activity, String baseEntityId) {
        MalariaProfileActivity.baseEntityId = baseEntityId;
        Intent intent = new Intent(activity, MalariaProfileActivity.class);
        intent.putExtra(BASE_ENTITY_ID, baseEntityId);
        isStartedFromReferrals = CoreReferralUtils.checkIfStartedFromReferrals(activity);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreation() {
        super.onCreation();
        this.setOnMemberTypeLoadedListener(memberType -> {
            switch (memberType.getMemberType()) {
                case CoreConstants.TABLE_NAME.ANC_MEMBER:
                    AncMedicalHistoryActivity.startMe(MalariaProfileActivity.this, memberType.getMemberObject());
                    break;
                case CoreConstants.TABLE_NAME.PNC_MEMBER:
                    PncMedicalHistoryActivity.startMe(MalariaProfileActivity.this, memberType.getMemberObject());
                    break;
                case CoreConstants.TABLE_NAME.CHILD:
                    ChildMedicalHistoryActivity.startMe(MalariaProfileActivity.this, memberType.getMemberObject());
                    break;
                default:
                    Timber.v("Member info undefined");
                    break;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        if (taskList.size() > 0) { // If we have referrals
            menu.findItem(R.id.action_malaria_followup).setVisible(true);
        }
        MenuItem item = menu.findItem(R.id.action_remove_member);
        if (item != null) {
            menu.removeItem(item.getItemId());
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == JsonFormUtils.REQUEST_CODE_GET_JSON) {
            try {
                String jsonString = data.getStringExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON);
                JSONObject form = new JSONObject(jsonString);
                String encounterType = form.getString(JsonFormUtils.ENCOUNTER_TYPE);
                if (encounterType.equals(CoreConstants.EventType.MALARIA_FOLLOW_UP_HF)) {
                    getPresenter().createHfMalariaFollowupEvent(Utils.getAllSharedPreferences(), jsonString, memberObject.getBaseEntityId(), getLocationId());
                }
            } catch (Exception ex) {
                Timber.e(ex);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private String getLocationId() {
        String locationId = null;
        if (this.taskList != null) {
            locationId = new ArrayList<>(taskList).get(taskList.size() - 1).getLocation();
        }
        return locationId;
    }

    @Override
    protected Class<? extends CoreFamilyProfileActivity> getFamilyProfileActivityClass() {
        return FamilyProfileActivity.class;
    }

    @Override
    protected void removeMember() {
        //Implemented from abstract class but not required right now for HF
    }

    @NonNull
    @Override
    public CoreFamilyOtherMemberActivityPresenter presenter() {
        if (presenter == null) {
            presenter = new FamilyOtherMemberActivityPresenter(this, new BaseFamilyOtherMemberProfileActivityModel(),
                    null, memberObject.getRelationalId(), memberObject.getBaseEntityId(),
                    memberObject.getFamilyHead(), memberObject.getPrimaryCareGiver(), memberObject.getAddress(),
                    memberObject.getLastName());
        }
        return (CoreFamilyOtherMemberActivityPresenter) presenter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateCommonPersonObjectClient();
        initializeTasksRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        super.onResumption();
        updateTitleWhenFromReferrals();
        ((FamilyOtherMemberActivityPresenter) presenter()).getReferralTasks(CoreConstants.REFERRAL_PLAN_ID, baseEntityId, this);
        if (referralRecyclerView != null && referralRecyclerView.getAdapter() != null) {
            referralRecyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    @Override
    protected void setupViews() {
        super.setupViews();
        initializeTasksRecyclerView();
    }

    private void updateTitleWhenFromReferrals() {
        if (isStartedFromReferrals) {
            ((CustomFontTextView) findViewById(R.id.toolbar_title)).setText(getString(R.string.return_to_task_details));
        }
    }

    private void initializeTasksRecyclerView() {
        referralRecyclerView = findViewById(R.id.referral_card_recycler_view);
        referralRow = findViewById(R.id.referral_row);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        referralRecyclerView.setLayoutManager(layoutManager);
    }

    @Override
    public void setProfileImage(String s, String s1) {
        //Overridden from abstract class not yet implemented
    }

    @Override
    public void setProfileDetailThree(String s) {
        //Implemented from abstract class but not required right now for HF
    }

    @Override
    public void toggleFamilyHead(boolean b) {
        //Implemented from abstract class but not required right now for HF
    }

    @Override
    public void togglePrimaryCaregiver(boolean b) {
        //Implemented from abstract class but not required right now for HF
    }

    @Override
    public void refreshList() {
        //Overridden from abstract class not yet implemented
    }

    @Override
    public void updateHasPhone(boolean hasPhone) {
        //Overridden from abstract class not yet implemented
    }

    @Override
    public void setFamilyServiceStatus(String status) {
        //Implemented from abstract class but not required right now for HF
    }

    @Override
    public void verifyHasPhone() {
        //Implemented from abstract class but not required right now for HF
    }

    @Override
    public void notifyHasPhone(boolean hasPhone) {
        //Overridden from abstract class not yet implemented
    }

    @Override
    public void updateReferralTasks(Set<Task> taskList) {
        this.taskList = taskList;
        invalidateOptionsMenu(); // We want to update the drop-down menu if we have referrals
        if (referralRecyclerView != null && taskList.size() > 0) {
            RecyclerView.Adapter mAdapter = new ReferralCardViewAdapter(taskList, this, getAncMemberObject(), memberObject.getFamilyHeadName(),
                    memberObject.getFamilyHeadPhoneNumber(), commonPersonObjectClient, CoreConstants.REGISTERED_ACTIVITIES.MALARIA_REGISTER_ACTIVITY);
            referralRecyclerView.setAdapter(mAdapter);
            referralRow.setVisibility(View.VISIBLE);
        }
    }

    public MemberObject getAncMemberObject() {
        MemberObject memberObject = new MemberObject();
        memberObject.setFirstName(this.memberObject.getFirstName());
        memberObject.setBaseEntityId(this.memberObject.getBaseEntityId());
        memberObject.setFirstName(this.memberObject.getFirstName());
        memberObject.setLastName(this.memberObject.getLastName());
        memberObject.setMiddleName(this.memberObject.getMiddleName());
        memberObject.setDob(this.memberObject.getAge());
        return memberObject;
    }

    public void updateCommonPersonObjectClient() {
        String query = new CoreMalariaRegisterFragmentModel().mainSelect(CoreConstants.TABLE_NAME.MALARIA_CONFIRMATION, String.format("ec_malaria_confirmation.base_entity_id = '%s'", baseEntityId));

        try (Cursor cursor = getCommonRepository(CoreConstants.TABLE_NAME.MALARIA_CONFIRMATION).rawCustomQueryForAdapter(query)) {
            if (cursor != null && cursor.moveToFirst()) {
                CommonPersonObject personObject = getCommonRepository(CoreConstants.TABLE_NAME.MALARIA_CONFIRMATION).readAllcommonforCursorAdapter(cursor);
                commonPersonObjectClient = new CommonPersonObjectClient(personObject.getCaseId(),
                        personObject.getDetails(), "");
                commonPersonObjectClient.setColumnmaps(personObject.getColumnmaps());
            }
        } catch (Exception ex) {
            Timber.e(ex, "Malaria profile --> setCommonPersonObjectClient");
        }
    }
}
