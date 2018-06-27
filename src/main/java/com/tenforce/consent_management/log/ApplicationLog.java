package com.tenforce.consent_management.log;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tenforce.consent_management.compliance.ComplianceChecker;
import java.util.List;
import java.util.stream.Collectors;

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
    "process":"send-invoice",
    "purpose":"http://www.specialprivacy.eu/vocabs/purposes#Charity",
    "processing":"http://www.specialprivacy.eu/vocabs/processing#AnyProcessing",
    "recipient":"http://www.specialprivacy.eu/langs/usage-policy#AnyRecipient",
    "storage":"http://www.specialprivacy.eu/vocabs/locations#AnyStorage",
    "userID":"728c5123-347b-44cf-8af6-b5098935d1e4",
    "data":[
       "http://www.specialprivacy.eu/vocabs/data#OnlineActivity",
       "http://www.specialprivacy.eu/vocabs/data#Purchase",
       "http://www.specialprivacy.eu/vocabs/data#Preference",
       "http://www.specialprivacy.eu/vocabs/data#Social"
    ],
    "eventID": "42235337-b62e-4c74-80a7-e6eddce8698f"
 }
  */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApplicationLog {
    private long timestamp;
    private String process;
    private String purpose;
    private String processing;
    private String recipient;
    private String storage;
    private String userID;
    private String eventID;
    private List<String> data;
    private boolean hasConsent = false;

    public String generateOWLConsentClass() {
        String owlDataItems = this.data.stream()
                .map(a -> String.format("<rdf:Description rdf:about=\"%s\"/>\n", a))
                .collect(Collectors.joining());
        String owlDataRestriction = "<owl:Restriction>" +
                "<owl:onProperty rdf:resource=\"hasData\"/>" +
               "<owl:someValuesFrom>" +
                "<owl:Class><owl:intersectionOf rdf:parseType=\"Collection\">" +
                owlDataItems +
                "</owl:intersectionOf></owl:Class>" +
                "</owl:someValuesFrom>" +
                "</owl:Restriction>";
        return "<?xml version=\"1.0\"?>\n" +
                "<rdf:RDF xmlns=\"http://www.semanticweb.org/langens-jonathan/ontologies/2018/2/untitled-ontology-16#\"\n" +
                "     xml:base=\"http://www.semanticweb.org/langens-jonathan/ontologies/2018/2/untitled-ontology-16\"\n" +
                "     xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" +
                "     xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n" +
                "     xmlns:xml=\"http://www.w3.org/XML/1998/namespace\"\n" +
                "     xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\"\n" +
                "     xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\">\n" +
                "    <owl:Ontology rdf:about=\"http://www.semanticweb.org/langens-jonathan/ontologies/policies/data-controller-policies\">\n" +
                "        <owl:imports rdf:resource=\"http://www.specialprivacy.eu/vocabs/recipients\"/>\n" +
                "        <owl:imports rdf:resource=\"http://www.specialprivacy.eu/vocabs/purposes\"/>\n" +
                "        <owl:imports rdf:resource=\"http://www.specialprivacy.eu/vocabs/duration\"/>\n" +
                "        <owl:imports rdf:resource=\"http://www.specialprivacy.eu/vocabs/data\"/>\n" +
                "        <owl:imports rdf:resource=\"http://www.specialprivacy.eu/vocabs/locations\"/>\n" +
                "        <owl:imports rdf:resource=\"http://www.specialprivacy.eu/vocabs/processing\"/>\n" +
                "        <owl:imports rdf:resource=\"http://www.semanticweb.org/langens-jonathan/ontologies/data-property-ontology\"/>\n" +
                "    </owl:Ontology>\n" +
                "    \n" +
                "\n" +
                "    <!-- " + ComplianceChecker.getDataControllerPolicyClassName("InMemory") + "-->\n" +
                "\n" +
                "    <owl:Class rdf:about=\"" + ComplianceChecker.getDataControllerPolicyClassName("InMemory") + "\">\n" +
                "        <owl:equivalentClass>\n" +
                "            <owl:Class>\n" +
                "                <owl:intersectionOf rdf:parseType=\"Collection\">\n" +
                "                    " + owlDataRestriction +
                "                    <owl:Restriction>\n" +
                "                        <owl:onProperty rdf:resource=\"spl:hasRecipient\"/>\n" +
                "                        <owl:someValuesFrom rdf:resource=\"" + this.recipient + "\"/>\n" +
                "                    </owl:Restriction>\n" +
                "                    <owl:Restriction>\n" +
                "                        <owl:onProperty rdf:resource=\"spl:hasStorage\"/>\n" +
                "                        <owl:someValuesFrom rdf:resource=\"" + this.storage + "\"/>\n" +
                "                    </owl:Restriction>\n" +
                "                    <owl:Restriction>\n" +
                "                        <owl:onProperty rdf:resource=\"spl:hasProcessing\"/>\n" +
                "                        <owl:someValuesFrom rdf:resource=\"" + this.processing + "\"/>\n" +
                "                    </owl:Restriction>\n" +
                "                    <owl:Restriction>\n" +
                "                        <owl:onProperty rdf:resource=\"spl:hasPurpose\"/>\n" +
                "                        <owl:someValuesFrom rdf:resource=\"" + this.purpose + "\"/>\n" +
                "                    </owl:Restriction>\n" +
                "                </owl:intersectionOf>\n" +
                "            </owl:Class>\n" +
                "        </owl:equivalentClass>\n" +
                "        <rdfs:subClassOf rdf:resource=\"http://www.semanticweb.org/langens-jonathan/ontologies/2018/2/untitled-ontology-16#DataControllerPolicies\"/>\n" +
                "    </owl:Class>\n" +
                "</rdf:RDF>\n" +
                "\n" +
                "\n" +
                "\n" +
                "<!-- Generated by the TenForce User Policy Creator -->\n" +
                "\n";
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public List<String> getData() {
        return data;
    }

    public void setData(List<String> data) {
        this.data = data;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getStorage() {
        return storage;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getProcessing() {
        return processing;
    }

    public void setProcessing(String processing) {
        this.processing = processing;
    }

    public String getProcess() {
        return process;
    }

    public void setProcess(String process) {
        this.process = process;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
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
