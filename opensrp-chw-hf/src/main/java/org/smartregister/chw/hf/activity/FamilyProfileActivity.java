package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.vijay.jsonwizard.utils.FormUtils;

import org.json.JSONException;
import org.smartregister.chw.anc.activity.BaseAncMemberProfileActivity;
import org.smartregister.chw.core.activity.CoreAboveFiveChildProfileActivity;
import org.smartregister.chw.core.activity.CoreChildProfileActivity;
import org.smartregister.chw.core.activity.CoreFamilyProfileActivity;
import org.smartregister.chw.core.activity.CoreFamilyProfileMenuActivity;
import org.smartregister.chw.core.activity.CoreFamilyRemoveMemberActivity;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.fp.dao.FpDao;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.fragment.FamilyProfileMemberFragment;
import org.smartregister.chw.hf.model.FamilyProfileModel;
import org.smartregister.chw.hf.presenter.FamilyProfilePresenter;
import org.smartregister.chw.pnc.activity.BasePncMemberProfileActivity;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.family.adapter.ViewPagerAdapter;
import org.smartregister.family.util.Constants;

import java.util.HashMap;

import timber.log.Timber;

public class FamilyProfileActivity extends CoreFamilyProfileActivity {
    private TextView tvEventDate;
    private TextView tvInterpunct;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        setupMenuOptions(menu);
        return true;
    }

    @Override
    protected void refreshPresenter() {
        presenter = new FamilyProfilePresenter(this, new FamilyProfileModel(familyName), familyBaseEntityId, familyHead, primaryCaregiver, familyName);
    }

    @Override
    protected void refreshList(Fragment fragment) {
        if (fragment instanceof FamilyProfileMemberFragment) {
            FamilyProfileMemberFragment familyProfileMemberFragment = ((FamilyProfileMemberFragment) fragment);
            if (familyProfileMemberFragment.presenter() != null) {
                familyProfileMemberFragment.refreshListView();
            }
        }
    }
    @Override
    protected void setupViews() {
        super.setupViews();
        tvEventDate = findViewById(R.id.textview_event_date);
        tvInterpunct = findViewById(R.id.interpunct);
    }

    @Override
    public void setEventDate(String eventDate) {
        tvEventDate.setVisibility(View.VISIBLE);
        tvInterpunct.setVisibility(View.VISIBLE);
        tvEventDate.setText(String.format(this.getString(R.string.created), eventDate));
    }

    @Override
    protected Class<? extends CoreFamilyRemoveMemberActivity> getFamilyRemoveMemberClass() {
        return FamilyRemoveMemberActivity.class;
    }

    @Override
    protected Class<? extends CoreFamilyProfileMenuActivity> getFamilyProfileMenuClass() {
        return FamilyProfileMenuActivity.class;
    }

    @Override
    protected void initializePresenter() {
        super.initializePresenter();
        presenter = new FamilyProfilePresenter(this, new FamilyProfileModel(familyName), familyBaseEntityId, familyHead, primaryCaregiver, familyName);
    }

    @Override
    protected ViewPager setupViewPager(ViewPager viewPager) {

        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(FamilyProfileMemberFragment.newInstance(this.getIntent().getExtras()),
                this.getString(org.smartregister.family.R.string.member).toUpperCase());

        viewPager.setAdapter(adapter);

        if (getIntent().getBooleanExtra(CoreConstants.INTENT_KEY.SERVICE_DUE, false) ||
                getIntent().getBooleanExtra(Constants.INTENT_KEY.GO_TO_DUE_PAGE, false)) {
            viewPager.setCurrentItem(1);
        }

        return viewPager;
    }

    @Override
    protected Class<?> getFamilyOtherMemberProfileActivityClass() {
        return FamilyOtherMemberProfileActivity.class;
    }

    @Override
    protected Class<? extends CoreAboveFiveChildProfileActivity> getAboveFiveChildProfileActivityClass() {
        return AboveFiveChildProfileActivity.class;
    }

    @Override
    protected Class<? extends CoreChildProfileActivity> getChildProfileActivityClass() {
        return ChildProfileActivity.class;
    }

    @Override
    protected Class<? extends BaseAncMemberProfileActivity> getAncMemberProfileActivityClass() {
        return AncMemberProfileActivity.class;
    }

    @Override
    protected Class<? extends BasePncMemberProfileActivity> getPncMemberProfileActivityClass() {
        return PncMemberProfileActivity.class;
    }

    @Override
    protected void goToFpProfile(String baseEntityId, Activity activity) {
        FamilyPlanningMemberProfileActivity.startFpMemberProfileActivity(activity, FpDao.getMember(baseEntityId));
    }

    @Override
    protected void goToHivProfile(String baseEntityId, Activity activity) {
        try {
            HivRegisterActivity.startHIVFormActivity(this, baseEntityId,CoreConstants.JSON_FORM.getHivRegistration(),(new FormUtils()).getFormJsonFromRepositoryOrAssets(this, CoreConstants.JSON_FORM.getHivRegistration()).toString());
        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    @Override
    protected void goToTbProfile(String baseEntityId, Activity activity) {
        try {
            TbRegisterActivity.startTbFormActivity(this, baseEntityId,CoreConstants.JSON_FORM.getTbRegistration(),(new FormUtils()).getFormJsonFromRepositoryOrAssets(this, CoreConstants.JSON_FORM.getTbRegistration()).toString());
        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    @Override
    protected boolean isAncMember(String baseEntityId) {
        return getFamilyProfilePresenter().isAncMember(baseEntityId);
    }

    @Override
    protected HashMap<String, String> getAncFamilyHeadNameAndPhone(String baseEntityId) {
        return getFamilyProfilePresenter().getAncFamilyHeadNameAndPhone(baseEntityId);
    }

    @Override
    protected CommonPersonObject getAncCommonPersonObject(String baseEntityId) {
        return getFamilyProfilePresenter().getAncCommonPersonObject(baseEntityId);
    }

    @Override
    protected CommonPersonObject getPncCommonPersonObject(String baseEntityId) {
        return getFamilyProfilePresenter().getPncCommonPersonObject(baseEntityId);
    }

    @Override
    protected boolean isPncMember(String baseEntityId) {
        return getFamilyProfilePresenter().isPncMember(baseEntityId);
    }

    private void setupMenuOptions(Menu menu) {

        MenuItem removeMember = menu.findItem(org.smartregister.chw.core.R.id.action_remove_member);
        MenuItem changeFamHead = menu.findItem(org.smartregister.chw.core.R.id.action_change_head);
        MenuItem changeCareGiver = menu.findItem(org.smartregister.chw.core.R.id.action_change_care_giver);

        if (removeMember != null) {
            removeMember.setVisible(false);
        }

        if (changeFamHead != null) {
            changeFamHead.setVisible(false);
        }

        if (changeCareGiver != null) {
            changeCareGiver.setVisible(false);
        }
    }

    public FamilyProfilePresenter getFamilyProfilePresenter() {
        return (FamilyProfilePresenter) presenter;
    }
}
