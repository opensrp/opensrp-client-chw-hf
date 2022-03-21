package org.smartregister.chw.hf.repository;

import android.database.Cursor;

import net.sqlcipher.database.SQLiteDatabase;

import org.smartregister.chw.anc.util.DBConstants;
import org.smartregister.chw.pnc.repository.ProfileRepository;
import org.smartregister.chw.pnc.util.Constants;
import org.smartregister.chw.pnc.util.PncUtil;
import org.smartregister.commonregistry.CommonPersonObjectClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class HfProfileRepository extends ProfileRepository {
    private static final String MOHTER_ENTITY_ID = "mother_entity_id";

    private CommonPersonObjectClient getChildMember(Cursor cursor) {
        String[] columnNames = cursor.getColumnNames();
        Map<String, String> details = new HashMap<>();

        for (String columnName : columnNames) {
            details.put(columnName, cursor.getString(cursor.getColumnIndex(columnName)));
        }

        CommonPersonObjectClient commonPersonObject = new CommonPersonObjectClient("", details, "");
        commonPersonObject.setColumnmaps(details);
        commonPersonObject.setCaseId(cursor.getString(cursor.getColumnIndex(DBConstants.KEY.BASE_ENTITY_ID)));

        return commonPersonObject;

    }


    public List<CommonPersonObjectClient> getChildrenLessThan49DaysOld(String motherBaseEntityID) {
        List<CommonPersonObjectClient> childMemberObjects = new ArrayList<>();

        SQLiteDatabase database = getReadableDatabase();
        net.sqlcipher.Cursor cursor = null;
        try {
            if (database == null) {
                return null;
            }
            cursor = database.rawQuery("SELECT * fROM " + Constants.TABLES.EC_CHILD + " WHERE " + MOHTER_ENTITY_ID + "=? AND is_closed = 0 ORDER by first_name ASC",
                    new String[]{motherBaseEntityID});
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    String dob = cursor.getString(cursor.getColumnIndex(DBConstants.KEY.DOB));
                    int childAgeInDays = PncUtil.getDaysDifference(dob);
                    if (childAgeInDays < 49) {
                        childMemberObjects.add(getChildMember(cursor));
                    }
                    cursor.moveToNext();
                }
            }
        } catch (Exception e) {
            Timber.e(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return childMemberObjects;
    }
}
