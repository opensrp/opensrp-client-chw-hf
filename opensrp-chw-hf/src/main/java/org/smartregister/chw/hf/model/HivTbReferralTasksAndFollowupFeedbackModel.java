package org.smartregister.chw.hf.model;

import org.smartregister.domain.Task;

public class HivTbReferralTasksAndFollowupFeedbackModel {
    private Task task;
    private HivTbFollowupFeedbackDetailsModel followupFeedbackDetailsModel;
    private String type;

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public HivTbFollowupFeedbackDetailsModel getFollowupFeedbackDetailsModel() {
        return followupFeedbackDetailsModel;
    }

    public void setFollowupFeedbackDetailsModel(HivTbFollowupFeedbackDetailsModel followupFeedbackDetailsModel) {
        this.followupFeedbackDetailsModel = followupFeedbackDetailsModel;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
