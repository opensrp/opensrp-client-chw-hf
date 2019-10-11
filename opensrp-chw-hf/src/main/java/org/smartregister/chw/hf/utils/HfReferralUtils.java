package org.smartregister.chw.hf.utils;

import android.view.View;
import android.widget.TextView;

import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.CoreReferralUtils;
import org.smartregister.chw.core.utils.Utils;
import org.smartregister.chw.hf.HealthFacilityApplication;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.repository.HfTaskRepository;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Task;

public class HfReferralUtils extends CoreReferralUtils {

    public static final String REGISTER_TYPE = "register_type";

    public static void displayReferralDay(CommonPersonObjectClient client, String referralType, TextView textViewReferralDay) {
        Task referralTask = getLatestClientReferralTask(client.entityId(), referralType);
        if (referralTask.getExecutionStartDate() != null) {
            textViewReferralDay.setVisibility(View.VISIBLE);
            String referralDay = textViewReferralDay.getContext().getResources().getString(
                    R.string.referral_day, Utils.formatReferralDuration(referralTask.getExecutionStartDate()
                            , textViewReferralDay.getContext()));
            textViewReferralDay.setText(referralDay);
        } else {
            textViewReferralDay.setVisibility(View.GONE);
        }
    }

    private static Task getLatestClientReferralTask(String baseEntityId, String referralType) {

        return ((HfTaskRepository) HealthFacilityApplication.getInstance()
                .getTaskRepository()).getLatestTaskByEntityId(baseEntityId, referralType);

    }

    public static String getTaskFocus(String registerType) {
        String focus = Constants.OTHER;
        if (registerType != null) {
            switch (registerType) {
                case Constants.CHILD:
                    focus = CoreConstants.TASKS_FOCUS.SICK_CHILD;
                    break;
                case Constants.ANC:
                    focus = CoreConstants.TASKS_FOCUS.ANC_DANGER_SIGNS;
                    break;
                case Constants.PNC:
                    focus = CoreConstants.TASKS_FOCUS.PNC_DANGER_SIGNS;
                    break;
                default:
                    focus = Constants.OTHER;
                    break;
            }
        }
        return focus;
    }
}
