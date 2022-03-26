package org.smartregister.chw.hf.job;

import android.content.Intent;

import org.smartregister.AllConstants;
import org.smartregister.chw.hf.sync.intent.PncCloseDateIntentService;
import org.smartregister.job.BaseJob;

import androidx.annotation.NonNull;

public class PncCloseDateServiceJob extends BaseJob {

    public static final String TAG = "PncCloseDateServiceJob";

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        Intent intent = new Intent(getApplicationContext(), PncCloseDateIntentService.class);
        getApplicationContext().startService(intent);
        return params != null && params.getExtras().getBoolean(AllConstants.INTENT_KEY.TO_RESCHEDULE, false) ? Result.RESCHEDULE : Result.SUCCESS;
    }
}
