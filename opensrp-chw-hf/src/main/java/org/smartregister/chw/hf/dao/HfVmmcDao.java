package org.smartregister.chw.hf.dao;

import static org.smartregister.chw.hf.utils.Constants.FOCUS.VMMC_REFERRALS;

import org.smartregister.chw.vmmc.util.Constants;
import org.smartregister.chw.vmmc.dao.VmmcDao;
import org.smartregister.repository.AllSharedPreferences;

import java.util.List;

public class HfVmmcDao extends VmmcDao {

    //trial ang
    public static Boolean isTheClientReferred(String baseEntityId) {
        AllSharedPreferences allSharedPreferences = org.smartregister.chw.core.utils.Utils.getAllSharedPreferences();
        String anm = allSharedPreferences.fetchRegisteredANM();
        String currentLoaction = allSharedPreferences.fetchUserLocalityId(anm);

        String sql = "SELECT base_entity_id FROM " + Constants.TABLES.VMMC_ENROLLMENT + " elc " +
                " INNER JOIN task t on elc.base_entity_id = t.for " +
                " WHERE base_entity_id = '" + baseEntityId + "' " +
                " AND t.location = '" + currentLoaction + "' COLLATE NOCASE " +
                " AND t.focus = '" + VMMC_REFERRALS + "' COLLATE NOCASE ";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "base_entity_id");

        List<String> res = readData(sql, dataMap);
        if (res != null && res.size() > 0)
            return true;
        return null;
    }
}