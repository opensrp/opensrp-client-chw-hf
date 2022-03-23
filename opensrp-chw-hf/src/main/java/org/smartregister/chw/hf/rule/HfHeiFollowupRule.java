package org.smartregister.chw.hf.rule;

import org.smartregister.chw.core.rule.HeiFollowupRule;

import java.util.Date;

import androidx.annotation.Nullable;

public class HfHeiFollowupRule extends HeiFollowupRule {
    public HfHeiFollowupRule(Date startDate, @Nullable Date latestFollowupDate, String baseEntityId) {
        super(startDate, latestFollowupDate, baseEntityId);
    }

    @Override
    public void updateDueDates() {
        super.updateDueDates();
    }
}
