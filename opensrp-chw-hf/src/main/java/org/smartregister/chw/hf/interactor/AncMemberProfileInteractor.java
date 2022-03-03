package org.smartregister.chw.hf.interactor;

import android.content.Context;

import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.core.interactor.CoreAncMemberProfileInteractor;
import org.smartregister.chw.hf.utils.Constants;

import java.util.Date;

public class AncMemberProfileInteractor extends CoreAncMemberProfileInteractor {

    public AncMemberProfileInteractor(Context context) {
        super(context);
    }

    @Override
    protected Date getLastVisitDate(MemberObject memberObject) {
        Date lastVisitDate = null;
        Visit lastVisit;
        lastVisit = AncLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), Constants.Events.ANC_RECURRING_FACILITY_VISIT);
        if (lastVisit != null) {
            lastVisitDate = lastVisit.getDate();
        } else {
            lastVisit = AncLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), Constants.Events.ANC_FIRST_FACILITY_VISIT);
            if (lastVisit != null) {
                lastVisitDate = lastVisit.getDate();
            }
        }

        return lastVisitDate;
    }
}
