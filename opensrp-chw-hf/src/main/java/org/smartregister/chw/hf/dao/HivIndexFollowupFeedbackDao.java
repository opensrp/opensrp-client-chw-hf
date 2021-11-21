package org.smartregister.chw.hf.dao;

import org.smartregister.chw.hf.model.HivIndexFollowupFeedbackDetailsModel;
import org.smartregister.dao.AbstractDao;

import java.util.List;

public class HivIndexFollowupFeedbackDao extends AbstractDao {
    public static AbstractDao.DataMap<HivIndexFollowupFeedbackDetailsModel> dataMap = cursor -> {
        HivIndexFollowupFeedbackDetailsModel followupFeedbackDetailsModel = new HivIndexFollowupFeedbackDetailsModel();
        followupFeedbackDetailsModel.setBaseEntityId(getCursorValue(cursor, "base_entity_id"));
        followupFeedbackDetailsModel.setFeedbackFormSubmissionId(getCursorValue(cursor, "entity_id"));
        followupFeedbackDetailsModel.setFollowedByChw(getCursorValue(cursor, "followed_by_chw"));
        followupFeedbackDetailsModel.setClientFound(getCursorValue(cursor, "client_found"));
        followupFeedbackDetailsModel.setAgreedToBeTested(getCursorValue(cursor, "agreed_to_be_tested"));
        followupFeedbackDetailsModel.setTestLocation(getCursorValue(cursor, "test_location"));
        return followupFeedbackDetailsModel;
    };

    public static List<HivIndexFollowupFeedbackDetailsModel> getHivIndexFollowupFeedback(String baseEntityId) {
        String sql = String.format(
                "SELECT  * from ec_hiv_index_chw_followup " +
                        "WHERE base_entity_id = '%s' AND is_closed == 0 ", baseEntityId);
        List<HivIndexFollowupFeedbackDetailsModel> feedbackList = readData(sql, dataMap);

        if (feedbackList == null)
            return null;

        return feedbackList;
    }

}
