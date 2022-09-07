package org.smartregister.chw.hf.rule;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.smartregister.chw.core.rule.PmtctFollowUpRule;
import org.smartregister.chw.core.utils.CoreConstants;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HfPmtctFollowupRule extends PmtctFollowUpRule {
    private final String baseEntityId;
    @NonNull
    private DateTime pmtctDate;
    @Nullable
    private DateTime latestFollowUpDate;
    private DateTime dueDate;
    private DateTime overDueDate;
    private DateTime expiryDate;

    public HfPmtctFollowupRule(Date pmtctDate, @Nullable Date latestFollowupDate, String baseEntityId) {
        super(pmtctDate, latestFollowupDate, baseEntityId);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        this.pmtctDate = new DateTime(sdf.format(pmtctDate));
        this.baseEntityId = baseEntityId;
        this.latestFollowUpDate = latestFollowupDate == null ? null : new DateTime(sdf.format(latestFollowupDate));
        updateDueDates();
    }

    @Override
    public boolean isValid(int dueDay, int overDueDay, int expiry) {
        //using the updateDueDates method to update the dueDate and overDueDate for faster refresh
        return false;
    }

    public void updateDueDates(){
        if (latestFollowUpDate != null) {
            this.dueDate = latestFollowUpDate.plusDays(0);
            this.overDueDate = latestFollowUpDate.plusDays(32);
            this.expiryDate = latestFollowUpDate.plusDays(356);
        } else {
            this.dueDate = pmtctDate.plusDays(0);
            this.overDueDate = pmtctDate.plusDays(32);
            this.expiryDate = pmtctDate.plusDays(356);
        }
    }

    @Override
    public String getBaseEntityId() {
        return baseEntityId;
    }

    public Date getDueDate() {
        return dueDate != null ? dueDate.toDate() : null;
    }

    public Date getOverDueDate() {
        return overDueDate != null ? overDueDate.toDate() : null;
    }

    @Override
    public String getButtonStatus() {
        DateTime currentDate = new DateTime(new LocalDate().toDate());
        DateTime lastVisit = latestFollowUpDate;

        if (currentDate.isBefore(expiryDate)) {
            if ((currentDate.isAfter(overDueDate) || currentDate.isEqual(overDueDate)))
                return CoreConstants.VISIT_STATE.OVERDUE;
            if ((currentDate.isAfter(dueDate) || currentDate.isEqual(dueDate)) && currentDate.isBefore(overDueDate))
                return CoreConstants.VISIT_STATE.DUE;
            if (lastVisit != null && currentDate.isEqual(lastVisit))
                return CoreConstants.VISIT_STATE.VISIT_DONE;
            return CoreConstants.VISIT_STATE.NOT_DUE_YET;

        }
        return CoreConstants.VISIT_STATE.EXPIRED;
    }


}
