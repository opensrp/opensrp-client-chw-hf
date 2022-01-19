package org.smartregister.chw.hf.schedulers;

import static org.smartregister.chw.hf.utils.Constants.Events.ANC_FACILITY_VISIT_NOT_DONE;
import static org.smartregister.chw.hf.utils.Constants.Events.ANC_FACILITY_VISIT_NOT_DONE_UNDO;
import static org.smartregister.chw.hf.utils.Constants.Events.ANC_FIRST_FACILITY_VISIT;
import static org.smartregister.chw.hf.utils.Constants.Events.ANC_RECURRING_FACILITY_VISIT;

import org.smartregister.chw.core.contract.ScheduleService;
import org.smartregister.chw.core.schedulers.ScheduleTaskExecutor;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hf.task.ANCVisitScheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HfScheduleTaskExecutor extends ScheduleTaskExecutor {

    private static HfScheduleTaskExecutor scheduleTaskExecutor;

    protected HfScheduleTaskExecutor() {
        //scheduleServiceMap.put();
    }

    public static HfScheduleTaskExecutor getInstance() {
        if (scheduleTaskExecutor == null) {
            scheduleTaskExecutor = new HfScheduleTaskExecutor();
        }
        return scheduleTaskExecutor;
    }

    @Override
    protected Map<String, List<ScheduleService>> getClassifier() {
        if (scheduleServiceMap == null || scheduleServiceMap.size() == 0) {
            scheduleServiceMap = new HashMap<>();

            initializeANCClassifier(scheduleServiceMap);


        }
        return scheduleServiceMap;
    }

    private void addToClassifers(String eventType, Map<String, List<ScheduleService>> classifier, List<ScheduleService> scheduleServices) {
        List<ScheduleService> services = classifier.get(eventType);
        if (services == null)
            services = new ArrayList<>();

        services.addAll(scheduleServices);
        classifier.put(eventType, services);
    }

    private void initializeANCClassifier(Map<String, List<ScheduleService>> classifier) {
        List<ScheduleService> scheduleServices = new ArrayList<>();
        scheduleServices.add(new ANCVisitScheduler());

        addToClassifers(CoreConstants.EventType.ANC_REGISTRATION, classifier, scheduleServices);
        addToClassifers(ANC_FIRST_FACILITY_VISIT, classifier, scheduleServices);
        addToClassifers(ANC_RECURRING_FACILITY_VISIT, classifier, scheduleServices);
        addToClassifers(ANC_FACILITY_VISIT_NOT_DONE, classifier, scheduleServices);
        addToClassifers(ANC_FACILITY_VISIT_NOT_DONE_UNDO, classifier, scheduleServices);
    }


}
