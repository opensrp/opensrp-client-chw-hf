package org.smartregister.chw.hf.interactor;

import org.smartregister.chw.core.job.ChwIndicatorGeneratingJob;
import org.smartregister.chw.core.job.CoreBasePncCloseJob;
import org.smartregister.chw.core.job.HomeVisitServiceJob;
import org.smartregister.chw.core.job.StockUsageReportJob;
import org.smartregister.chw.core.job.VaccineRecurringServiceJob;
import org.smartregister.chw.hf.BuildConfig;
import org.smartregister.chw.hf.job.MarkPmtctAndHeiLtfServiceJob;
import org.smartregister.immunization.job.VaccineServiceJob;
import org.smartregister.job.ImageUploadServiceJob;
import org.smartregister.job.PlanIntentServiceJob;
import org.smartregister.job.PullUniqueIdsServiceJob;
import org.smartregister.job.SyncLocationsByLevelAndTagsServiceJob;
import org.smartregister.job.SyncLocationsByTeamIdsJob;
import org.smartregister.job.SyncServiceJob;
import org.smartregister.job.SyncTaskServiceJob;
import org.smartregister.login.interactor.BaseLoginInteractor;
import org.smartregister.view.contract.BaseLoginContract;

import java.util.concurrent.TimeUnit;

public class LoginInteractor extends BaseLoginInteractor implements BaseLoginContract.Interactor {
    public LoginInteractor(BaseLoginContract.Presenter loginPresenter) {
        super(loginPresenter);
    }

    @Override
    protected void scheduleJobsPeriodically() {
        SyncServiceJob.scheduleJob(SyncServiceJob.TAG, TimeUnit.MINUTES.toMinutes(
                BuildConfig.DATA_SYNC_DURATION_MINUTES), getFlexValue(BuildConfig.DATA_SYNC_DURATION_MINUTES));

        ImageUploadServiceJob.scheduleJob(ImageUploadServiceJob.TAG, TimeUnit.MINUTES.toMinutes(
                BuildConfig.IMAGE_UPLOAD_MINUTES), getFlexValue(BuildConfig.IMAGE_UPLOAD_MINUTES));

        PullUniqueIdsServiceJob.scheduleJob(PullUniqueIdsServiceJob.TAG, TimeUnit.MINUTES.toMinutes(
                BuildConfig.PULL_UNIQUE_IDS_MINUTES), getFlexValue(BuildConfig.PULL_UNIQUE_IDS_MINUTES));

        VaccineRecurringServiceJob.scheduleJob(VaccineRecurringServiceJob.TAG, TimeUnit.MINUTES.toMinutes(
                BuildConfig.VACCINE_SYNC_PROCESSING_MINUTES), getFlexValue(BuildConfig.VACCINE_SYNC_PROCESSING_MINUTES));

        HomeVisitServiceJob.scheduleJob(HomeVisitServiceJob.TAG, TimeUnit.MINUTES.toMinutes(
                BuildConfig.HOME_VISIT_MINUTES), getFlexValue(BuildConfig.HOME_VISIT_MINUTES));

        SyncTaskServiceJob.scheduleJob(SyncTaskServiceJob.TAG, TimeUnit.MINUTES.toMinutes(
                BuildConfig.DATA_SYNC_DURATION_MINUTES), getFlexValue(BuildConfig.DATA_SYNC_DURATION_MINUTES));

        StockUsageReportJob.scheduleJob(StockUsageReportJob.TAG, TimeUnit.MINUTES.toMinutes(
                BuildConfig.STOCK_USAGE_REPORT_MINUTES), getFlexValue(BuildConfig.STOCK_USAGE_REPORT_MINUTES));

        ChwIndicatorGeneratingJob.scheduleJob(ChwIndicatorGeneratingJob.TAG, TimeUnit.MINUTES.toMinutes(BuildConfig.REPORT_INDICATOR_GENERATION_MINUTES), getFlexValue(BuildConfig.REPORT_INDICATOR_GENERATION_MINUTES));
        MarkPmtctAndHeiLtfServiceJob.scheduleJob(MarkPmtctAndHeiLtfServiceJob.TAG, TimeUnit.MINUTES.toMinutes(BuildConfig.REPORT_INDICATOR_GENERATION_MINUTES), getFlexValue(BuildConfig.REPORT_INDICATOR_GENERATION_MINUTES));
    }

    @Override
    protected void scheduleJobsImmediately() {
        super.scheduleJobsImmediately();
        SyncServiceJob.scheduleJobImmediately(SyncServiceJob.TAG);
        HomeVisitServiceJob.scheduleJobImmediately(HomeVisitServiceJob.TAG);
        SyncTaskServiceJob.scheduleJobImmediately(SyncTaskServiceJob.TAG);
        CoreBasePncCloseJob.scheduleJobImmediately(CoreBasePncCloseJob.TAG);
        PlanIntentServiceJob.scheduleJobImmediately(PlanIntentServiceJob.TAG);
        VaccineServiceJob.scheduleJobImmediately(VaccineServiceJob.TAG);
        VaccineRecurringServiceJob.scheduleJobImmediately(VaccineRecurringServiceJob.TAG);
        StockUsageReportJob.scheduleJobImmediately(StockUsageReportJob.TAG);
        SyncLocationsByTeamIdsJob.scheduleJobImmediately(SyncLocationsByTeamIdsJob.TAG);
        SyncLocationsByLevelAndTagsServiceJob.scheduleJobImmediately(SyncLocationsByLevelAndTagsServiceJob.TAG);
        ChwIndicatorGeneratingJob.scheduleJobImmediately(ChwIndicatorGeneratingJob.TAG);
        MarkPmtctAndHeiLtfServiceJob.scheduleJobImmediately(MarkPmtctAndHeiLtfServiceJob.TAG);
    }
}
