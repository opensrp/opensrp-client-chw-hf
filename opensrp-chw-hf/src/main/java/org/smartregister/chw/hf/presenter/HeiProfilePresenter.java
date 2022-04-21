package org.smartregister.chw.hf.presenter;

import org.smartregister.chw.hf.interactor.HeiProfileInteractor;
import org.smartregister.chw.pmtct.contract.PmtctProfileContract;
import org.smartregister.chw.pmtct.domain.MemberObject;
import org.smartregister.chw.pmtct.presenter.BasePmtctProfilePresenter;
import org.smartregister.repository.AllSharedPreferences;

public class HeiProfilePresenter extends BasePmtctProfilePresenter {
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

}
