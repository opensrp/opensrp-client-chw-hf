package org.smartregister.chw.hf.interactor;

import org.smartregister.chw.pmtct.contract.PmtctProfileContract;
import org.smartregister.chw.pmtct.domain.MemberObject;
import org.smartregister.chw.pmtct.interactor.BasePmtctProfileInteractor;

public class HeiProfileInteractor extends BasePmtctProfileInteractor {

    @Override
    public void refreshProfileInfo(MemberObject memberObject, PmtctProfileContract.InteractorCallBack callback) {
        super.refreshProfileInfo(memberObject, callback);
    }
}
