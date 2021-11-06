package org.smartregister.chw.hf.dao;

import org.smartregister.chw.hf.model.HivTbFollowupFeedbackDetailsModel;
import org.smartregister.dao.AbstractDao;

import java.util.List;

/**
 * Created by cozej4 on 6/21/20.
 *
 * @author cozej4 https://github.com/cozej4
 */
public class HivFollowupFeedbackDao extends AbstractDao {

    public static AbstractDao.DataMap<HivTbFollowupFeedbackDetailsModel> dataMap = cursor -> {
        HivTbFollowupFeedbackDetailsModel followupFeedbackDetailsModel = new HivTbFollowupFeedbackDetailsModel();
        followupFeedbackDetailsModel.setBaseEntityId(getCursorValue(cursor, "entity_id"));
        followupFeedbackDetailsModel.setFeedbackFormSubmissionId(getCursorValue(cursor, "base_entity_id"));
        followupFeedbackDetailsModel.setChwName(getCursorValue(cursor, "chw_name"));
        followupFeedbackDetailsModel.setFollowupFeedback(getCursorValue(cursor, "followup_status"));
        followupFeedbackDetailsModel.setFollowupFeedbackDate(getCursorValue(cursor, "hiv_community_followup_visit_date"));
        followupFeedbackDetailsModel.setFeedbackType(getCursorValue(cursor, "feedback_type"));
        return followupFeedbackDetailsModel;
    };

    public static List<HivTbFollowupFeedbackDetailsModel> getHivFollowupFeedback(String baseEntityId) {
        String sql = String.format(
                "SELECT  'HIV' as feedback_type, ec_hiv_community_feedback.* FROM ec_hiv_community_feedback " +
                        "WHERE ec_hiv_community_feedback.entity_id = '%s' AND mark_as_done IS NULL ", baseEntityId);
        List<HivTbFollowupFeedbackDetailsModel> feedbackList = readData(sql, dataMap);

        if (feedbackList == null)
            return null;

        return feedbackList;
    }

    public static List<HivTbFollowupFeedbackDetailsModel> getTbFollowupFeedback(String baseEntityId) {
        String sql = String.format(
                "SELECT 'TB' as feedback_type, ec_tb_community_feedback.* FROM ec_tb_community_feedback " +
                        "WHERE ec_tb_community_feedback.entity_id = '%s' AND mark_as_done IS NULL ", baseEntityId);
        List<HivTbFollowupFeedbackDetailsModel> feedbackList = readData(sql, dataMap);

        if (feedbackList == null)
            return null;

        return feedbackList;
    }
}
