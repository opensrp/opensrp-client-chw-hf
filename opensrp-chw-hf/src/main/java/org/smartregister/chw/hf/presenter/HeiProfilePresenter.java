package org.smartregister.chw.hf.presenter;

import org.apache.commons.lang3.tuple.Triple;
import org.smartregister.chw.hf.interactor.HeiProfileInteractor;
import org.smartregister.chw.pmtct.contract.PmtctProfileContract;
import org.smartregister.chw.pmtct.domain.MemberObject;
import org.smartregister.chw.pmtct.presenter.BasePmtctProfilePresenter;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.contract.FamilyProfileContract;
import org.smartregister.family.domain.FamilyEventClient;
import org.smartregister.repository.AllSharedPreferences;

public class HeiProfilePresenter extends BasePmtctProfilePresenter implements FamilyProfileContract.InteractorCallBack {
    private HeiProfileInteractor heiProfileInteractor;

    public HeiProfilePresenter(PmtctProfileContract.View view, PmtctProfileContract.Interactor interactor, MemberObject memberObject) {
        super(view, interactor, memberObject);
        this.heiProfileInteractor = (HeiProfileInteractor) interactor;
    }

    public void createHeiCommunityFollowupReferralEvent(AllSharedPreferences allSharedPreferences, String jsonString, String entityID) throws Exception {
        heiProfileInteractor.createHeiCommunityFollowupReferralEvent(allSharedPreferences, jsonString, entityID);
    }

    public void createHeiNumberRegistrationEvent(AllSharedPreferences allSharedPreferences, String jsonString, String entityID) throws Exception {
        heiProfileInteractor.createHeiNumberRegistrationEvent(allSharedPreferences, jsonString, entityID);
    }

    @Override
    public void startFormForEdit(CommonPersonObjectClient commonPersonObjectClient) {
        //override
    }

    @Override
    public void refreshProfileTopSection(CommonPersonObjectClient commonPersonObjectClient) {
        //override
    }

    @Override
    public void onUniqueIdFetched(Triple<String, String, String> triple, String s) {
        //override
    }

    @Override
    public void onNoUniqueId() {
        //override
    }

    @Override
    public void onRegistrationSaved(boolean b, boolean b1, FamilyEventClient familyEventClient) {
        //override
    }
}
