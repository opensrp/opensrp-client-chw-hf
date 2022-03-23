package org.smartregister.chw.hf.rule;

import org.smartregister.chw.core.rule.HeiFollowupRule;
import org.smartregister.chw.hf.dao.HeiDao;
import org.smartregister.chw.hf.utils.Constants;

import java.util.Date;

import androidx.annotation.Nullable;

public class HfHeiFollowupRule extends HeiFollowupRule {
    public HfHeiFollowupRule(Date startDate, @Nullable Date latestFollowupDate, String baseEntityId) {
        super(startDate, latestFollowupDate, baseEntityId);
    }

    @Override
    public void updateDueDates() {

        if(isFirstVisit()){
            this.dueDate = startDate.plusDays(0);
            this.overDueDate = startDate.plusDays(7);
        }else if (HeiDao.getVisitNumber(getBaseEntityId()) == 1 && !HeiDao.getNextHivTestAge(getBaseEntityId()).equalsIgnoreCase(Constants.HeiHIVTestAtAge.AT_9_MONTHS) && latestFollowupDate != null) {
            this.dueDate = startDate.plusDays(42);
            this.overDueDate = startDate.plusDays(49);
        }else if(latestFollowupDate != null){
            this.dueDate = latestFollowupDate.plusDays(30);
            this.overDueDate = latestFollowupDate.plusDays(37);
        }
    }
}
