package org.smartregister.chw.hf.sync.intent;

import android.app.IntentService;
import android.content.Intent;

import org.smartregister.chw.hf.utils.PncVisitUtils;
import org.smartregister.chw.hf.utils.VisitUtils;

import timber.log.Timber;

public class ProcessAncAndPncVisitsIntentService extends IntentService {

    private static final String TAG = ProcessAncAndPncVisitsIntentService.class.getSimpleName();


    public ProcessAncAndPncVisitsIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            PncVisitUtils.processVisits();
            VisitUtils.processVisits();
        } catch (Exception e) {
            Timber.e(e);
        }
    }

}
