package org.smartregister.chw.hf.model;

public class PmtctFollowupFeedbackModel {
    private ChwFollowupFeedbackDetailsModel followupFeedbackDetailsModel;
    private String type;

    public ChwFollowupFeedbackDetailsModel getFollowupFeedbackDetailsModel() {
        return followupFeedbackDetailsModel;
    }

    public void setFollowupFeedbackDetailsModel(ChwFollowupFeedbackDetailsModel followupFeedbackDetailsModel) {
        this.followupFeedbackDetailsModel = followupFeedbackDetailsModel;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
