package org.smartregister.chw.hf.actionhelper;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.hf.R;

import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class PncHivTestingAction implements BaseAncHomeVisitAction.AncHomeVisitActionHelper {
   protected MemberObject memberObject;
   private String jsonPayload;

   private String hiv_test_result;
   private BaseAncHomeVisitAction.ScheduleStatus scheduleStatus;
   private String subTitle;
   private Context context;

   public PncHivTestingAction(MemberObject memberObject) {
      this.memberObject = memberObject;
   }

   @Override
   public void onJsonFormLoaded(String jsonPayload, Context context, Map<String, List<VisitDetail>> map) {
      this.jsonPayload = jsonPayload;
      this.context = context;
   }

   @Override
   public String getPreProcessed() {

      try {
         JSONObject jsonObject = new JSONObject(jsonPayload);
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
         hiv_test_result = CoreJsonFormUtils.getValue(jsonObject, "hiv");
      } catch (JSONException e) {
         Timber.e(e);
      }
   }

   @Override
   public BaseAncHomeVisitAction.ScheduleStatus getPreProcessedStatus() {
      return scheduleStatus;
   }

   @Override
   public String getPreProcessedSubTitle() {
      return subTitle;
   }

   @Override
   public String postProcess(String s) {
      return s;
   }

   @Override
   public String evaluateSubTitle() {
      if (StringUtils.isBlank(hiv_test_result))
         return null;

      return hiv_test_result.equalsIgnoreCase("test_not_conducted") ? context.getString(R.string.hiv_testing_not_done) : context.getString(R.string.hiv_testing_done);
   }

   @Override
   public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
      if (StringUtils.isBlank(hiv_test_result))
         return BaseAncHomeVisitAction.Status.PENDING;
      else {
         return BaseAncHomeVisitAction.Status.COMPLETED;
      }
   }

   @Override
   public void onPayloadReceived(BaseAncHomeVisitAction baseAncHomeVisitAction) {
      Timber.d("onPayloadReceived");
   }
}
