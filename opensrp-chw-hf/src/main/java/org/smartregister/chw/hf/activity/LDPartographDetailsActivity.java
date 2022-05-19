package org.smartregister.chw.hf.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.presenter.BaseAncMedicalHistoryPresenter;
import org.smartregister.chw.core.activity.CoreAncMedicalHistoryActivity;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.interactor.LDPartographDetailsInteractor;
import org.smartregister.chw.ld.domain.MemberObject;

import java.util.List;

public class LDPartographDetailsActivity extends CoreAncMedicalHistoryActivity {
    private static MemberObject ldMemberObject;
    private Flavor flavor = new LDPartographDetailsActivityFlv();
    private ProgressBar progressBar;

    public static void startMe(Activity activity, MemberObject memberObject) {
        Intent intent = new Intent(activity, LDPartographDetailsActivity.class);
        ldMemberObject = memberObject;
        activity.startActivity(intent);
    }

    @Override
    public void initializePresenter() {
        presenter = new BaseAncMedicalHistoryPresenter(new LDPartographDetailsInteractor(), this, ldMemberObject.getBaseEntityId());
    }

    @Override
    public void setUpView() {
        findViewById(R.id.collapsing_toolbar).setBackgroundColor(getResources().getColor(R.color.primary));

        Drawable upArrow = this.getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp);
        upArrow.setColorFilter(this.getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        linearLayout = findViewById(org.smartregister.chw.opensrp_chw_anc.R.id.linearLayoutMedicalHistory);
        progressBar = findViewById(org.smartregister.chw.opensrp_chw_anc.R.id.progressBarMedicalHistory);


        TextView tvTitle = findViewById(org.smartregister.chw.opensrp_chw_anc.R.id.tvTitle);
        tvTitle.setText(getString(org.smartregister.chw.opensrp_chw_anc.R.string.back_to, ldMemberObject.getFullName()));
        tvTitle.setTextColor(getResources().getColor(org.smartregister.ld.R.color.white));

        TextView medicalHistory = findViewById(org.smartregister.chw.opensrp_chw_anc.R.id.medical_history);
        medicalHistory.setVisibility(View.GONE);
    }

    @Override
    public View renderView(List<Visit> visits) {
        super.renderView(visits);
        View view = flavor.bindViews(this);
        displayLoadingState(true);
        flavor.processViewData(visits, this);
        displayLoadingState(false);
        TextView ldPartographDetailsTitle = view.findViewById(org.smartregister.chw.core.R.id.customFontTextViewHealthFacilityVisitTitle);
        ldPartographDetailsTitle.setText(getString(R.string.partograph_details_title));
        return view;
    }

    @Override
    public void displayLoadingState(boolean state) {
        progressBar.setVisibility(state ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
}
