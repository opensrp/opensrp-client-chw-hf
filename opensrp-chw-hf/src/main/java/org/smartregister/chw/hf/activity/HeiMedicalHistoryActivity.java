package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.presenter.BaseAncMedicalHistoryPresenter;
import org.smartregister.chw.core.activity.CoreAncMedicalHistoryActivity;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.interactor.HeiMedicalHistoryInteractor;
import org.smartregister.chw.pmtct.domain.MemberObject;

import java.util.List;

public class HeiMedicalHistoryActivity extends CoreAncMedicalHistoryActivity {
    private static MemberObject heiMemberObject;
    private Flavor flavor = new HeiMedicalHistoryActivityFlv();
    private ProgressBar progressBar;

    public static void startMe(Activity activity, MemberObject memberObject) {
        Intent intent = new Intent(activity, HeiMedicalHistoryActivity.class);
        heiMemberObject = memberObject;
        activity.startActivity(intent);
    }

    @Override
    public void initializePresenter() {
        presenter = new BaseAncMedicalHistoryPresenter(new HeiMedicalHistoryInteractor(), this, heiMemberObject.getBaseEntityId());
    }

    @Override
    public void setUpView() {
        linearLayout = findViewById(org.smartregister.chw.opensrp_chw_anc.R.id.linearLayoutMedicalHistory);
        progressBar = findViewById(org.smartregister.chw.opensrp_chw_anc.R.id.progressBarMedicalHistory);

        TextView tvTitle = findViewById(org.smartregister.chw.opensrp_chw_anc.R.id.tvTitle);
        tvTitle.setText(getString(org.smartregister.chw.opensrp_chw_anc.R.string.back_to, heiMemberObject.getFullName()));
    }

    @Override
    public View renderView(List<Visit> visits) {
        super.renderView(visits);
        View view = flavor.bindViews(this);
        displayLoadingState(true);
        flavor.processViewData(visits, this);
        displayLoadingState(false);
        TextView heiVisitTitle = view.findViewById(org.smartregister.chw.core.R.id.customFontTextViewHealthFacilityVisitTitle);
        heiVisitTitle.setText(R.string.hei_visit);
        return view;
    }

    @Override
    public void displayLoadingState(boolean state) {
        progressBar.setVisibility(state ? View.VISIBLE : View.GONE);
    }
}
