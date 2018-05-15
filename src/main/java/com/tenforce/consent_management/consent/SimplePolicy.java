package com.tenforce.consent_management.consent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by langens-jonathan on 3/28/18.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SimplePolicy {
    private String dataCollection;

    private String processCollection;

    private String purposeCollection;

    private String recipientCollection;

    private String locationCollection;

    public SimplePolicy(String dataCollection, String processCollection, String purposeCollection, String recipientCollection, String locationCollection) {
        this.dataCollection = dataCollection;
        this.processCollection = processCollection;
        this.purposeCollection = purposeCollection;
        this.recipientCollection = recipientCollection;
        this.locationCollection = locationCollection;
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

    public String getLocationCollection() {
        return locationCollection;
    }

    public void setLocationCollection(String locationCollection) {
        this.locationCollection = locationCollection;
    }

    public SimplePolicy clone() {
        return new SimplePolicy(
                this.dataCollection,
                this.processCollection,
                this.purposeCollection,
                this.recipientCollection,
                this.locationCollection
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
        // adding the locationCollection
//        toreturn += "                            <owl:Restriction>\n" +
//                "                                <owl:onProperty rdf:resource=\"spl:hasStorage\"/>\n" +
//                "                                <owl:someValuesFrom rdf:resource=\"" + this.locationCollection + "\"/>\n" +
//                "                            </owl:Restriction>\n";
        // ending the OWL Class
        toreturn += "                        </owl:intersectionOf>\n" +
                "                    </owl:Class>\n";
        return toreturn;
    }
}
