package org.smartregister.chw.hf.repository;

import static org.smartregister.util.Utils.getAllSharedPreferences;

import net.sqlcipher.Cursor;

import org.smartregister.chw.core.repository.ChwTaskRepository;
import org.smartregister.chw.core.utils.ChwDBConstants;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.domain.Task;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.TaskNotesRepository;

import java.util.HashSet;
import java.util.Set;

import timber.log.Timber;

public class HfTaskRepository extends ChwTaskRepository {

    public HfTaskRepository(TaskNotesRepository taskNotesRepository) {
        super(taskNotesRepository);
    }

    public Task getLatestTaskByEntityId(String forEntity, String referralType) {
        AllSharedPreferences allSharedPreferences = getAllSharedPreferences();
        String anmIdentifier = allSharedPreferences.fetchRegisteredANM();

        Task task = new Task();
        try (Cursor cursor = getReadableDatabase().rawQuery(String.format("SELECT * FROM %s WHERE %s = ? AND %s = ? AND %s = ? AND %s = ? AND %s <> ?  ORDER BY %s DESC LIMIT 1",
                        TASK_TABLE, ChwDBConstants.TaskTable.BUSINESS_STATUS, ChwDBConstants.TaskTable.STATUS,
                        TASK_TABLE + "." + ChwDBConstants.TaskTable.FOR, ChwDBConstants.TaskTable.FOCUS, ChwDBConstants.TaskTable.LOCATION, ChwDBConstants.TaskTable.START),
                new String[]{CoreConstants.BUSINESS_STATUS.REFERRED, Task.TaskStatus.READY.name(), forEntity, referralType, allSharedPreferences.fetchUserLocalityId(anmIdentifier)})) {
            if (cursor.moveToFirst()) {
                task = readCursor(cursor);
            }
        } catch (Exception e) {
            Timber.e(e);
        }
        return task;
    }

    @Override
    public Set<Task> getReferralTasksForClientByStatus(String planId, String forEntity, String businessStatus) {
        AllSharedPreferences allSharedPreferences = getAllSharedPreferences();
        String anm = allSharedPreferences.fetchRegisteredANM();

        Cursor cursor = null;
        Set<Task> taskSet = new HashSet<>();
        try {
            cursor = getReadableDatabase().rawQuery(String.format("SELECT * FROM %s WHERE %s=? COLLATE NOCASE AND %s =? AND %s = ? COLLATE NOCASE AND code = 'Referral' AND  %s <> ? ",
                    TASK_TABLE, CoreConstants.DB_CONSTANTS.PLAN_ID, CoreConstants.DB_CONSTANTS.BUSINESS_STATUS, CoreConstants.DB_CONSTANTS.FOR, CoreConstants.DB_CONSTANTS.OWNER), new String[]{planId, businessStatus, forEntity, anm});
            while (cursor.moveToNext()) {
                Task task = readCursor(cursor);
                taskSet.add(task);
            }
        } catch (Exception e) {
            Timber.e(e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return taskSet;
    }
}
