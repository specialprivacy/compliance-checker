package com.tenforce.consent_management.consent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by langens-jonathan on 3/28/18.
 *
 * This class encapsulates a simple policy. A simple policy is a 5 tuple consisting of:
 * - data
 * - process
 * - purpose
 * - recipient
 * - storage
 *
 * Further this class sports a method to turn it into a partial OWL string.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SimplePolicy {
    private String dataCollection;

    private String processCollection;

    private String purposeCollection;

    private String recipientCollection;

    private String storageCollection;

    public SimplePolicy(String dataCollection, String processCollection, String purposeCollection, String recipientCollection, String storageCollection) {
        this.dataCollection = dataCollection;
        this.processCollection = processCollection;
        this.purposeCollection = purposeCollection;
        this.recipientCollection = recipientCollection;
        this.storageCollection = storageCollection;
    }

    public SimplePolicy() {
        this("","","","","");
    }

    public String getDataCollection() {
        return dataCollection;
    }

    public void setDataCollection(String dataCollection) {
        this.dataCollection = dataCollection;
    }

    public String getProcessCollection() {
        return processCollection;
    }

    public void setProcessCollection(String processCollection) {
        this.processCollection = processCollection;
    }

    public String getPurposeCollection() {
        return purposeCollection;
    }

    public void setPurposeCollection(String purposeCollection) {
        this.purposeCollection = purposeCollection;
    }

    public String getRecipientCollection() {
        return recipientCollection;
    }

    public void setRecipientCollection(String recipientCollection) {
        this.recipientCollection = recipientCollection;
    }

    public String getStorageCollection() {
        return storageCollection;
    }

    public void setStorageCollection(String storageCollection) {
        this.storageCollection = storageCollection;
    }

    public SimplePolicy clone() {
        return new SimplePolicy(
                this.dataCollection,
                this.processCollection,
                this.purposeCollection,
                this.recipientCollection,
                this.storageCollection
        );
    }

    public String toString() {
        // starting the OWL Class
        String toreturn = "                    <owl:Class>\n" +
                "                        <owl:intersectionOf rdf:parseType=\"Collection\">\n";
        // adding the dataCollection
        toreturn += "                            <owl:Restriction>\n" +
                "                                <owl:onProperty rdf:resource=\"spl:hasData\"/>\n" +
                "                                <owl:someValuesFrom rdf:resource=\"" + this.dataCollection + "\"/>\n" +
                "                            </owl:Restriction>\n";
        // adding the processCollection
        toreturn += "                            <owl:Restriction>\n" +
                "                                <owl:onProperty rdf:resource=\"spl:hasProcessing\"/>\n" +
                "                                <owl:someValuesFrom rdf:resource=\"" + this.processCollection + "\"/>\n" +
                "                            </owl:Restriction>\n";
        // adding the purposeCollection
        toreturn += "                            <owl:Restriction>\n" +
                "                                <owl:onProperty rdf:resource=\"spl:hasPurpose\"/>\n" +
                "                                <owl:someValuesFrom rdf:resource=\"" + this.purposeCollection + "\"/>\n" +
                "                            </owl:Restriction>\n";
        // adding the recipientCollection
        toreturn += "                            <owl:Restriction>\n" +
                "                                <owl:onProperty rdf:resource=\"spl:hasRecipient\"/>\n" +
                "                                <owl:someValuesFrom rdf:resource=\"" + this.recipientCollection + "\"/>\n" +
                "                            </owl:Restriction>\n";
        // adding the storageCollection
        toreturn += "                            <owl:Restriction>\n" +
                "                                <owl:onProperty rdf:resource=\"spl:hasStorage\"/>\n" +
                "                                <owl:someValuesFrom rdf:resource=\"" + this.storageCollection + "\"/>\n" +
                "                            </owl:Restriction>\n";
        // ending the OWL Class
        toreturn += "                        </owl:intersectionOf>\n" +
                "                    </owl:Class>\n";
        return toreturn;
    }
}
