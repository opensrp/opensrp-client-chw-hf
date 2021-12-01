package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Intent;


import org.smartregister.chw.hf.interactor.EacVisitInteractor;
import org.smartregister.chw.pmtct.domain.MemberObject;
import org.smartregister.chw.pmtct.presenter.BasePmtctHomeVisitPresenter;
import org.smartregister.chw.pmtct.util.Constants;

import java.text.MessageFormat;


public class PmtctEacVisitActivity extends PmtctFollowupVisitActivity{
    public static void startEacActivity(Activity activity, String baseEntityID) {
        Intent intent = new Intent(activity, PmtctEacVisitActivity.class);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID,baseEntityID );
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.EDIT_MODE, false);
        activity.startActivity(intent);
    }
    @Override
    protected void registerPresenter() {
        presenter = new BasePmtctHomeVisitPresenter(memberObject,this,new EacVisitInteractor());
    }
    @Override
    public void redrawHeader(MemberObject memberObject) {
        tvTitle.setText(MessageFormat.format("{0}, {1} \u00B7 {2}", memberObject.getFullName(), memberObject.getAge(), getString(org.smartregister.chw.hf.R.string.pmtct_record_eac_visit)));
    }
}
