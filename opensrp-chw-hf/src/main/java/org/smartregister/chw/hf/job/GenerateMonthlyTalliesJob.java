package org.smartregister.chw.hf.job;

import android.content.Intent;

import androidx.annotation.NonNull;

import org.smartregister.AllConstants;
import org.smartregister.chw.hf.sync.intent.GenerateMonthlyTalliesIntentService;
import org.smartregister.job.BaseJob;

public class GenerateMonthlyTalliesJob extends BaseJob {

    public static final String TAG = "GenerateMonthlyTalliesJob";

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        Intent intent = new Intent(getApplicationContext(), GenerateMonthlyTalliesIntentService.class);
        getApplicationContext().startService(intent);
        return params != null && params.getExtras().getBoolean(AllConstants.INTENT_KEY.TO_RESCHEDULE, false) ? Result.RESCHEDULE : Result.SUCCESS;
    }
}
