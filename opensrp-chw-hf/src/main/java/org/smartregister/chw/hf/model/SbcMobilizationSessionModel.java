package org.smartregister.chw.hf.model;

public class SbcMobilizationSessionModel {
    private String sessionDate;

    private String communitySbcActivityType;

    private String sessionId;

    public String getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(String sessionDate) {
        this.sessionDate = sessionDate;
    }

    public String getCommunitySbcActivityType() {
        return communitySbcActivityType;
    }

    public void setCommunitySbcActivityType(String communitySbcActivityType) {
        this.communitySbcActivityType = communitySbcActivityType;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
