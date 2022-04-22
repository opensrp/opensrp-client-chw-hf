package org.smartregister.chw.hf.utils;

import org.apache.commons.lang3.time.DateUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.pmtct.PmtctLibrary;
import org.smartregister.chw.pmtct.domain.Visit;
import org.smartregister.chw.pmtct.repository.VisitDetailsRepository;
import org.smartregister.chw.pmtct.repository.VisitRepository;
import org.smartregister.chw.pmtct.util.VisitUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import timber.log.Timber;

public class HeiVisitUtils extends VisitUtils {
    public static void processVisits() throws Exception {
        processVisits(PmtctLibrary.getInstance().visitRepository(), PmtctLibrary.getInstance().visitDetailsRepository());
    }

    public static void processVisits(VisitRepository visitRepository, VisitDetailsRepository visitDetailsRepository) throws Exception {

        List<Visit> visits = visitRepository.getAllUnSynced();
        List<Visit> heiFollowupVisits = new ArrayList<>();


        for (Visit v : visits) {
            Date truncatedUpdatedDate = DateUtils.truncate(v.getUpdatedAt(), Calendar.DATE);
            Date today = DateUtils.truncate(new Date(), Calendar.DATE);

            if (truncatedUpdatedDate.before(today) && v.getVisitType().equalsIgnoreCase(Constants.Events.HEI_FOLLOWUP)) {
                try {
                    heiFollowupVisits.add(v);
                } catch (Exception e) {
                    Timber.e(e);
                }
            }

        }

        if (heiFollowupVisits.size() > 0) {
            processVisits(heiFollowupVisits, visitRepository, visitDetailsRepository);
        }
    }

    public static boolean computeCompletionStatus(JSONArray obs, String checkString) throws JSONException {
        int size = obs.length();
        for (int i = 0; i < size; i++) {
            JSONObject checkObj = obs.getJSONObject(i);
            if (checkObj.getString("fieldCode").equalsIgnoreCase(checkString)) {
                return true;
            }
        }
        return false;
    }

}
