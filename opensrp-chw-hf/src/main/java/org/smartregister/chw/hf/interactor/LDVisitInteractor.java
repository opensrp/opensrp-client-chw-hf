package org.smartregister.chw.hf.interactor;

import android.content.Context;

import org.smartregister.chw.anc.util.VisitUtils;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.actionhelper.LDBloodGroupTestActionHelper;
import org.smartregister.chw.hf.actionhelper.LDGeneralExaminationActionHelper;
import org.smartregister.chw.hf.actionhelper.LDHBTestActionHelper;
import org.smartregister.chw.hf.actionhelper.LDHIVTestActionHelper;
import org.smartregister.chw.hf.actionhelper.LDMalariaTestActionHelper;
import org.smartregister.chw.hf.actionhelper.LDSyphilisTestActionHelper;
import org.smartregister.chw.hf.actionhelper.LDVaginalExaminationActionHelper;
import org.smartregister.chw.hf.dao.LDDao;
import org.smartregister.chw.hf.utils.Constants;
import org.smartregister.chw.hf.utils.LDVisitUtils;
import org.smartregister.chw.ld.LDLibrary;
import org.smartregister.chw.ld.contract.BaseLDVisitContract;
import org.smartregister.chw.ld.domain.MemberObject;
import org.smartregister.chw.ld.domain.Visit;
import org.smartregister.chw.ld.domain.VisitDetail;
import org.smartregister.chw.ld.interactor.BaseLDVisitInteractor;
import org.smartregister.chw.ld.model.BaseLDVisitAction;
import org.smartregister.clientandeventmodel.Event;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

/**
 * Created by Kassim Sheghembe on 2022-05-06
 */
public class LDVisitInteractor extends BaseLDVisitInteractor {
    protected Context context;
    final LinkedHashMap<String, BaseLDVisitAction> actionList = new LinkedHashMap<>();
    Map<String, List<VisitDetail>> details = null;
    private MemberObject memberObject;

    @Override
    public void calculateActions(BaseLDVisitContract.View view, MemberObject memberObject, BaseLDVisitContract.InteractorCallBack callBack) {

        context = view.getContext();
        this.memberObject = memberObject;

        if (view.getEditMode()) {
            Visit lastVisit = LDLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), org.smartregister.chw.ld.util.Constants.EVENT_TYPE.LD_GENERAL_EXAMINATION);

