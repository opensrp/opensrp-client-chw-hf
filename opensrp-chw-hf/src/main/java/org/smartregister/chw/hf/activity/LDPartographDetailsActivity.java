package org.smartregister.chw.hf.activity;

import static org.smartregister.AllConstants.DEFAULT_LOCALITY_NAME;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gkemon.XMLtoPDF.PdfGenerator;
import com.gkemon.XMLtoPDF.PdfGeneratorListener;
import com.gkemon.XMLtoPDF.model.FailureResponse;
import com.gkemon.XMLtoPDF.model.SuccessResponse;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.presenter.BaseAncMedicalHistoryPresenter;
import org.smartregister.chw.core.activity.CoreAncMedicalHistoryActivity;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.interactor.LDPartographDetailsInteractor;
import org.smartregister.chw.ld.LDLibrary;
import org.smartregister.chw.ld.dao.LDDao;
import org.smartregister.chw.ld.domain.MemberObject;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

public class LDPartographDetailsActivity extends CoreAncMedicalHistoryActivity {
    private static MemberObject ldMemberObject;
    private Flavor flavor = new LDPartographDetailsActivityFlv();
    private ProgressBar progressBar;
    private RelativeLayout headerLayout;

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

        TextView facilityName = findViewById(org.smartregister.ld.R.id.facility_name);
        TextView clientName = findViewById(org.smartregister.ld.R.id.client_name);
        TextView gravida = findViewById(org.smartregister.ld.R.id.gravida);
        TextView para = findViewById(org.smartregister.ld.R.id.para);
        TextView admissionDate = findViewById(org.smartregister.ld.R.id.admission_date);
        TextView admissionTime = findViewById(org.smartregister.ld.R.id.admission_time);

        String facilityNameString = LDLibrary.getInstance().context().allSharedPreferences().getPreference(DEFAULT_LOCALITY_NAME);

        if (StringUtils.isNotBlank(facilityNameString)) {
            facilityName.setText(facilityNameString);
        } else {
            facilityName.setVisibility(View.GONE);
        }

        clientName.setText(MessageFormat.format(getString(org.smartregister.ld.R.string.partograph_client_name), ldMemberObject.getFirstName(), ldMemberObject.getMiddleName(), ldMemberObject.getLastName()));
        gravida.setText(MessageFormat.format(getString(org.smartregister.ld.R.string.partograph_gravida), LDDao.getGravida(ldMemberObject.getBaseEntityId())));
        para.setText(MessageFormat.format(getString(org.smartregister.ld.R.string.partograph_para), LDDao.getPara(ldMemberObject.getBaseEntityId())));
        admissionDate.setText(MessageFormat.format(getString(org.smartregister.ld.R.string.partograph_admission_date), LDDao.getAdmissionDate(ldMemberObject.getBaseEntityId())));
        admissionTime.setText(MessageFormat.format(getString(org.smartregister.ld.R.string.partograph_admission_time), LDDao.getAdmissionTime(ldMemberObject.getBaseEntityId())));
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
        getMenuInflater().inflate(org.smartregister.ld.R.menu.partograph_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == org.smartregister.ld.R.id.action_download_partograph) {
            downloadPartograph();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void downloadPartograph() {
        headerLayout = findViewById(R.id.header_layout);
        headerLayout.setVisibility(View.VISIBLE);


        int age = 0;
        try {
            age = Integer.parseInt(ldMemberObject.getAge());
        } catch (Exception e) {
            Timber.e(e);
        }
        View mView = findViewById(R.id.main_layout);
        PdfGenerator.getBuilder()
                .setContext(LDPartographDetailsActivity.this)
                .fromViewSource()
                .fromView(mView)
                .setFileName(String.format(Locale.getDefault(), "%s %s %s, %d",
                        ldMemberObject.getFirstName(),
                        ldMemberObject.getMiddleName(),
                        ldMemberObject.getLastName(),
                        age))
                .setFolderNameOrPath("MyFolder/MyDemoHorizontalText/")
                .actionAfterPDFGeneration(PdfGenerator.ActionAfterPDFGeneration.OPEN)
                .build(new PdfGeneratorListener() {
                    @Override
                    public void onFailure(FailureResponse failureResponse) {
                        super.onFailure(failureResponse);
                    }

                    @Override
                    public void showLog(String log) {
                        super.showLog(log);
                    }

                    @Override
                    public void onStartPDFGeneration() {
                        /*When PDF generation begins to start*/
                    }

                    @Override
                    public void onFinishPDFGeneration() {
                        /*When PDF generation is finished*/
                        headerLayout.setVisibility(View.GONE);
                    }

                    @Override
                    public void onSuccess(SuccessResponse response) {
                        super.onSuccess(response);
                    }
                });
    }
}
