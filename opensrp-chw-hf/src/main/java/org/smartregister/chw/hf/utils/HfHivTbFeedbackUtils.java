package org.smartregister.chw.hf.utils;

import static org.smartregister.chw.hiv.util.DBConstants.Key.HIV_COMMUNITY_FOLLOWUP_VISIT_DATE;

import android.view.View;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.tb.util.DBConstants;
import org.smartregister.commonregistry.CommonPersonObjectClient;

import java.math.BigDecimal;
import java.util.Calendar;

public class HfHivTbFeedbackUtils {
    public static void displayReferralDay(CommonPersonObjectClient client, TextView textViewReferralDay) {

        String followupDate = null;
        if (client.getColumnmaps().get(DBConstants.Key.TB_COMMUNITY_FOLLOWUP_VISIT_DATE) != null) {
            followupDate = client.getColumnmaps().get(DBConstants.Key.TB_COMMUNITY_FOLLOWUP_VISIT_DATE);
        } else if (client.getColumnmaps().get(HIV_COMMUNITY_FOLLOWUP_VISIT_DATE) != null) {
            followupDate = client.getColumnmaps().get(HIV_COMMUNITY_FOLLOWUP_VISIT_DATE);
        }


        if (followupDate != null) {
            textViewReferralDay.setVisibility(View.VISIBLE);
            String referralDay = textViewReferralDay.getContext().getResources().getString(
                    R.string.feedback_day, getReferralPeriod(new DateTime(new BigDecimal(followupDate).longValue())));
            textViewReferralDay.setText(referralDay);
        } else {
            textViewReferralDay.setVisibility(View.GONE);
        }
    }

    private static String getReferralPeriod(DateTime executionStartDate) {
        String standardDays = String.valueOf(new Duration(executionStartDate,
                new DateTime(Calendar.getInstance().getTime())).abs().toStandardDays());

        return standardDays.toLowerCase().replace("p", "");
    }

}
