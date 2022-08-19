package org.smartregister.chw.hf.sync.intent;

import android.content.Intent;

import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;

import org.smartregister.chw.core.job.CloseExpiredReferralsServiceJob;
import org.smartregister.chw.core.job.SyncTaskWithClientEventsServiceJob;
import org.smartregister.chw.hf.sync.helper.HfTaskServiceHelper;
import org.smartregister.sync.intent.SyncTaskIntentService;

import java.util.concurrent.TimeUnit;

import timber.log.Timber;

public class HfSyncTaskIntentService extends SyncTaskIntentService {
    @Override
    protected void onHandleIntent(Intent intent) {
        HfTaskServiceHelper taskServiceHelper = HfTaskServiceHelper.getInstance();
        taskServiceHelper.syncTasks();
        scheduleJobLater(SyncTaskWithClientEventsServiceJob.TAG, 5L);
        CloseExpiredReferralsServiceJob.scheduleJobImmediately(CloseExpiredReferralsServiceJob.TAG);
    }

    public void scheduleJobLater(String jobTag, Long startMins) {
        if (JobManager.instance().getAllJobRequestsForTag(jobTag).isEmpty()) {
            new JobRequest.Builder(SyncTaskWithClientEventsServiceJob.TAG)
                    .setExecutionWindow(TimeUnit.MINUTES.toMillis(startMins), TimeUnit.MINUTES.toMillis(startMins + 10))
                    .build()
                    .schedule();
        } else {
            Timber.d("Skipping schedule for job with name " + jobTag + " : Already Exists!");
        }
    }
}
