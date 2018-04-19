package com.tenforce.consent_management.compliance;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

/**
 * Created by langens-jonathan on 4/18/18.
 *
 * An OWLReasonerFactory provides a basic abstraction on the OWLReasoner instantiation
 */
public interface OWLReasonerFactory {
    public OWLReasoner instantiateReasoner(OWLOntology ontology);
}
