package org.smartregister.chw.hf.utils;

import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.HomeVisitUtil;
import org.smartregister.chw.hf.rule.HfHeiFollowupRule;

import java.util.Date;

public class HfHomeVisitUtil extends HomeVisitUtil {
    public static HfHeiFollowupRule getHeiVisitStatus(Date heiStartDate, Date followupDate, String baseEntityId) {
        HfHeiFollowupRule heiFollowupRule = new HfHeiFollowupRule(heiStartDate, followupDate, baseEntityId);
        CoreChwApplication.getInstance().getRulesEngineHelper().getHeiRule(heiFollowupRule, CoreConstants.RULE_FILE.HEI_FOLLOWUP_VISIT);
        return heiFollowupRule;
    }
}
