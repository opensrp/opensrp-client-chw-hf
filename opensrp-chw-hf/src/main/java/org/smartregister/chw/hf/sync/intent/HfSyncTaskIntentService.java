package org.smartregister.chw.hf.sync.intent;

import android.content.Intent;

import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;

import org.smartregister.AllConstants;
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
        scheduleJob(SyncTaskWithClientEventsServiceJob.TAG, 15L, 5L);
        CloseExpiredReferralsServiceJob.scheduleJobImmediately(CloseExpiredReferralsServiceJob.TAG);
    }

    public void scheduleJob(String jobTag, Long start, Long flex) {

        if (JobManager.instance().getAllJobRequestsForTag(jobTag).isEmpty()) {

            PersistableBundleCompat extras = new PersistableBundleCompat();
            extras.putBoolean(AllConstants.INTENT_KEY.TO_RESCHEDULE, false);

            JobRequest.Builder jobRequest = new JobRequest.Builder(jobTag)
                    .setExtras(extras)
                    .setPeriodic(TimeUnit.MINUTES.toMillis(start), TimeUnit.MINUTES.toMillis(flex));
            try {

                int jobId = jobRequest.build().schedule();
                Timber.d("Scheduling job with name " + jobTag + " : JOB ID " + jobId + " periodically every " + start + " minutes and flex value of " + flex + " minutes");

            } catch (Exception e) {
                Timber.e(e);
            }
        } else {
            Timber.d("Skipping schedule for job with name " + jobTag + " : Already Exists!");
        }
    }
}
