package org.smartregister.chw.hf.model;

import java.io.Serializable;

public class ChwFollowupFeedbackDetailsModel implements Serializable {
    private String feedbackType;
    private String baseEntityId;
    private String feedbackFormSubmissionId;
    private String followupFeedback;
    private String followupFeedbackDate;
    private String chwName;

    public String getBaseEntityId() {
        return baseEntityId;
    }

    public void setBaseEntityId(String baseEntityId) {
        this.baseEntityId = baseEntityId;
    }

    public String getFollowupFeedback() {
        return followupFeedback;
    }

    public void setFollowupFeedback(String followupFeedback) {
        this.followupFeedback = followupFeedback;
    }

    public String getFollowupFeedbackDate() {
        return followupFeedbackDate;
    }

    public void setFollowupFeedbackDate(String followupFeedbackDate) {
        this.followupFeedbackDate = followupFeedbackDate;
    }

    public String getChwName() {
        return chwName;
    }

    public void setChwName(String chwName) {
        this.chwName = chwName;
    }

    public String getFeedbackFormSubmissionId() {
        return feedbackFormSubmissionId;
    }

    public void setFeedbackFormSubmissionId(String feedbackFormSubmissionId) {
        this.feedbackFormSubmissionId = feedbackFormSubmissionId;
    }

    public String getFeedbackType() {
        return feedbackType;
    }

    public void setFeedbackType(String feedbackType) {
        this.feedbackType = feedbackType;
    }
}