            if (lastVisit != null) {
                details = org.smartregister.chw.ld.util.VisitUtils.getVisitGroups(LDLibrary.getInstance().visitDetailsRepository().getVisits(lastVisit.getVisitId()));
            }
        }

        final Runnable runnable = () -> {
            // update the local database incase of manual date adjustment
            try {
                VisitUtils.processVisits(memberObject.getBaseEntityId());
            } catch (Exception e) {
                Timber.e(e);
            }

            try {

                evaluateGenExamination(details);
                evaluateVaginalExamination(details);

                if (LDDao.getHivStatus(memberObject.getBaseEntityId()) == null ||
                        (!Objects.equals(LDDao.getHivStatus(memberObject.getBaseEntityId()), Constants.HIV_STATUS.POSITIVE) && testDateIsThreeMonthsAgo(memberObject.getBaseEntityId()))) {
                    evaluateHIVStatus(details);
                }
                if (LDDao.getBloodGroup(memberObject.getBaseEntityId()) == null ||
                        (Objects.equals(LDDao.getBloodGroup(memberObject.getBaseEntityId()), "test_not_conducted"))) {
                    evaluateBloodGroupTest(details);
                }

                if (hbTestMoreThanTwoWeeksAgo(memberObject.getBaseEntityId())) {
                    evaluateHBTest(details);
                }

                if (!syphilisTestConductedDuringRegistration(memberObject.getBaseEntityId()))
                    evaluateSyphilisTest(details);

                if (!malariaTestConductedDuringRegistration(memberObject.getBaseEntityId()))
                    evaluateMalariatest(details);

            } catch (BaseLDVisitAction.ValidationException e) {
                Timber.e(e);
            }

            appExecutors.mainThread().execute(() -> callBack.preloadActions(actionList));
        };

        appExecutors.diskIO().execute(runnable);
    }

    public static boolean malariaTestConductedDuringRegistration(String baseEntityId) {
        if (LDDao.getMalariaTest(baseEntityId) != null) {
            String malariaTest = LDDao.getMalariaTest(baseEntityId);
            return !malariaTest.equalsIgnoreCase(Constants.FormConstants.ClinicFindings.Malaria.MALARIA_TEST_NOT_DONE);
        }
        return false;
    }

    public static boolean syphilisTestConductedDuringRegistration(String baseEntityId) {
        if (LDDao.getSyphilisTest(baseEntityId) != null) {
            String syphilisTest = LDDao.getSyphilisTest(baseEntityId);
            return !syphilisTest.equalsIgnoreCase(Constants.FormConstants.ClinicFindings.Syphilis.SYPHILIS_TEST_NOT_DONE);
        }
        return false;
    }

    public static boolean hbTestMoreThanTwoWeeksAgo(String baseEntityId) {
        if (LDDao.getHbTestDate(baseEntityId) != null) {
            try {
                DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                String hbTestDate = LDDao.getHbTestDate(baseEntityId);
                Date testDate = dateFormat.parse(hbTestDate);
                if (testDate != null) {
                    Date twoWeeksAgo = new Date(System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 14));
                    if (testDate.before(twoWeeksAgo)) {
                        return true;
                    } else {
                        return false;
                    }
                }
                return true;
            } catch (Exception e) {
                Timber.e(e);
            }
        }
        return true;
    }

    public static boolean testDateIsThreeMonthsAgo(String baseEntityId) {
        if (LDDao.getPmtctTestDate(baseEntityId) != null) {
            try {
                DateFormat completeDateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                String pmtctTestDate = LDDao.getPmtctTestDate(baseEntityId);
                Date testDate = completeDateFormat.parse(pmtctTestDate);
                if (testDate != null) {
                    Date threeMonthsAgo = new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(90));
                    if (testDate.before(threeMonthsAgo)) {
                        return true;
                    } else {
                        return false;
                    }
                }
                return true;

            } catch (Exception e) {
                Timber.e(e);
            }
        }
        return true;
    }

    private void evaluateMalariatest(Map<String, List<VisitDetail>> details) throws BaseLDVisitAction.ValidationException {

        String title = context.getString(R.string.lb_visit_malaria_test_status_action_title);

        LDMalariaTestActionHelper actionHelper = new LDMalariaTestActionHelper(context);
        BaseLDVisitAction action = getBuilder(title)
                .withOptional(true)
                .withHelper(actionHelper)
                .withDetails(details)
                .withFormName(Constants.JsonForm.LDVisit.getLdMalariaTestForm())
                .build();

        actionList.put(title, action);
    }

    private void evaluateSyphilisTest(Map<String, List<VisitDetail>> details) throws BaseLDVisitAction.ValidationException {

        String title = context.getString(R.string.lb_visit_syphilis_test_status_action_title);

        LDSyphilisTestActionHelper actionHelper = new LDSyphilisTestActionHelper(context);
        BaseLDVisitAction action = getBuilder(title)
                .withOptional(true)
                .withHelper(actionHelper)
                .withDetails(details)
                .withFormName(Constants.JsonForm.LDVisit.getSyphilisTestForm())
                .build();

        actionList.put(title, action);
    }

    private void evaluateHIVStatus(Map<String, List<VisitDetail>> details) throws BaseLDVisitAction.ValidationException {

        String title = context.getString(R.string.lb_visit_hiv_test_status_action_title);

        LDHIVTestActionHelper actionHelper = new LDHIVTestActionHelper(context);
        BaseLDVisitAction action = getBuilder(title)
                .withOptional(true)
                .withHelper(actionHelper)
                .withDetails(details)
                .withFormName(Constants.JsonForm.LDVisit.getLdHivTest())
                .build();

        actionList.put(title, action);
    }

    private void evaluateBloodGroupTest(Map<String, List<VisitDetail>> details) throws BaseLDVisitAction.ValidationException {

        String title = context.getString(R.string.lb_visit_blood_group_test_action_title);

        LDBloodGroupTestActionHelper actionHelper = new LDBloodGroupTestActionHelper(context);
        BaseLDVisitAction action = getBuilder(title)
                .withOptional(true)
                .withHelper(actionHelper)
                .withDetails(details)
                .withFormName(Constants.JsonForm.LDVisit.getLdBloodGroupTest())
                .build();

        actionList.put(title, action);
    }

    private void evaluateHBTest(Map<String, List<VisitDetail>> details) throws BaseLDVisitAction.ValidationException {

        String title = context.getString(R.string.lb_visit_hb_test_action_title);

        LDHBTestActionHelper actionHelper = new LDHBTestActionHelper(context);
        BaseLDVisitAction action = getBuilder(title)
                .withOptional(true)
                .withHelper(actionHelper)
                .withDetails(details)
                .withFormName(Constants.JsonForm.LDVisit.getLdHBTestForm())
                .build();

        actionList.put(title, action);

    }

    private void evaluateVaginalExamination(Map<String, List<VisitDetail>> details) throws BaseLDVisitAction.ValidationException {
        LDVaginalExaminationActionHelper actionHelper = new LDVaginalExaminationActionHelper(context, memberObject.getBaseEntityId());
        BaseLDVisitAction action = getBuilder(context.getString(R.string.lb_visit_vaginal_assessment))
                .withOptional(true)
                .withDetails(details)
                .withHelper(actionHelper)
                .withFormName(Constants.JsonForm.LDVisit.getLdVaginalExamination())
                .build();

        actionList.put(context.getString(R.string.lb_visit_vaginal_assessment), action);

    }

    private void evaluateGenExamination(Map<String, List<VisitDetail>> details) throws BaseLDVisitAction.ValidationException {

        LDGeneralExaminationActionHelper actionHelper = new LDGeneralExaminationActionHelper(context, memberObject);
        BaseLDVisitAction action = getBuilder(context.getString(R.string.lb_visit_general_examination))
                .withOptional(false)
                .withDetails(details)
                .withHelper(actionHelper)
                .withFormName(Constants.JsonForm.LDVisit.getLdGeneralExamination())
                .build();

        actionList.put(context.getString(R.string.lb_visit_general_examination), action);
    }

    @Override
    public MemberObject getMemberClient(String memberID) {

        return LDDao.getMember(memberID);
    }

    public BaseLDVisitAction.Builder getBuilder(String title) {
        return new BaseLDVisitAction.Builder(context, title);
    }

    @Override
    protected String getEncounterType() {
        return org.smartregister.chw.ld.util.Constants.EVENT_TYPE.LD_GENERAL_EXAMINATION;
    }

    @Override
    protected String getTableName() {
        return org.smartregister.chw.ld.util.Constants.TABLES.EC_LD_GENERAL_EXAMINATION;
    }

    @Override
    protected void processExternalVisits(Visit visit, Map<String, BaseLDVisitAction> externalVisits, String memberID) throws Exception {
        super.processExternalVisits(visit, externalVisits, memberID);

        /*List<Visit> visits = new ArrayList<>(1);
        visits.add(visit);
        org.smartregister.chw.ld.util.VisitUtils.processVisits(visits, LDLibrary.getInstance().visitRepository(), LDLibrary.getInstance().visitDetailsRepository());*/
        try {

            LDVisitUtils.processVisits(memberID);
        } catch (Exception e) {
            Timber.e(e);
        }

    }

    @Override
    protected void prepareEvent(Event baseEvent) {
        super.prepareEvent(baseEvent);
    }

}
