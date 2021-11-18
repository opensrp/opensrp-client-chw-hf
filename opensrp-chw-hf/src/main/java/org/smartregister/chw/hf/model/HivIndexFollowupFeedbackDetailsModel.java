package org.smartregister.chw.hf.model;

import java.io.Serializable;

public class HivIndexFollowupFeedbackDetailsModel implements Serializable {
    private String followedByChw;
    private String baseEntityId;
    private String feedbackFormSubmissionId;
    private String agreedToBeTested;
    private String clientFound;
    private String testLocation;

    public String getBaseEntityId() {
        return baseEntityId;
    }

    public void setBaseEntityId(String baseEntityId) {
        this.baseEntityId = baseEntityId;
    }

    public String getAgreedToBeTested() {
        return agreedToBeTested;
    }

    public void setAgreedToBeTested(String agreedToBeTested) {
        this.agreedToBeTested = agreedToBeTested;
    }

    public String getClientFound() {
        return clientFound;
    }

    public void setClientFound(String clientFound) {
        this.clientFound = clientFound;
    }

    public String getTestLocation() {
        return testLocation;
    }

    public void setTestLocation(String testLocation) {
        this.testLocation = testLocation;
    }

    public String getFeedbackFormSubmissionId() {
        return feedbackFormSubmissionId;
    }

    public void setFeedbackFormSubmissionId(String feedbackFormSubmissionId) {
        this.feedbackFormSubmissionId = feedbackFormSubmissionId;
    }

    public String getFollowedByChw() {
        return followedByChw;
    }

    public void setFollowedByChw(String followedByChw) {
        this.followedByChw = followedByChw;
    }
}
