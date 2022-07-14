package org.smartregister.chw.hf.actionhelper;

import android.content.Context;

import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.utils.JsonFormUtils;
import org.smartregister.chw.ld.domain.VisitDetail;
import org.smartregister.chw.ld.model.BaseLDVisitAction;

import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * Author issyzac on 2022-07-13
 */

public class LDSyphilisTestActionHelper implements BaseLDVisitAction.LDVisitActionHelper {

    private final Context context;
    private String syphilisTest;

    public LDSyphilisTestActionHelper(Context context){
        this.context = context;
    }

    @Override
    public void onJsonFormLoaded(String s, Context context, Map<String, List<VisitDetail>> map) {
        //TODO: implement
    }

    @Override
    public String getPreProcessed() {
        return null;
    }

    @Override
    public void onPayloadReceived(String jsonPayload) {
        try{
            syphilisTest = JsonFormUtils.getFieldValue(jsonPayload, "syphilis");
        }catch (Exception e){
            Timber.e(e);
        }
    }

    @Override
    public BaseLDVisitAction.ScheduleStatus getPreProcessedStatus() {
        return null;
    }

    @Override
    public String getPreProcessedSubTitle() {
        return null;
    }

    @Override
    public String postProcess(String s) {
        return null;
    }

    @Override
    public String evaluateSubTitle() {
        String subtitle = "";
        if (syphilisTest != null && !syphilisTest.isEmpty()){
            subtitle = context.getString(R.string.syphilis_test_result) + " : ";
            if (syphilisTest.equalsIgnoreCase("positive")){
                subtitle += context.getString(R.string.positive);
            } else if (syphilisTest.equalsIgnoreCase("negative")){
                subtitle += context.getString(R.string.negative);
            }else if (syphilisTest.equalsIgnoreCase("test_not_conducted")){
                subtitle += context.getString(R.string.test_not_conducted);
            }
        }
        return subtitle;
    }

    @Override
    public BaseLDVisitAction.Status evaluateStatusOnPayload() {
        if (syphilisTest != null && !syphilisTest.isEmpty()){
            return BaseLDVisitAction.Status.COMPLETED;
        }else{
            return BaseLDVisitAction.Status.PENDING;
        }
    }

    @Override
    public void onPayloadReceived(BaseLDVisitAction baseLDVisitAction) {
        //TODO: implement
    }
}
