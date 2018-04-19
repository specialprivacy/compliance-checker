package com.tenforce.consent_management.compliance;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

/**
 * Created by langens-jonathan on 4/18/18.
 */
public class HermiTReasonerFactory implements OWLReasonerFactory {
    public OWLReasoner instantiateReasoner(OWLOntology ontology) {
        return new Reasoner(ontology);
    }
}
