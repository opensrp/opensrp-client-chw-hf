package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.presenter.BaseAncMedicalHistoryPresenter;
import org.smartregister.chw.anc.util.Constants;
import org.smartregister.chw.core.activity.CoreAncMedicalHistoryActivity;
import org.smartregister.chw.hf.interactor.AncMedicalHistoryInteractor;

import java.util.List;

public class AncMedicalHistoryActivity extends CoreAncMedicalHistoryActivity {
    private Flavor flavor = new AncMedicalHistoryActivityFlv();

    public static void startMe(Activity activity, MemberObject memberObject) {
        Intent intent = new Intent(activity, AncMedicalHistoryActivity.class);
        intent.putExtra(Constants.ANC_MEMBER_OBJECTS.MEMBER_PROFILE_OBJECT, memberObject);
        activity.startActivity(intent);
    }

    @Override
    public void initializePresenter() {
        presenter = new BaseAncMedicalHistoryPresenter(new AncMedicalHistoryInteractor(), this, memberObject.getBaseEntityId());
    }

    @Override
    public View renderView(List<Visit> visits) {
        super.renderView(visits);
        View view = flavor.bindViews(this);
        displayLoadingState(true);
        flavor.processViewData(visits, this);
        displayLoadingState(false);
        return view;
    }
}
