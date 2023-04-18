package org.smartregister.chw.hf.dao;

import org.smartregister.chw.core.domain.Child;
import org.smartregister.dao.AbstractDao;

import java.util.List;

public class HfChildDao  extends AbstractDao  {
    public static String getNoMotherChildQuery(String baseEntityID) {
        return "select  c.base_entity_id , c.first_name , c.last_name , c.middle_name , c.mother_entity_id , c.relational_id , c.dob , c.date_created ,  lastVisit.last_visit_date , last_visit_not_done_date " +
                "from ec_child c " +
                "inner join ec_family_member m on c.base_entity_id = m.base_entity_id COLLATE NOCASE " +
                "inner join ec_family f on f.base_entity_id = m.relational_id COLLATE NOCASE  " +
                "left join ( " +
                " select base_entity_id , max(visit_date) last_visit_date " +
                " from visits " +
                " where visit_type in ('Child Home Visit') " +
                " group by base_entity_id " +
                ") lastVisit on lastVisit.base_entity_id = c.base_entity_id " +
                "left join ( " +
                " select base_entity_id , max(visit_date) last_visit_not_done_date " +
                " from visits " +
                " where visit_type in ('Visit not done') " +
                " group by base_entity_id " +
                ") lastVisitNotDone on lastVisitNotDone.base_entity_id = c.base_entity_id " +
                "where c.base_entity_id = '" + baseEntityID + "' " +
                "and  m.date_removed is null and m.is_closed = 0 " +
                "and ((( julianday('now') - julianday(c.dob))/365.25) < 5) and c.is_closed = 0 ";
    }

    public static Child getNoMotherChild(String baseEntityID) {
        String sql = getNoMotherChildQuery(baseEntityID);

        List<Child> values = AbstractDao.readData(sql, getChildDataMap());
        if (values == null || values.size() != 1)
            return null;

        return values.get(0);
    }

    public static AbstractDao.DataMap<Child> getChildDataMap() {
        return c -> {
            Child record = new Child();
            record.setBaseEntityID(getCursorValue(c, "base_entity_id"));
            record.setFirstName(getCursorValue(c, "first_name"));
            record.setLastName(getCursorValue(c, "last_name"));
            record.setMiddleName(getCursorValue(c, "middle_name"));
            record.setMotherBaseEntityID(getCursorValue(c, "mother_entity_id"));
            record.setFamilyBaseEntityID(getCursorValue(c, "relational_id"));
            record.setDateOfBirth(getCursorValueAsDate(c, "dob", getDobDateFormat()));
            record.setDateCreated(getCursorValueAsDate(c, "date_created", getDobDateFormat()));
            record.setLastVisitDate(getCursorValueAsDate(c, "last_visit_date"));
            record.setLastVisitNotDoneDate(getCursorValueAsDate(c, "last_visit_not_done_date"));
            return record;
        };
    }
}
