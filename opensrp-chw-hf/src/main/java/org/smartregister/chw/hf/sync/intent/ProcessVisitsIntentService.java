package org.smartregister.chw.hf.sync.intent;

import android.app.IntentService;
import android.content.Intent;

import org.smartregister.chw.hf.utils.HeiVisitUtils;
import org.smartregister.chw.hf.utils.PmtctVisitUtils;
import org.smartregister.chw.hf.utils.PncVisitUtils;
import org.smartregister.chw.hf.utils.VisitUtils;
import org.smartregister.chw.kvp.util.KvpVisitsUtil;
import org.smartregister.chw.kvp.util.PrEPVisitsUtil;

import timber.log.Timber;

public class ProcessVisitsIntentService extends IntentService {

    private static final String TAG = ProcessVisitsIntentService.class.getSimpleName();


    public ProcessVisitsIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            PncVisitUtils.processVisits();
            VisitUtils.processVisits();
            HeiVisitUtils.processVisits();
            PmtctVisitUtils.processVisits();
            KvpVisitsUtil.processVisits();
            PrEPVisitsUtil.processVisits();
        } catch (Exception e) {
            Timber.e(e);
        }
    }

}
