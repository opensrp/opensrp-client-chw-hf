package org.smartregister.chw.hf.sync.intent;

import android.content.Intent;

import org.smartregister.chw.core.job.CloseExpiredReferralsServiceJob;
import org.smartregister.chw.core.job.SyncTaskWithClientEventsServiceJob;
import org.smartregister.chw.hf.sync.helper.HfTaskServiceHelper;
import org.smartregister.sync.intent.SyncTaskIntentService;

import timber.log.Timber;

public class HfSyncTaskIntentService extends SyncTaskIntentService {
    @Override
    protected void onHandleIntent(Intent intent) {
        HfTaskServiceHelper taskServiceHelper = HfTaskServiceHelper.getInstance();
        taskServiceHelper.syncTasks();
        SyncTaskWithClientEventsServiceJob.scheduleJobImmediately(SyncTaskWithClientEventsServiceJob.TAG);
        Timber.e("Coze");
        CloseExpiredReferralsServiceJob.scheduleJobImmediately(CloseExpiredReferralsServiceJob.TAG);
    }
}
