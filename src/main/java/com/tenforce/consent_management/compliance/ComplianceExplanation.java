package com.tenforce.consent_management.compliance;

import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by langens-jonathan on 3/15/18.
 *
 * This is a data structture to help sharing compliance explanations
 * around the service.
 *
 * A lot of TODO work here though.
 */
public class ComplianceExplanation {
    private boolean isCompliant;

    private Set<Set<String>> explanations;

    public ComplianceExplanation(boolean isCompliant, Set<Set<OWLAxiom>> explanations) {
        this.isCompliant = isCompliant;
        this.explanations = new HashSet<Set<String>>();
        for(Set<OWLAxiom> axiomSet : explanations) {
            Set<String> stringSet = new HashSet<String>();
            for(OWLAxiom axiom : axiomSet) {
                stringSet.add(axiom.toString());
            }
            this.explanations.add(stringSet);
        }
    }

    public boolean getIsCompliant() {
        return this.isCompliant;
    }

    public Set<Set<String>> getExplanations() {
        return this.explanations;
    }
}
