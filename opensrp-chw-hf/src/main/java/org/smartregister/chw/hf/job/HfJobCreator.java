package org.smartregister.chw.hf.job;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

import org.smartregister.chw.core.job.ChwIndicatorGeneratingJob;
import org.smartregister.chw.core.job.CloseExpiredReferralsServiceJob;
import org.smartregister.chw.core.job.SyncTaskWithClientEventsServiceJob;
import org.smartregister.chw.core.job.VaccineRecurringServiceJob;
import org.smartregister.chw.hf.sync.intent.HfSyncClientEventsPerTaskIntentService;
import org.smartregister.chw.hf.sync.intent.HfSyncTaskIntentService;
import org.smartregister.job.ExtendedSyncServiceJob;
import org.smartregister.job.ImageUploadServiceJob;
import org.smartregister.job.LocationStructureServiceJob;
import org.smartregister.job.P2pServiceJob;
import org.smartregister.job.PullUniqueIdsServiceJob;
import org.smartregister.job.SyncLocationsByLevelAndTagsServiceJob;
import org.smartregister.job.SyncLocationsByTeamIdsJob;
import org.smartregister.job.SyncServiceJob;
import org.smartregister.job.SyncTaskServiceJob;
import org.smartregister.job.ValidateSyncDataServiceJob;
import org.smartregister.sync.intent.SyncIntentService;

import timber.log.Timber;

/**
 *
 */
public class HfJobCreator implements JobCreator {
    @Nullable
    @Override
    public Job create(@NonNull String tag) {
        switch (tag) {
            case SyncServiceJob.TAG:
                return new SyncServiceJob(SyncIntentService.class);
            case ExtendedSyncServiceJob.TAG:
                return new ExtendedSyncServiceJob();
            case PullUniqueIdsServiceJob.TAG:
                return new PullUniqueIdsServiceJob();
            case ValidateSyncDataServiceJob.TAG:
                return new ValidateSyncDataServiceJob();
            case ImageUploadServiceJob.TAG:
                return new ImageUploadServiceJob();
            case VaccineRecurringServiceJob.TAG:
                return new VaccineRecurringServiceJob();
            case LocationStructureServiceJob.TAG:
                return new LocationStructureServiceJob();
            case SyncTaskServiceJob.TAG:
                return new SyncTaskServiceJob(HfSyncTaskIntentService.class);
            //TODO Include plan intent service when implementation is done on the server
       /*     case PlanIntentServiceJob.TAG:
                return new PlanIntentServiceJob();*/
            case SyncTaskWithClientEventsServiceJob.TAG:
                return new SyncTaskWithClientEventsServiceJob(HfSyncClientEventsPerTaskIntentService.class);
            case CloseExpiredReferralsServiceJob.TAG:
                return new CloseExpiredReferralsServiceJob();
            case SyncLocationsByTeamIdsJob.TAG:
                return new SyncLocationsByTeamIdsJob();
            case SyncLocationsByLevelAndTagsServiceJob.TAG:
                return new SyncLocationsByLevelAndTagsServiceJob();
            case ChwIndicatorGeneratingJob.TAG:
                return new ChwIndicatorGeneratingJob();
            case MarkPmtctAndHeiLtfServiceJob.TAG:
                return new MarkPmtctAndHeiLtfServiceJob();
            case ProcessVisitsServiceJob.TAG:
                return new ProcessVisitsServiceJob();
            case PncCloseDateServiceJob.TAG:
                return new PncCloseDateServiceJob();
            case P2pServiceJob.TAG:
                return new P2pServiceJob();
            case GenerateMonthlyTalliesJob.TAG:
                return new GenerateMonthlyTalliesJob();
            case CloseVmmcMemberServiceJob.TAG:
                return new CloseVmmcMemberServiceJob();
            default:
                Timber.d("Please create job and specify the right job tag");
                return null;
        }
    }
}
