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

public class LDMalariaTestActionHelper implements BaseLDVisitAction.LDVisitActionHelper {

    private final Context context;
    private String malariaTest;

    public LDMalariaTestActionHelper(Context context){
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
            malariaTest = JsonFormUtils.getFieldValue(jsonPayload, "malaria");
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
        if (malariaTest != null && !malariaTest.isEmpty()){
            subtitle = context.getString(R.string.malaria_test_result) + " : ";
            if (malariaTest.equalsIgnoreCase("positive")){
                subtitle += context.getString(R.string.positive);
            }else if (malariaTest.equalsIgnoreCase("negative")){
                subtitle += context.getString(R.string.negative);
            }else if (malariaTest.equalsIgnoreCase("test_not_conducted")){
                subtitle += context.getString(R.string.test_not_conducted);
            }
        }
        return subtitle;
    }

    @Override
    public BaseLDVisitAction.Status evaluateStatusOnPayload() {
        if (malariaTest != null && !malariaTest.isEmpty()){
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
