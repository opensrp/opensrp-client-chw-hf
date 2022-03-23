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
        super.updateDueDates();

        if (HeiDao.getNextHivTestAge(getBaseEntityId()).equalsIgnoreCase(Constants.HeiHIVTestAtAge.AT_6_WEEKS) && latestFollowupDate != null) {
            this.dueDate = latestFollowupDate.plusDays(42);
            this.overDueDate = latestFollowupDate.plusDays(49);
        }else if(latestFollowupDate != null){
            this.dueDate = latestFollowupDate.plusDays(30);
            this.overDueDate = latestFollowupDate.plusDays(37);
        }
    }
}
