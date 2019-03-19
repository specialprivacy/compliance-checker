package com.tenforce.consent_management.log;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tenforce.consent_management.compliance.ComplianceChecker;
import com.tenforce.consent_management.kafka.ApplicationLogConsumer;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.sql.Timestamp;

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
    private String eventID;
    private long timestamp;
    private long checkedTimestamp = new Timestamp(System.currentTimeMillis()).getTime();
    private String process;
    private String userID;
    private boolean hasConsent = false;
    private String purpose;
    private String processing;
    private String recipient;
    private String storage;
    private List<String> data;

    private static final OWLDataFactory dataFactory = OWLManager.getOWLDataFactory();

    private static final String SPL = "http://www.specialprivacy.eu/langs/usage-policy#";
    private static final OWLObjectProperty HAS_DATA = dataFactory.getOWLObjectProperty(IRI.create(SPL + "hasData"));
    private static final OWLObjectProperty HAS_PROCESSING = dataFactory.getOWLObjectProperty(IRI.create(SPL + "hasProcessing"));
    private static final OWLObjectProperty HAS_PURPOSE = dataFactory.getOWLObjectProperty(IRI.create(SPL + "hasPurpose"));
    private static final OWLObjectProperty HAS_RECIPIENT = dataFactory.getOWLObjectProperty(IRI.create(SPL + "hasRecipient"));
    private static final OWLObjectProperty HAS_STORAGE = dataFactory.getOWLObjectProperty(IRI.create(SPL + "hasStorage"));

    public OWLClassExpression toOWL() {
        Set<OWLClassExpression> dataRestrictions = this.data.stream()
                .map(a -> dataFactory.getOWLClass(IRI.create(a)))
                .collect(Collectors.toSet());
        

        return dataFactory.getOWLObjectIntersectionOf(
                dataFactory.getOWLObjectSomeValuesFrom(HAS_DATA, dataFactory.getOWLObjectIntersectionOf(dataRestrictions)),
                dataFactory.getOWLObjectSomeValuesFrom(HAS_PROCESSING, dataFactory.getOWLClass(IRI.create(this.processing))),
                dataFactory.getOWLObjectSomeValuesFrom(HAS_PURPOSE, dataFactory.getOWLClass(IRI.create(this.purpose))),
                dataFactory.getOWLObjectSomeValuesFrom(HAS_RECIPIENT, dataFactory.getOWLClass(IRI.create(this.recipient))),
                dataFactory.getOWLObjectSomeValuesFrom(HAS_STORAGE, dataFactory.getOWLClass(IRI.create(this.storage)))
        );

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
