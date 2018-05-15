package com.tenforce.consent_management.consent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by langens-jonathan on 3/28/18.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SimplePolicy {
    private String data;

    private String processing;

    private String purpose;

    private String recipient;

    private String storage;

    public SimplePolicy(String data, String processing, String purpose, String recipient, String storage) {
        this.data = data;
        this.processing = processing;
        this.purpose = purpose;
        this.recipient = recipient;
        this.storage = storage;
    }

    public SimplePolicy() {
        this("","","","","");
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getProcessing() {
        return processing;
    }

    public void setProcessing(String processing) {
        this.processing = processing;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getStorage() {
        return storage;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }

    public SimplePolicy clone() {
        return new SimplePolicy(
                this.data,
                this.processing,
                this.purpose,
                this.recipient,
                this.storage
        );
    }

    public String toString() {
        // starting the OWL Class
        String toreturn = "                    <owl:Class>\n" +
                "                        <owl:intersectionOf rdf:parseType=\"Collection\">\n";
        // adding the data
        toreturn += "                            <owl:Restriction>\n" +
                "                                <owl:onProperty rdf:resource=\"spl:hasData\"/>\n" +
                "                                <owl:someValuesFrom rdf:resource=\"" + this.data + "\"/>\n" +
                "                            </owl:Restriction>\n";
        // adding the processing
        toreturn += "                            <owl:Restriction>\n" +
                "                                <owl:onProperty rdf:resource=\"spl:hasProcessing\"/>\n" +
                "                                <owl:someValuesFrom rdf:resource=\"" + this.processing + "\"/>\n" +
                "                            </owl:Restriction>\n";
        // adding the purpose
        toreturn += "                            <owl:Restriction>\n" +
                "                                <owl:onProperty rdf:resource=\"spl:hasPurpose\"/>\n" +
                "                                <owl:someValuesFrom rdf:resource=\"" + this.purpose + "\"/>\n" +
                "                            </owl:Restriction>\n";
        // adding the recipient
        toreturn += "                            <owl:Restriction>\n" +
                "                                <owl:onProperty rdf:resource=\"spl:hasRecipient\"/>\n" +
                "                                <owl:someValuesFrom rdf:resource=\"" + this.recipient + "\"/>\n" +
                "                            </owl:Restriction>\n";
        // adding the storage
//        toreturn += "                            <owl:Restriction>\n" +
//                "                                <owl:onProperty rdf:resource=\"spl:hasStorage\"/>\n" +
//                "                                <owl:someValuesFrom rdf:resource=\"" + this.storage + "\"/>\n" +
//                "                            </owl:Restriction>\n";
        // ending the OWL Class
        toreturn += "                        </owl:intersectionOf>\n" +
                "                    </owl:Class>\n";
        return toreturn;
    }
}
