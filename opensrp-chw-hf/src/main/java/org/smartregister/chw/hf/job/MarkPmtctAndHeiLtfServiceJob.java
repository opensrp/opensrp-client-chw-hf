package org.smartregister.chw.hf.job;

import android.content.Intent;

import androidx.annotation.NonNull;

import org.smartregister.AllConstants;
import org.smartregister.chw.hf.sync.intent.MarkPmtctAndHeiLtfIntentService;
import org.smartregister.job.BaseJob;

/**
 * Created by cozej4 on 2022-03-24.
 *
 * @author cozej4 https://github.com/cozej4
 */
public class MarkPmtctAndHeiLtfServiceJob extends BaseJob {

    public static final String TAG = "MarkPmtctAndHeiLtfServiceJob";

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        Intent intent = new Intent(getApplicationContext(), MarkPmtctAndHeiLtfIntentService.class);
        getApplicationContext().startService(intent);
        return params != null && params.getExtras().getBoolean(AllConstants.INTENT_KEY.TO_RESCHEDULE, false) ? Result.RESCHEDULE : Result.SUCCESS;
    }
}
