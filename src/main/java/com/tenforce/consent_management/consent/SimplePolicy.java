package com.tenforce.consent_management.consent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

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

    private String processingCollection;

    private String purposeCollection;

    private String recipientCollection;

    private String storageCollection;

    private static final OWLDataFactory dataFactory = OWLManager.getOWLDataFactory();

    private static final String SPL = "http://www.specialprivacy.eu/langs/usage-policy#";
    private static final OWLObjectProperty HAS_DATA = dataFactory.getOWLObjectProperty(IRI.create(SPL + "hasData"));
    private static final OWLObjectProperty HAS_PROCESSING = dataFactory.getOWLObjectProperty(IRI.create(SPL + "hasProcessing"));
    private static final OWLObjectProperty HAS_PURPOSE = dataFactory.getOWLObjectProperty(IRI.create(SPL + "hasPurpose"));
    private static final OWLObjectProperty HAS_RECIPIENT = dataFactory.getOWLObjectProperty(IRI.create(SPL + "hasRecipient"));
    private static final OWLObjectProperty HAS_STORAGE = dataFactory.getOWLObjectProperty(IRI.create(SPL + "hasStorage"));

    public SimplePolicy(String dataCollection, String processingCollection, String purposeCollection, String recipientCollection, String storageCollection) {
        this.dataCollection = dataCollection;
        this.processingCollection = processingCollection;
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

    public String getProcessingCollection() {
        return processingCollection;
    }

    public void setProcessingCollection(String processingCollection) {
        this.processingCollection = processingCollection;
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
                this.processingCollection,
                this.purposeCollection,
                this.recipientCollection,
                this.storageCollection
        );
    }

    public OWLClassExpression toOWL() {
        return dataFactory.getOWLObjectIntersectionOf(
                dataFactory.getOWLObjectSomeValuesFrom(HAS_DATA, dataFactory.getOWLClass(IRI.create(this.dataCollection))),
                dataFactory.getOWLObjectSomeValuesFrom(HAS_PROCESSING, dataFactory.getOWLClass(IRI.create(this.processingCollection))),
                dataFactory.getOWLObjectSomeValuesFrom(HAS_PURPOSE, dataFactory.getOWLClass(IRI.create(this.purposeCollection))),
                dataFactory.getOWLObjectSomeValuesFrom(HAS_RECIPIENT, dataFactory.getOWLClass(IRI.create(this.recipientCollection))),
                dataFactory.getOWLObjectSomeValuesFrom(HAS_STORAGE, dataFactory.getOWLClass(IRI.create(this.storageCollection)))
        );
    }
}
