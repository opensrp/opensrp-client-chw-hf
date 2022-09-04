package org.smartregister.chw.hf.rule;

import androidx.annotation.Nullable;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.smartregister.chw.core.rule.HeiFollowupRule;

import java.util.Date;

public class HfHeiFollowupRule extends HeiFollowupRule {
    public HfHeiFollowupRule(Date startDate, @Nullable Date latestFollowupDate, String baseEntityId) {
        super(startDate, latestFollowupDate, baseEntityId);
    }

    @Override
    public void updateDueDates() {

        if (isFirstVisit()) {
            this.dueDate = startDate.plusDays(0);
            this.overDueDate = startDate.plusDays(7);
        }  else if (latestFollowupDate != null) {
            this.dueDate = latestFollowupDate.plusDays(0);
            this.overDueDate = latestFollowupDate.plusDays(7);
        }   else {
            this.dueDate = startDate.plusDays(30);
            this.overDueDate = startDate.plusDays(37);
        }
    }

    @Override
    public int getDatesDiff() {

        if (isFirstVisit()) {
            return Days.daysBetween(new DateTime(startDate), new DateTime()).getDays();
        } else
            return Days.daysBetween(new DateTime(this.dueDate), new DateTime()).getDays();
    }
}
