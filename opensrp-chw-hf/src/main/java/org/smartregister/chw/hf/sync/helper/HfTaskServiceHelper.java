package org.smartregister.chw.hf.sync.helper;

import androidx.annotation.NonNull;

import org.smartregister.CoreLibrary;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.location.helper.LocationHelper;
import org.smartregister.repository.TaskRepository;
import org.smartregister.sync.helper.TaskServiceHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class HfTaskServiceHelper extends TaskServiceHelper {

    protected static HfTaskServiceHelper instance;

    public HfTaskServiceHelper(TaskRepository taskRepository) {
        super(taskRepository);
    }

    public static HfTaskServiceHelper getInstance() {
        if (instance == null) {
            instance = new HfTaskServiceHelper(CoreLibrary.getInstance().context().getTaskRepository());
        }
        return instance;
    }

    @Override
    protected List<String> getLocationIds() {
        LocationHelper locationHelper = LocationHelper.getInstance();
        return getLocationsInHierarchy(locationHelper);
    }

    @NonNull
    private ArrayList<String> getLocationsInHierarchy(LocationHelper locationHelper) {
        ArrayList<String> locations = new ArrayList<>();
        if (locationHelper != null) {
            //This would return the location and its sub-levels based off the allowed locations i.e Council, Ward, Facility, Village
            //If a referral exists in any of the location hierarchy as group id it would be synced.
            String defaultLocation = locationHelper.getDefaultLocation();
            List<String> locationsFromHierarchy = locationHelper.locationsFromHierarchy(true, defaultLocation);
            locations.addAll(locationsFromHierarchy);
        }
        return locations;
    }

    @Override
    protected Set<String> getPlanDefinitionIds() {
        return Collections.singleton(CoreConstants.REFERRAL_PLAN_ID);
    }
}
