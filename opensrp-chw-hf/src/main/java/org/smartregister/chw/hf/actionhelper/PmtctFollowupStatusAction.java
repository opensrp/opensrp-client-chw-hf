package org.smartregister.chw.hf.actionhelper;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.pmtct.domain.MemberObject;
import org.smartregister.chw.pmtct.domain.VisitDetail;
import org.smartregister.chw.pmtct.model.BasePmtctHomeVisitAction;

import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class PmtctFollowupStatusAction implements BasePmtctHomeVisitAction.PmtctHomeVisitActionHelper {
   protected MemberObject memberObject;
   private String jsonPayload;
   protected String followup_status;
   private Context context;
   private String subTitle;

   public PmtctFollowupStatusAction(MemberObject memberObject) {
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
      } catch (JSONException e) {
         e.printStackTrace();
      }

      return null;
   }

   @Override
   public void onPayloadReceived(String jsonPayload) {
      try {
         JSONObject jsonObject = new JSONObject(jsonPayload);
         followup_status = CoreJsonFormUtils.getValue(jsonObject, "followup_status");
      } catch (JSONException e) {
         e.printStackTrace();
      }
   }

   @Override
   public BasePmtctHomeVisitAction.ScheduleStatus getPreProcessedStatus() {
      return null;
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
      if (StringUtils.isBlank(followup_status))
         return null;

      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append("Followup Status Recorded");

      return stringBuilder.toString();
   }

   @Override
   public BasePmtctHomeVisitAction.Status evaluateStatusOnPayload() {
      if (StringUtils.isBlank(followup_status))
         return BasePmtctHomeVisitAction.Status.PENDING;
      else {
         return BasePmtctHomeVisitAction.Status.COMPLETED;
      }
   }

   @Override
   public void onPayloadReceived(BasePmtctHomeVisitAction basePmtctHomeVisitAction) {
      Timber.d("onPayloadReceived");
   }
}
