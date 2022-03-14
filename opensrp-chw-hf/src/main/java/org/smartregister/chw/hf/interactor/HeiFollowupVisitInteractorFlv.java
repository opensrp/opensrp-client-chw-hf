package org.smartregister.chw.hf.interactor;

import android.content.Context;

import org.smartregister.chw.hf.actionhelper.HeiAntibodyTestAction;
import org.smartregister.chw.hf.actionhelper.HeiArvPrescriptionHighOrLowRiskInfantAction;
import org.smartregister.chw.hf.actionhelper.HeiArvPrescrptionHighRiskInfantAction;
import org.smartregister.chw.hf.actionhelper.HeiCtxAction;
import org.smartregister.chw.hf.actionhelper.HeiDnaPcrTestAction;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.pmtct.PmtctLibrary;
import org.smartregister.chw.pmtct.contract.BasePmtctHomeVisitContract;
import org.smartregister.chw.pmtct.domain.MemberObject;
import org.smartregister.chw.pmtct.domain.Visit;
import org.smartregister.chw.pmtct.domain.VisitDetail;
import org.smartregister.chw.pmtct.model.BasePmtctHomeVisitAction;
import org.smartregister.chw.pmtct.util.VisitUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HeiFollowupVisitInteractorFlv implements PmtctFollowupVisitInteractor.Flavor {


    @Override
    public LinkedHashMap<String, BasePmtctHomeVisitAction> calculateActions(BasePmtctHomeVisitContract.View view, MemberObject memberObject, BasePmtctHomeVisitContract.InteractorCallBack interactorCallBack) throws BasePmtctHomeVisitAction.ValidationException {
        LinkedHashMap<String, BasePmtctHomeVisitAction> actionList = new LinkedHashMap<>();

        Context context = view.getContext();

        Map<String, List<VisitDetail>> details = null;
        // get the preloaded data
        if (view.getEditMode()) {
            Visit lastVisit = PmtctLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), Constants.Events.HEI_FOLLOWUP);
            if (lastVisit != null) {
                details = VisitUtils.getVisitGroups(PmtctLibrary.getInstance().visitDetailsRepository().getVisits(lastVisit.getVisitId()));
            }
        }

        evaluateHEIActions(actionList, details, memberObject, context);

        return actionList;
    }

    private void evaluateHEIActions(LinkedHashMap<String, BasePmtctHomeVisitAction> actionList, Map<String, List<VisitDetail>> details, MemberObject memberObject, Context context) throws BasePmtctHomeVisitAction.ValidationException {

        BasePmtctHomeVisitAction DNAPCRTest = new BasePmtctHomeVisitAction.Builder(context, "DNA-PCR Sample Collection")
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JsonForm.getHeiDnaPcrSampleCollection())
                .withHelper(new HeiDnaPcrTestAction(memberObject))
                .build();
        actionList.put("DNA-PCR Sample Collection", DNAPCRTest);

        BasePmtctHomeVisitAction AntibodyTest = new BasePmtctHomeVisitAction.Builder(context, "Antibody Test Sample Collection")
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JsonForm.getHeiAntibodyTestSampleCollection())
                .withHelper(new HeiAntibodyTestAction(memberObject))
                .build();
        actionList.put("Antibody Test Sample Collection", AntibodyTest);

        BasePmtctHomeVisitAction CtxPrescription = new BasePmtctHomeVisitAction.Builder(context, "CTX Prescription")
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JsonForm.getHeiCtxPrescription())
                .withHelper(new HeiCtxAction(memberObject))
                .build();
        actionList.put("CTX Prescription", CtxPrescription);

        BasePmtctHomeVisitAction ARVPrescriptionHighRisk = new BasePmtctHomeVisitAction.Builder(context, "ARV Prescription (AZT + 3C and NVP)")
                .withOptional(true)
                .withDetails(details)
                .withFormName(Constants.JsonForm.getHeiArvPrescriptionHighRiskInfant())
                .withHelper(new HeiArvPrescrptionHighRiskInfantAction(memberObject))
                .build();
        actionList.put("ARV Prescription (AZT + 3C and NVP)", ARVPrescriptionHighRisk);

        BasePmtctHomeVisitAction ARVPrescriptionHighAndLowRisk = new BasePmtctHomeVisitAction.Builder(context, "ARV Prescription (NVP)")
                .withOptional(true)
                .withDetails(details)
                .withFormName(Constants.JsonForm.getHeiArvPrescriptionHighOrLowRiskInfant())
                .withHelper(new HeiArvPrescriptionHighOrLowRiskInfantAction(memberObject))
                .build();
        actionList.put("ARV Prescription (NVP)", ARVPrescriptionHighAndLowRisk);
    }


}


