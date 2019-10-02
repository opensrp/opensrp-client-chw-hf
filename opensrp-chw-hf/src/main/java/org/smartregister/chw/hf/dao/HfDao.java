package org.smartregister.chw.hf.dao;

import org.smartregister.chw.core.dao.AbstractDao;

import java.util.List;

import timber.log.Timber;

public class HfDao extends AbstractDao {
    public static Integer getAllClientsCount() {
        String sql = "SELECT SUM(c) as s\n" +
                "FROM (\n" +
                "         SELECT COUNT(*) AS c\n" +
                "         FROM ec_child\n" +
                "                  inner join ec_family_member on ec_family_member.base_entity_id = ec_child.base_entity_id\n" +
                "                  inner join ec_family on ec_family.base_entity_id = ec_family_member.relational_id\n" +
                "         WHERE ec_family_member.is_closed = '0'\n" +
                "           AND ec_family_member.date_removed is null\n" +
                "           AND cast(strftime('%Y-%m-%d %H:%M:%S', 'now') - strftime('%Y-%m-%d %H:%M:%S', ec_child.dob) as int) > 0\n" +
                "         UNION ALL\n" +
                "         SELECT COUNT(*)\n" +
                "         FROM ec_anc_register\n" +
                "                  inner join ec_family_member on ec_family_member.base_entity_id = ec_anc_register.base_entity_id\n" +
                "                  inner join ec_family on ec_family.base_entity_id = ec_family_member.relational_id\n" +
                "         where ec_family_member.date_removed is null\n" +
                "           and ec_anc_register.is_closed is 0\n" +
                "\n" +
                "         UNION ALL\n" +
                "         SELECT COUNT(*)\n" +
                "         FROM ec_pregnancy_outcome\n" +
                "                  inner join ec_family_member on ec_family_member.base_entity_id = ec_pregnancy_outcome.base_entity_id\n" +
                "                  inner join ec_family on ec_family.base_entity_id = ec_family_member.relational_id\n" +
                "         where ec_family_member.date_removed is null\n" +
                "           and ec_pregnancy_outcome.is_closed is 0\n" +
                "           AND ec_pregnancy_outcome.base_entity_id NOT IN\n" +
                "               (SELECT base_entity_id FROM ec_anc_register WHERE ec_anc_register.is_closed IS 0)\n" +
                "     );";
        Timber.i("All client count sql -> %s", sql);
        DataMap<Integer> dataMap = cursor -> getCursorIntValue(cursor, "s");
        List<Integer> res = readData(sql, dataMap);
        return res.get(0);
    }
}
