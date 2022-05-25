package org.smartregister.chw.hf.utils;

import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.rule.PmtctFollowUpRule;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.HomeVisitUtil;
import org.smartregister.chw.hf.rule.HfHeiFollowupRule;
import org.smartregister.chw.hf.rule.HfPmtctFollowupRule;

import java.util.Date;

public class HfHomeVisitUtil extends HomeVisitUtil {
    public static HfHeiFollowupRule getHeiVisitStatus(Date heiStartDate, Date followupDate, String baseEntityId) {
        HfHeiFollowupRule heiFollowupRule = new HfHeiFollowupRule(heiStartDate, followupDate, baseEntityId);
        CoreChwApplication.getInstance().getRulesEngineHelper().getHeiRule(heiFollowupRule, CoreConstants.RULE_FILE.HEI_FOLLOWUP_VISIT);
        return heiFollowupRule;
    }

    public static PmtctFollowUpRule getPmtctVisitStatus(Date pmtctRegisterDate, Date followUpDate, String baseEntityId) {
        PmtctFollowUpRule pmtctFollowUpRule = new HfPmtctFollowupRule(pmtctRegisterDate, followUpDate, baseEntityId);
        CoreChwApplication.getInstance().getRulesEngineHelper().getPmtctRule(pmtctFollowUpRule, CoreConstants.RULE_FILE.PMTCT_FOLLOW_UP_VISIT);
        return pmtctFollowUpRule;
    }
}
