package org.smartregister.chw.hf.model;

import androidx.annotation.NonNull;

import org.smartregister.chw.fp.model.BaseFpRegisterFragmentModel;
import org.smartregister.chw.fp.util.FamilyPlanningConstants;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.family.util.DBConstants;

import java.util.HashSet;
import java.util.Set;

public class FpEcpRegisterFragmentModel extends BaseFpRegisterFragmentModel {

    @NonNull
    @Override
    public String mainSelect(@NonNull String tableName, @NonNull String mainCondition) {
        SmartRegisterQueryBuilder queryBuilder = new SmartRegisterQueryBuilder();
        queryBuilder.selectInitiateMainTable(tableName, mainColumns(tableName));
        queryBuilder.customJoin("INNER JOIN " + FamilyPlanningConstants.DBConstants.FAMILY_MEMBER + " ON  " + tableName + "." + FamilyPlanningConstants.DBConstants.BASE_ENTITY_ID + " = " + FamilyPlanningConstants.DBConstants.FAMILY_MEMBER + "." + FamilyPlanningConstants.DBConstants.BASE_ENTITY_ID + " COLLATE NOCASE ");
        queryBuilder.customJoin("INNER JOIN " + FamilyPlanningConstants.DBConstants.FAMILY + " ON  " + FamilyPlanningConstants.DBConstants.FAMILY_MEMBER + "." + FamilyPlanningConstants.DBConstants.RELATIONAL_ID + " = " + FamilyPlanningConstants.DBConstants.FAMILY + "." + FamilyPlanningConstants.DBConstants.BASE_ENTITY_ID);
        queryBuilder.customJoin("LEFT JOIN (select base_entity_id , max(visit_date) visit_date from visits GROUP by base_entity_id) VISIT_SUMMARY ON VISIT_SUMMARY.base_entity_id = " + tableName + "." + DBConstants.KEY.BASE_ENTITY_ID);

        return queryBuilder.mainCondition(mainCondition);
    }

    protected String[] mainColumns(String tableName) {
        Set<String> columnList = new HashSet<>();
        columnList.add(tableName + "." + FamilyPlanningConstants.DBConstants.LAST_INTERACTED_WITH);
        columnList.add(tableName + "." + FamilyPlanningConstants.DBConstants.BASE_ENTITY_ID);
        columnList.add(FamilyPlanningConstants.DBConstants.FAMILY_MEMBER + "." + FamilyPlanningConstants.DBConstants.RELATIONAL_ID + " as relationalid");
        columnList.add(FamilyPlanningConstants.DBConstants.FAMILY_MEMBER + "." + FamilyPlanningConstants.DBConstants.RELATIONAL_ID);
        columnList.add(FamilyPlanningConstants.DBConstants.FAMILY_MEMBER + "." + FamilyPlanningConstants.DBConstants.FIRST_NAME);
        columnList.add(FamilyPlanningConstants.DBConstants.FAMILY_MEMBER + "." + FamilyPlanningConstants.DBConstants.MIDDLE_NAME);
        columnList.add(FamilyPlanningConstants.DBConstants.FAMILY_MEMBER + "." + FamilyPlanningConstants.DBConstants.LAST_NAME);
        columnList.add(FamilyPlanningConstants.DBConstants.FAMILY_MEMBER + "." + FamilyPlanningConstants.DBConstants.GENDER);
        columnList.add(FamilyPlanningConstants.DBConstants.FAMILY_MEMBER + "." + FamilyPlanningConstants.DBConstants.DOB);
        columnList.add(FamilyPlanningConstants.DBConstants.FAMILY + "." + FamilyPlanningConstants.DBConstants.VILLAGE_TOWN);

        return columnList.toArray(new String[columnList.size()]);
    }
}
