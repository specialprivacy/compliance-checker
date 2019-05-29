
package com.tenforce.consent_management.log;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by langens-jonathan on 4/25/18.
 *
 * A class for representing the application log format is it was initially agreed
 * upon.
 *
 * Supports converting from JSON and transforming into an OWL string. An example of the JSON
 * variant can be found below:
 {
 "timestamp":1524667034141,
 "process":"send-invoice-P1",
 "userID":"728c5123-347b-44cf-8af6-b5098935d1e4",
 "eventID": "42235337-b62e-4c74-80a7-e6eddce8698f"
 }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApplicationLog {
    private long timestamp;
    private String process;
    private String userID;
    private String eventID;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getProcess() {
        return process;
    }

    public void setProcess(String process) {
        this.process = process;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }
}
