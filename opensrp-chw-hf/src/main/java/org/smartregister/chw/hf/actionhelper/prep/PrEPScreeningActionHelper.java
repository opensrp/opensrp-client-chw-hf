package org.smartregister.chw.hf.actionhelper.prep;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.hf.dao.HfKvpDao;
import org.smartregister.chw.kvp.domain.VisitDetail;
import org.smartregister.chw.kvp.model.BaseKvpVisitAction;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

public class PrEPScreeningActionHelper implements BaseKvpVisitAction.KvpVisitActionHelper {

    protected String should_initiate;
    private String jsonPayload;
    private String baseEntityId;

    public PrEPScreeningActionHelper(String baseEntityId) {
        this.baseEntityId = baseEntityId;
    }

    @Override
    public void onJsonFormLoaded(String jsonPayload, Context context, Map<String, List<VisitDetail>> map) {
        this.jsonPayload = jsonPayload;
    }

    @Override
    public String getPreProcessed() {
        try {
            JSONObject jsonObject = new JSONObject(jsonPayload);
            JSONObject global = jsonObject.getJSONObject("global");

            Date hbvTestDate = HfKvpDao.getHbvTestDate(baseEntityId);
            if (hbvTestDate != null) {
                Date threeMonthsAgo = new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(180));
                global.put("should_record_hbv_tests", hbvTestDate.before(threeMonthsAgo));
            } else {
                global.put("should_record_hbv_tests", true);
            }

            Date hcvTestDate = HfKvpDao.getHcvTestDate(baseEntityId);
            if (hcvTestDate != null) {
                Date threeMonthsAgo = new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(180));
                global.put("should_record_hcv_tests", hcvTestDate.before(threeMonthsAgo));
            } else {
                global.put("should_record_hcv_tests", true);
            }

            Date crclTestDate = HfKvpDao.getCrclTestDate(baseEntityId);
            if (crclTestDate != null) {
                Date threeMonthsAgo = new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(180));
                global.put("should_record_crcl_tests", crclTestDate.before(threeMonthsAgo));
            } else {
                global.put("should_record_crcl_tests", true);
            }

            String crclResults = HfKvpDao.getCrclResults(baseEntityId);
            global.put("record_rcl_tests_results", crclResults != null && crclResults.equalsIgnoreCase("no_results"));

            if (crclTestDate != null && crclResults != null && crclResults.equalsIgnoreCase("less_than_60")) {
                Date fourDaysAgo = new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(14));
                global.put("should_record_crcl_tests", crclTestDate.before(fourDaysAgo));
            }


            return jsonObject.toString();
        } catch (Exception e) {
            Timber.e(e);
        }


        return null;
    }

    @Override
    public void onPayloadReceived(String jsonPayload) {
        try {
            JSONObject jsonObject = new JSONObject(jsonPayload);
            should_initiate = CoreJsonFormUtils.getValue(jsonObject, "should_initiate");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public BaseKvpVisitAction.ScheduleStatus getPreProcessedStatus() {
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
        return null;
    }

    @Override
    public BaseKvpVisitAction.Status evaluateStatusOnPayload() {
        if (StringUtils.isBlank(should_initiate)) {
            return BaseKvpVisitAction.Status.PENDING;
        } else if (should_initiate.equalsIgnoreCase("no")) {
            return BaseKvpVisitAction.Status.PARTIALLY_COMPLETED;
        } else {
            return BaseKvpVisitAction.Status.COMPLETED;
        }
    }

    @Override
    public void onPayloadReceived(BaseKvpVisitAction baseKvpVisitAction) {
        //overridden
    }
}
