package com.tenforce.consent_management.log;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CheckedApplicationLog {
    private long timestamp;
    private String eventID;
    private boolean hasConsent = false;
    private long timeTaken;
    private long checkingTime;

    public long getCheckingTime() {
        return checkingTime;
    }

    public void setCheckingTime(long checkingTime) {
        this.checkingTime = checkingTime;
    }

    public CheckedApplicationLog() {
        this.timestamp = System.currentTimeMillis() * 1000;
    }

    public CheckedApplicationLog(ApplicationLog alog) {
        this.eventID = alog.getEventID().toString();
        this.timestamp = System.currentTimeMillis() * 1000;
        this.timeTaken = this.timestamp  - ( alog.getTimestamp() * 1000 );
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getTimeTaken() {
        return timeTaken;
    }

    public void setTimeTaken(long timeTaken) {
        this.timeTaken = timeTaken;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public boolean isHasConsent() {
        return hasConsent;
    }

    public void setHasConsent(boolean hasConsent) {
        this.hasConsent = hasConsent;
    }
}